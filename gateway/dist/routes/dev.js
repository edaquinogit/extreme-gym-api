export async function devRoutes(app, logger) {
    app.post('/dev/simulate-read', async (request, reply) => {
        const body = request.body;
        if (!body?.credentialType || !body?.externalIdentifier) {
            return reply.status(400).send({ error: 'credentialType and externalIdentifier are required' });
        }
        logger.info({ body }, 'Dev simulate-read received');
        return { status: 'received', payload: body };
    });
}
