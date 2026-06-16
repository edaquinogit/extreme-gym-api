import type { Logger } from 'pino'
import type { GatewayConfig } from '../config'
import type { SqliteDatabase } from '../storage/db'
import type { BackendClient } from '../backend/client'
import { randomUUID } from 'crypto'
import type { LocalAccessEvent } from '../types'

export type GatewayDependencies = {
  config: GatewayConfig
  database: SqliteDatabase
  backendClient: BackendClient
  logger: Logger
}

export type AccessDecision = {
  allowed: boolean
  reason: string
}

type BackendCredencialTipo = 'FACE_TEMPLATE' | 'QR_CODE' | 'CARTAO' | 'PIN'

type BackendAccessEvent = {
  idempotencyKey: string
  alunoId: number | null
  credencialTipo: BackendCredencialTipo
  identificadorExterno: string
  resultado: 'LIBERADO' | 'BLOQUEADO'
  motivo: string
  modo: 'ONLINE' | 'OFFLINE'
  origem: 'GATEWAY'
  dataHoraEvento: string
  identificadorExternoEvento: string
}

function toOptionalBackendId(value?: string | null): number | null {
  if (value == null || value === '') return null
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

function toBackendLocalDateTime(epochMillis: number): string {
  return new Date(epochMillis).toISOString().replace(/Z$/, '')
}

function toBackendResult(result: LocalAccessEvent['result']): BackendAccessEvent['resultado'] {
  return result === 'ALLOWED' ? 'LIBERADO' : 'BLOQUEADO'
}

const CREDENCIAL_TIPO_ALIASES: Record<string, BackendCredencialTipo> = {
  FACE_TEMPLATE: 'FACE_TEMPLATE',
  FACE: 'FACE_TEMPLATE',
  FACE_ID: 'FACE_TEMPLATE',
  QR_CODE: 'QR_CODE',
  QR: 'QR_CODE',
  QRCODE: 'QR_CODE',
  CARTAO: 'CARTAO',
  CARD: 'CARTAO',
  RFID: 'CARTAO',
  PIN: 'PIN',
}

function toBackendCredencialTipo(credentialType: string, logger: Logger): BackendCredencialTipo {
  const normalized = CREDENCIAL_TIPO_ALIASES[credentialType.toUpperCase()]
  if (!normalized) {
    logger.warn({ credentialType }, 'Unknown credentialType; defaulting to CARTAO for backend sync')
    return 'CARTAO'
  }
  return normalized
}

export class GatewayService {
  public config: GatewayConfig
  private database: SqliteDatabase
  private backendClient: BackendClient
  private logger: Logger

  constructor(deps: GatewayDependencies) {
    this.config = deps.config
    this.database = deps.database
    this.backendClient = deps.backendClient
    this.logger = deps.logger
  }

  async initialize(): Promise<void> {
    this.logger.info('Initializing gateway service')
    await this.database.initialize()
    try {
      await this.fetchAndStoreSnapshot()
    } catch (err) {
      this.logger.warn({ err }, 'Could not fetch initial snapshot from backend; starting with existing local data')
    }
  }

  // Public refresh method for administrative trigger
  async refreshSnapshot(): Promise<void> {
    await this.fetchAndStoreSnapshot()
  }

  async pendingEventsCount(): Promise<number> {
    return this.database.pendingEventsCount()
  }

  private async fetchAndStoreSnapshot(): Promise<void> {
    this.logger.info('Fetching snapshot from backend')
    const payload: any = await this.backendClient.fetchSnapshot()
    const itens = payload?.itens
    if (!Array.isArray(itens)) {
      this.logger.warn({ payload }, 'Snapshot payload unexpected shape; aborting snapshot store')
      return
    }

    const snapshotVersion = String(payload.versaoSnapshot ?? 'unknown')
    const generatedAt = payload.geradoEm ? Date.parse(payload.geradoEm) : Date.now()

    // Map backend items (PT field names) to the gateway's local SnapshotItem shape.
    // id is derived from (credencialTipo, identificadorExterno) — the actual lookup key —
    // so an aluno with more than one credential doesn't collide into a single row.
    const items = itens.map((it: any) => ({
      id: `${it.credencialTipo ?? 'UNKNOWN'}:${it.identificadorExterno ?? randomUUID()}`,
      snapshotVersion,
      generatedAt,
      validUntil: it.validoAte ? Date.parse(it.validoAte) : null,
      credentialType: String(it.credencialTipo ?? 'UNKNOWN'),
      externalIdentifier: String(it.identificadorExterno ?? ''),
      allowed: !!it.liberado,
      blockReason: it.motivoBloqueio ?? null,
      validUntilAccess: it.validoAte ? Date.parse(it.validoAte) : null,
      updatedAt: it.atualizadoEm ? Date.parse(it.atualizadoEm) : Date.now(),
    }))

    this.database.saveSnapshotItems(items)
    this.logger.info({ count: items.length }, 'Stored snapshot items locally')
  }

  private async createAndPersistEvent(event: LocalAccessEvent): Promise<void> {
    // guarantee persistence; don't lose events
    try {
      await this.database.createAccessEvent(event)
      this.logger.debug({ id: event.id, idempotencyKey: event.idempotencyKey }, 'Persisted local access event')
    } catch (err) {
      this.logger.error({ err, eventId: event.id }, 'Failed to persist access event — aborting operation')
      // In production we must not lose events; escalate by throwing
      throw err
    }
  }

  async validateAccess(rawCredentialType: string, externalIdentifier: string): Promise<AccessDecision> {
    this.logger.info({ credentialType: rawCredentialType, externalIdentifier }, 'Evaluating access request')

    // Normalize once to the backend's vocabulary so the snapshot lookup below (populated
    // from the backend's own credencialTipo values) and the online/sync payloads all agree.
    const credentialType = toBackendCredencialTipo(rawCredentialType, this.logger)

    const idempotencyKey = randomUUID()
    const baseEvent: Omit<LocalAccessEvent, 'id' | 'idempotencyKey' | 'result' | 'reason' | 'mode' | 'synced' | 'syncedAt' | 'syncAttempts' | 'lastSyncError'> = {
      alunoId: null,
      deviceId: this.config.gateway.id,
      credentialType,
      externalIdentifier,
      eventTime: Date.now(),
      receivedAt: Date.now(),
    }

    // Try online validation first
    try {
      const onlinePayload = {
        credencialTipo: credentialType,
        identificadorExterno: externalIdentifier,
        origem: 'GATEWAY',
        idempotencyKey,
        dataHoraEvento: toBackendLocalDateTime(Date.now()),
      }
      this.logger.debug({ credentialType, externalIdentifier }, 'Attempting online validation')
      const response: any = await this.backendClient.validateOnline(onlinePayload)

      const allowed = !!(response && response.permitido)
      const alunoId = response?.alunoId ?? null
      const reason = response?.motivo ?? (allowed ? 'ALLOWED by backend' : 'BLOCKED by backend')

      const event: LocalAccessEvent = {
        id: randomUUID(),
        idempotencyKey,
        alunoId,
        credentialType,
        externalIdentifier,
        deviceId: this.config.gateway.id,
        mode: 'ONLINE',
        result: allowed ? 'ALLOWED' : 'BLOCKED',
        reason,
        eventTime: baseEvent.eventTime,
        receivedAt: baseEvent.receivedAt,
        synced: false,
        syncAttempts: 0,
      }

      await this.createAndPersistEvent(event)

      return { allowed, reason }
    } catch (err) {
      this.logger.warn({ err }, 'Backend online validation failed — falling back to offline if enabled')
      // Backend unavailable -> fallback
      if (!this.config.feature.offlineMode) {
        const reason = 'Backend unreachable and offline mode disabled'
        // persist blocked event
        const event: LocalAccessEvent = {
          id: randomUUID(),
          idempotencyKey,
          alunoId: null,
          deviceId: this.config.gateway.id,
          credentialType,
          externalIdentifier,
          mode: 'OFFLINE',
          result: 'BLOCKED',
          reason,
          eventTime: baseEvent.eventTime,
          receivedAt: baseEvent.receivedAt,
          synced: false,
          syncAttempts: 0,
        }
        await this.createAndPersistEvent(event)
        return { allowed: false, reason }
      }

      // Offline conservative policy
      const snapshot = await this.database.getSnapshotItem(credentialType, externalIdentifier)
      if (!snapshot) {
        const reason = 'No offline authorization found'
        const event: LocalAccessEvent = {
          id: randomUUID(),
          idempotencyKey,
          alunoId: null,
          deviceId: this.config.gateway.id,
          credentialType,
          externalIdentifier,
          mode: 'OFFLINE',
          result: 'BLOCKED',
          reason,
          eventTime: baseEvent.eventTime,
          receivedAt: baseEvent.receivedAt,
          synced: false,
          syncAttempts: 0,
        }
        await this.createAndPersistEvent(event)
        return { allowed: false, reason }
      }

      if (!snapshot.allowed) {
        const reason = 'Credential blocked locally'
        const event: LocalAccessEvent = {
          id: randomUUID(),
          idempotencyKey,
          alunoId: null,
          deviceId: this.config.gateway.id,
          credentialType,
          externalIdentifier,
          mode: 'OFFLINE',
          result: 'BLOCKED',
          reason,
          eventTime: baseEvent.eventTime,
          receivedAt: baseEvent.receivedAt,
          synced: false,
          syncAttempts: 0,
        }
        await this.createAndPersistEvent(event)
        return { allowed: false, reason }
      }

      const now = Date.now()
      if (snapshot.validUntil && snapshot.validUntil < now) {
        const reason = 'Offline snapshot entry expired'
        const event: LocalAccessEvent = {
          id: randomUUID(),
          idempotencyKey,
          alunoId: null,
          deviceId: this.config.gateway.id,
          credentialType,
          externalIdentifier,
          mode: 'OFFLINE',
          result: 'BLOCKED',
          reason,
          eventTime: baseEvent.eventTime,
          receivedAt: baseEvent.receivedAt,
          synced: false,
          syncAttempts: 0,
        }
        await this.createAndPersistEvent(event)
        return { allowed: false, reason }
      }

      // Otherwise allow
      const reason = 'Offline authorization granted'
      const event: LocalAccessEvent = {
        id: randomUUID(),
        idempotencyKey,
        alunoId: null,
        deviceId: this.config.gateway.id,
        credentialType,
        externalIdentifier,
        mode: 'OFFLINE',
        result: 'ALLOWED',
        reason,
        eventTime: baseEvent.eventTime,
        receivedAt: baseEvent.receivedAt,
        synced: false,
        syncAttempts: 0,
      }
      await this.createAndPersistEvent(event)
      return { allowed: true, reason }
    }
  }

  async syncPendingEvents(): Promise<{ synced: number; failed: number }> {
    this.logger.info('Syncing pending events with backend')
    const batchSize = this.config.timing.syncBatchSize
    const pending = this.database.fetchPendingEvents(batchSize)
    if (pending.length === 0) {
      this.logger.info('No pending events to sync')
      return { synced: 0, failed: 0 }
    }

    try {
      const payload: BackendAccessEvent[] = pending.map((e) => ({
        idempotencyKey: e.idempotencyKey,
        alunoId: toOptionalBackendId(e.alunoId),
        credencialTipo: toBackendCredencialTipo(e.credentialType, this.logger),
        identificadorExterno: e.externalIdentifier,
        resultado: toBackendResult(e.result),
        motivo: e.reason,
        modo: e.mode,
        origem: 'GATEWAY',
        dataHoraEvento: toBackendLocalDateTime(e.eventTime),
        identificadorExternoEvento: e.id,
      }))

      await this.backendClient.syncEventsBatch(payload)
      const ids = pending.map((p) => p.id)
      this.database.markEventsSynced(ids)
      this.logger.info({ count: ids.length }, 'Successfully synced events')
      return { synced: ids.length, failed: 0 }
    } catch (err: any) {
      this.logger.warn({ err }, 'Failed to sync events; will keep them for retry')
      // increment attempt counters for each pending event
      for (const p of pending) {
        this.database.incrementSyncAttempt(p.id, String(err?.message ?? err))
      }
      return { synced: 0, failed: pending.length }
    }
  }

  async sendHeartbeat(): Promise<boolean> {
    this.logger.debug('Sending heartbeat')
    try {
      const payload = {
        gatewayId: this.config.gateway.id,
        timestamp: toBackendLocalDateTime(Date.now()),
        status: 'ATIVO',
        modoOperacao: this.config.feature.offlineMode ? 'HIBRIDO' : 'ONLINE',
        pendingEvents: this.database.pendingEventsCount(),
        version: this.config.gateway.version,
      }
      await this.backendClient.sendHeartbeat(payload)
      this.logger.debug('Heartbeat accepted by backend')
      return true
    } catch (err) {
      this.logger.warn({ err }, 'Heartbeat failed')
      return false
    }
  }
}
