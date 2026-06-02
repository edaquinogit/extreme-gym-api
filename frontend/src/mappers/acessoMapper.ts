import type { AcessoResponse } from '../types/acesso'

export type AcessoAPI = {
  alunoId?: number | null
  alunoNome?: string | null
  acessoLiberado?: boolean | null
  motivo?: string | null
  matriculaId?: number | null
  dataValidadeMatricula?: string | null
}

export type AcessoRequestAPI = {
  alunoId: number
}

export function acessoApiToViewModel(api: AcessoAPI): AcessoResponse {
  return {
    alunoId: api.alunoId ?? 0,
    alunoNome: api.alunoNome ?? '',
    acessoLiberado: api.acessoLiberado ?? false,
    motivo: api.motivo ?? '',
    matriculaId: api.matriculaId ?? null,
    dataValidadeMatricula: api.dataValidadeMatricula ?? null,
  }
}

export function acessoViewModelToAPI(alunoId: number): AcessoRequestAPI {
  return { alunoId }
}
