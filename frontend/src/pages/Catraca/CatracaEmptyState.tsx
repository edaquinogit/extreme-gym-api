export function CatracaEmptyState() {
  return (
    <section className="catraca-empty-state" aria-label="Estado inicial da catraca">
      <p className="catraca-placeholder">Aguardando validação de acesso.</p>
      <p className="catraca-hint">
        Use a busca manual quando a identificação automática não estiver
        disponível.
      </p>
      <p className="catraca-hint">Nenhum acesso registrado hoje.</p>
    </section>
  )
}
