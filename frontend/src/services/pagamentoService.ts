import { httpClient } from './httpClient'
import {
  pagamentoApiToViewModel,
  pagamentoViewModelToAPI,
  type PagamentoAPI,
} from '../mappers/pagamentoMapper'
import type { PagamentoRequestDTO } from '../types/pagamento'

const PAGAMENTOS_PATH = '/pagamentos'

export const pagamentoService = {
  listar: async () => {
    const pagamentos = await httpClient.get<PagamentoAPI[]>(PAGAMENTOS_PATH)
    return pagamentos.map(pagamentoApiToViewModel)
  },
  obterPorId: async (id: number) => {
    const pagamento = await httpClient.get<PagamentoAPI>(`${PAGAMENTOS_PATH}/${id}`)
    return pagamentoApiToViewModel(pagamento)
  },
  listarPorMatricula: async (matriculaId: number) => {
    const pagamentos = await httpClient.get<PagamentoAPI[]>(
      `${PAGAMENTOS_PATH}/matricula/${matriculaId}`,
    )
    return pagamentos.map(pagamentoApiToViewModel)
  },
  registrar: async (data: PagamentoRequestDTO) => {
    const pagamento = await httpClient.post<PagamentoAPI>(
      PAGAMENTOS_PATH,
      pagamentoViewModelToAPI(data),
    )
    return pagamentoApiToViewModel(pagamento)
  },
  cancelar: (id: number) => httpClient.patch<void>(`${PAGAMENTOS_PATH}/${id}/cancelar`),
}
