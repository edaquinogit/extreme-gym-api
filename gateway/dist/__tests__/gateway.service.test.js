// Ensure required env vars are set before importing project modules that parse them
process.env.GATEWAY_ID = process.env.GATEWAY_ID ?? 'gw-test';
process.env.GATEWAY_NAME = process.env.GATEWAY_NAME ?? 'gw';
process.env.BACKEND_BASE_URL = process.env.BACKEND_BASE_URL ?? 'http://example';
process.env.DEVICE_ID = process.env.DEVICE_ID ?? 'dev-1';
process.env.DEVICE_API_KEY = process.env.DEVICE_API_KEY ?? 'k';
process.env.DEVICE_HMAC_SECRET = process.env.DEVICE_HMAC_SECRET ?? 's';
import { beforeEach, describe, expect, it } from 'vitest';
import { SqliteDatabase } from '../storage/db';
import { GatewayService } from '../core/gateway';
import { logger } from '../logs/logger';
class FakeBackendClient {
    cfg;
    snapshot = [];
    validateResponse = null;
    syncCalled = false;
    constructor(cfg) {
        this.cfg = cfg;
    }
    async fetchSnapshot() {
        return this.snapshot;
    }
    async validateOnline(_payload) {
        if (this.validateResponse && this.validateResponse instanceof Error)
            throw this.validateResponse;
        return this.validateResponse;
    }
    async sendHeartbeat(_payload) {
        return { ok: true };
    }
    async syncEventsBatch(_events) {
        this.syncCalled = true;
        return { ok: true };
    }
}
function makeConfig() {
    return {
        environment: 'test',
        gateway: { id: 'gw-test', name: 'gw', port: 4000, dataPath: ':memory:' },
        backend: { baseUrl: 'http://example', deviceId: 'dev-1', deviceApiKey: 'k', deviceHmacSecret: 's' },
        storage: { databasePath: ':memory:' },
        timing: { snapshotRefreshSeconds: 300, snapshotTtlSeconds: 900, syncIntervalSeconds: 60, syncBatchSize: 50 },
        feature: { offlineMode: true, offlineStrict: true },
        logger: { level: 'silent' },
    };
}
describe('GatewayService orchestration (conservative offline)', () => {
    let db;
    let cfg;
    let backend;
    let svc;
    beforeEach(async () => {
        cfg = makeConfig();
        db = new SqliteDatabase(cfg.storage.databasePath, logger);
        backend = new FakeBackendClient(cfg);
        svc = new GatewayService({ config: cfg, database: db, backendClient: backend, logger });
        await db.initialize();
    });
    it('saves snapshot fetched from backend on initialize', async () => {
        backend.snapshot = [
            { id: 's1', snapshotVersion: 'v1', generatedAt: Date.now(), validUntil: Date.now() + 10000, credentialType: 'CARD', externalIdentifier: 'abc', allowed: true },
        ];
        await svc.initialize();
        const item = db.getSnapshotItem('CARD', 'abc');
        expect(item).not.toBeNull();
        expect(item?.allowed).toBeTruthy();
    });
    it('online validation allowed persists ONLINE event', async () => {
        backend.validateResponse = { allowed: true, alunoId: 'al1', reason: 'ok' };
        const res = await svc.validateAccess('CARD', 'online-ok');
        expect(res.allowed).toBe(true);
        // pending events should be 1
        expect(db.pendingEventsCount()).toBe(1);
    });
    it('online validation blocked persists ONLINE blocked event', async () => {
        backend.validateResponse = { allowed: false, reason: 'blocked' };
        const res = await svc.validateAccess('CARD', 'online-block');
        expect(res.allowed).toBe(false);
        expect(db.pendingEventsCount()).toBe(1);
    });
    it('backend unavailable falls back to offline and blocks without snapshot', async () => {
        backend.validateResponse = new Error('network');
        const res = await svc.validateAccess('CARD', 'no-snapshot');
        expect(res.allowed).toBe(false);
        expect(db.pendingEventsCount()).toBe(1);
    });
    it('backend unavailable falls back to offline and allows with valid snapshot', async () => {
        // insert snapshot
        db.saveSnapshotItems([
            { id: 'ss', snapshotVersion: 'v', generatedAt: Date.now(), validUntil: Date.now() + 10000, credentialType: 'CARD', externalIdentifier: 'ok-card', allowed: true, updatedAt: Date.now() },
        ]);
        backend.validateResponse = new Error('network');
        const res = await svc.validateAccess('CARD', 'ok-card');
        expect(res.allowed).toBe(true);
        expect(db.pendingEventsCount()).toBe(1);
    });
    it('syncPendingEvents marks events as synced when backend succeeds', async () => {
        backend.validateResponse = new Error('network');
        await svc.validateAccess('CARD', 'to-sync');
        expect(db.pendingEventsCount()).toBe(1);
        const result = await svc.syncPendingEvents();
        expect(result.synced).toBe(1);
        expect(db.pendingEventsCount()).toBe(0);
    });
});
