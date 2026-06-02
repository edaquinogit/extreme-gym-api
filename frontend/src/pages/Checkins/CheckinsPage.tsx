import { DataTable } from '../../components/tables/DataTable'
import { PageHeader } from '../../components/ui/PageHeader'
import { StateMessage } from '../../components/ui/StateMessage'
import { StatusBadge } from '../../components/ui/StatusBadge'
import { useResourceList } from '../../hooks/useResourceList'
import { checkinService } from '../../services/checkinService'
import { formatDate } from '../../utils/formatDate'

export function CheckinsPage() {
  const { data: checkins, errorMessage, isLoading } = useResourceList({
    load: checkinService.listar,
  })

  return (
    <>
      <PageHeader
        eyebrow="Check-ins"
        title="Acessos da academia"
        description="Histórico de entradas autorizadas ou bloqueadas pelo backend."
      />

      <section className="content-panel">
        {isLoading && <StateMessage title="Carregando check-ins..." />}
        {!isLoading && errorMessage && (
          <StateMessage title="Não foi possível carregar os check-ins." description={errorMessage} />
        )}
        {!isLoading && !errorMessage && checkins.length === 0 && (
          <StateMessage title="Nenhum check-in encontrado" description="Quando houver acessos registrados, eles aparecerão aqui." />
        )}
        {!isLoading && !errorMessage && checkins.length > 0 && (
          <DataTable headers={['ID', 'Aluno', 'Data e hora', 'Status', 'Motivo']}>
            {checkins.map((checkin) => (
              <tr key={checkin.id}>
                <td>{checkin.id}</td>
                <td>{checkin.alunoNome ?? checkin.aluno?.nome ?? '-'}</td>
                <td>{formatDate(checkin.dataHora)}</td>
                <td><StatusBadge status={checkin.status} /></td>
                <td>{checkin.motivoBloqueio ?? '-'}</td>
              </tr>
            ))}
          </DataTable>
        )}
      </section>
    </>
  )
}
