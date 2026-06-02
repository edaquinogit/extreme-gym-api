import { httpClient } from './httpClient'
import {
  alunoApiToViewModel,
  alunoStatusToAPI,
  alunoViewModelToAPI,
  type AlunoAPI,
} from '../mappers/alunoMapper'
import type { AlunoRequestDTO, StatusAluno } from '../types/aluno'

const ALUNOS_PATH = '/alunos'

export const alunoService = {
  listar: async () => {
    const alunos = await httpClient.get<AlunoAPI[]>(ALUNOS_PATH)
    return alunos.map(alunoApiToViewModel)
  },
  buscar: async (id: number) => {
    const aluno = await httpClient.get<AlunoAPI>(`${ALUNOS_PATH}/${id}`)
    return alunoApiToViewModel(aluno)
  },
  obterPorId: async (id: number) => {
    const aluno = await httpClient.get<AlunoAPI>(`${ALUNOS_PATH}/${id}`)
    return alunoApiToViewModel(aluno)
  },
  cadastrar: async (data: AlunoRequestDTO) => {
    const aluno = await httpClient.post<AlunoAPI>(ALUNOS_PATH, alunoViewModelToAPI(data))
    return alunoApiToViewModel(aluno)
  },
  criar: async (data: AlunoRequestDTO) => {
    const aluno = await httpClient.post<AlunoAPI>(ALUNOS_PATH, alunoViewModelToAPI(data))
    return alunoApiToViewModel(aluno)
  },
  atualizar: async (id: number, data: AlunoRequestDTO) => {
    const aluno = await httpClient.put<AlunoAPI>(
      `${ALUNOS_PATH}/${id}`,
      alunoViewModelToAPI(data),
    )
    return alunoApiToViewModel(aluno)
  },
  alterarStatus: async (id: number, status: StatusAluno) => {
    const aluno = await httpClient.patch<AlunoAPI>(
      `${ALUNOS_PATH}/${id}/status`,
      alunoStatusToAPI(status),
    )
    return alunoApiToViewModel(aluno)
  },
  remover: (id: number) => httpClient.delete<void>(`${ALUNOS_PATH}/${id}`),
  inativar: (id: number) => httpClient.delete<void>(`${ALUNOS_PATH}/${id}`),
}
