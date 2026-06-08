import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { CatracaPage } from './CatracaPage'
import { acessoService } from '../../services/acessoService'
import { accessDeviceService } from '../../services/accessDeviceService'
import { accessEventService } from '../../services/accessEventService'
import { alunoService } from '../../services/alunoService'
import { checkinService } from '../../services/checkinService'
import { matriculaService } from '../../services/matriculaService'
import { pagamentoService } from '../../services/pagamentoService'
import { HttpError } from '../../services/httpClient'

vi.mock('../../services/acessoService', () => ({
  acessoService: {
    validar: vi.fn(),
  },
}))

vi.mock('../../services/accessDeviceService', () => ({
  accessDeviceService: {
    listar: vi.fn(),
  },
}))

vi.mock('../../services/accessEventService', () => ({
  accessEventService: {
    listar: vi.fn(),
  },
}))

vi.mock('../../services/alunoService', () => ({
  alunoService: {
    buscar: vi.fn(),
  },
}))

vi.mock('../../services/checkinService', () => ({
  checkinService: {
    registrar: vi.fn(),
  },
}))

vi.mock('../../services/matriculaService', () => ({
  matriculaService: {
    listar: vi.fn(),
  },
}))

vi.mock('../../services/pagamentoService', () => ({
  pagamentoService: {
    listar: vi.fn(),
  },
}))

const mockedAcessoService = vi.mocked(acessoService)
const mockedAccessDeviceService = vi.mocked(accessDeviceService)
const mockedAccessEventService = vi.mocked(accessEventService)
const mockedAlunoService = vi.mocked(alunoService)
const mockedCheckinService = vi.mocked(checkinService)
const mockedMatriculaService = vi.mocked(matriculaService)
const mockedPagamentoService = vi.mocked(pagamentoService)

describe('CatracaPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedAccessDeviceService.listar.mockResolvedValue([])
    mockedAccessEventService.listar.mockResolvedValue([])
    mockedAlunoService.buscar.mockResolvedValue({
      id: 10,
      nome: 'Ana Silva',
      email: 'ana@example.com',
      telefone: '71999999999',
      status: 'ATIVO',
      dataCadastro: '2026-01-10T10:00:00',
    })
    mockedMatriculaService.listar.mockResolvedValue([])
    mockedPagamentoService.listar.mockResolvedValue([])
    mockedCheckinService.registrar.mockResolvedValue({
      id: 1,
      alunoId: 10,
      alunoNome: 'Ana Silva',
      permitido: true,
      dataHora: '2026-06-02T10:00:00',
      status: 'AUTORIZADO',
      motivo: 'Acesso liberado.',
    })
  })

  it('renders initial operational state without fake device telemetry', async () => {
    render(<CatracaPage />)

    expect(screen.getByText('Controle de Acesso')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Identificação do aluno' })).toBeInTheDocument()
    expect(screen.getByLabelText('ID do aluno')).toHaveFocus()
    expect(await screen.findByText('Gateway externo ainda não validado')).toBeInTheDocument()
    expect(screen.getByText('Nenhum dispositivo de acesso foi retornado pelo backend.')).toBeInTheDocument()
    expect(screen.getByText('Aguardando validação de acesso.')).toBeInTheDocument()
    expect(screen.getByText('Use a busca manual quando a identificação automática não estiver disponível.')).toBeInTheDocument()
    expect(screen.getByText('Nenhum evento de acesso retornado pelo backend.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Verificar acesso' })).toBeDisabled()
    expect(mockedAccessDeviceService.listar).toHaveBeenCalled()
    expect(mockedAccessEventService.listar).toHaveBeenCalled()
  })

  it('renders runtime device and recent events returned by human JWT endpoints', async () => {
    mockedAccessDeviceService.listar.mockResolvedValue([
      {
        id: 3,
        nome: 'Gateway recepção',
        tipo: 'GATEWAY',
        status: 'ATIVO',
        modoOperacao: 'HIBRIDO',
        identificadorExterno: 'gw-rec',
        fabricante: 'Extreme',
        modelo: 'EG-1',
        ipLocal: '10.0.0.5',
        unidade: 'Centro',
        ultimaComunicacaoEm: '2026-06-07T10:00:00',
        criadoEm: '2026-06-07T09:00:00',
        atualizadoEm: '2026-06-07T10:00:00',
      },
    ])
    mockedAccessEventService.listar.mockResolvedValue([
      {
        id: 9,
        alunoId: 10,
        alunoNome: 'Ana Silva',
        dispositivoId: 3,
        dispositivoNome: 'Gateway recepção',
        matriculaId: 5,
        origem: 'GATEWAY',
        modo: 'ONLINE',
        resultado: 'LIBERADO',
        motivo: 'Matrícula ativa.',
        dataHoraEvento: '2026-06-07T10:01:00',
        dataHoraRecebimento: '2026-06-07T10:01:01',
        sincronizado: true,
        identificadorExternoEvento: 'evt-9',
        criadoEm: '2026-06-07T10:01:01',
      },
    ])

    render(<CatracaPage />)

    expect(await screen.findByText('Gateway recepção')).toBeInTheDocument()
    expect(screen.getByText('Dispositivo ativo')).toBeInTheDocument()
    expect(screen.getByText('Ana Silva')).toBeInTheDocument()
  })

  it('shows loading while access validation is pending', async () => {
    let resolveValidation: (
      value: Awaited<ReturnType<typeof acessoService.validar>>,
    ) => void
    const validationPromise = new Promise<Awaited<ReturnType<typeof acessoService.validar>>>((resolve) => {
      resolveValidation = resolve
    })
    mockedAcessoService.validar.mockReturnValue(validationPromise)

    render(<CatracaPage />)

    fireEvent.change(screen.getByLabelText('ID do aluno'), { target: { value: '10' } })
    fireEvent.click(screen.getByRole('button', { name: 'Verificar acesso' }))

    expect(screen.getByRole('button', { name: 'Verificando...' })).toBeDisabled()
    expect(screen.getByText('Validando acesso...')).toBeInTheDocument()
    expect(screen.getByText('Consultando a situação do aluno.')).toBeInTheDocument()

    resolveValidation!({
      alunoId: 10,
      alunoNome: 'Ana Silva',
      acessoLiberado: true,
      motivo: 'Matrícula ativa.',
      matriculaId: null,
      dataValidadeMatricula: null,
    })

    expect(await screen.findByText('Acesso liberado.')).toBeInTheDocument()
  })

  it('shows released access and registers check-in through the real handler', async () => {
    mockedAcessoService.validar.mockResolvedValue({
      alunoId: 10,
      alunoNome: 'Ana Silva',
      acessoLiberado: true,
      motivo: 'Matrícula ativa.',
      matriculaId: 5,
      dataValidadeMatricula: '2026-06-30',
    })
    mockedMatriculaService.listar.mockResolvedValue([
      {
        id: 5,
        alunoId: 10,
        planoNome: 'Mensal',
        status: 'ATIVA',
        dataInicio: '2026-06-01',
        dataFim: '2026-06-30',
      },
    ])
    mockedPagamentoService.listar.mockResolvedValue([
      {
        id: 2,
        alunoId: 10,
        matriculaId: 5,
        valor: 120,
        status: 'PAGO',
        dataPagamento: '2026-06-01T10:00:00',
      },
    ])

    render(<CatracaPage />)

    fireEvent.change(screen.getByLabelText('ID do aluno'), { target: { value: '10' } })
    fireEvent.keyDown(screen.getByLabelText('ID do aluno'), { key: 'Enter' })

    expect(await screen.findByText('Acesso liberado.')).toBeInTheDocument()
    expect(screen.getByText('Entrada registrada com sucesso.')).toBeInTheDocument()
    expect(screen.getByText('Ana Silva')).toBeInTheDocument()
    expect(await screen.findByText('#5 - Mensal')).toBeInTheDocument()
    await waitFor(() => expect(mockedCheckinService.registrar).toHaveBeenCalledWith(10))
  })

  it('shows blocked access and the backend reason', async () => {
    mockedAcessoService.validar.mockResolvedValue({
      alunoId: 12,
      alunoNome: 'Carla Lima',
      acessoLiberado: false,
      motivo: 'pagamento pendente',
      matriculaId: null,
      dataValidadeMatricula: null,
    })

    render(<CatracaPage />)

    fireEvent.change(screen.getByLabelText('ID do aluno'), { target: { value: '12' } })
    fireEvent.click(screen.getByRole('button', { name: 'Verificar acesso' }))

    expect(await screen.findByText('Acesso bloqueado.')).toBeInTheDocument()
    expect(screen.getByText('Motivo: pagamento pendente')).toBeInTheDocument()
    expect(screen.getByText('Direcione o aluno para regularizar o pagamento.')).toBeInTheDocument()
    expect(mockedCheckinService.registrar).not.toHaveBeenCalled()
  })

  it('shows a friendly error when the validation API fails', async () => {
    mockedAcessoService.validar.mockRejectedValue(new Error('Network error'))

    render(<CatracaPage />)

    fireEvent.change(screen.getByLabelText('ID do aluno'), { target: { value: '13' } })
    fireEvent.click(screen.getByRole('button', { name: 'Verificar acesso' }))

    expect(await screen.findByText('Não foi possível validar o acesso')).toBeInTheDocument()
    expect(screen.getByText('Tente novamente em alguns instantes.')).toBeInTheDocument()
    expect(screen.queryByText('Network error')).not.toBeInTheDocument()
  })

  it('shows a specific not found state for 404', async () => {
    mockedAcessoService.validar.mockRejectedValue(new HttpError('Not found', 404, null))

    render(<CatracaPage />)

    fireEvent.change(screen.getByLabelText('ID do aluno'), { target: { value: '99' } })
    fireEvent.click(screen.getByRole('button', { name: 'Verificar acesso' }))

    expect(await screen.findByText('Aluno não encontrado')).toBeInTheDocument()
    expect(screen.getByText('Nenhum aluno encontrado com os dados informados.')).toBeInTheDocument()
  })

  it('does not render undefined or null when optional backend data is absent', async () => {
    mockedAcessoService.validar.mockResolvedValue({
      alunoId: 14,
      alunoNome: '',
      acessoLiberado: false,
      motivo: '',
      matriculaId: null,
      dataValidadeMatricula: null,
    })
    mockedAlunoService.buscar.mockRejectedValue(new Error('Aluno indisponível'))

    render(<CatracaPage />)

    fireEvent.change(screen.getByLabelText('ID do aluno'), { target: { value: '14' } })
    fireEvent.click(screen.getByRole('button', { name: 'Verificar acesso' }))

    expect(await screen.findByText('Aluno sem nome informado')).toBeInTheDocument()
    expect(document.body.textContent).not.toMatch(/undefined|null/)
  })
})
