import { describe, expect, it } from 'vitest'
import {
  accessCredentialApiToViewModel,
  accessCredentialCreateToApi,
  maskIdentifier,
} from './accessCredentialMapper'

describe('accessCredentialMapper', () => {
  it('masks external identifier in the view model', () => {
    const credential = accessCredentialApiToViewModel({
      id: 4,
      alunoId: 9,
      tipo: 'QR_CODE',
      identificadorExterno: 'qr-secret-123',
      fornecedor: 'gateway',
      status: 'ATIVA',
      cadastradoEm: '2026-06-07T10:00:00',
    })

    expect(credential.identificadorExternoMascarado).toBe('qr*********23')
    expect(JSON.stringify(credential)).not.toContain('qr-secret-123')
  })

  it('creates API payload with raw identifier only for submission', () => {
    expect(
      accessCredentialCreateToApi({
        tipo: 'QR_CODE',
        identificadorExterno: ' qr-123 ',
        fornecedor: ' ',
        versaoTermo: ' v1 ',
      }),
    ).toEqual({
      tipo: 'QR_CODE',
      identificadorExterno: 'qr-123',
      versaoTermo: 'v1',
    })
  })

  it('masks short identifiers completely', () => {
    expect(maskIdentifier('1234')).toBe('****')
  })
})
