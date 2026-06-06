import type { FastifyInstance } from 'fastify'
import type { GatewayService } from '../core/gateway'
import type { Logger } from 'pino'
import type { GatewayConfig } from '../config'
import { healthRoutes } from './health.js'
import { statusRoutes } from './status.js'
import { devRoutes } from './dev.js'
import { adminRoutes } from './admin.js'

export function buildRoutes(app: FastifyInstance, deps: {
  gatewayService: GatewayService
  adapter: unknown
  config: GatewayConfig
  logger: Logger
}) {
  healthRoutes(app)
  statusRoutes(app, deps.gatewayService)

  if (deps.config.environment === 'development') {
    devRoutes(app, deps.logger)
  }
  // Admin routes enabled in all environments but protected by API key
  adminRoutes(app, { gatewayService: deps.gatewayService, config: deps.config })
}
