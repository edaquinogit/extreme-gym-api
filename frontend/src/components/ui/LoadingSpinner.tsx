import React from 'react'

export function LoadingSpinner({ size = 20 }: { size?: number }) {
  const style: React.CSSProperties = {
    width: size,
    height: size,
    borderRadius: 999,
    border: '3px solid var(--color-border-soft)',
    borderTopColor: 'var(--color-primary-muted)',
    animation: 'spin 900ms linear infinite',
  }

  return <div className="spinner" style={style} aria-hidden="true" />
}

export default LoadingSpinner
