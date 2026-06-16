import { describe, expect, it } from 'vitest'
import { planoApiToViewModel, planoViewModelToAPI } from './planoMapper'

describe('planoMapper', () => {
  it('maps API fields to UI fields without exposing backend-only names', () => {
    const plano = planoApiToViewModel({
      id: 10,
      nome: 'Mensal',
      descricao: 'Plano mensal',
      valorMensal: '149.90',
      duracaoEmDias: 30,
      ativo: true,
      dataCadastro: '2026-01-10',
    })

    expect(plano).toEqual({
      id: 10,
      nome: 'Mensal',
      descricao: 'Plano mensal',
      valor: 149.9,
      duracaoDias: 30,
      status: 'ATIVO',
      dataCadastro: '2026-01-10',
    })
    expect(plano).not.toHaveProperty('valorMensal')
    expect(plano).not.toHaveProperty('duracaoEmDias')
    expect(plano).not.toHaveProperty('ativo')
  })

  it('maps inactive plans and safe defaults for optional fields', () => {
    const plano = planoApiToViewModel({ ativo: false })

    expect(plano.status).toBe('INATIVO')
    expect(plano.valor).toBe(0)
    expect(plano.id).toBe(0)
    expect(plano.nome).toBe('')
    expect(plano.duracaoDias).toBeUndefined()
  })

  it('maps active plans when ativo is true or omitted', () => {
    expect(planoApiToViewModel({ ativo: true }).status).toBe('ATIVO')
    expect(planoApiToViewModel({}).status).toBe('ATIVO')
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
