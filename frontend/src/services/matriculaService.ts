import { httpClient } from './httpClient'
import {
  matriculaApiToViewModel,
  matriculaViewModelToAPI,
  type MatriculaAPI,
} from '../mappers/matriculaMapper'
import type { MatriculaRequestDTO } from '../types/matricula'

const MATRICULAS_PATH = '/matriculas'

export const matriculaService = {
  listar: async () => {
    const matriculas = await httpClient.get<MatriculaAPI[]>(MATRICULAS_PATH)
    return matriculas.map(matriculaApiToViewModel)
  },
  obterPorId: async (id: number) => {
    const matricula = await httpClient.get<MatriculaAPI>(`${MATRICULAS_PATH}/${id}`)
    return matriculaApiToViewModel(matricula)
  },
  criar: async (data: MatriculaRequestDTO) => {
    const matricula = await httpClient.post<MatriculaAPI>(
      MATRICULAS_PATH,
      matriculaViewModelToAPI(data),
    )
    return matriculaApiToViewModel(matricula)
  },
  cancelar: (id: number) => httpClient.patch<void>(`${MATRICULAS_PATH}/${id}/cancelar`),
}
