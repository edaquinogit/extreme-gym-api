import type { ReactNode } from 'react'

type ChartCardProps = {
  title: string
  description?: string
  loading?: boolean
  error?: string | null
  children?: ReactNode
}

export function ChartCard({ title, description, loading, error, children }: ChartCardProps) {
  return (
    <section className="content-panel chart-card" aria-labelledby={`chart-${title}`}>
      <header className="chart-card-header">
        <div>
          <strong id={`chart-${title}`} className="chart-card-title">{title}</strong>
          {description && <small className="chart-card-description">{description}</small>}
        </div>
      </header>

      <div className="chart-card-body">
        {loading ? (
          <div className="chart-card-status">
            <div className="spinner" aria-hidden="true" />
            <small>Carregando...</small>
          </div>
        ) : error ? (
          <div className="chart-card-status chart-card-status--error">
            <small>{error}</small>
          </div>
        ) : (
          children
        )}
      </div>
    </section>
  )
}

export default ChartCard
