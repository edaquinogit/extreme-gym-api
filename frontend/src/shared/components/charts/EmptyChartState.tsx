export function EmptyChartState({ message }: { message?: string }) {
  return (
    <div className="chart-empty-state">
      <div className="chart-empty-state-title">Sem dados suficientes</div>
      <div className="chart-empty-state-text">{message ?? 'Ainda não há dados suficientes para este gráfico.'}</div>
    </div>
  )
}

export default EmptyChartState
