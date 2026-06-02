import { useCallback, useEffect, useSyncExternalStore } from 'react'

function subscribe(callback: () => void) {
  window.addEventListener('popstate', callback)
  window.addEventListener('app:navigation', callback)

  return () => {
    window.removeEventListener('popstate', callback)
    window.removeEventListener('app:navigation', callback)
  }
}

function getSnapshot() {
  return window.location.pathname
}

export function navigateTo(path: string) {
  if (window.location.pathname === path) {
    return
  }

  window.history.pushState(null, '', path)
  window.dispatchEvent(new CustomEvent('app:navigation'))
}

export function useCurrentPath() {
  return useSyncExternalStore(subscribe, getSnapshot, getSnapshot)
}

export function useNavigate() {
  return useCallback((path: string) => navigateTo(path), [])
}

export function useRedirect(shouldRedirect: boolean, path: string) {
  useEffect(() => {
    if (shouldRedirect) {
      navigateTo(path)
    }
  }, [path, shouldRedirect])
}
