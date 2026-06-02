import { httpClient } from './httpClient'
import { authApiToViewModel } from '../mappers/authMapper'
import type {
  LoginCredentials,
  LoginResponse,
  RegisterCredentials,
} from '../types/auth'

const LOGIN_PATH = '/auth/login'
const REGISTER_PATH = '/auth/register'

export const authService = {
  async login(credentials: LoginCredentials) {
    const response = await httpClient.post<LoginResponse>(
      LOGIN_PATH,
      {
        username: credentials.username.trim(),
        password: credentials.password,
      },
      {
        skipAuth: true,
      },
    )
    return authApiToViewModel(response)
  },

  async register(credentials: RegisterCredentials) {
    const response = await httpClient.post<LoginResponse>(
      REGISTER_PATH,
      {
        nome: credentials.nome.trim(),
        email: credentials.email.trim(),
        senha: credentials.senha,
      },
      {
        skipAuth: true,
      },
    )
    return authApiToViewModel(response)
  },
}
