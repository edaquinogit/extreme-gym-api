import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { authService } from '../services/authService'
import type { AuthUser, LoginCredentials, RegisterCredentials } from '../types/auth'
import {
  clearAuthStorage,
  getStoredToken,
  getStoredUser,
  setStoredToken,
  setStoredUser,
} from '../utils/storage'
import { AuthContext } from './authContextValue'

type AuthProviderProps = {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [token, setToken] = useState<string | null>(() => getStoredToken())
  const [user, setUser] = useState<AuthUser | null>(() =>
    getStoredUser<AuthUser>(),
  )
  const [isLoading, setIsLoading] = useState(false)

  const logout = useCallback(() => {
    clearAuthStorage()
    setToken(null)
    setUser(null)
  }, [])

  const login = useCallback(async (credentials: LoginCredentials) => {
    setIsLoading(true)

    try {
      const auth = await authService.login(credentials)

      setStoredToken(auth.token)
      setStoredUser(auth.user)
      setToken(auth.token)
      setUser(auth.user)
    } finally {
      setIsLoading(false)
    }
  }, [])

  const register = useCallback(async (credentials: RegisterCredentials) => {
    setIsLoading(true)

    try {
      const auth = await authService.register(credentials)

      setStoredToken(auth.token)
      setStoredUser(auth.user)
      setToken(auth.token)
      setUser(auth.user)
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    window.addEventListener('auth:unauthorized', logout)

    return () => {
      window.removeEventListener('auth:unauthorized', logout)
    }
  }, [logout])

  const value = useMemo(
    () => ({
      user,
      token,
      isAuthenticated: Boolean(token),
      isLoading,
      login,
      register,
      logout,
    }),
    [isLoading, login, logout, register, token, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
