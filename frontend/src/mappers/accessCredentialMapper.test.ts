import { describe, expect, it } from 'vitest'
import {
  accessCredentialApiToViewModel,
  accessCredentialViewModelToAPI,
} from './accessCredentialMapper'

describe('accessCredentialMapper', () => {
  it('maps credential reference without exposing raw identifier fully', () => {
    expect(
      accessCredentialApiToViewModel({
        id: 3,
        alunoId: 9,
        tipo: 'QR_CODE',
        identificadorExterno: 'credential-123456',
        status: 'ATIVA',
      }),
    ).toMatchObject({
      tipoLabel: 'QR Code',
      statusLabel: 'Ativa',
      identificadorExternoMascarado: 'cre••••456',
      fornecedor: 'Fornecedor não informado',
    })
  })

  it('normalizes credential request', () => {
    expect(
      accessCredentialViewModelToAPI({
        tipo: 'CARTAO',
        identificadorExterno: ' card-1 ',
        fornecedor: '  ',
      }),
    ).toEqual({
      tipo: 'CARTAO',
      identificadorExterno: 'card-1',
      fornecedor: null,
      status: undefined,
      termoAceitoEm: null,
      versaoTermo: null,
    })
  })
})
