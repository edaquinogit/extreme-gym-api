import { httpClient } from './httpClient'
import {
  planoApiToViewModel,
  planoViewModelToAPI,
  type PlanoAPI,
} from '../mappers/planoMapper'
import type { PlanoRequestDTO } from '../types/plano'

const PLANOS_PATH = '/planos'

export const planoService = {
  listar: async () => {
    const planos = await httpClient.get<PlanoAPI[]>(PLANOS_PATH)
    return planos.map(planoApiToViewModel)
  },
  obterPorId: async (id: number) => {
    const plano = await httpClient.get<PlanoAPI>(`${PLANOS_PATH}/${id}`)
    return planoApiToViewModel(plano)
  },
  criar: async (data: PlanoRequestDTO) => {
    const plano = await httpClient.post<PlanoAPI>(PLANOS_PATH, planoViewModelToAPI(data))
    return planoApiToViewModel(plano)
  },
  atualizar: async (id: number, data: PlanoRequestDTO) => {
    const plano = await httpClient.put<PlanoAPI>(
      `${PLANOS_PATH}/${id}`,
      planoViewModelToAPI(data),
    )
    return planoApiToViewModel(plano)
  },
  inativar: (id: number) => httpClient.delete<void>(`${PLANOS_PATH}/${id}`),
}
