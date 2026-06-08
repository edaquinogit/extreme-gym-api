import {
  accessDeviceApiToViewModel,
  accessDeviceCreateToApi,
  accessDeviceCreatedApiToViewModel,
  type AccessDeviceAPI,
  type AccessDeviceCreatedAPI,
} from '../mappers/accessDeviceMapper'
import type { AccessDeviceCreatePayload } from '../types/accessDevice'
import { httpClient } from './httpClient'

const ACCESS_DEVICES_PATH = '/dispositivos-acesso'

export const accessDeviceService = {
  listar: async () => {
    const devices = await httpClient.get<AccessDeviceAPI[]>(ACCESS_DEVICES_PATH)
    return devices.map(accessDeviceApiToViewModel)
  },
  criar: async (payload: AccessDeviceCreatePayload) => {
    const created = await httpClient.post<AccessDeviceCreatedAPI>(
      ACCESS_DEVICES_PATH,
      accessDeviceCreateToApi(payload),
    )
    return accessDeviceCreatedApiToViewModel(created)
  },
}
