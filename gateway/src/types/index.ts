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
