import type { FastifyInstance } from 'fastify'
import type { Logger } from 'pino'

export async function devRoutes(app: FastifyInstance, logger: Logger) {
  app.post('/dev/simulate-read', async (request, reply) => {
    const body = request.body as { credentialType?: string; externalIdentifier?: string }
    if (!body?.credentialType || !body?.externalIdentifier) {
      return reply.status(400).send({ error: 'credentialType and externalIdentifier are required' })
    }

    logger.info({ body }, 'Dev simulate-read received')
    return { status: 'received', payload: body }
  })
}
