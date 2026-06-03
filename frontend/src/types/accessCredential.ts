export type TipoCredencialAcesso =
  | 'FACE_TEMPLATE'
  | 'QR_CODE'
  | 'CARTAO'
  | 'PIN'

export type StatusCredencialAcesso =
  | 'ATIVA'
  | 'INATIVA'
  | 'REVOGADA'
  | 'PENDENTE'

export type CredencialAcesso = {
  id: number
  alunoId: number
  tipo: TipoCredencialAcesso
  tipoLabel: string
  identificadorExterno: string
  identificadorExternoMascarado: string
  fornecedor: string
  status: StatusCredencialAcesso
  statusLabel: string
  cadastradoEm: string | null
  cadastradoEmLabel: string
  revogadoEm: string | null
  revogadoEmLabel: string
  termoAceitoEm: string | null
  termoAceitoEmLabel: string
  versaoTermo: string
  criadoEm: string | null
  atualizadoEm: string | null
}

export type CredencialAcessoRequest = {
  tipo: TipoCredencialAcesso
  identificadorExterno: string
  fornecedor?: string | null
  status?: StatusCredencialAcesso
  termoAceitoEm?: string | null
  versaoTermo?: string | null
}
