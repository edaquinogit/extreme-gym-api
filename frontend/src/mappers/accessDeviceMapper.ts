import type {
  DispositivoAcesso,
  DispositivoAcessoRequest,
  ModoOperacaoDispositivo,
  StatusDispositivoAcesso,
  TipoDispositivoAcesso,
} from '../types/accessDevice'

export type DispositivoAcessoAPI = {
  id?: number | null
  nome?: string | null
  tipo?: TipoDispositivoAcesso | null
  fabricante?: string | null
  modelo?: string | null
  identificadorExterno?: string | null
  ipLocal?: string | null
  unidade?: string | null
  status?: StatusDispositivoAcesso | null
  modoOperacao?: ModoOperacaoDispositivo | null
  ultimaComunicacaoEm?: string | null
  criadoEm?: string | null
  atualizadoEm?: string | null
}

export type DispositivoAcessoRequestAPI = Omit<DispositivoAcessoRequest, 'apiKey'>

const tipoLabels: Record<TipoDispositivoAcesso, string> = {
  CATRACA_FACIAL: 'Catraca facial',
  CATRACA_QR: 'Catraca QR',
  RECEPCAO: 'Recepção',
  OUTRO: 'Outro',
}

const statusLabels: Record<StatusDispositivoAcesso, string> = {
  ATIVO: 'Ativo',
  INATIVO: 'Inativo',
  MANUTENCAO: 'Manutenção',
  OFFLINE: 'Offline',
}

const modoLabels: Record<ModoOperacaoDispositivo, string> = {
  ONLINE: 'Online',
  OFFLINE: 'Offline',
  HIBRIDO: 'Híbrido',
}

export function accessDeviceApiToViewModel(api: DispositivoAcessoAPI): DispositivoAcesso {
  const tipo = api.tipo ?? 'OUTRO'
  const status = api.status ?? 'OFFLINE'
  const modoOperacao = api.modoOperacao ?? 'HIBRIDO'

  return {
    id: api.id ?? 0,
    nome: api.nome?.trim() || 'Dispositivo sem nome',
    tipo,
    tipoLabel: tipoLabels[tipo],
    fabricante: emptyToNull(api.fabricante),
    modelo: emptyToNull(api.modelo),
    identificadorExterno: emptyToNull(api.identificadorExterno),
    ipLocal: emptyToNull(api.ipLocal),
    unidade: api.unidade?.trim() || 'Unidade principal',
    status,
    statusLabel: statusLabels[status],
    modoOperacao,
    modoOperacaoLabel: modoLabels[modoOperacao],
    ultimaComunicacaoEm: api.ultimaComunicacaoEm ?? null,
    ultimaComunicacaoLabel: formatDateTime(api.ultimaComunicacaoEm, 'Última comunicação não registrada.'),
    criadoEm: api.criadoEm ?? null,
    atualizadoEm: api.atualizadoEm ?? null,
  }
}

export function accessDeviceViewModelToAPI(
  data: DispositivoAcessoRequest,
): DispositivoAcessoRequestAPI {
  return {
    nome: data.nome.trim(),
    tipo: data.tipo,
    fabricante: emptyToNull(data.fabricante),
    modelo: emptyToNull(data.modelo),
    identificadorExterno: emptyToNull(data.identificadorExterno),
    ipLocal: emptyToNull(data.ipLocal),
    unidade: emptyToNull(data.unidade),
    status: data.status,
    modoOperacao: data.modoOperacao,
  }
}

export function formatAccessDeviceStatus(status: StatusDispositivoAcesso) {
  return statusLabels[status]
}

export function formatOperationMode(mode: ModoOperacaoDispositivo) {
  return modoLabels[mode]
}

function emptyToNull(value?: string | null) {
  const normalized = value?.trim()
  return normalized ? normalized : null
}

function formatDateTime(value?: string | null, fallback = '-') {
  if (!value) {
    return fallback
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
