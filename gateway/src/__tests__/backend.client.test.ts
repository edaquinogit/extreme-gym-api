import { beforeEach, describe, expect, it, vi } from 'vitest'
import { BackendClient } from '../backend/client'
import type { GatewayConfig } from '../config'
import type { BackendEventSyncItem, BackendHeartbeatPayload } from '../types'
import pino from 'pino'

const axiosMock = vi.hoisted(() => ({
  create: vi.fn(),
  get: vi.fn(),
  post: vi.fn(),
}))

vi.mock('axios', () => ({
  default: {
    create: axiosMock.create,
  },
}))

function makeConfig(): GatewayConfig {
  return {
    environment: 'test',
    gateway: { id: 'gw-test', name: 'gw', port: 4000, dataPath: ':memory:' },
    backend: { baseUrl: 'http://backend.test', deviceId: '101', deviceApiKey: 'k', deviceHmacSecret: 's' },
    admin: { apiKey: 'admin-k' },
    storage: { databasePath: ':memory:' },
    timing: { snapshotRefreshSeconds: 300, snapshotTtlSeconds: 900, syncIntervalSeconds: 60, syncBatchSize: 50 },
    feature: { offlineMode: true, offlineStrict: true },
    logger: { level: 'silent' },
  }
}

const logger = pino({ level: 'silent' })

describe('BackendClient contract with Spring Boot API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    axiosMock.create.mockReturnValue({
      get: axiosMock.get,
      post: axiosMock.post,
    })
    axiosMock.get.mockResolvedValue({ data: [] })
    axiosMock.post.mockResolvedValue({ data: { ok: true } })
  })

  it('sends heartbeat to the backend device heartbeat path', async () => {
    const client = new BackendClient(makeConfig(), logger)
    const payload: BackendHeartbeatPayload = { statusOperacional: 'ONLINE' }

    await client.sendHeartbeat(payload)

    expect(axiosMock.post).toHaveBeenCalledWith(
      '/dispositivos-acesso/101/heartbeat',
      payload,
      expect.objectContaining({
        headers: expect.objectContaining({
          'X-Device-Api-Key': 'k',
          'X-Gateway-Id': 'gw-test',
        }),
      }),
    )
  })

  it('wraps event sync batch payload in the eventos property', async () => {
    const client = new BackendClient(makeConfig(), logger)
    const events: BackendEventSyncItem[] = [
      {
        alunoId: 55,
        dispositivoId: 101,
        origem: 'GATEWAY',
        modo: 'ONLINE',
        resultado: 'LIBERADO',
        motivo: 'ok',
        dataHoraEvento: '2026-06-04T12:00:00.000',
        sincronizado: true,
        identificadorExternoEvento: 'event-1',
        idempotencyKey: 'idem-1',
      },
    ]

    await client.syncEventsBatch(events)

    expect(axiosMock.post).toHaveBeenCalledWith(
      '/eventos-acesso/sincronizar-lote',
      { eventos: events },
      expect.objectContaining({
        headers: expect.objectContaining({
          'X-Device-Api-Key': 'k',
          'X-Gateway-Id': 'gw-test',
        }),
      }),
    )
  })
})
