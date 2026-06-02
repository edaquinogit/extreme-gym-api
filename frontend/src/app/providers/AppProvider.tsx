import type { ReactNode } from 'react'
import { AuthProvider } from '../../contexts/AuthContext'

type AppProviderProps = {
  children: ReactNode
}

export function AppProvider({ children }: AppProviderProps) {
  return <AuthProvider>{children}</AuthProvider>
}
