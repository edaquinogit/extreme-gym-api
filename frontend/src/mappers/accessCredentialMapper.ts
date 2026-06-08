import type {
  AccessCredential,
  AccessCredentialCreatePayload,
  AccessCredentialStatus,
  AccessCredentialType,
} from '../types/accessCredential'

export type AccessCredentialAPI = {
  id?: number | null
  alunoId?: number | null
  tipo?: string | null
  identificadorExterno?: string | null
  fornecedor?: string | null
  status?: string | null
  cadastradoEm?: string | null
  revogadoEm?: string | null
  termoAceitoEm?: string | null
  versaoTermo?: string | null
  criadoEm?: string | null
  atualizadoEm?: string | null
}

export type AccessCredentialCreateAPI = {
  tipo: AccessCredentialType
  identificadorExterno: string
  fornecedor?: string
  termoAceitoEm?: string
  versaoTermo?: string
}

export function accessCredentialApiToViewModel(
  api: AccessCredentialAPI,
): AccessCredential {
  return {
    id: api.id ?? 0,
    alunoId: api.alunoId ?? 0,
    tipo: normalizeType(api.tipo),
    identificadorExternoMascarado: maskIdentifier(api.identificadorExterno),
    fornecedor: api.fornecedor?.trim() || 'Fornecedor não informado',
    status: normalizeStatus(api.status),
    cadastradoEm: api.cadastradoEm ?? '',
    revogadoEm: api.revogadoEm ?? null,
    termoAceitoEm: api.termoAceitoEm ?? null,
    versaoTermo: api.versaoTermo?.trim() || 'Não informada',
    criadoEm: api.criadoEm ?? '',
    atualizadoEm: api.atualizadoEm ?? '',
  }
}

export function accessCredentialCreateToApi(
  payload: AccessCredentialCreatePayload,
): AccessCredentialCreateAPI {
  return removeBlankValues({
    tipo: payload.tipo,
    identificadorExterno: payload.identificadorExterno.trim(),
    fornecedor: payload.fornecedor?.trim(),
    termoAceitoEm: payload.termoAceitoEm,
    versaoTermo: payload.versaoTermo?.trim(),
  })
}

function normalizeType(value?: string | null): AccessCredentialType {
  const normalized = String(value ?? '').trim().toUpperCase()
  if (
    normalized === 'CARTAO' ||
    normalized === 'FACE_TEMPLATE' ||
    normalized === 'PIN' ||
    normalized === 'QR_CODE'
  ) {
    return normalized
  }
  return 'QR_CODE'
}

function normalizeStatus(value?: string | null): AccessCredentialStatus {
  const normalized = String(value ?? '').trim().toUpperCase()
  if (
    normalized === 'ATIVA' ||
    normalized === 'INATIVA' ||
    normalized === 'PENDENTE' ||
    normalized === 'REVOGADA'
  ) {
    return normalized
  }
  return 'PENDENTE'
}

export function maskIdentifier(value?: string | null) {
  const identifier = value?.trim() ?? ''
  if (!identifier) {
    return 'Identificador não informado'
  }
  if (identifier.length <= 4) {
    return '*'.repeat(identifier.length)
  }
  return `${identifier.slice(0, 2)}${'*'.repeat(Math.max(4, identifier.length - 4))}${identifier.slice(-2)}`
}

function removeBlankValues<T extends Record<string, unknown>>(payload: T) {
  return Object.fromEntries(
    Object.entries(payload).filter(([, value]) => value !== undefined && value !== ''),
  ) as T
}
