export type StatusMatricula =
  | 'ATIVA'
  | 'CANCELADA'
  | 'VENCIDA'

export type Matricula = {
  id: number
  alunoId?: number
  alunoNome?: string
  aluno?: { id: number; nome: string }
  planoId?: number
  planoNome?: string
  plano?: { id: number; nome: string }
  status: StatusMatricula
  dataInicio?: string
  dataFim?: string
  dataVencimento?: string
  dataCadastro?: string
}

export type MatriculaRequestDTO = {
  alunoId: number
  planoId: number
  dataInicio: string
}
