import type { Matricula, MatriculaRequestDTO, StatusMatricula } from '../types/matricula'

export type MatriculaAPI = {
  id?: number | null
  alunoId?: number | null
  alunoNome?: string | null
  planoId?: number | null
  planoNome?: string | null
  dataInicio?: string | null
  dataFim?: string | null
  status?: StatusMatricula | null
  dataCadastro?: string | null
}

export type MatriculaRequestAPI = {
  alunoId: number
  planoId: number
  dataInicio: string
}

export function matriculaApiToViewModel(api: MatriculaAPI): Matricula {
  return {
    id: api.id ?? 0,
    alunoId: api.alunoId ?? undefined,
    alunoNome: api.alunoNome ?? undefined,
    planoId: api.planoId ?? undefined,
    planoNome: api.planoNome ?? undefined,
    status: api.status ?? 'ATIVA',
    dataInicio: api.dataInicio ?? undefined,
    dataFim: api.dataFim ?? undefined,
    dataVencimento: api.dataFim ?? undefined,
    dataCadastro: api.dataCadastro ?? undefined,
  }
}

export function matriculaViewModelToAPI(data: MatriculaRequestDTO): MatriculaRequestAPI {
  return {
    alunoId: data.alunoId,
    planoId: data.planoId,
    dataInicio: data.dataInicio,
  }
}
