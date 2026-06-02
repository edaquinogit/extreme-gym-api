import { useCallback, useEffect, useRef, useState } from 'react'
import { useApiError } from './useApiError'

type UseResourceListOptions<TData> = {
  load: () => Promise<TData[]>
}

export function useResourceList<TData>({ load }: UseResourceListOptions<TData>) {
  const [data, setData] = useState<TData[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [reloadError, setReloadError] = useState<string | null>(null)
  const { getErrorMessage } = useApiError()
  const isMountedRef = useRef(true)

  const loadData = useCallback(async () => {
    try {
      setIsLoading(true)
      setErrorMessage(null)
      setReloadError(null)

      const response = await load()

      if (isMountedRef.current) {
        setData(response)
      }
    } catch (error) {
      if (isMountedRef.current) {
        const message = getErrorMessage(error)
        setErrorMessage(message)
        setReloadError(message)
      }
    } finally {
      if (isMountedRef.current) {
        setIsLoading(false)
      }
    }
  }, [getErrorMessage, load])

  useEffect(() => {
    isMountedRef.current = true

    const timeoutId = window.setTimeout(() => {
      void loadData()
    }, 0)

    return () => {
      window.clearTimeout(timeoutId)
      isMountedRef.current = false
    }
  }, [loadData])

  return { data, setData, errorMessage, isLoading, reload: loadData, reloadError }
}
