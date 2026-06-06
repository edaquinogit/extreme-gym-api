export type StatusPlano = 'ATIVO' | 'INATIVO'

export type Plano = {
  id: number
  nome: string
  descricao?: string
  valor: number
  duracaoDias?: number
  status: StatusPlano
  dataCadastro?: string
}

export type PlanoRequestDTO = {
  nome: string
  descricao?: string
  valor: number
  duracaoDias: number
}
