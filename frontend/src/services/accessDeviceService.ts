import {
  accessDeviceApiToViewModel,
  accessDeviceViewModelToAPI,
  type DispositivoAcessoAPI,
} from '../mappers/accessDeviceMapper'
import type {
  DispositivoAcessoRequest,
  StatusDispositivoAcesso,
} from '../types/accessDevice'
import { httpClient } from './httpClient'

const ACCESS_DEVICES_PATH = '/dispositivos-acesso'

export const accessDeviceService = {
  listar: async () => {
    const dispositivos = await httpClient.get<DispositivoAcessoAPI[]>(ACCESS_DEVICES_PATH)
    return dispositivos.map(accessDeviceApiToViewModel)
  },
  obterPorId: async (id: number) => {
    const dispositivo = await httpClient.get<DispositivoAcessoAPI>(`${ACCESS_DEVICES_PATH}/${id}`)
    return accessDeviceApiToViewModel(dispositivo)
  },
  criar: async (data: DispositivoAcessoRequest) => {
    const dispositivo = await httpClient.post<DispositivoAcessoAPI>(
      ACCESS_DEVICES_PATH,
      accessDeviceViewModelToAPI(data),
    )
    return accessDeviceApiToViewModel(dispositivo)
  },
  atualizar: async (id: number, data: DispositivoAcessoRequest) => {
    const dispositivo = await httpClient.put<DispositivoAcessoAPI>(
      `${ACCESS_DEVICES_PATH}/${id}`,
      accessDeviceViewModelToAPI(data),
    )
    return accessDeviceApiToViewModel(dispositivo)
  },
  alterarStatus: async (id: number, status: StatusDispositivoAcesso) => {
    const dispositivo = await httpClient.patch<DispositivoAcessoAPI>(
      `${ACCESS_DEVICES_PATH}/${id}/status`,
      { status },
    )
    return accessDeviceApiToViewModel(dispositivo)
  },
}
