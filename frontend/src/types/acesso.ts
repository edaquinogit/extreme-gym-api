export type AcessoResponse = {
  alunoId: number
  alunoNome: string
  acessoLiberado: boolean
  motivo: string
  matriculaId: number | null
  dataValidadeMatricula: string | null
}
