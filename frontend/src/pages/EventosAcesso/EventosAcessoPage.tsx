import { useMemo, useState } from 'react'
import { DataTable } from '../../components/tables/DataTable'
import { FilterBar } from '../../components/ui/FilterBar'
import { PageHeader } from '../../components/ui/PageHeader'
import { StateMessage } from '../../components/ui/StateMessage'
import { StatusBadge } from '../../components/ui/StatusBadge'
import { useResourceList } from '../../hooks/useResourceList'
import { accessEventService } from '../../services/accessEventService'
import type { AccessEventMode, AccessEventOrigin, AccessEventResult } from '../../types/accessEvent'
import { formatDate } from '../../utils/formatDate'

export function EventosAcessoPage() {
  const { data: events, errorMessage, isLoading } = useResourceList({ load: accessEventService.listar })
  const [resultadoFilter, setResultadoFilter] = useState<AccessEventResult | 'TODOS'>('TODOS')
  const [modoFilter, setModoFilter] = useState<AccessEventMode | 'TODOS'>('TODOS')
  const [origemFilter, setOrigemFilter] = useState<AccessEventOrigin | 'TODOS'>('TODOS')

  const filteredEvents = useMemo(
    () =>
      events.filter((event) =>
        (resultadoFilter === 'TODOS' || event.resultado === resultadoFilter) &&
        (modoFilter === 'TODOS' || event.modo === modoFilter) &&
        (origemFilter === 'TODOS' || event.origem === origemFilter),
      ),
    [events, modoFilter, origemFilter, resultadoFilter],
  )

  return (
    <>
      <PageHeader
        eyebrow="Controle de acesso"
        title="Eventos de acesso"
        description="Consulte acessos liberados, bloqueados e sincronizados pelo contrato backend."
      />

      <FilterBar summary={!isLoading && !errorMessage ? `${filteredEvents.length} de ${events.length} eventos` : undefined}>
        <select aria-label="Filtrar por resultado" value={resultadoFilter} onChange={(event) => setResultadoFilter(event.target.value as AccessEventResult | 'TODOS')}>
          <option value="TODOS">Todos os resultados</option>
          <option value="LIBERADO">Liberado</option>
          <option value="BLOQUEADO">Bloqueado</option>
        </select>
        <select aria-label="Filtrar por modo" value={modoFilter} onChange={(event) => setModoFilter(event.target.value as AccessEventMode | 'TODOS')}>
          <option value="TODOS">Todos os modos</option>
          <option value="ONLINE">Online</option>
          <option value="OFFLINE">Offline</option>
        </select>
        <select aria-label="Filtrar por origem" value={origemFilter} onChange={(event) => setOrigemFilter(event.target.value as AccessEventOrigin | 'TODOS')}>
          <option value="TODOS">Todas as origens</option>
          <option value="DISPOSITIVO">Dispositivo</option>
          <option value="GATEWAY">Gateway</option>
          <option value="QR_CODE">QR Code</option>
          <option value="MANUAL">Manual</option>
          <option value="RECEPCAO">Recepção</option>
          <option value="SISTEMA">Sistema</option>
          <option value="FACE_ID">Face ID</option>
        </select>
      </FilterBar>

      <section className="content-panel">
        {isLoading && <StateMessage title="Carregando eventos de acesso..." />}

        {!isLoading && errorMessage && (
          <StateMessage title="Não foi possível carregar os eventos de acesso." description={errorMessage} />
        )}

        {!isLoading && !errorMessage && events.length === 0 && (
          <StateMessage
            title="Nenhum evento de acesso registrado."
            description="Os acessos liberados ou bloqueados aparecerão aqui."
          />
        )}

        {!isLoading && !errorMessage && events.length > 0 && filteredEvents.length === 0 && (
          <StateMessage title="Nenhum evento encontrado" description="Ajuste os filtros para consultar outros eventos." />
        )}

        {!isLoading && !errorMessage && filteredEvents.length > 0 && (
          <DataTable headers={['Data/hora', 'Resultado', 'Aluno', 'Dispositivo', 'Origem', 'Modo', 'Sincronização', 'Motivo']}>
            {filteredEvents.map((event) => (
              <tr key={event.id}>
                <td>{formatDate(event.dataHoraEvento)}</td>
                <td><StatusBadge status={event.resultado} /></td>
                <td>{event.alunoNome || (event.alunoId ? `Aluno #${event.alunoId}` : 'Aluno não informado')}</td>
                <td>{event.dispositivoNome || (event.dispositivoId ? `Dispositivo #${event.dispositivoId}` : 'Dispositivo não informado')}</td>
                <td><StatusBadge status={event.origem} /></td>
                <td><StatusBadge status={event.modo} /></td>
                <td><StatusBadge status={event.sincronizado ? 'SINCRONIZADO' : 'PENDENTE'} /></td>
                <td>{event.motivo}</td>
              </tr>
            ))}
          </DataTable>
        )}
      </section>
    </>
  )
}
