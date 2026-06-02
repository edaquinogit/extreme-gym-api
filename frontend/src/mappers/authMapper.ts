import type { AuthUser, LoginResponse } from '../types/auth'

export type LoginResponseAPI = LoginResponse

export function authApiToViewModel(response: LoginResponseAPI): {
  token: string
  user: AuthUser
} {
  const token = response.token ?? response.accessToken ?? response.jwt

  if (!token) {
    throw new Error('Resposta de login sem token JWT.')
  }

  return {
    token,
    user: normalizeUser(response),
  }
}

function normalizeUser(response: LoginResponseAPI): AuthUser {
  return response.user ?? response.usuario ?? {
    id: response.usuarioId,
    usuarioId: response.usuarioId,
    nome: response.nome,
    username: response.username,
    email: response.email,
    role: response.role,
    roles: response.role ? [response.role] : undefined,
  }
}
