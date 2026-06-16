export type GlobalSearchGroup = 'alunos' | 'matriculas' | 'pagamentos' | 'planos'

export type GlobalSearchItemType =
  | 'ALUNO'
  | 'MATRICULA'
  | 'PAGAMENTO'
  | 'PLANO'

export type GlobalSearchItem = {
  id: number
  tipo: GlobalSearchItemType
  tipoLabel: string
  titulo: string
  subtitulo: string
  status: string
  rota: string | null
  metadata: Record<string, unknown>
}

export type GlobalSearchResult = {
  alunos: GlobalSearchItem[]
  matriculas: GlobalSearchItem[]
  pagamentos: GlobalSearchItem[]
  planos: GlobalSearchItem[]
}
