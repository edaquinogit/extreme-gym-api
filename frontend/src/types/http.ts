export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

export type HttpRequestOptions = Omit<RequestInit, 'body' | 'method'> & {
  body?: unknown
  method?: HttpMethod
  skipAuth?: boolean
}
