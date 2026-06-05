export type SnapshotItem = {
  id: string
  snapshotVersion: string
  generatedAt: number
  validUntil?: number | null
  credentialType: string
  externalIdentifier: string
  allowed: boolean
  blockReason?: string | null
  validUntilAccess?: number | null
  updatedAt: number
}

export type BackendSnapshotItem = {
  id?: string | number
  snapshotVersion?: string | number
  version?: string | number
  generatedAt?: string | number
  validUntil?: string | number | null
  credentialType?: string
  type?: string
  externalIdentifier?: string
  key?: string
  allowed?: boolean
  blockReason?: string | null
  validUntilAccess?: string | number | null
}

export type BackendValidateAccessRequest = {
  credentialType: string
  externalIdentifier: string
  deviceId: string
  idempotencyKey: string
}

export type BackendValidateAccessResponse = {
  allowed?: boolean
  alunoId?: string | number | null
  reason?: string
}

export type BackendHeartbeatPayload = {
  statusOperacional: 'ONLINE'
}

export type BackendEventSyncItem = {
  alunoId: number | null
  dispositivoId: number | string
  origem: 'GATEWAY'
  modo: 'ONLINE' | 'OFFLINE'
  resultado: 'LIBERADO' | 'BLOQUEADO'
  motivo: string
  dataHoraEvento: string
  sincronizado: boolean
  identificadorExternoEvento: string
  idempotencyKey: string
}

export type BackendEventSyncRequest = {
  eventos: BackendEventSyncItem[]
}

export type LocalAccessEvent = {
  id: string
  idempotencyKey: string
  alunoId?: string | null
  deviceId: string
  credentialType: string
  externalIdentifier: string
  mode: 'ONLINE' | 'OFFLINE'
  result: 'ALLOWED' | 'BLOCKED'
  reason: string
  eventTime: number
  receivedAt: number
  synced: boolean
  syncedAt?: number | null
  syncAttempts: number
  lastSyncError?: string | null
}
