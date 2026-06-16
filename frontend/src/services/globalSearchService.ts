import {
  globalSearchApiToViewModel,
  type GlobalSearchAPI,
} from '../mappers/globalSearchMapper'
import { httpClient } from './httpClient'

const MIN_SEARCH_LENGTH = 2

export const globalSearchService = {
  buscar: async (termo: string, limit = 5) => {
    const normalizedTerm = termo.trim()

    if (normalizedTerm.length < MIN_SEARCH_LENGTH) {
      return globalSearchApiToViewModel({})
    }

    const params = new URLSearchParams({
      termo: normalizedTerm,
      limit: String(limit),
    })
    const result = await httpClient.get<GlobalSearchAPI>(`/busca-global?${params.toString()}`)
    return globalSearchApiToViewModel(result)
  },
}
