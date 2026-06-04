import type { FastifyInstance } from 'fastify'
import type { GatewayService } from '../core/gateway'

export async function statusRoutes(app: FastifyInstance, gatewayService: GatewayService) {
  app.get('/status', async () => ({
    gatewayId: gatewayService.config.gateway.id,
    gatewayName: gatewayService.config.gateway.name,
    backendBaseUrl: gatewayService.config.backend.baseUrl,
    offlineMode: gatewayService.config.feature.offlineMode,
    offlineStrict: gatewayService.config.feature.offlineStrict,
    pendingEvents: await gatewayService.pendingEventsCount(),
  }))
}
