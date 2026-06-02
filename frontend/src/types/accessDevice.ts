export type StatusDispositivoAcesso =
  | 'ONLINE'
  | 'OFFLINE'
  | 'MANUTENCAO'
  | 'DESCONHECIDO'

export type ModoOperacaoDispositivo = 'ONLINE' | 'OFFLINE' | 'HIBRIDO'

export type DispositivoAcessoViewModel = {
  id: string
  nome: string
  tipo: 'CATRACA' | 'GATEWAY' | 'FACIAL' | 'OUTRO'
  fabricante: string | null
  modelo: string | null
  status: StatusDispositivoAcesso
  modoOperacao: ModoOperacaoDispositivo
  ultimaComunicacaoEm: string | null
  eventosPendentes: number
  unidade: string
}
