export class MockAccessDeviceAdapter {
    callback;
    running = false;
    logger;
    constructor({ logger }) {
        this.logger = logger;
    }
    async start() {
        this.running = true;
        this.logger.info('MockAccessDeviceAdapter started');
    }
    async stop() {
        this.running = false;
        this.logger.info('MockAccessDeviceAdapter stopped');
    }
    onCredentialRead(callback) {
        this.callback = callback;
    }
    async unlock() {
        this.logger.info('Mock adapter unlock requested');
    }
    async deny(reason) {
        this.logger.info({ reason }, 'Mock adapter deny requested');
    }
    getStatus() {
        return this.running ? 'active' : 'idle';
    }
    async simulateCredentialRead(payload) {
        if (!this.running) {
            throw new Error('Adapter not started');
        }
        if (!this.callback) {
            throw new Error('No credential callback configured');
        }
        this.logger.info({ payload }, 'Simulated credential read');
        await this.callback(payload);
    }
}
