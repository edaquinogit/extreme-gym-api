import { describe, expect, it } from 'vitest'
import { authApiToViewModel } from './authMapper'

describe('authMapper', () => {
  it('maps token and user fields from the confirmed backend format', () => {
    expect(
      authApiToViewModel({
        token: 'jwt-token',
        usuarioId: 1,
        nome: 'Admin',
        username: 'admin',
        email: 'admin@example.com',
        role: 'ADMIN',
      }),
    ).toEqual({
      token: 'jwt-token',
      user: {
        id: 1,
        usuarioId: 1,
        nome: 'Admin',
        username: 'admin',
        email: 'admin@example.com',
        role: 'ADMIN',
        roles: ['ADMIN'],
      },
    })
  })

  it('keeps compatibility with nested user responses', () => {
    expect(
      authApiToViewModel({
        accessToken: 'legacy-token',
        user: {
          id: 2,
          nome: 'Recepcao',
          role: 'RECEPCAO',
        },
      }),
    ).toEqual({
      token: 'legacy-token',
      user: {
        id: 2,
        nome: 'Recepcao',
        role: 'RECEPCAO',
      },
    })
  })

  it('fails fast when login response has no token', () => {
    expect(() => authApiToViewModel({ nome: 'Sem token' })).toThrow(
      'Resposta de login sem token JWT.',
    )
  })
})
