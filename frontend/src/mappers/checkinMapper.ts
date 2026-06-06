import type { Checkin, StatusCheckin } from '../types/checkin'

export type CheckinAPI = {
  id?: number | null
  alunoId?: number | null
  alunoNome?: string | null
  matriculaId?: number | null
  permitido?: boolean | null
  motivo?: string | null
  dataHora?: string | null
}

export type CheckinRequestAPI = {
  alunoId: number
}

export function checkinApiToViewModel(api: CheckinAPI): Checkin {
  return {
    id: api.id ?? 0,
    alunoId: api.alunoId ?? undefined,
    alunoNome: api.alunoNome ?? undefined,
    matriculaId: api.matriculaId ?? null,
    dataHora: api.dataHora ?? '',
    status: permitidoToStatus(api.permitido),
    motivoBloqueio: api.permitido ? undefined : api.motivo ?? undefined,
  }
}

export function checkinViewModelToAPI(alunoId: number): CheckinRequestAPI {
  return { alunoId }
}

function permitidoToStatus(permitido?: boolean | null): StatusCheckin {
  return permitido ? 'AUTORIZADO' : 'BLOQUEADO'
}
