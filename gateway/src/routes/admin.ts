import type { FastifyInstance } from 'fastify'
import type { GatewayService } from '../core/gateway'
import type { GatewayConfig } from '../config'

export async function adminRoutes(app: FastifyInstance, deps: { gatewayService: GatewayService; config: GatewayConfig }) {
  // simple API key protection
  app.addHook('preHandler', async (request, reply) => {
    if (!request.url.startsWith('/admin/')) {
      return
    }

    const adminKey = request.headers['x-admin-api-key'] as string | undefined
    if (!adminKey || adminKey !== deps.config.admin.apiKey) {
      reply.code(401).send({ error: 'unauthorized' })
    }
  })

  app.post('/admin/snapshot-refresh', async () => {
    await deps.gatewayService.refreshSnapshot()
    return { ok: true }
  })

  app.post('/admin/sync', async () => {
    const result = await deps.gatewayService.syncPendingEvents()
    return result
  })

  app.post('/admin/heartbeat', async () => {
    const ok = await deps.gatewayService.sendHeartbeat()
    return { ok }
  })
}
