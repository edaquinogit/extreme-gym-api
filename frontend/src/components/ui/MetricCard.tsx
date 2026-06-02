import React from 'react'
import { LoadingSpinner } from './LoadingSpinner'

type MetricCardProps = {
  title?: string
  label?: string
  value?: React.ReactNode
  helper?: string
  description?: string
  icon?: React.ReactNode
  trend?: string
  loading?: boolean
  error?: string | null
  variant?: 'neutral' | 'success' | 'warning' | 'danger' | 'info'
}

export function MetricCard({
  description,
  error,
  helper,
  icon,
  label,
  loading,
  title,
  trend,
  value,
  variant = 'neutral',
}: MetricCardProps) {
  const metricLabel = label ?? title ?? 'Indicador'
  const helperText = description ?? helper

  return (
    <article className={`metric-card metric-card--${variant}`} role="group" aria-label={metricLabel}>
      <div className="metric-card-header">
        <span>{metricLabel}</span>
        <i aria-hidden="true">{icon}</i>
      </div>

      {loading ? (
        <div className="metric-card-loading">
          <LoadingSpinner size={18} />
          <small>Carregando...</small>
        </div>
      ) : error ? (
        <div>
          <strong>-</strong>
          <small className="metric-card-error">{error}</small>
        </div>
      ) : value === undefined || value === null ? (
        <div>
          <strong>-</strong>
          <small>{helperText}</small>
        </div>
      ) : (
        <>
          <strong>{value}</strong>
          {helperText && <small>{helperText}</small>}
          {trend && <em>{trend}</em>}
        </>
      )}
    </article>
  )
}

export default MetricCard
