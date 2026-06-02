import type { Aluno, AlunoRequestDTO, StatusAluno } from '../types/aluno'

export type AlunoAPI = {
  id?: number | null
  nome?: string | null
  email?: string | null
  telefone?: string | null
  status?: StatusAluno | null
  dataCadastro?: string | null
}

export type AlunoStatusUpdateAPI = {
  status: StatusAluno
}

export function alunoApiToViewModel(api: AlunoAPI): Aluno {
  return {
    id: api.id ?? 0,
    nome: api.nome ?? '',
    email: api.email ?? '',
    telefone: api.telefone ?? '',
    status: api.status ?? 'ATIVO',
    dataCadastro: api.dataCadastro ?? '',
  }
}

export function alunoViewModelToAPI(data: AlunoRequestDTO): AlunoRequestDTO {
  return {
    nome: data.nome.trim(),
    email: data.email.trim(),
    telefone: data.telefone.trim(),
  }
}

export function alunoStatusToAPI(status: StatusAluno): AlunoStatusUpdateAPI {
  return { status }
}
