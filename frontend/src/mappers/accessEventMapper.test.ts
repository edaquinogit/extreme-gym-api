import { describe, expect, it } from 'vitest'
import { accessEventApiToViewModel } from './accessEventMapper'

describe('accessEventMapper', () => {
  it('maps event enums and masks external identifier', () => {
    expect(
      accessEventApiToViewModel({
        id: 7,
        dispositivoId: 2,
        origem: 'GATEWAY',
        modo: 'OFFLINE',
        resultado: 'BLOQUEADO',
        motivo: 'Aluno inadimplente',
        sincronizado: false,
        identificadorExternoEvento: 'abcdef123456',
      }),
    ).toMatchObject({
      origemLabel: 'Gateway',
      modoLabel: 'Offline',
      resultadoLabel: 'Bloqueado',
      sincronizacaoLabel: 'Pendente',
      identificadorExternoEventoMascarado: 'abc••••456',
    })
  })

  it('uses safe defaults for incomplete event payload', () => {
    expect(accessEventApiToViewModel({})).toMatchObject({
      id: 0,
      alunoNome: 'Aluno não informado',
      dispositivoNome: 'Dispositivo #0',
      motivo: 'Motivo não informado',
      dataHoraEventoLabel: '-',
    })
  })
})
