import { httpClient } from './httpClient'
import {
  checkinApiToViewModel,
  checkinViewModelToAPI,
  type CheckinAPI,
} from '../mappers/checkinMapper'
import type { Checkin } from '../types/checkin'

const CHECKINS_PATH = '/checkins'

export const checkinService = {
  listar: async () => {
    const checkins = await httpClient.get<CheckinAPI[]>(CHECKINS_PATH)
    return checkins.map(checkinApiToViewModel)
  },
  obterPorId: async (id: number) => {
    const checkin = await httpClient.get<CheckinAPI>(`${CHECKINS_PATH}/${id}`)
    return checkinApiToViewModel(checkin)
  },
  listarPorAluno: async (alunoId: number) => {
    const checkins = await httpClient.get<CheckinAPI[]>(`${CHECKINS_PATH}/aluno/${alunoId}`)
    return checkins.map(checkinApiToViewModel)
  },
  registrar: async (alunoId: number): Promise<Checkin> => {
    const checkin = await httpClient.post<CheckinAPI>(
      CHECKINS_PATH,
      checkinViewModelToAPI(alunoId),
    )
    return checkinApiToViewModel(checkin)
  },
}
