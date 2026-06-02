import { httpClient } from './httpClient'
import {
  acessoApiToViewModel,
  acessoViewModelToAPI,
  type AcessoAPI,
} from '../mappers/acessoMapper'

export const acessoService = {
  validar: async (alunoId: number) => {
    const acesso = await httpClient.post<AcessoAPI>(
      '/acessos/validar',
      acessoViewModelToAPI(alunoId),
    )
    return acessoApiToViewModel(acesso)
  },
}
