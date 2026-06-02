import type { Plano, PlanoRequestDTO, StatusPlano } from '../types/plano'

export type PlanoAPI = {
  id?: number | null
  nome?: string | null
  descricao?: string | null
  valorMensal?: number | string | null
  duracaoEmDias?: number | null
  ativo?: boolean | null
  dataCadastro?: string | null
}

export type PlanoRequestAPI = {
  nome: string
  descricao?: string
  valorMensal: number
  duracaoEmDias: number
}

export function planoApiToViewModel(api: PlanoAPI): Plano {
  return {
    id: api.id ?? 0,
    nome: api.nome ?? '',
    descricao: api.descricao ?? undefined,
    valor: toNumber(api.valorMensal),
    duracaoDias: api.duracaoEmDias ?? undefined,
    status: ativoToStatus(api.ativo),
    ativo: api.ativo ?? false,
    dataCadastro: api.dataCadastro ?? undefined,
  }
}

export function planoViewModelToAPI(data: PlanoRequestDTO): PlanoRequestAPI {
  return {
    nome: data.nome.trim(),
    descricao: data.descricao?.trim() || undefined,
    valorMensal: data.valor,
    duracaoEmDias: data.duracaoDias,
  }
}

function ativoToStatus(ativo?: boolean | null): StatusPlano {
  return ativo === false ? 'INATIVO' : 'ATIVO'
}

function toNumber(value: number | string | null | undefined): number {
  if (typeof value === 'number') return value
  if (typeof value === 'string') return Number(value) || 0
  return 0
}
