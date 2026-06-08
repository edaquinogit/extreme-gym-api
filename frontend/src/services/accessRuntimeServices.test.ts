import { beforeEach, describe, expect, it, vi } from 'vitest'
import { accessCredentialService } from './accessCredentialService'
import { accessDeviceService } from './accessDeviceService'
import { accessEventService } from './accessEventService'
import { httpClient } from './httpClient'

vi.mock('./httpClient', () => ({
  httpClient: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
  },
}))

const mockedHttpClient = vi.mocked(httpClient)

describe('access runtime services', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('uses human JWT endpoints for devices and events', async () => {
    mockedHttpClient.get.mockResolvedValueOnce([])
    mockedHttpClient.get.mockResolvedValueOnce([])

    await accessDeviceService.listar()
    await accessEventService.listar()

    expect(mockedHttpClient.get).toHaveBeenNthCalledWith(1, '/dispositivos-acesso')
    expect(mockedHttpClient.get).toHaveBeenNthCalledWith(2, '/eventos-acesso')
  })

  it('creates devices without sending a device api key header', async () => {
    mockedHttpClient.post.mockResolvedValue({
      dispositivo: { id: 1, nome: 'Gateway' },
      apiKeyPlaintext: 'secret',
    })

    await accessDeviceService.criar({
      nome: 'Gateway',
      tipo: 'GATEWAY',
      modoOperacao: 'HIBRIDO',
    })

    expect(mockedHttpClient.post).toHaveBeenCalledWith('/dispositivos-acesso', {
      nome: 'Gateway',
      tipo: 'GATEWAY',
      modoOperacao: 'HIBRIDO',
    })
  })

  it('uses scoped credential endpoints for list, create and revoke', async () => {
    mockedHttpClient.get.mockResolvedValueOnce([])
    mockedHttpClient.post.mockResolvedValueOnce({ id: 2, alunoId: 7, identificadorExterno: 'qr-1' })
    mockedHttpClient.patch.mockResolvedValueOnce({ id: 2, alunoId: 7, status: 'REVOGADA' })

    await accessCredentialService.listarPorAluno(7)
    await accessCredentialService.criar(7, {
      tipo: 'QR_CODE',
      identificadorExterno: 'qr-1',
    })
    await accessCredentialService.revogar(2)

    expect(mockedHttpClient.get).toHaveBeenCalledWith('/alunos/7/credenciais-acesso')
    expect(mockedHttpClient.post).toHaveBeenCalledWith('/alunos/7/credenciais-acesso', {
      tipo: 'QR_CODE',
      identificadorExterno: 'qr-1',
    })
    expect(mockedHttpClient.patch).toHaveBeenCalledWith('/credenciais-acesso/2/revogar', {})
  })
})
