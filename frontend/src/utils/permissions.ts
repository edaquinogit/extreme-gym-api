import type { AuthUser } from '../types/auth'

export type AppRole = 'ADMIN' | 'RECEPCAO' | 'CATRACA'

export function hasRole(user: AuthUser | null, allowedRoles: readonly AppRole[]) {
  const roles = getUserRoles(user)

  return roles.some((role) => allowedRoles.includes(role))
}

function getUserRoles(user: AuthUser | null): AppRole[] {
  if (!user) {
    return []
  }

  const roles = [...(user.roles ?? []), user.role]
    .filter((role): role is string => Boolean(role))
    .map((role) => role.trim().toUpperCase())

  return roles.filter(isAppRole)
}

function isAppRole(role: string): role is AppRole {
  return role === 'ADMIN' || role === 'RECEPCAO' || role === 'CATRACA'
}
