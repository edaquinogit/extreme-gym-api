import { StateMessage } from './StateMessage'

type ErrorStateProps = {
  title?: string
  description?: string
  actionLabel?: string
  onAction?: () => void
}

export function ErrorState({
  actionLabel,
  description,
  onAction,
  title = 'Não foi possível carregar os dados.',
}: ErrorStateProps) {
  return (
    <div className="error-state">
      <StateMessage title={title} description={description} />
      {actionLabel && onAction && (
        <button type="button" className="ghost-button compact" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  )
}
