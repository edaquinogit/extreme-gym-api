import {
  accessCredentialApiToViewModel,
  accessCredentialViewModelToAPI,
  type CredencialAcessoAPI,
} from '../mappers/accessCredentialMapper'
import type { CredencialAcessoRequest } from '../types/accessCredential'
import { httpClient } from './httpClient'

export const accessCredentialService = {
  listarPorAluno: async (alunoId: number) => {
    const credenciais = await httpClient.get<CredencialAcessoAPI[]>(
      `/alunos/${alunoId}/credenciais-acesso`,
    )
    return credenciais.map(accessCredentialApiToViewModel)
  },
  criar: async (alunoId: number, data: CredencialAcessoRequest) => {
    const credencial = await httpClient.post<CredencialAcessoAPI>(
      `/alunos/${alunoId}/credenciais-acesso`,
      accessCredentialViewModelToAPI(data),
    )
    return accessCredentialApiToViewModel(credencial)
  },
  revogar: async (id: number) => {
    const credencial = await httpClient.patch<CredencialAcessoAPI>(
      `/credenciais-acesso/${id}/revogar`,
    )
    return accessCredentialApiToViewModel(credencial)
  },
}
