import type { FastifyInstance } from 'fastify'
import type { GatewayService } from '../core/gateway'
import type { Logger } from 'pino'
import { healthRoutes } from './health'
import { statusRoutes } from './status'
import { devRoutes } from './dev'

export function buildRoutes(app: FastifyInstance, deps: {
  gatewayService: GatewayService
  adapter: unknown
  config: { environment: string }
  logger: Logger
}) {
  healthRoutes(app)
  statusRoutes(app, deps.gatewayService)

  if (deps.config.environment === 'development') {
    devRoutes(app, deps.logger)
  }
}
