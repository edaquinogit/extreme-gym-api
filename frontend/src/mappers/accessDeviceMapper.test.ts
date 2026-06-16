import { describe, expect, it } from 'vitest'
import { accessDeviceApiToViewModel, accessDeviceViewModelToAPI } from './accessDeviceMapper'

describe('accessDeviceMapper', () => {
  it('maps backend device fields with labels and safe defaults', () => {
    expect(
      accessDeviceApiToViewModel({
        id: 1,
        nome: 'Catraca entrada',
        tipo: 'CATRACA_QR',
        status: 'ATIVO',
        modoOperacao: 'HIBRIDO',
        ultimaComunicacaoEm: '2026-06-02T12:00:00',
      }),
    ).toMatchObject({
      id: 1,
      nome: 'Catraca entrada',
      tipoLabel: 'Catraca QR',
      statusLabel: 'Ativo',
      modoOperacaoLabel: 'Híbrido',
      unidade: 'Unidade principal',
    })
  })

  it('does not send api key fields to backend from frontend mapper', () => {
    expect(
      accessDeviceViewModelToAPI({
        nome: ' Recepção ',
        tipo: 'RECEPCAO',
        status: 'ATIVO',
        modoOperacao: 'ONLINE',
      }),
    ).toEqual({
      nome: 'Recepção',
      tipo: 'RECEPCAO',
      fabricante: null,
      modelo: null,
      identificadorExterno: null,
      ipLocal: null,
      unidade: null,
      status: 'ATIVO',
      modoOperacao: 'ONLINE',
    })
  })
})
