import { describe, expect, it } from 'vitest'
import { checkinApiToViewModel, checkinViewModelToAPI } from './checkinMapper'

describe('checkinMapper', () => {
  it('maps permitted access to authorized status without an undue block reason', () => {
    const checkin = checkinApiToViewModel({
      id: 5,
      alunoId: 10,
      alunoNome: 'Ana',
      matriculaId: 2,
      permitido: true,
      motivo: 'Matricula ativa',
      dataHora: '2026-06-02T10:00:00',
    })

    expect(checkin.id).toBe(5)
    expect(checkin.alunoId).toBe(10)
    expect(checkin.alunoNome).toBe('Ana')
    expect(checkin.matriculaId).toBe(2)
    expect(checkin.dataHora).toBe('2026-06-02T10:00:00')
    expect(checkin.status).toBe('AUTORIZADO')
    expect(checkin.motivoBloqueio).toBeUndefined()
    expect(checkin).not.toHaveProperty('permitido')
    expect(checkin).not.toHaveProperty('motivo')
  })

  it('maps blocked access and preserves motivo as motivoBloqueio', () => {
    const checkin = checkinApiToViewModel({
      permitido: false,
      motivo: 'Matricula vencida',
    })

    expect(checkin.status).toBe('BLOQUEADO')
    expect(checkin.motivoBloqueio).toBe('Matricula vencida')
  })

  it('handles null or undefined motivo safely', () => {
    expect(checkinApiToViewModel({ permitido: false, motivo: null }).motivoBloqueio).toBeUndefined()
    expect(checkinApiToViewModel({ permitido: false }).motivoBloqueio).toBeUndefined()
  })

  it('creates check-in payload with alunoId only', () => {
    expect(checkinViewModelToAPI(42)).toEqual({ alunoId: 42 })
  })
})
