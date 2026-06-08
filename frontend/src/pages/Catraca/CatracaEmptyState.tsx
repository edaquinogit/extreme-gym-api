import type { AccessEvent } from '../../types/accessEvent'

type CatracaEmptyStateProps = {
  eventosRecentes: AccessEvent[]
}

export function CatracaEmptyState({ eventosRecentes }: CatracaEmptyStateProps) {
  return (
    <section className="catraca-empty-state" aria-label="Estado inicial da catraca">
      <p className="catraca-placeholder">Aguardando validação de acesso.</p>
      <p className="catraca-hint">
        Use a busca manual quando a identificação automática não estiver
        disponível.
      </p>
      {eventosRecentes.length > 0 ? (
        <div className="catraca-recent-events" aria-label="Eventos recentes">
          {eventosRecentes.slice(0, 4).map((event) => (
            <div className="catraca-recent-event" key={event.id}>
              <strong>{event.resultado === 'LIBERADO' ? 'Liberado' : 'Bloqueado'}</strong>
              <span>{event.alunoNome || (event.alunoId ? `Aluno #${event.alunoId}` : 'Aluno não informado')}</span>
            </div>
          ))}
        </div>
      ) : (
        <p className="catraca-hint">Nenhum evento de acesso retornado pelo backend.</p>
      )}
    </section>
  )
}
