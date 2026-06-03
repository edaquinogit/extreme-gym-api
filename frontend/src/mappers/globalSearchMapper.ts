import type {
  GlobalSearchItem,
  GlobalSearchItemType,
  GlobalSearchResult,
} from '../types/globalSearch'

export type GlobalSearchItemAPI = {
  id?: number | null
  tipo?: GlobalSearchItemType | null
  titulo?: string | null
  subtitulo?: string | null
  status?: string | null
  rota?: string | null
  metadata?: Record<string, unknown> | null
}

export type GlobalSearchAPI = {
  alunos?: GlobalSearchItemAPI[] | null
  matriculas?: GlobalSearchItemAPI[] | null
  pagamentos?: GlobalSearchItemAPI[] | null
  planos?: GlobalSearchItemAPI[] | null
}

const tipoLabels: Record<GlobalSearchItemType, string> = {
  ALUNO: 'Aluno',
  MATRICULA: 'Matrícula',
  PAGAMENTO: 'Pagamento',
  PLANO: 'Plano',
}

export function globalSearchApiToViewModel(api: GlobalSearchAPI): GlobalSearchResult {
  return {
    alunos: mapItems(api.alunos),
    matriculas: mapItems(api.matriculas),
    pagamentos: mapItems(api.pagamentos),
    planos: mapItems(api.planos),
  }
}

function mapItems(items?: GlobalSearchItemAPI[] | null): GlobalSearchItem[] {
  return (items ?? []).map(globalSearchItemApiToViewModel)
}

export function globalSearchItemApiToViewModel(api: GlobalSearchItemAPI): GlobalSearchItem {
  const tipo = api.tipo ?? 'ALUNO'

  return {
    id: api.id ?? 0,
    tipo,
    tipoLabel: tipoLabels[tipo],
    titulo: api.titulo?.trim() || 'Resultado sem título',
    subtitulo: api.subtitulo?.trim() || '-',
    status: api.status?.trim() || '-',
    rota: normalizeRoute(api.rota),
    metadata: api.metadata ?? {},
  }
}

function normalizeRoute(rota?: string | null) {
  if (!rota || !rota.startsWith('/')) {
    return null
  }

  return rota
}
