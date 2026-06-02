import { lazy, Suspense } from 'react'
import { appPaths } from '../../app/routes/paths'
import { navigateTo } from '../../app/routes/router'
import { useAuth } from '../../hooks/useAuth'
import { API_URL } from '../../config/api'
import { LoadingSpinner } from '../../components/ui/LoadingSpinner'
import { MetricCard } from '../../components/ui/MetricCard'
import { useDashboardData } from './useDashboardData'
import ChartCard from '../../shared/components/charts/ChartCard'
import EmptyChartState from '../../shared/components/charts/EmptyChartState'

const PaymentsStatusChart = lazy(() => import('../../shared/components/charts/PaymentsStatusChart'))
const CheckinsLast7DaysChart = lazy(() => import('../../shared/components/charts/CheckinsLast7DaysChart'))
const RevenueByMonthChart = lazy(() => import('../../shared/components/charts/RevenueByMonthChart'))
const MatriculasStatusChart = lazy(() => import('../../shared/components/charts/MatriculasStatusChart'))

export function DashboardPage() {
  const { user } = useAuth()

  const firstName = (() => {
    const raw = user?.nome ?? user?.name ?? user?.username ?? user?.email ?? 'Admin'
    return raw.split(/[\s@]/)[0]
  })()

  const data = useDashboardData()

  const dashboardActions: Array<{ title: string; detail: string; tone?: 'normal' | 'error' }> = []

  if (!data.loading && !data.error) {
    if (data.pagamentosVencidos && data.pagamentosVencidos > 0) {
      dashboardActions.push({
        title: 'Existem pagamentos vencidos para acompanhar.',
        detail: 'Priorize a recuperação financeira e contacte os clientes com atraso.',
      })
    }

    if (data.pagamentosPendentes && data.pagamentosPendentes > 0) {
      dashboardActions.push({
        title: 'Há pagamentos pendentes.',
        detail: 'Verifique boletos e confirmações para reduzir inadimplência.',
      })
    }

    if (data.checkinsHoje !== undefined && data.checkinsHoje === 0) {
      dashboardActions.push({
        title: 'Nenhum check-in registrado hoje.',
        detail: 'Acompanhe a frequência dos alunos para identificar fluxos de atendimento.',
      })
    }

    if (data.proximosVencimentos && data.proximosVencimentos.length > 0) {
      dashboardActions.push({
        title: 'Há próximos vencimentos agendados.',
        detail: 'Fique atento a pagamentos e renovações nos próximos 14 dias.',
      })
    }
  }

  return (
    <div className="dashboard-page">
      <section className="dashboard-welcome-banner">
        <div className="dashboard-welcome-content">
          <p className="page-kicker">BEM-VINDO</p>
          <h1>Olá, {firstName}.</h1>
          <p className="page-description">
            Priorize atendimentos, pagamentos e acessos com dados reais dos módulos disponíveis.
          </p>
          <div className="quick-actions" aria-label="Ações rápidas">
            <button type="button" className="primary-button" onClick={() => navigateTo(appPaths.alunos)}>
              Novo aluno
            </button>
            <button type="button" className="secondary-button" onClick={() => navigateTo(appPaths.matriculas)}>
              Nova matrícula
            </button>
            <button type="button" className="secondary-button" onClick={() => navigateTo(appPaths.pagamentos)}>
              Registrar pagamento
            </button>
            <button type="button" className="ghost-button" onClick={() => navigateTo(appPaths.acessos)}>
              Validar acesso
            </button>
          </div>
        </div>
        <div className="dashboard-welcome-badges">
          <span className="welcome-badge is-role">{user?.role ?? 'ADMIN'}</span>
          <span className="welcome-badge is-status">{data.loading ? 'Conectando...' : data.error ? 'API indisponível' : 'API conectada'}</span>
          <span className="welcome-badge is-url">{API_URL}</span>
        </div>
      </section>

      <section className="dashboard-overview" aria-label="Resumo operacional">
        <article className="overview-panel">
          <span className="overview-label">Operação</span>
          <strong>Ambiente administrativo ativo</strong>
          <p>
            Use o menu lateral para acompanhar alunos, planos, matrículas,
            pagamentos e check-ins em um fluxo único de atendimento.
          </p>
        </article>

        <article className="overview-panel is-compact">
          <span className="overview-label">Status</span>
          <strong>{data.loading ? 'Carregando dados...' : data.error ? 'Erro nos dados' : 'Dados atualizados'}</strong>
          <p>{data.error ?? 'Dados carregados diretamente dos módulos disponíveis.'}</p>
        </article>
      </section>

      <section className="metric-grid">
        <MetricCard title="Alunos ativos" value={data.alunosAtivos ?? '-'} helper="Base de alunos em acompanhamento" loading={data.loading} error={data.error ?? null} variant="success" />

        <MetricCard title="Matrículas ativas" value={data.matriculasAtivas ?? '-'} helper="Contratos vigentes no período" loading={data.loading} error={data.error ?? null} variant="info" />

        <MetricCard title="Pagamentos pendentes" value={data.pagamentosPendentes ?? '-'} helper="Itens financeiros a acompanhar" loading={data.loading} error={data.error ?? null} variant="warning" />

        <MetricCard title="Pagamentos vencidos" value={data.pagamentosVencidos ?? '-'} helper="Exibido apenas quando derivado com segurança" loading={data.loading} error={data.error ?? null} variant="danger" />

        <MetricCard title="Check-ins hoje" value={data.checkinsHoje ?? '-'} helper="Movimento registrado no dia" loading={data.loading} error={data.error ?? null} />

        <MetricCard title="Receita mensal" value={data.receitaMensal ?? '-'} helper="Resumo financeiro do mês" loading={data.loading} error={data.error ?? null} variant="success" />
      </section>

      <section className="dashboard-actions">
        <div className="dashboard-actions-header">
          <div>
            <span className="overview-label">Resumo operacional</span>
            <h2>Atenção imediata</h2>
            <p>Os principais pontos abaixo ajudam a priorizar as decisões de gestão do dia.</p>
          </div>
        </div>

        <div className="dashboard-action-list">
          {data.loading ? (
            <div className="dashboard-action-item">
              <LoadingSpinner size={18} />
              <div>
                <strong>Carregando recomendações do painel</strong>
                <p>Os dados estão sendo carregados diretamente da API.</p>
              </div>
            </div>
          ) : data.error ? (
            <div className="dashboard-action-item dashboard-action-item--error">
              <strong>Erro ao carregar as recomendações</strong>
              <p>{data.error}</p>
            </div>
          ) : dashboardActions.length > 0 ? (
            dashboardActions.map((action) => (
              <div key={action.title} className={`dashboard-action-item ${action.tone === 'error' ? 'dashboard-action-item--error' : ''}`}>
                <div className="dashboard-action-icon" aria-hidden />
                <div>
                  <strong>{action.title}</strong>
                  <p>{action.detail}</p>
                </div>
              </div>
            ))
          ) : (
            <div className="dashboard-action-item dashboard-action-item--success">
              <div className="dashboard-action-icon" aria-hidden />
              <div>
                <strong>Painel pronto para uso</strong>
                <p>Os dados estão atualizados e o fluxo operacional está visível.</p>
              </div>
            </div>
          )}
        </div>
      </section>

      <section className="dashboard-section">
        <h2>Visão gerencial</h2>
        <p className="section-description">Indicadores que mostram a situação financeira e o movimento de acesso.</p>
        <div className="charts-grid">
          <ChartCard title="Status dos pagamentos" description="Distribuição dos pagamentos por situação financeira." loading={data.loading} error={data.error ?? null}>
            {data.pagamentosPorStatus && data.pagamentosPorStatus.length > 0 ? (
              <Suspense fallback={<div className="chart-loading-fallback"><LoadingSpinner size={18} /><small>Carregando gráfico...</small></div>}>
                <PaymentsStatusChart data={data.pagamentosPorStatus} />
              </Suspense>
            ) : (
              <EmptyChartState message="Nenhum pagamento registrado ainda. Registre cobranças para visualizar a distribuição financeira." />
            )}
          </ChartCard>

          <ChartCard title="Check-ins últimos 7 dias" description="Movimento registrado na academia nos últimos 7 dias." loading={data.loading} error={data.error ?? null}>
            {data.checkinsPorDia && data.checkinsPorDia.length > 0 ? (
              <Suspense fallback={<div className="chart-loading-fallback"><LoadingSpinner size={18} /><small>Carregando gráfico...</small></div>}>
                <CheckinsLast7DaysChart data={data.checkinsPorDia} />
              </Suspense>
            ) : (
              <EmptyChartState message="Nenhum check-in encontrado nos últimos 7 dias. Os movimentos aparecerão aqui conforme os alunos acessarem a academia." />
            )}
          </ChartCard>
        </div>
      </section>

      <section className="dashboard-section">
        <h2>Financeiro e matrículas</h2>
        <p className="section-description">Dados financeiros e de matrículas para apoiar o controle operacional.</p>
        <div className="charts-grid">
          <ChartCard title="Receita (últimos 6 meses)" description="Receita confirmada a partir de pagamentos pagos." loading={data.loading} error={data.error ?? null}>
            {data.receitaPorMes && data.receitaPorMes.some((m) => m.value > 0) ? (
              <Suspense fallback={<div className="chart-loading-fallback"><LoadingSpinner size={18} /><small>Carregando gráfico...</small></div>}>
                <RevenueByMonthChart data={data.receitaPorMes} />
              </Suspense>
            ) : (
              <EmptyChartState message="Ainda não há receita confirmada nos últimos 6 meses. Pagamentos PAGO serão exibidos aqui." />
            )}
          </ChartCard>

          <ChartCard title="Matrículas por status" description="Situação atual das matrículas cadastradas." loading={data.loading} error={data.error ?? null}>
            {data.matriculasPorStatus && data.matriculasPorStatus.length > 0 ? (
              <Suspense fallback={<div className="chart-loading-fallback"><LoadingSpinner size={18} /><small>Carregando gráfico...</small></div>}>
                <MatriculasStatusChart data={data.matriculasPorStatus} />
              </Suspense>
            ) : (
              <EmptyChartState message="Nenhuma matrícula encontrada. Cadastre matrículas para acompanhar o desempenho." />
            )}
          </ChartCard>
        </div>
        <div className="content-panel upcoming-panel">
          <strong>Vencimentos próximos</strong>
          {data.loading ? (
            <small>Carregando...</small>
          ) : data.proximosVencimentos && data.proximosVencimentos.length > 0 ? (
            <div className="upcoming-list">
              {data.proximosVencimentos.map((p) => (
                <div key={p.id} className="upcoming-item">
                  <div>
                    <div>{p.alunoNome ?? '—'}</div>
                    <small>{p.dataVencimento}</small>
                  </div>
                  <div>{p.valor ? p.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) : '-'}</div>
                </div>
              ))}
            </div>
          ) : (
            <div className="upcoming-empty">
              <small>Nenhum vencimento próximo nos próximos 14 dias.</small>
            </div>
          )}
        </div>
      </section>
    </div>
  )
}
