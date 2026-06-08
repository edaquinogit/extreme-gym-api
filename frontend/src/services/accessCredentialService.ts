import {
  accessCredentialApiToViewModel,
  accessCredentialCreateToApi,
  type AccessCredentialAPI,
} from '../mappers/accessCredentialMapper'
import type { AccessCredentialCreatePayload } from '../types/accessCredential'
import { httpClient } from './httpClient'

export const accessCredentialService = {
  listarPorAluno: async (alunoId: number) => {
    const credentials = await httpClient.get<AccessCredentialAPI[]>(
      `/alunos/${alunoId}/credenciais-acesso`,
    )
    return credentials.map(accessCredentialApiToViewModel)
  },
  criar: async (alunoId: number, payload: AccessCredentialCreatePayload) => {
    const credential = await httpClient.post<AccessCredentialAPI>(
      `/alunos/${alunoId}/credenciais-acesso`,
      accessCredentialCreateToApi(payload),
    )
    return accessCredentialApiToViewModel(credential)
  },
  revogar: async (id: number) => {
    const credential = await httpClient.patch<AccessCredentialAPI>(
      `/credenciais-acesso/${id}/revogar`,
      {},
    )
    return accessCredentialApiToViewModel(credential)
  },
}
