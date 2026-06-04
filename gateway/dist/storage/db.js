import Database from 'better-sqlite3';
export class SqliteDatabase {
    db;
    path;
    logger;
    constructor(path, logger) {
        this.path = path;
        this.logger = logger;
    }
    async initialize() {
        this.db = new Database(this.path);
        this.db.pragma('journal_mode = WAL');
        this.db.exec(`
      CREATE TABLE IF NOT EXISTS authorized_snapshot (
        id TEXT PRIMARY KEY,
        snapshot_version TEXT,
        generated_at INTEGER,
        valid_until INTEGER,
        credential_type TEXT,
        external_identifier TEXT,
        allowed INTEGER,
        block_reason TEXT,
        valid_until_access INTEGER,
        updated_at INTEGER
      );

      CREATE INDEX IF NOT EXISTS idx_snapshot_credential ON authorized_snapshot(credential_type, external_identifier);

      CREATE TABLE IF NOT EXISTS local_access_events (
        id TEXT PRIMARY KEY,
        idempotency_key TEXT,
        aluno_id TEXT,
        device_id TEXT,
        credential_type TEXT,
        external_identifier TEXT,
        mode TEXT,
        result TEXT,
        reason TEXT,
        event_time INTEGER,
        received_at INTEGER,
        synced INTEGER DEFAULT 0,
        synced_at INTEGER,
        sync_attempts INTEGER DEFAULT 0,
        last_sync_error TEXT
      );

      CREATE INDEX IF NOT EXISTS idx_local_access_events_synced ON local_access_events(synced);
    `);
        this.logger?.info({ path: this.path }, 'Initialized SQLite database');
    }
    pendingEventsCount() {
        if (!this.db)
            throw new Error('Database not initialized');
        const row = this.db.prepare('SELECT COUNT(*) as count FROM local_access_events WHERE synced = 0').get();
        return row?.count ?? 0;
    }
    fetchPendingEvents(limit = 50) {
        if (!this.db)
            throw new Error('Database not initialized');
        const rows = this.db
            .prepare('SELECT * FROM local_access_events WHERE synced = 0 ORDER BY received_at ASC LIMIT ?')
            .all(limit);
        return rows;
    }
    getSnapshotItem(credentialType, externalIdentifier) {
        if (!this.db)
            throw new Error('Database not initialized');
        const row = this.db.prepare('SELECT * FROM authorized_snapshot WHERE credential_type = ? AND external_identifier = ? LIMIT 1').get(credentialType, externalIdentifier);
        return row ?? null;
    }
    saveSnapshotItems(items) {
        if (!this.db)
            throw new Error('Database not initialized');
        const insert = this.db.prepare(`INSERT OR REPLACE INTO authorized_snapshot (
        id,
        snapshot_version,
        generated_at,
        valid_until,
        credential_type,
        external_identifier,
        allowed,
        block_reason,
        valid_until_access,
        updated_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`);
        const transaction = this.db.transaction((payload) => {
            for (const item of payload) {
                insert.run(item.id, item.snapshotVersion, item.generatedAt, item.validUntil ?? null, item.credentialType, item.externalIdentifier, item.allowed ? 1 : 0, item.blockReason ?? null, item.validUntilAccess ?? null, Date.now());
            }
        });
        transaction(items);
    }
    async createAccessEvent(event) {
        if (!this.db)
            throw new Error('Database not initialized');
        this.db.prepare(`INSERT OR REPLACE INTO local_access_events (
        id,
        idempotency_key,
        aluno_id,
        device_id,
        credential_type,
        external_identifier,
        mode,
        result,
        reason,
        event_time,
        received_at,
        synced,
        synced_at,
        sync_attempts,
        last_sync_error
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`).run(event.id, event.idempotencyKey, event.alunoId ?? null, event.deviceId, event.credentialType, event.externalIdentifier, event.mode, event.result, event.reason, event.eventTime, event.receivedAt, event.synced ? 1 : 0, event.syncedAt ?? null, event.syncAttempts, event.lastSyncError ?? null);
    }
    markEventsSynced(ids) {
        if (!this.db)
            throw new Error('Database not initialized');
        const stmt = this.db.prepare('UPDATE local_access_events SET synced = 1, synced_at = ?, last_sync_error = NULL WHERE id = ?');
        const tx = this.db.transaction((items) => {
            const now = Date.now();
            for (const id of items)
                stmt.run(now, id);
        });
        tx(ids);
    }
    incrementSyncAttempt(id, lastError) {
        if (!this.db)
            throw new Error('Database not initialized');
        this.db.prepare('UPDATE local_access_events SET sync_attempts = sync_attempts + 1, last_sync_error = ? WHERE id = ?').run(lastError, id);
    }
    close() {
        if (!this.db)
            return;
        this.db.close();
        this.logger?.info('Closed database connection');
    }
}
