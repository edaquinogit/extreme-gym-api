import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { CredenciaisAcessoAluno } from './CredenciaisAcessoAluno'
import { accessCredentialService } from '../../services/accessCredentialService'

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({
    user: { id: 1, nome: 'Admin', role: 'ADMIN' },
  }),
}))

vi.mock('../../services/accessCredentialService', () => ({
  accessCredentialService: {
    listarPorAluno: vi.fn(),
    criar: vi.fn(),
    revogar: vi.fn(),
  },
}))

const mockedAccessCredentialService = vi.mocked(accessCredentialService)

describe('CredenciaisAcessoAluno', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedAccessCredentialService.listarPorAluno.mockResolvedValue([
      {
        id: 2,
        alunoId: 7,
        tipo: 'QR_CODE',
        identificadorExternoMascarado: 'qr****01',
        fornecedor: 'Gateway',
        status: 'ATIVA',
        cadastradoEm: '2026-06-07T10:00:00',
        revogadoEm: null,
        termoAceitoEm: null,
        versaoTermo: 'v1',
        criadoEm: '2026-06-07T10:00:00',
        atualizadoEm: '2026-06-07T10:00:00',
      },
    ])
  })

  it('lists masked credentials and revokes through the real service endpoint wrapper', async () => {
    mockedAccessCredentialService.revogar.mockResolvedValue({
      id: 2,
      alunoId: 7,
      tipo: 'QR_CODE',
      identificadorExternoMascarado: 'qr****01',
      fornecedor: 'Gateway',
      status: 'REVOGADA',
      cadastradoEm: '2026-06-07T10:00:00',
      revogadoEm: '2026-06-07T11:00:00',
      termoAceitoEm: null,
      versaoTermo: 'v1',
      criadoEm: '2026-06-07T10:00:00',
      atualizadoEm: '2026-06-07T11:00:00',
    })

    render(<CredenciaisAcessoAluno alunoId={7} />)

    expect(await screen.findByText('QR Code')).toBeInTheDocument()
    expect(screen.getByText('qr****01')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Revogar' }))

    await waitFor(() => expect(mockedAccessCredentialService.revogar).toHaveBeenCalledWith(2))
    expect(await screen.findByText('Revogada')).toBeInTheDocument()
  })

  it('creates credentials without exposing the submitted identifier in the list', async () => {
    mockedAccessCredentialService.listarPorAluno.mockResolvedValue([])
    mockedAccessCredentialService.criar.mockResolvedValue({
      id: 3,
      alunoId: 7,
      tipo: 'PIN',
      identificadorExternoMascarado: '12**56',
      fornecedor: 'Recepção',
      status: 'ATIVA',
      cadastradoEm: '2026-06-07T10:00:00',
      revogadoEm: null,
      termoAceitoEm: null,
      versaoTermo: 'v1',
      criadoEm: '2026-06-07T10:00:00',
      atualizadoEm: '2026-06-07T10:00:00',
    })

    render(<CredenciaisAcessoAluno alunoId={7} />)

    fireEvent.change(await screen.findByLabelText('Tipo *'), { target: { value: 'PIN' } })
    fireEvent.change(screen.getByLabelText('Identificador externo *'), { target: { value: '123456' } })
    fireEvent.change(screen.getByLabelText('Fornecedor'), { target: { value: 'Recepção' } })
    fireEvent.click(screen.getByRole('button', { name: 'Cadastrar credencial' }))

    await waitFor(() =>
      expect(mockedAccessCredentialService.criar).toHaveBeenCalledWith(7, {
        tipo: 'PIN',
        identificadorExterno: '123456',
        fornecedor: 'Recepção',
        termoAceitoEm: '',
        versaoTermo: '',
      }),
    )
    expect(await screen.findByText('12**56')).toBeInTheDocument()
    expect(document.body.textContent).not.toContain('123456')
  })
})
