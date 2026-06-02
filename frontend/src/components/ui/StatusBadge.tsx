type StatusBadgeProps = {
  status: string
  className?: string
  children?: string
}

function normalizeStatus(status: string) {
  return String(status).trim().toUpperCase()
}

function formatStatus(status: string) {
  const normalized = normalizeStatus(status).replace(/_/g, ' ')

  return normalized
    .toLowerCase()
    .replace(/\b\w/g, (char) => char.toUpperCase())
}

export function StatusBadge({
  status,
  className = '',
  children,
}: StatusBadgeProps) {
  const normalizedStatus = normalizeStatus(status)

  return (
    <span className={`status-badge ${className}`.trim()} data-status={normalizedStatus}>
      {children ?? formatStatus(normalizedStatus)}
    </span>
  )
}
