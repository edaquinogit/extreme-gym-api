export type AccessCredentialType = 'CARTAO' | 'FACE_TEMPLATE' | 'PIN' | 'QR_CODE'

export type AccessCredentialStatus = 'ATIVA' | 'INATIVA' | 'PENDENTE' | 'REVOGADA'

export type AccessCredential = {
  id: number
  alunoId: number
  tipo: AccessCredentialType
  identificadorExternoMascarado: string
  fornecedor: string
  status: AccessCredentialStatus
  cadastradoEm: string
  revogadoEm: string | null
  termoAceitoEm: string | null
  versaoTermo: string
  criadoEm: string
  atualizadoEm: string
}

export type AccessCredentialCreatePayload = {
  tipo: AccessCredentialType
  identificadorExterno: string
  fornecedor?: string
  termoAceitoEm?: string
  versaoTermo?: string
}
