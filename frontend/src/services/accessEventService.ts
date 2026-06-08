import {
  accessEventApiToViewModel,
  type AccessEventAPI,
} from '../mappers/accessEventMapper'
import { httpClient } from './httpClient'

const ACCESS_EVENTS_PATH = '/eventos-acesso'

export const accessEventService = {
  listar: async () => {
    const events = await httpClient.get<AccessEventAPI[]>(ACCESS_EVENTS_PATH)
    return events.map(accessEventApiToViewModel)
  },
}
