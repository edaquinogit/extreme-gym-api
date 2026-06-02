import type { ReactNode } from 'react'

type FilterBarProps = {
  children: ReactNode
  summary?: ReactNode
}

export function FilterBar({ children, summary }: FilterBarProps) {
  return (
    <div className="filter-bar">
      <div className="filter-bar-controls">{children}</div>
      {summary && <div className="filter-bar-summary">{summary}</div>}
    </div>
  )
}

