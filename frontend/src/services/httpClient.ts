import { API_URL } from '../config/api'
import type { HttpRequestOptions } from '../types/http'
import { clearAuthStorage, getStoredToken } from '../utils/storage'

export class HttpError extends Error {
  readonly status: number
  readonly body: unknown

  constructor(
    message: string,
    status: number,
    body: unknown,
  ) {
    super(message)
    this.name = 'HttpError'
    this.status = status
    this.body = body
  }
}

async function request<TResponse>(
  path: string,
  options: HttpRequestOptions = {},
): Promise<TResponse> {
  const {
    body,
    headers,
    method = 'GET',
    skipAuth = false,
    ...fetchOptions
  } = options
  const requestHeaders = new Headers(headers)

  if (body !== undefined && !requestHeaders.has('Content-Type')) {
    requestHeaders.set('Content-Type', 'application/json')
  }

  const token = getStoredToken()

  if (!skipAuth && token && !requestHeaders.has('Authorization')) {
    requestHeaders.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${API_URL}${normalizePath(path)}`, {
    ...fetchOptions,
    method,
    headers: requestHeaders,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  const responseBody = await parseResponseBody(response)

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthStorage()
      window.dispatchEvent(new CustomEvent('auth:unauthorized'))
    }

    throw new HttpError(
      getErrorMessage(response.status, responseBody),
      response.status,
      responseBody,
    )
  }

  return responseBody as TResponse
}

function normalizePath(path: string) {
  return path.startsWith('/') ? path : `/${path}`
}

async function parseResponseBody(response: Response) {
  if (response.status === 204) {
    return null
  }

  const contentType = response.headers.get('content-type')

  if (contentType?.includes('application/json')) {
    return response.json()
  }

  return response.text()
}

export const httpClient = {
  get: <TResponse>(
    path: string,
    options?: Omit<HttpRequestOptions, 'method' | 'body'>,
  ) => request<TResponse>(path, { ...options, method: 'GET' }),
  post: <TResponse>(
    path: string,
    body?: unknown,
    options?: Omit<HttpRequestOptions, 'method' | 'body'>,
  ) => request<TResponse>(path, { ...options, method: 'POST', body }),
  put: <TResponse>(
    path: string,
    body?: unknown,
    options?: Omit<HttpRequestOptions, 'method' | 'body'>,
  ) => request<TResponse>(path, { ...options, method: 'PUT', body }),
  patch: <TResponse>(
    path: string,
    body?: unknown,
    options?: Omit<HttpRequestOptions, 'method' | 'body'>,
  ) => request<TResponse>(path, { ...options, method: 'PATCH', body }),
  delete: <TResponse>(
    path: string,
    options?: Omit<HttpRequestOptions, 'method' | 'body'>,
  ) => request<TResponse>(path, { ...options, method: 'DELETE' }),
}

function getErrorMessage(status: number, body: unknown) {
  if (isApiErrorBody(body)) {
    return body.message ?? body.error ?? 'Erro ao comunicar com a API.'
  }

  if (status === 401) {
    return 'Sua sessão expirou. Faça login novamente.'
  }

  return 'Erro ao comunicar com a API.'
}

function isApiErrorBody(
  body: unknown,
): body is { message?: string; error?: string } {
  return typeof body === 'object' && body !== null
}
