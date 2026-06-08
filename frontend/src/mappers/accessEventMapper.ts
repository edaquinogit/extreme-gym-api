import type {
  AccessEvent,
  AccessEventMode,
  AccessEventOrigin,
  AccessEventResult,
} from '../types/accessEvent'

export type AccessEventAPI = {
  id?: number | null
  alunoId?: number | null
  alunoNome?: string | null
  dispositivoId?: number | null
  dispositivoNome?: string | null
  matriculaId?: number | null
  origem?: string | null
  modo?: string | null
  resultado?: string | null
  motivo?: string | null
  dataHoraEvento?: string | null
  dataHoraRecebimento?: string | null
  sincronizado?: boolean | null
  identificadorExternoEvento?: string | null
  criadoEm?: string | null
}

export function accessEventApiToViewModel(api: AccessEventAPI): AccessEvent {
  const dataHoraRecebimento = api.dataHoraRecebimento ?? ''

  return {
    id: api.id ?? 0,
    alunoId: api.alunoId ?? null,
    alunoNome: api.alunoNome?.trim() ?? '',
    dispositivoId: api.dispositivoId ?? null,
    dispositivoNome: api.dispositivoNome?.trim() ?? '',
    matriculaId: api.matriculaId ?? null,
    origem: normalizeOrigin(api.origem),
    modo: normalizeMode(api.modo),
    resultado: normalizeResult(api.resultado),
    motivo: api.motivo?.trim() || 'Motivo não informado.',
    dataHoraEvento: api.dataHoraEvento ?? '',
    dataHoraRecebimento,
    sincronizado: api.sincronizado ?? Boolean(dataHoraRecebimento),
    identificadorExternoEvento: api.identificadorExternoEvento?.trim() ?? '',
    criadoEm: api.criadoEm ?? dataHoraRecebimento,
  }
}

function normalizeOrigin(value?: string | null): AccessEventOrigin {
  const normalized = String(value ?? '').trim().toUpperCase()
  if (
    normalized === 'DISPOSITIVO' ||
    normalized === 'FACE_ID' ||
    normalized === 'GATEWAY' ||
    normalized === 'MANUAL' ||
    normalized === 'QR_CODE' ||
    normalized === 'RECEPCAO' ||
    normalized === 'SISTEMA'
  ) {
    return normalized
  }
  return 'SISTEMA'
}

function normalizeMode(value?: string | null): AccessEventMode {
  const normalized = String(value ?? '').trim().toUpperCase()
  return normalized === 'OFFLINE' ? 'OFFLINE' : 'ONLINE'
}

function normalizeResult(value?: string | null): AccessEventResult {
  const normalized = String(value ?? '').trim().toUpperCase()
  return normalized === 'LIBERADO' ? 'LIBERADO' : 'BLOQUEADO'
}
