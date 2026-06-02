export type StatusCheckin = 'AUTORIZADO' | 'BLOQUEADO'

export type Checkin = {
  id: number
  alunoId?: number
  alunoNome?: string
  aluno?: { id: number; nome: string }
  matriculaId?: number | null
  permitido: boolean
  dataHora: string
  status: StatusCheckin
  motivo?: string
  motivoBloqueio?: string
}
