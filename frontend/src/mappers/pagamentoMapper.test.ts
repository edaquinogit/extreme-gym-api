import { describe, expect, it } from 'vitest'
import { pagamentoApiToViewModel, pagamentoViewModelToAPI } from './pagamentoMapper'

describe('pagamentoMapper', () => {
  it.each(['PAGO', 'PENDENTE', 'CANCELADO'] as const)(
    'preserves supported payment status %s',
    (status) => {
      expect(pagamentoApiToViewModel({ status }).status).toBe(status)
    },
  )

  it('does not depend on dataVencimento from payment API', () => {
    const pagamento = pagamentoApiToViewModel({
      id: 8,
      matriculaId: 12,
      alunoNome: 'Maria',
      valor: '180.50',
      formaPagamento: 'PIX',
      dataPagamento: null,
    })

    expect(pagamento.valor).toBe(180.5)
    expect(pagamento.status).toBe('PENDENTE')
    expect(pagamento.dataPagamento).toBeNull()
    expect(pagamento.dataVencimento).toBeUndefined()
  })

  it('creates registration payload with backend-supported fields only', () => {
    expect(
      pagamentoViewModelToAPI({
        matriculaId: 12,
        valor: 180.5,
        formaPagamento: 'PIX',
      }),
    ).toEqual({
      matriculaId: 12,
      valor: 180.5,
      formaPagamento: 'PIX',
    })
  })
})
