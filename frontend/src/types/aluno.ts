export type StatusAluno = 'ATIVO' | 'INADIMPLENTE' | 'BLOQUEADO' | 'CANCELADO' | 'INATIVO'

export type Aluno = {
  id: number
  nome: string
  email: string
  telefone: string
  status: StatusAluno
  dataCadastro: string
}

export type AlunoRequestDTO = {
  nome: string
  email: string
  telefone: string
}
