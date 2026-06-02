import { describe, expect, it } from 'vitest'
import { checkinApiToViewModel, checkinViewModelToAPI } from './checkinMapper'

describe('checkinMapper', () => {
  it('maps permitted access to authorized status', () => {
    const checkin = checkinApiToViewModel({
      permitido: true,
      motivo: 'Matricula ativa',
    })

    expect(checkin.permitido).toBe(true)
    expect(checkin.status).toBe('AUTORIZADO')
    expect(checkin.motivo).toBe('Matricula ativa')
    expect(checkin.motivoBloqueio).toBeUndefined()
  })

  it('maps blocked access and preserves motivo as motivoBloqueio', () => {
    const checkin = checkinApiToViewModel({
      permitido: false,
      motivo: 'Matricula vencida',
    })

    expect(checkin.permitido).toBe(false)
    expect(checkin.status).toBe('BLOQUEADO')
    expect(checkin.motivo).toBe('Matricula vencida')
    expect(checkin.motivoBloqueio).toBe('Matricula vencida')
  })

  it('creates check-in payload with alunoId only', () => {
    expect(checkinViewModelToAPI(42)).toEqual({ alunoId: 42 })
  })
})
