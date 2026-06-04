import axios from 'axios'
import type { AxiosInstance } from 'axios'
import type { GatewayConfig } from '../config'
import type { Logger } from 'pino'

export class BackendClient {
  private http: AxiosInstance
  private config: GatewayConfig
  private logger: Logger

  constructor(config: GatewayConfig, logger: Logger) {
    this.config = config
    this.logger = logger
    this.http = axios.create({
      baseURL: config.backend.baseUrl,
      timeout: 10_000,
      headers: {
        'X-Device-Api-Key': config.backend.deviceApiKey,
        'X-Gateway-Id': config.gateway.id,
      },
    })
  }

  private getHeaders() {
    return {
      'X-Device-Api-Key': this.config.backend.deviceApiKey,
      'X-Gateway-Id': this.config.gateway.id,
      'X-Request-Timestamp': new Date().toISOString(),
    }
  }

  async fetchSnapshot(): Promise<unknown> {
    this.logger.info('Fetching authorized snapshot from backend')
    const response = await this.http.get('/controle-acesso/snapshot-autorizados', {
      headers: this.getHeaders(),
    })
    return response.data
  }

  async validateOnline(payload: unknown): Promise<unknown> {
    this.logger.debug({ payload }, 'Validating access online')
    const response = await this.http.post('/controle-acesso/validar-dispositivo', payload, {
      headers: this.getHeaders(),
    })
    return response.data
  }

  async sendHeartbeat(payload: unknown): Promise<unknown> {
    this.logger.debug('Sending heartbeat to backend')
    const response = await this.http.post('/dispositivos-acesso/heartbeat', payload, {
      headers: this.getHeaders(),
    })
    return response.data
  }

  async syncEventsBatch(events: unknown[]): Promise<unknown> {
    this.logger.info({ count: events.length }, 'Syncing events batch')
    const response = await this.http.post('/eventos-acesso/sincronizar-lote', events, {
      headers: this.getHeaders(),
    })
    return response.data
  }
}
