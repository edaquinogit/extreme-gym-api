import type { ReactNode } from 'react'

type PageHeaderProps = {
  eyebrow: string
  title: string
  description: string
  action?: ReactNode
}

export function PageHeader({ action, description, eyebrow, title }: PageHeaderProps) {
  return (
    <section className="page-header">
      <div className="page-header-copy">
        <p className="page-kicker">{eyebrow}</p>
        <h1>{title}</h1>
        <p className="page-description">{description}</p>
      </div>
      {action && <div className="page-header-action">{action}</div>}
    </section>
  )
}
