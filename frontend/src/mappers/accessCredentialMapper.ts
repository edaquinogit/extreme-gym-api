import type {
  CredencialAcesso,
  CredencialAcessoRequest,
  StatusCredencialAcesso,
  TipoCredencialAcesso,
} from '../types/accessCredential'

export type CredencialAcessoAPI = {
  id?: number | null
  alunoId?: number | null
  tipo?: TipoCredencialAcesso | null
  identificadorExterno?: string | null
  fornecedor?: string | null
  status?: StatusCredencialAcesso | null
  cadastradoEm?: string | null
  revogadoEm?: string | null
  termoAceitoEm?: string | null
  versaoTermo?: string | null
  criadoEm?: string | null
  atualizadoEm?: string | null
}

const tipoLabels: Record<TipoCredencialAcesso, string> = {
  FACE_TEMPLATE: 'Referência facial externa',
  QR_CODE: 'QR Code',
  CARTAO: 'Cartão',
  PIN: 'PIN',
}

const statusLabels: Record<StatusCredencialAcesso, string> = {
  ATIVA: 'Ativa',
  INATIVA: 'Inativa',
  REVOGADA: 'Revogada',
  PENDENTE: 'Pendente',
}

export function accessCredentialApiToViewModel(api: CredencialAcessoAPI): CredencialAcesso {
  const tipo = api.tipo ?? 'QR_CODE'
  const status = api.status ?? 'PENDENTE'

  return {
    id: api.id ?? 0,
    alunoId: api.alunoId ?? 0,
    tipo,
    tipoLabel: tipoLabels[tipo],
    identificadorExterno: api.identificadorExterno ?? '',
    identificadorExternoMascarado: maskIdentifier(api.identificadorExterno),
    fornecedor: api.fornecedor?.trim() || 'Fornecedor não informado',
    status,
    statusLabel: statusLabels[status],
    cadastradoEm: api.cadastradoEm ?? null,
    cadastradoEmLabel: formatDateTime(api.cadastradoEm),
    revogadoEm: api.revogadoEm ?? null,
    revogadoEmLabel: formatDateTime(api.revogadoEm),
    termoAceitoEm: api.termoAceitoEm ?? null,
    termoAceitoEmLabel: formatDateTime(api.termoAceitoEm),
    versaoTermo: api.versaoTermo?.trim() || '-',
    criadoEm: api.criadoEm ?? null,
    atualizadoEm: api.atualizadoEm ?? null,
  }
}

export function accessCredentialViewModelToAPI(
  data: CredencialAcessoRequest,
): CredencialAcessoRequest {
  return {
    tipo: data.tipo,
    identificadorExterno: data.identificadorExterno.trim(),
    fornecedor: emptyToNull(data.fornecedor),
    status: data.status,
    termoAceitoEm: data.termoAceitoEm ?? null,
    versaoTermo: emptyToNull(data.versaoTermo),
  }
}

function emptyToNull(value?: string | null) {
  const normalized = value?.trim()
  return normalized ? normalized : null
}

function formatDateTime(value?: string | null) {
  if (!value) {
    return '-'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date)
}

function maskIdentifier(value?: string | null) {
  if (!value) {
    return '-'
  }

  if (value.length <= 6) {
    return '••••'
  }

  return `${value.slice(0, 3)}••••${value.slice(-3)}`
}
