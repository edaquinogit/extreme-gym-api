import {
  accessEventApiToViewModel,
  type EventoAcessoAPI,
} from '../mappers/accessEventMapper'
import { httpClient } from './httpClient'

const ACCESS_EVENTS_PATH = '/eventos-acesso'

export const accessEventService = {
  listar: async () => {
    const eventos = await httpClient.get<EventoAcessoAPI[]>(ACCESS_EVENTS_PATH)
    return eventos.map(accessEventApiToViewModel)
  },
  listarHoje: async () => {
    const eventos = await httpClient.get<EventoAcessoAPI[]>(`${ACCESS_EVENTS_PATH}/hoje`)
    return eventos.map(accessEventApiToViewModel)
  },
  listarPorAluno: async (alunoId: number) => {
    const eventos = await httpClient.get<EventoAcessoAPI[]>(
      `${ACCESS_EVENTS_PATH}/aluno/${alunoId}`,
    )
    return eventos.map(accessEventApiToViewModel)
  },
  listarPorDispositivo: async (dispositivoId: number) => {
    const eventos = await httpClient.get<EventoAcessoAPI[]>(
      `${ACCESS_EVENTS_PATH}/dispositivo/${dispositivoId}`,
    )
    return eventos.map(accessEventApiToViewModel)
  },
}
