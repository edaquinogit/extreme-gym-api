import { randomUUID } from 'crypto';
export class GatewayService {
    config;
    database;
    backendClient;
    logger;
    constructor(deps) {
        this.config = deps.config;
        this.database = deps.database;
        this.backendClient = deps.backendClient;
        this.logger = deps.logger;
    }
    async initialize() {
        this.logger.info('Initializing gateway service');
        await this.database.initialize();
        try {
            await this.fetchAndStoreSnapshot();
        }
        catch (err) {
            this.logger.warn({ err }, 'Could not fetch initial snapshot from backend; starting with existing local data');
        }
    }
    // Public refresh method for administrative trigger
    async refreshSnapshot() {
        await this.fetchAndStoreSnapshot();
    }
    async pendingEventsCount() {
        return this.database.pendingEventsCount();
    }
    async fetchAndStoreSnapshot() {
        this.logger.info('Fetching snapshot from backend');
        const payload = await this.backendClient.fetchSnapshot();
        if (!Array.isArray(payload)) {
            this.logger.warn({ payload }, 'Snapshot payload unexpected shape; aborting snapshot store');
            return;
        }
        // Map backend items to SnapshotItem shape conservatively
        const items = payload.map((it) => ({
            id: String(it.id ?? randomUUID()),
            snapshotVersion: String(it.snapshotVersion ?? it.version ?? 'unknown'),
            generatedAt: Number(it.generatedAt ?? Date.now()),
            validUntil: it.validUntil ? Number(it.validUntil) : null,
            credentialType: String(it.credentialType ?? it.type ?? 'UNKNOWN'),
            externalIdentifier: String(it.externalIdentifier ?? it.key ?? ''),
            allowed: !!it.allowed,
            blockReason: it.blockReason ?? null,
            validUntilAccess: it.validUntilAccess ? Number(it.validUntilAccess) : null,
            updatedAt: Date.now(),
        }));
        this.database.saveSnapshotItems(items);
        this.logger.info({ count: items.length }, 'Stored snapshot items locally');
    }
    async createAndPersistEvent(event) {
        // guarantee persistence; don't lose events
        try {
            await this.database.createAccessEvent(event);
            this.logger.debug({ id: event.id, idempotencyKey: event.idempotencyKey }, 'Persisted local access event');
        }
        catch (err) {
            this.logger.error({ err, eventId: event.id }, 'Failed to persist access event — aborting operation');
            // In production we must not lose events; escalate by throwing
            throw err;
        }
    }
    async validateAccess(credentialType, externalIdentifier) {
        this.logger.info({ credentialType, externalIdentifier }, 'Evaluating access request');
        const idempotencyKey = randomUUID();
        const baseEvent = {
            alunoId: null,
            deviceId: this.config.gateway.id,
            credentialType,
            externalIdentifier,
            eventTime: Date.now(),
            receivedAt: Date.now(),
        };
        // Try online validation first
        try {
            const onlinePayload = {
                credentialType,
                externalIdentifier,
                deviceId: this.config.backend.deviceId,
                idempotencyKey,
            };
            this.logger.debug({ credentialType, externalIdentifier }, 'Attempting online validation');
            const response = await this.backendClient.validateOnline(onlinePayload);
            const allowed = !!(response && response.allowed);
            const alunoId = response?.alunoId ?? null;
            const reason = response?.reason ?? (allowed ? 'ALLOWED by backend' : 'BLOCKED by backend');
            const event = {
                id: randomUUID(),
                idempotencyKey,
                alunoId,
                credentialType,
                externalIdentifier,
                deviceId: this.config.gateway.id,
                mode: 'ONLINE',
                result: allowed ? 'ALLOWED' : 'BLOCKED',
                reason,
                eventTime: baseEvent.eventTime,
                receivedAt: baseEvent.receivedAt,
                synced: false,
                syncAttempts: 0,
            };
            await this.createAndPersistEvent(event);
            return { allowed, reason };
        }
        catch (err) {
            this.logger.warn({ err }, 'Backend online validation failed — falling back to offline if enabled');
            // Backend unavailable -> fallback
            if (!this.config.feature.offlineMode) {
                const reason = 'Backend unreachable and offline mode disabled';
                // persist blocked event
                const event = {
                    id: randomUUID(),
                    idempotencyKey,
                    alunoId: null,
                    deviceId: this.config.gateway.id,
                    credentialType,
                    externalIdentifier,
                    mode: 'OFFLINE',
                    result: 'BLOCKED',
                    reason,
                    eventTime: baseEvent.eventTime,
                    receivedAt: baseEvent.receivedAt,
                    synced: false,
                    syncAttempts: 0,
                };
                await this.createAndPersistEvent(event);
                return { allowed: false, reason };
            }
            // Offline conservative policy
            const snapshot = await this.database.getSnapshotItem(credentialType, externalIdentifier);
            if (!snapshot) {
                const reason = 'No offline authorization found';
                const event = {
                    id: randomUUID(),
                    idempotencyKey,
                    alunoId: null,
                    deviceId: this.config.gateway.id,
                    credentialType,
                    externalIdentifier,
                    mode: 'OFFLINE',
                    result: 'BLOCKED',
                    reason,
                    eventTime: baseEvent.eventTime,
                    receivedAt: baseEvent.receivedAt,
                    synced: false,
                    syncAttempts: 0,
                };
                await this.createAndPersistEvent(event);
                return { allowed: false, reason };
            }
            if (!snapshot.allowed) {
                const reason = 'Credential blocked locally';
                const event = {
                    id: randomUUID(),
                    idempotencyKey,
                    alunoId: null,
                    deviceId: this.config.gateway.id,
                    credentialType,
                    externalIdentifier,
                    mode: 'OFFLINE',
                    result: 'BLOCKED',
                    reason,
                    eventTime: baseEvent.eventTime,
                    receivedAt: baseEvent.receivedAt,
                    synced: false,
                    syncAttempts: 0,
                };
                await this.createAndPersistEvent(event);
                return { allowed: false, reason };
            }
            const now = Date.now();
            if (snapshot.validUntil && snapshot.validUntil < now) {
                const reason = 'Offline snapshot entry expired';
                const event = {
                    id: randomUUID(),
                    idempotencyKey,
                    alunoId: null,
                    deviceId: this.config.gateway.id,
                    credentialType,
                    externalIdentifier,
                    mode: 'OFFLINE',
                    result: 'BLOCKED',
                    reason,
                    eventTime: baseEvent.eventTime,
                    receivedAt: baseEvent.receivedAt,
                    synced: false,
                    syncAttempts: 0,
                };
                await this.createAndPersistEvent(event);
                return { allowed: false, reason };
            }
            // Otherwise allow
            const reason = 'Offline authorization granted';
            const event = {
                id: randomUUID(),
                idempotencyKey,
                alunoId: null,
                deviceId: this.config.gateway.id,
                credentialType,
                externalIdentifier,
                mode: 'OFFLINE',
                result: 'ALLOWED',
                reason,
                eventTime: baseEvent.eventTime,
                receivedAt: baseEvent.receivedAt,
                synced: false,
                syncAttempts: 0,
            };
            await this.createAndPersistEvent(event);
            return { allowed: true, reason };
        }
    }
    async syncPendingEvents() {
        this.logger.info('Syncing pending events with backend');
        const batchSize = this.config.timing.syncBatchSize;
        const pending = this.database.fetchPendingEvents(batchSize);
        if (pending.length === 0) {
            this.logger.info('No pending events to sync');
            return { synced: 0, failed: 0 };
        }
        try {
            const payload = pending.map((e) => ({
                id: e.id,
                idempotencyKey: e.idempotencyKey,
                alunoId: e.alunoId,
                deviceId: e.deviceId,
                credentialType: e.credentialType,
                externalIdentifier: e.externalIdentifier,
                mode: e.mode,
                result: e.result,
                reason: e.reason,
                eventTime: e.eventTime,
            }));
            await this.backendClient.syncEventsBatch(payload);
            const ids = pending.map((p) => p.id);
            this.database.markEventsSynced(ids);
            this.logger.info({ count: ids.length }, 'Successfully synced events');
            return { synced: ids.length, failed: 0 };
        }
        catch (err) {
            this.logger.warn({ err }, 'Failed to sync events; will keep them for retry');
            // increment attempt counters for each pending event
            for (const p of pending) {
                this.database.incrementSyncAttempt(p.id, String(err?.message ?? err));
            }
            return { synced: 0, failed: pending.length };
        }
    }
    async sendHeartbeat() {
        this.logger.debug('Sending heartbeat');
        try {
            const payload = {
                gatewayId: this.config.gateway.id,
                timestamp: Date.now(),
                pendingCount: this.pendingEventsCount(),
            };
            await this.backendClient.sendHeartbeat(payload);
            this.logger.debug('Heartbeat accepted by backend');
            return true;
        }
        catch (err) {
            this.logger.warn({ err }, 'Heartbeat failed');
            return false;
        }
    }
}
