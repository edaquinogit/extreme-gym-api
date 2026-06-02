import { useCallback } from 'react'
import { HttpError } from '../services/httpClient'

export function useApiError() {
  const getErrorMessage = useCallback((error: unknown) => {
    if (error instanceof HttpError) {
      return error.message
    }

    if (error instanceof Error) {
      return error.message
    }

    return 'Não foi possível concluir a ação agora.'
  }, [])

  return {
    getErrorMessage,
  }
}
