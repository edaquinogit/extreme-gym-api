import type { AccessDeviceAdapter, CredentialReadPayload, OnCredentialReadCallback } from './AccessDeviceAdapter'
import type { Logger } from 'pino'

export class MockAccessDeviceAdapter implements AccessDeviceAdapter {
  private callback?: OnCredentialReadCallback
  private running = false
  private readonly logger: Logger

  constructor({ logger }: { logger: Logger }) {
    this.logger = logger
  }

  async start(): Promise<void> {
    this.running = true
    this.logger.info('MockAccessDeviceAdapter started')
  }

  async stop(): Promise<void> {
    this.running = false
    this.logger.info('MockAccessDeviceAdapter stopped')
  }

  onCredentialRead(callback: OnCredentialReadCallback): void {
    this.callback = callback
  }

  async unlock(): Promise<void> {
    this.logger.info('Mock adapter unlock requested')
  }

  async deny(reason: string): Promise<void> {
    this.logger.info({ reason }, 'Mock adapter deny requested')
  }

  getStatus() {
    return this.running ? 'active' : 'idle'
  }

  async simulateCredentialRead(payload: CredentialReadPayload): Promise<void> {
    if (!this.running) {
      throw new Error('Adapter not started')
    }
    if (!this.callback) {
      throw new Error('No credential callback configured')
    }
    this.logger.info({ payload }, 'Simulated credential read')
    await this.callback(payload)
  }
}
