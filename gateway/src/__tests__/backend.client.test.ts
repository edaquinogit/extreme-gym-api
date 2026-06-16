import { beforeEach, describe, expect, it, vi } from 'vitest'
import { BackendClient } from '../backend/client'
import type { GatewayConfig } from '../config'

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
    gateway: { id: 'gw-test', name: 'gw', version: '0.1.0', port: 4000, dataPath: ':memory:' },
    backend: { baseUrl: 'http://backend.test', deviceId: '101', deviceApiKey: 'k', deviceHmacSecret: 's' },
    admin: { apiKey: 'admin-k' },
    storage: { databasePath: ':memory:' },
    timing: { snapshotRefreshSeconds: 300, snapshotTtlSeconds: 900, syncIntervalSeconds: 60, syncBatchSize: 50 },
    feature: { offlineMode: true, offlineStrict: true },
    logger: { level: 'silent' },
  }
}

const logger = {
  info: vi.fn(),
  debug: vi.fn(),
  warn: vi.fn(),
  error: vi.fn(),
}

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
    const client = new BackendClient(makeConfig(), logger as any)
    const payload = {
      gatewayId: 'gw-test',
      timestamp: '2026-06-04T12:00:00.000',
      status: 'ATIVO',
      modoOperacao: 'HIBRIDO',
      pendingEvents: 0,
      version: '0.1.0',
    }

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
    const client = new BackendClient(makeConfig(), logger as any)
    const events = [
      {
        alunoId: 55,
        credencialTipo: 'CARTAO',
        identificadorExterno: 'card-123',
        origem: 'GATEWAY',
        modo: 'ONLINE',
        resultado: 'LIBERADO',
        motivo: 'ok',
        dataHoraEvento: '2026-06-04T12:00:00.000',
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
