import { describe, expect, it } from 'vitest'
import { accessEventApiToViewModel } from './accessEventMapper'

describe('accessEventMapper', () => {
  it('maps access events with safe defaults', () => {
    expect(
      accessEventApiToViewModel({
        id: 3,
        alunoId: 10,
        dispositivoId: 2,
        matriculaId: 5,
        origem: 'DISPOSITIVO',
        modo: 'OFFLINE',
        resultado: 'LIBERADO',
        motivo: 'Acesso permitido',
        dataHoraEvento: '2026-06-07T12:00:00',
        dataHoraRecebimento: '2026-06-07T12:00:01',
      }),
    ).toEqual({
      id: 3,
      alunoId: 10,
      alunoNome: '',
      dispositivoId: 2,
      dispositivoNome: '',
      matriculaId: 5,
      origem: 'DISPOSITIVO',
      modo: 'OFFLINE',
      resultado: 'LIBERADO',
      motivo: 'Acesso permitido',
      dataHoraEvento: '2026-06-07T12:00:00',
      dataHoraRecebimento: '2026-06-07T12:00:01',
      sincronizado: true,
      identificadorExternoEvento: '',
      criadoEm: '2026-06-07T12:00:01',
    })
  })

  it('does not render undefined or null values', () => {
    const event = accessEventApiToViewModel({})

    expect(JSON.stringify(event)).not.toContain('undefined')
    expect(event.resultado).toBe('BLOQUEADO')
    expect(event.motivo).toBe('Motivo não informado.')
  })
})
