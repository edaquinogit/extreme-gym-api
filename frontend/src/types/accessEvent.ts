export type OrigemEventoAcesso =
  | 'FACE_ID'
  | 'MANUAL'
  | 'QR_CODE'
  | 'RECEPCAO'
  | 'SISTEMA'
  | 'GATEWAY'

export type ModoEventoAcesso = 'ONLINE' | 'OFFLINE'

export type ResultadoAcessoEvento = 'LIBERADO' | 'BLOQUEADO'

export type EventoAcesso = {
  id: number
  alunoId: number | null
  alunoNome: string
  dispositivoId: number
  dispositivoNome: string
  matriculaId: number | null
  origem: OrigemEventoAcesso
  origemLabel: string
  modo: ModoEventoAcesso
  modoLabel: string
  resultado: ResultadoAcessoEvento
  resultadoLabel: string
  motivo: string
  dataHoraEvento: string | null
  dataHoraEventoLabel: string
  dataHoraRecebimento: string | null
  sincronizado: boolean
  sincronizacaoLabel: string
  identificadorExternoEvento: string | null
  identificadorExternoEventoMascarado: string
  criadoEm: string | null
}
