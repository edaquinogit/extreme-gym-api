import type {
  AccessDevice,
  AccessDeviceCreated,
  AccessDeviceCreatePayload,
  AccessDeviceOperationMode,
  AccessDeviceStatus,
  AccessDeviceType,
} from '../types/accessDevice'

export type AccessDeviceAPI = {
  id?: number | null
  nome?: string | null
  tipo?: string | null
  status?: string | null
  modoOperacao?: string | null
  identificadorExterno?: string | null
  fabricante?: string | null
  modelo?: string | null
  ipLocal?: string | null
  unidade?: string | null
  ultimaComunicacaoEm?: string | null
  criadoEm?: string | null
  atualizadoEm?: string | null
  apiKeyPlaintext?: string | null
  apiKeyHash?: string | null
}

export type AccessDeviceCreatedAPI = {
  dispositivo?: AccessDeviceAPI | null
  apiKeyPlaintext?: string | null
}

export type AccessDeviceCreateAPI = {
  nome: string
  tipo: AccessDeviceType
  modoOperacao: AccessDeviceOperationMode
  identificadorExterno?: string
  fabricante?: string
  modelo?: string
  ipLocal?: string
  unidade?: string
}

export function accessDeviceApiToViewModel(api: AccessDeviceAPI): AccessDevice {
  return {
    id: api.id ?? 0,
    nome: safeText(api.nome, 'Dispositivo sem nome'),
    tipo: normalizeDeviceType(api.tipo),
    status: normalizeDeviceStatus(api.status),
    modoOperacao: normalizeOperationMode(api.modoOperacao),
    identificadorExterno: safeText(api.identificadorExterno),
    fabricante: safeText(api.fabricante),
    modelo: safeText(api.modelo),
    ipLocal: safeText(api.ipLocal),
    unidade: safeText(api.unidade),
    ultimaComunicacaoEm: api.ultimaComunicacaoEm ?? null,
    criadoEm: api.criadoEm ?? '',
    atualizadoEm: api.atualizadoEm ?? '',
  }
}

export function accessDeviceCreatedApiToViewModel(
  api: AccessDeviceCreatedAPI,
): AccessDeviceCreated {
  return {
    dispositivo: accessDeviceApiToViewModel(api.dispositivo ?? {}),
    apiKeyPlaintext: api.apiKeyPlaintext ?? '',
  }
}

export function accessDeviceCreateToApi(
  payload: AccessDeviceCreatePayload,
): AccessDeviceCreateAPI {
  return removeBlankValues({
    nome: payload.nome.trim(),
    tipo: payload.tipo,
    modoOperacao: payload.modoOperacao,
    identificadorExterno: payload.identificadorExterno?.trim(),
    fabricante: payload.fabricante?.trim(),
    modelo: payload.modelo?.trim(),
    ipLocal: payload.ipLocal?.trim(),
    unidade: payload.unidade?.trim(),
  })
}

function normalizeDeviceStatus(value?: string | null): AccessDeviceStatus {
  const normalized = String(value ?? '').trim().toUpperCase()
  if (
    normalized === 'ATIVO' ||
    normalized === 'INATIVO' ||
    normalized === 'MANUTENCAO' ||
    normalized === 'OFFLINE'
  ) {
    return normalized
  }
  return 'OFFLINE'
}

function normalizeDeviceType(value?: string | null): AccessDeviceType {
  const normalized = String(value ?? '').trim().toUpperCase()
  if (
    normalized === 'CATRACA_FACIAL' ||
    normalized === 'CATRACA_QR' ||
    normalized === 'RECEPCAO' ||
    normalized === 'GATEWAY' ||
    normalized === 'OUTRO'
  ) {
    return normalized
  }
  return 'OUTRO'
}

function normalizeOperationMode(value?: string | null): AccessDeviceOperationMode {
  const normalized = String(value ?? '').trim().toUpperCase()
  if (normalized === 'ONLINE' || normalized === 'OFFLINE' || normalized === 'HIBRIDO') {
    return normalized
  }
  return 'ONLINE'
}

function safeText(value?: string | null, fallback = '') {
  return value?.trim() || fallback
}

function removeBlankValues<T extends Record<string, unknown>>(payload: T) {
  return Object.fromEntries(
    Object.entries(payload).filter(([, value]) => value !== undefined && value !== ''),
  ) as T
}
