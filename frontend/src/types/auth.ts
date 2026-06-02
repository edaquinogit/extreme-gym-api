export type AuthUser = {
  id?: number
  usuarioId?: number
  nome?: string
  name?: string
  email?: string
  username?: string
  role?: string
  roles?: string[]
}

export type LoginCredentials = {
  username: string
  password: string
}

export type RegisterCredentials = {
  nome: string
  email: string
  senha: string
}

export type LoginResponse = {
  token?: string
  accessToken?: string
  jwt?: string
  type?: string
  expiresInSeconds?: number
  usuarioId?: number
  nome?: string
  username?: string
  email?: string
  role?: string
  user?: AuthUser
  usuario?: AuthUser
}
