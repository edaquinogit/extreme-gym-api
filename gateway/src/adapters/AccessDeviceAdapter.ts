export type CredentialReadPayload = {
  credentialType: string
  externalIdentifier: string
}

export type AccessDeviceStatus = 'idle' | 'active' | 'error' | 'offline'

export type OnCredentialReadCallback = (payload: CredentialReadPayload) => Promise<void>

export interface AccessDeviceAdapter {
  start(): Promise<void>
  stop(): Promise<void>
  onCredentialRead(callback: OnCredentialReadCallback): void
  unlock(): Promise<void>
  deny(reason: string): Promise<void>
  getStatus(): AccessDeviceStatus
}
