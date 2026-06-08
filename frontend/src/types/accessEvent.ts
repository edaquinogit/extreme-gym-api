export type AccessEventOrigin =
  | 'DISPOSITIVO'
  | 'FACE_ID'
  | 'GATEWAY'
  | 'MANUAL'
  | 'QR_CODE'
  | 'RECEPCAO'
  | 'SISTEMA'

export type AccessEventMode = 'ONLINE' | 'OFFLINE'

export type AccessEventResult = 'LIBERADO' | 'BLOQUEADO'

export type AccessEvent = {
  id: number
  alunoId: number | null
  alunoNome: string
  dispositivoId: number | null
  dispositivoNome: string
  matriculaId: number | null
  origem: AccessEventOrigin
  modo: AccessEventMode
  resultado: AccessEventResult
  motivo: string
  dataHoraEvento: string
  dataHoraRecebimento: string
  sincronizado: boolean
  identificadorExternoEvento: string
  criadoEm: string
}
