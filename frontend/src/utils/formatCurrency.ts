export function formatCurrency(value?: number) {
  if (value === undefined || value === null) {
    return '-'
  }

  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value)
}
