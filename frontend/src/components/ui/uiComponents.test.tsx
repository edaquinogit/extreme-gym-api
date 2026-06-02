import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { Button } from './Button'
import { ConfirmDialog } from './ConfirmDialog'
import { ErrorState } from './ErrorState'
import { FilterBar } from './FilterBar'
import { FormField } from './FormField'
import { IconButton } from './IconButton'
import { MetricCard } from './MetricCard'
import { Skeleton } from './Skeleton'
import { StateMessage } from './StateMessage'
import { StatusBadge } from './StatusBadge'

describe('UI components', () => {
  it('renders Button variants and preserves disabled state', () => {
    render(
      <>
        <Button variant="primary">Salvar</Button>
        <Button variant="danger" disabled>
          Excluir
        </Button>
      </>,
    )

    expect(screen.getByRole('button', { name: 'Salvar' })).toBeEnabled()
    expect(screen.getByRole('button', { name: 'Excluir' })).toBeDisabled()
  })

  it('renders IconButton with accessible label', () => {
    render(<IconButton label="Abrir menu" icon={<span>≡</span>} />)

    expect(screen.getByRole('button', { name: 'Abrir menu' })).toBeInTheDocument()
  })

  it('connects FormField label, hint and error to the control', () => {
    render(
      <FormField label="Nome" hint="Informe o nome completo." error="Nome é obrigatório." required>
        <input />
      </FormField>,
    )

    const input = screen.getByLabelText('Nome *')
    expect(input).toHaveAttribute('aria-required', 'true')
    expect(screen.getByText('Informe o nome completo.')).toBeInTheDocument()
    expect(screen.getByText('Nome é obrigatório.')).toBeInTheDocument()
  })

  it('formats StatusBadge values from backend statuses', () => {
    render(
      <>
        <StatusBadge status="ATIVO" />
        <StatusBadge status="PAGAMENTO_PENDENTE" />
      </>,
    )

    expect(screen.getByText('Ativo')).toBeInTheDocument()
    expect(screen.getByText('Pagamento Pendente')).toBeInTheDocument()
  })

  it('renders MetricCard value and loading state', () => {
    const { rerender } = render(
      <MetricCard title="Alunos ativos" value={300} helper="Base atual" />,
    )

    expect(screen.getByText('300')).toBeInTheDocument()
    expect(screen.getByText('Base atual')).toBeInTheDocument()

    rerender(<MetricCard title="Alunos ativos" loading />)

    expect(screen.getByText('Carregando...')).toBeInTheDocument()
  })

  it('renders StateMessage and ErrorState action', () => {
    const onAction = vi.fn()

    render(
      <>
        <StateMessage title="Nenhum aluno encontrado." description="Ajuste a busca." />
        <ErrorState description="Tente novamente." actionLabel="Recarregar" onAction={onAction} />
      </>,
    )

    expect(screen.getByText('Nenhum aluno encontrado.')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Recarregar' }))
    expect(onAction).toHaveBeenCalledTimes(1)
  })

  it('renders Skeleton as an aria-hidden loading placeholder', () => {
    const { container } = render(<Skeleton lines={2} />)

    expect(container.querySelector('.skeleton')).toHaveAttribute('aria-hidden', 'true')
    expect(container.querySelectorAll('.skeleton span')).toHaveLength(2)
  })

  it('renders FilterBar controls and summary', () => {
    render(
      <FilterBar summary="2 resultados">
        <input aria-label="Buscar" />
      </FilterBar>,
    )

    expect(screen.getByLabelText('Buscar')).toBeInTheDocument()
    expect(screen.getByText('2 resultados')).toBeInTheDocument()
  })

  it('handles ConfirmDialog cancel and confirm actions', () => {
    const onCancel = vi.fn()
    const onConfirm = vi.fn()

    render(
      <ConfirmDialog
        isOpen
        title="Cancelar matrícula"
        description="Deseja cancelar esta matrícula?"
        onCancel={onCancel}
        onConfirm={onConfirm}
      />,
    )

    fireEvent.click(screen.getByRole('button', { name: 'Cancelar' }))
    fireEvent.click(screen.getByRole('button', { name: 'Confirmar' }))

    expect(onCancel).toHaveBeenCalledTimes(1)
    expect(onConfirm).toHaveBeenCalledTimes(1)
  })
})
