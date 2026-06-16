import type {
  EventoAcesso,
  ModoEventoAcesso,
  OrigemEventoAcesso,
  ResultadoAcessoEvento,
} from '../types/accessEvent'

export type EventoAcessoAPI = {
  id?: number | null
  alunoId?: number | null
  alunoNome?: string | null
  dispositivoId?: number | null
  dispositivoNome?: string | null
  matriculaId?: number | null
  origem?: OrigemEventoAcesso | null
  modo?: ModoEventoAcesso | null
  resultado?: ResultadoAcessoEvento | null
  motivo?: string | null
  dataHoraEvento?: string | null
  dataHoraRecebimento?: string | null
  sincronizado?: boolean | null
  identificadorExternoEvento?: string | null
  criadoEm?: string | null
}

const origemLabels: Record<OrigemEventoAcesso, string> = {
  FACE_ID: 'Face ID externo',
  MANUAL: 'Manual',
  QR_CODE: 'QR Code',
  RECEPCAO: 'Recepção',
  SISTEMA: 'Sistema',
  GATEWAY: 'Gateway',
}

const modoLabels: Record<ModoEventoAcesso, string> = {
  ONLINE: 'Online',
  OFFLINE: 'Offline',
}

const resultadoLabels: Record<ResultadoAcessoEvento, string> = {
  LIBERADO: 'Liberado',
  BLOQUEADO: 'Bloqueado',
}

export function accessEventApiToViewModel(api: EventoAcessoAPI): EventoAcesso {
  const origem = api.origem ?? 'SISTEMA'
  const modo = api.modo ?? 'ONLINE'
  const resultado = api.resultado ?? 'BLOQUEADO'
  const sincronizado = api.sincronizado ?? false

  return {
    id: api.id ?? 0,
    alunoId: api.alunoId ?? null,
    alunoNome: api.alunoNome?.trim() || 'Aluno não informado',
    dispositivoId: api.dispositivoId ?? 0,
    dispositivoNome: api.dispositivoNome?.trim() || `Dispositivo #${api.dispositivoId ?? 0}`,
    matriculaId: api.matriculaId ?? null,
    origem,
    origemLabel: origemLabels[origem],
    modo,
    modoLabel: modoLabels[modo],
    resultado,
    resultadoLabel: resultadoLabels[resultado],
    motivo: api.motivo?.trim() || 'Motivo não informado',
    dataHoraEvento: api.dataHoraEvento ?? null,
    dataHoraEventoLabel: formatDateTime(api.dataHoraEvento),
    dataHoraRecebimento: api.dataHoraRecebimento ?? null,
    sincronizado,
    sincronizacaoLabel: sincronizado ? 'Sincronizado' : 'Pendente',
    identificadorExternoEvento: api.identificadorExternoEvento ?? null,
    identificadorExternoEventoMascarado: maskIdentifier(api.identificadorExternoEvento),
    criadoEm: api.criadoEm ?? null,
  }
}

export function formatAccessEventOrigin(origem: OrigemEventoAcesso) {
  return origemLabels[origem]
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
