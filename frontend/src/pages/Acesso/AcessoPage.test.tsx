import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AcessoPage } from './AcessoPage'
import { acessoService } from '../../services/acessoService'
import { checkinService } from '../../services/checkinService'
import { HttpError } from '../../services/httpClient'

vi.mock('../../services/acessoService', () => ({
  acessoService: {
    validar: vi.fn(),
  },
}))

vi.mock('../../services/checkinService', () => ({
  checkinService: {
    listar: vi.fn(),
    registrar: vi.fn(),
  },
}))

const mockedAcessoService = vi.mocked(acessoService)
const mockedCheckinService = vi.mocked(checkinService)

describe('AcessoPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedCheckinService.listar.mockResolvedValue([])
  })

  it('renders initial state and keeps validate disabled with empty field', async () => {
    render(<AcessoPage />)

    expect(screen.getByRole('heading', { name: 'Validar acesso' })).toBeInTheDocument()
    expect(screen.getByLabelText('ID do aluno *')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Validar acesso' })).toBeDisabled()
    expect(await screen.findByText('Nenhum check-in registrado.')).toBeInTheDocument()
  })

  it('allows typing the student id and validates with Enter', async () => {
    mockedAcessoService.validar.mockResolvedValue({
      alunoId: 10,
      alunoNome: 'Ana Silva',
      acessoLiberado: true,
      motivo: 'Matrícula ativa.',
      matriculaId: 5,
      dataValidadeMatricula: '2026-06-30',
    })

    render(<AcessoPage />)

    fireEvent.change(screen.getByLabelText('ID do aluno *'), { target: { value: '10' } })
    fireEvent.submit(screen.getByRole('button', { name: 'Validar acesso' }).closest('form')!)

    expect(mockedAcessoService.validar).toHaveBeenCalledWith(10)
    expect(await screen.findByText('Acesso liberado.')).toBeInTheDocument()
    expect(screen.getByText('Ana Silva')).toBeInTheDocument()
    expect(screen.getByText('Matrícula #5')).toBeInTheDocument()
  })

  it('shows loading while access validation is pending', async () => {
    let resolveValidation: Parameters<typeof mockedAcessoService.validar.mockResolvedValue>[0]
    const validationPromise = new Promise<Awaited<ReturnType<typeof acessoService.validar>>>((resolve) => {
      resolveValidation = {
        alunoId: 11,
        alunoNome: 'Bruno Souza',
        acessoLiberado: false,
        motivo: 'Pagamento pendente.',
        matriculaId: null,
        dataValidadeMatricula: null,
      }
      window.setTimeout(() => resolve(resolveValidation), 20)
    })
    mockedAcessoService.validar.mockReturnValue(validationPromise)

    render(<AcessoPage />)

    fireEvent.change(screen.getByLabelText('ID do aluno *'), { target: { value: '11' } })
    fireEvent.click(screen.getByRole('button', { name: 'Validar acesso' }))

    expect(screen.getByRole('button', { name: 'Validando acesso...' })).toBeDisabled()
    expect(screen.getByText('Consultando a situação do aluno.')).toBeInTheDocument()
    expect(await screen.findByText('Acesso bloqueado.')).toBeInTheDocument()
  })

  it('shows blocked access and backend reason', async () => {
    mockedAcessoService.validar.mockResolvedValue({
      alunoId: 12,
      alunoNome: 'Carla Lima',
      acessoLiberado: false,
      motivo: 'Aluno bloqueado.',
      matriculaId: null,
      dataValidadeMatricula: null,
    })

    render(<AcessoPage />)

    fireEvent.change(screen.getByLabelText('ID do aluno *'), { target: { value: '12' } })
    fireEvent.click(screen.getByRole('button', { name: 'Validar acesso' }))

    expect(await screen.findByText('Acesso bloqueado.')).toBeInTheDocument()
    expect(screen.getByText('Aluno bloqueado.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Registrar check-in' })).not.toBeInTheDocument()
  })

  it('shows not found message for 404', async () => {
    mockedAcessoService.validar.mockRejectedValue(new HttpError('Not found', 404, null))

    render(<AcessoPage />)

    fireEvent.change(screen.getByLabelText('ID do aluno *'), { target: { value: '99' } })
    fireEvent.click(screen.getByRole('button', { name: 'Validar acesso' }))

    expect(await screen.findByText('Aluno não encontrado')).toBeInTheDocument()
    expect(screen.getByText('Nenhum aluno encontrado com os dados informados.')).toBeInTheDocument()
  })

  it('shows friendly API error message when validation fails', async () => {
    mockedAcessoService.validar.mockRejectedValue(new Error('Network error'))

    render(<AcessoPage />)

    fireEvent.change(screen.getByLabelText('ID do aluno *'), { target: { value: '13' } })
    fireEvent.click(screen.getByRole('button', { name: 'Validar acesso' }))

    expect(await screen.findByText('Não foi possível validar o acesso')).toBeInTheDocument()
    expect(screen.getByText('Não foi possível validar o acesso agora. Tente novamente em alguns instantes.')).toBeInTheDocument()
  })

  it('shows check-in button only for released access and registers check-in', async () => {
    mockedAcessoService.validar.mockResolvedValue({
      alunoId: 14,
      alunoNome: 'Daniel Costa',
      acessoLiberado: true,
      motivo: 'Matrícula ativa.',
      matriculaId: 8,
      dataValidadeMatricula: null,
    })
    mockedCheckinService.registrar.mockResolvedValue({
      id: 1,
      alunoId: 14,
      alunoNome: 'Daniel Costa',
      permitido: true,
      dataHora: '2026-06-02T10:00:00',
      status: 'AUTORIZADO',
      motivo: 'Acesso liberado.',
    })

    render(<AcessoPage />)

    fireEvent.change(screen.getByLabelText('ID do aluno *'), { target: { value: '14' } })
    fireEvent.click(screen.getByRole('button', { name: 'Validar acesso' }))

    const checkinButton = await screen.findByRole('button', { name: 'Registrar check-in' })
    fireEvent.click(checkinButton)

    await waitFor(() => expect(mockedCheckinService.registrar).toHaveBeenCalledWith(14))
    expect(await screen.findByText('Check-in registrado com sucesso para Daniel Costa.')).toBeInTheDocument()
  })
})
