type StateMessageProps = {
  title: string
  description?: string
}

export function StateMessage({ title, description }: StateMessageProps) {
  return (
    <div className="state-message">
      <strong>{title}</strong>
      {description && <p>{description}</p>}
    </div>
  )
}
