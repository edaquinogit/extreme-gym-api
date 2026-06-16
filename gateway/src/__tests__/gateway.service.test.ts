// Ensure required env vars are set before importing project modules that parse them
process.env.GATEWAY_ID = process.env.GATEWAY_ID ?? 'gw-test'
process.env.GATEWAY_NAME = process.env.GATEWAY_NAME ?? 'gw'
process.env.BACKEND_BASE_URL = process.env.BACKEND_BASE_URL ?? 'http://example'
process.env.DEVICE_ID = process.env.DEVICE_ID ?? 'dev-1'
process.env.DEVICE_API_KEY = process.env.DEVICE_API_KEY ?? 'k'
process.env.DEVICE_HMAC_SECRET = process.env.DEVICE_HMAC_SECRET ?? 's'
process.env.ADMIN_API_KEY = process.env.ADMIN_API_KEY ?? 'admin-k'

import { beforeEach, describe, expect, it } from 'vitest'
import { SqliteDatabase } from '../storage/db'
import { GatewayService } from '../core/gateway'
import { logger } from '../logs/logger'
import type { GatewayConfig } from '../config'

class FakeBackendClient {
  public snapshot: any = { itens: [] }
  public validateResponse: any = null
  public lastValidatePayload: any = null
  public syncCalled = false
  public syncedEvents: any[] = []
  public heartbeatPayload: any = null

  constructor(public cfg: GatewayConfig) {}

  async fetchSnapshot() {
    return this.snapshot
  }

  async validateOnline(payload: any) {
    this.lastValidatePayload = payload
    if (this.validateResponse && this.validateResponse instanceof Error) throw this.validateResponse
    return this.validateResponse
  }

  async sendHeartbeat(payload: any) {
    this.heartbeatPayload = payload
    return { ok: true }
  }

  async syncEventsBatch(events: any[]) {
    this.syncCalled = true
    this.syncedEvents = events
    return { ok: true }
  }
}

function makeConfig(): GatewayConfig {
  return {
    environment: 'test',
    gateway: { id: 'gw-test', name: 'gw', version: '0.1.0', port: 4000, dataPath: ':memory:' },
    backend: { baseUrl: 'http://example', deviceId: '101', deviceApiKey: 'k', deviceHmacSecret: 's' },
    admin: { apiKey: 'admin-k' },
    storage: { databasePath: ':memory:' },
    timing: { snapshotRefreshSeconds: 300, snapshotTtlSeconds: 900, syncIntervalSeconds: 60, syncBatchSize: 50 },
    feature: { offlineMode: true, offlineStrict: true },
    logger: { level: 'silent' },
  }
}

describe('GatewayService orchestration (conservative offline)', () => {
  let db: SqliteDatabase
  let cfg: GatewayConfig
  let backend: FakeBackendClient
  let svc: GatewayService

  beforeEach(async () => {
    cfg = makeConfig()
    db = new SqliteDatabase(cfg.storage.databasePath, logger)
    backend = new FakeBackendClient(cfg)
    svc = new GatewayService({ config: cfg, database: db as any, backendClient: backend as any, logger })
    await db.initialize()
  })

  it('saves snapshot fetched from backend on initialize', async () => {
    backend.snapshot = {
      versaoSnapshot: 'v1',
      geradoEm: new Date().toISOString(),
      validoAte: null,
      totalCredenciais: 1,
      totalLiberados: 1,
      totalBloqueados: 0,
      itens: [
        {
          alunoId: 1,
          credencialTipo: 'CARTAO',
          identificadorExterno: 'abc',
          liberado: true,
          motivoBloqueio: null,
          validoAte: new Date(Date.now() + 10000).toISOString(),
          atualizadoEm: new Date().toISOString(),
        },
      ],
    }
    await svc.initialize()
    const item = db.getSnapshotItem('CARTAO', 'abc')
    expect(item).not.toBeNull()
    expect(item?.allowed).toBeTruthy()
  })

  it('online validation sends the backend validar-dispositivo DTO shape', async () => {
    backend.validateResponse = { permitido: true, alunoId: 'al1', motivo: 'ok' }
    await svc.validateAccess('CARD', 'online-ok')

    expect(backend.lastValidatePayload).toMatchObject({
      credencialTipo: 'CARTAO',
      identificadorExterno: 'online-ok',
      origem: 'GATEWAY',
    })
    expect(backend.lastValidatePayload.idempotencyKey).toBeTruthy()
    expect(backend.lastValidatePayload.dataHoraEvento).toMatch(/^\d{4}-\d{2}-\d{2}T/)
  })

  it('online validation allowed persists ONLINE event', async () => {
    backend.validateResponse = { permitido: true, alunoId: 'al1', motivo: 'ok' }
    const res = await svc.validateAccess('CARD', 'online-ok')
    expect(res.allowed).toBe(true)
    // pending events should be 1
    expect(db.pendingEventsCount()).toBe(1)
  })

  it('online validation blocked persists ONLINE blocked event', async () => {
    backend.validateResponse = { permitido: false, motivo: 'blocked' }
    const res = await svc.validateAccess('CARD', 'online-block')
    expect(res.allowed).toBe(false)
    expect(db.pendingEventsCount()).toBe(1)
  })

  it('backend unavailable falls back to offline and blocks without snapshot', async () => {
    backend.validateResponse = new Error('network')
    const res = await svc.validateAccess('CARD', 'no-snapshot')
    expect(res.allowed).toBe(false)
    expect(db.pendingEventsCount()).toBe(1)
  })

  it('backend unavailable falls back to offline and allows with valid snapshot', async () => {
    // insert snapshot using the same normalized vocabulary fetchAndStoreSnapshot would store
    db.saveSnapshotItems([
      { id: 'ss', snapshotVersion: 'v', generatedAt: Date.now(), validUntil: Date.now() + 10000, credentialType: 'CARTAO', externalIdentifier: 'ok-card', allowed: true, updatedAt: Date.now() },
    ] as any)
    backend.validateResponse = new Error('network')
    const res = await svc.validateAccess('CARD', 'ok-card')
    expect(res.allowed).toBe(true)
    expect(db.pendingEventsCount()).toBe(1)
  })

  it('syncPendingEvents marks events as synced when backend succeeds', async () => {
    backend.validateResponse = new Error('network')
    await svc.validateAccess('CARD', 'to-sync')
    expect(db.pendingEventsCount()).toBe(1)
    const result = await svc.syncPendingEvents()
    expect(result.synced).toBe(1)
    expect(db.pendingEventsCount()).toBe(0)
  })

  it('syncPendingEvents maps local events to backend Java DTO fields', async () => {
    backend.validateResponse = { permitido: false, alunoId: 55, motivo: 'blocked by rule' }
    await svc.validateAccess('CARD', 'to-sync')

    const result = await svc.syncPendingEvents()

    expect(result.synced).toBe(1)
    expect(backend.syncedEvents).toHaveLength(1)
    expect(backend.syncedEvents[0]).toMatchObject({
      alunoId: 55,
      credencialTipo: 'CARTAO',
      identificadorExterno: 'to-sync',
      origem: 'GATEWAY',
      modo: 'ONLINE',
      resultado: 'BLOQUEADO',
      motivo: 'blocked by rule',
    })
    expect(backend.syncedEvents[0].dataHoraEvento).toMatch(/^\d{4}-\d{2}-\d{2}T/)
    expect(backend.syncedEvents[0].identificadorExternoEvento).toBeTruthy()
    expect(backend.syncedEvents[0].idempotencyKey).toBeTruthy()
  })

  it('sendHeartbeat uses the backend heartbeat DTO shape', async () => {
    const result = await svc.sendHeartbeat()

    expect(result).toBe(true)
    expect(backend.heartbeatPayload).toMatchObject({
      gatewayId: 'gw-test',
      status: 'ATIVO',
      modoOperacao: 'HIBRIDO',
      pendingEvents: 0,
      version: '0.1.0',
    })
    expect(backend.heartbeatPayload.timestamp).toMatch(/^\d{4}-\d{2}-\d{2}T/)
  })
})
