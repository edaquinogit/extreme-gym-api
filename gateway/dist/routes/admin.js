export async function adminRoutes(app, deps) {
    // simple API key protection
    app.addHook('preHandler', async (request, reply) => {
        const adminKey = request.headers['x-admin-api-key'];
        if (!adminKey || adminKey !== deps.config.backend.deviceApiKey) {
            reply.code(401).send({ error: 'unauthorized' });
        }
    });
    app.post('/admin/snapshot-refresh', async () => {
        await deps.gatewayService.refreshSnapshot();
        return { ok: true };
    });
    app.post('/admin/sync', async () => {
        const result = await deps.gatewayService.syncPendingEvents();
        return result;
    });
    app.post('/admin/heartbeat', async () => {
        const ok = await deps.gatewayService.sendHeartbeat();
        return { ok };
    });
}
