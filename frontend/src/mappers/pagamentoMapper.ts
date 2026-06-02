import type {
  Pagamento,
  PagamentoRequestDTO,
  StatusPagamento,
} from '../types/pagamento'

export type PagamentoAPI = {
  id?: number | null
  matriculaId?: number | null
  alunoId?: number | null
  alunoNome?: string | null
  planoId?: number | null
  planoNome?: string | null
  valor?: number | string | null
  formaPagamento?: Pagamento['formaPagamento'] | null
  status?: StatusPagamento | null
  dataPagamento?: string | null
  dataCadastro?: string | null
}

export type PagamentoRequestAPI = {
  matriculaId: number
  valor: number
  formaPagamento: Pagamento['formaPagamento']
}

export function pagamentoApiToViewModel(api: PagamentoAPI): Pagamento {
  return {
    id: api.id ?? 0,
    matriculaId: api.matriculaId ?? undefined,
    alunoId: api.alunoId ?? undefined,
    alunoNome: api.alunoNome ?? undefined,
    planoId: api.planoId ?? undefined,
    planoNome: api.planoNome ?? undefined,
    valor: toNumber(api.valor),
    formaPagamento: api.formaPagamento ?? undefined,
    status: api.status ?? 'PENDENTE',
    dataPagamento: api.dataPagamento ?? null,
    dataCadastro: api.dataCadastro ?? undefined,
    dataVencimento: undefined,
  }
}

export function pagamentoViewModelToAPI(data: PagamentoRequestDTO): PagamentoRequestAPI {
  return {
    matriculaId: data.matriculaId,
    valor: data.valor,
    formaPagamento: data.formaPagamento,
  }
}

function toNumber(value: number | string | null | undefined): number {
  if (typeof value === 'number') return value
  if (typeof value === 'string') return Number(value) || 0
  return 0
}
