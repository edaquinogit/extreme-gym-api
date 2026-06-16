export type TipoDispositivoAcesso =
  | 'CATRACA_FACIAL'
  | 'CATRACA_QR'
  | 'RECEPCAO'
  | 'OUTRO'

export type StatusDispositivoAcesso =
  | 'ATIVO'
  | 'INATIVO'
  | 'MANUTENCAO'
  | 'OFFLINE'

export type ModoOperacaoDispositivo = 'ONLINE' | 'OFFLINE' | 'HIBRIDO'

export type DispositivoAcesso = {
  id: number
  nome: string
  tipo: TipoDispositivoAcesso
  tipoLabel: string
  fabricante: string | null
  modelo: string | null
  identificadorExterno: string | null
  ipLocal: string | null
  unidade: string
  status: StatusDispositivoAcesso
  statusLabel: string
  modoOperacao: ModoOperacaoDispositivo
  modoOperacaoLabel: string
  ultimaComunicacaoEm: string | null
  ultimaComunicacaoLabel: string
  criadoEm: string | null
  atualizadoEm: string | null
}

export type DispositivoAcessoViewModel = DispositivoAcesso & {
  eventosPendentes: number
}

export type DispositivoAcessoRequest = {
  nome: string
  tipo: TipoDispositivoAcesso
  fabricante?: string | null
  modelo?: string | null
  identificadorExterno?: string | null
  ipLocal?: string | null
  unidade?: string | null
  status: StatusDispositivoAcesso
  modoOperacao: ModoOperacaoDispositivo
}
