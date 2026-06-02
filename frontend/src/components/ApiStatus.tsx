import { API_URL } from '../config/api'

export function ApiStatus() {
  return (
    <div className="api-status" aria-label="Configuração da API">
      <span>API configurada</span>
      <strong>{API_URL}</strong>
    </div>
  )
}
