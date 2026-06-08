import { describe, expect, it } from 'vitest'
import {
  accessDeviceApiToViewModel,
  accessDeviceCreateToApi,
  accessDeviceCreatedApiToViewModel,
} from './accessDeviceMapper'

describe('accessDeviceMapper', () => {
  it('maps device response without exposing API key or hash', () => {
    const device = accessDeviceApiToViewModel({
      id: 7,
      nome: 'Gateway recepção',
      tipo: 'GATEWAY',
      status: 'ATIVO',
      modoOperacao: 'HIBRIDO',
      identificadorExterno: 'gateway-01',
      apiKeyPlaintext: 'secret',
      apiKeyHash: '$2a$secret-hash',
    })

    expect(device).toEqual({
      id: 7,
      nome: 'Gateway recepção',
      tipo: 'GATEWAY',
      status: 'ATIVO',
      modoOperacao: 'HIBRIDO',
      identificadorExterno: 'gateway-01',
      fabricante: '',
      modelo: '',
      ipLocal: '',
      unidade: '',
      ultimaComunicacaoEm: null,
      criadoEm: '',
      atualizadoEm: '',
    })
    expect(JSON.stringify(device)).not.toContain('secret')
  })

  it('keeps one-time API key only in creation response object', () => {
    const created = accessDeviceCreatedApiToViewModel({
      dispositivo: { id: 8, nome: 'Catraca QR', tipo: 'CATRACA_QR' },
      apiKeyPlaintext: 'one-time-key',
    })

    expect(created.dispositivo.nome).toBe('Catraca QR')
    expect(created.apiKeyPlaintext).toBe('one-time-key')
    expect(JSON.stringify(created.dispositivo)).not.toContain('one-time-key')
  })

  it('trims create payload and removes blank optional fields', () => {
    expect(
      accessDeviceCreateToApi({
        nome: ' Gateway ',
        tipo: 'GATEWAY',
        modoOperacao: 'ONLINE',
        fabricante: ' ',
        modelo: ' Modelo X ',
      }),
    ).toEqual({
      nome: 'Gateway',
      tipo: 'GATEWAY',
      modoOperacao: 'ONLINE',
      modelo: 'Modelo X',
    })
  })
})
