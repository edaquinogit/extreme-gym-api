const AUTH_TOKEN_KEY = 'extreme_gym_auth_token'
const AUTH_USER_KEY = 'extreme_gym_auth_user'

function safeLocalStorage(): Storage | null {
  try {
    if (typeof window === 'undefined' || !window.localStorage) return null
    return window.localStorage
  } catch {
    return null
  }
}

export function getStoredToken(): string | null {
  try {
    const storage = safeLocalStorage()
    return storage ? storage.getItem(AUTH_TOKEN_KEY) : null
  } catch {
    return null
  }
}

export function setStoredToken(token: string): void {
  try {
    const storage = safeLocalStorage()
    if (!storage) return
    storage.setItem(AUTH_TOKEN_KEY, token)
  } catch {
    // noop on storage write errors
  }
}

export function removeStoredToken(): void {
  try {
    const storage = safeLocalStorage()
    if (!storage) return
    storage.removeItem(AUTH_TOKEN_KEY)
  } catch {
    // noop
  }
}

export function getStoredUser<TUser>(): TUser | null {
  try {
    const storage = safeLocalStorage()
    if (!storage) return null

    const rawUser = storage.getItem(AUTH_USER_KEY)
    if (!rawUser) return null

    try {
      return JSON.parse(rawUser) as TUser
    } catch {
      storage.removeItem(AUTH_USER_KEY)
      return null
    }
  } catch {
    return null
  }
}

export function setStoredUser(user: unknown): void {
  try {
    const storage = safeLocalStorage()
    if (!storage) return
    storage.setItem(AUTH_USER_KEY, JSON.stringify(user))
  } catch {
    // noop
  }
}

export function removeStoredUser(): void {
  try {
    const storage = safeLocalStorage()
    if (!storage) return
    storage.removeItem(AUTH_USER_KEY)
  } catch {
    // noop
  }
}

export function clearAuthStorage(): void {
  removeStoredToken()
  removeStoredUser()
}
