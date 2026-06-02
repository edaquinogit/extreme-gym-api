import { describe, expect, it } from 'vitest'
import { planoApiToViewModel, planoViewModelToAPI } from './planoMapper'

describe('planoMapper', () => {
  it('maps API fields to UI fields', () => {
    expect(
      planoApiToViewModel({
        id: 10,
        nome: 'Mensal',
        descricao: 'Plano mensal',
        valorMensal: '149.90',
        duracaoEmDias: 30,
        ativo: true,
        dataCadastro: '2026-01-10',
      }),
    ).toEqual({
      id: 10,
      nome: 'Mensal',
      descricao: 'Plano mensal',
      valor: 149.9,
      duracaoDias: 30,
      status: 'ATIVO',
      ativo: true,
      dataCadastro: '2026-01-10',
    })
  })

  it('maps inactive plans and safe defaults', () => {
    const plano = planoApiToViewModel({ ativo: false })

    expect(plano.status).toBe('INATIVO')
    expect(plano.ativo).toBe(false)
    expect(plano.valor).toBe(0)
  })

  it('creates API payload with backend field names', () => {
    expect(
      planoViewModelToAPI({
        nome: ' Trimestral ',
        descricao: ' 3 meses ',
        valor: 299.9,
        duracaoDias: 90,
      }),
    ).toEqual({
      nome: 'Trimestral',
      descricao: '3 meses',
      valorMensal: 299.9,
      duracaoEmDias: 90,
    })
  })
})
