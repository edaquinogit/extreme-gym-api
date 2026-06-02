import { describe, expect, it } from 'vitest'
import { matriculaApiToViewModel, matriculaViewModelToAPI } from './matriculaMapper'

describe('matriculaMapper', () => {
  it('maps dataFim to the UI date fields used by existing screens', () => {
    const matricula = matriculaApiToViewModel({
      id: 3,
      alunoId: 11,
      alunoNome: 'Joao',
      planoId: 5,
      planoNome: 'Mensal',
      dataInicio: '2026-02-01',
      dataFim: '2026-03-03',
      status: 'ATIVA',
      dataCadastro: '2026-02-01',
    })

    expect(matricula.dataFim).toBe('2026-03-03')
    expect(matricula.dataVencimento).toBe('2026-03-03')
    expect(matricula.alunoNome).toBe('Joao')
    expect(matricula.planoNome).toBe('Mensal')
  })

  it('keeps safe defaults when linked aluno or plano fields are missing', () => {
    const matricula = matriculaApiToViewModel({})

    expect(matricula.id).toBe(0)
    expect(matricula.status).toBe('ATIVA')
    expect(matricula.alunoId).toBeUndefined()
    expect(matricula.planoId).toBeUndefined()
  })

  it('creates API payload without UI-only fields', () => {
    expect(
      matriculaViewModelToAPI({
        alunoId: 1,
        planoId: 2,
        dataInicio: '2026-01-15',
      }),
    ).toEqual({
      alunoId: 1,
      planoId: 2,
      dataInicio: '2026-01-15',
    })
  })
})
