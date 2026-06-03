import { useMemo, useState } from 'react'
import { DataTable } from '../../components/tables/DataTable'
import { PageHeader } from '../../components/ui/PageHeader'
import { StateMessage } from '../../components/ui/StateMessage'
import { StatusBadge } from '../../components/ui/StatusBadge'
import { useResourceList } from '../../hooks/useResourceList'
import { accessEventService } from '../../services/accessEventService'
import type {
  ModoEventoAcesso,
  OrigemEventoAcesso,
  ResultadoAcessoEvento,
} from '../../types/accessEvent'

export function EventosAcessoPage() {
  const { data: eventos, errorMessage, isLoading, reload } = useResourceList({
    load: accessEventService.listarHoje,
  })
  const [resultadoFilter, setResultadoFilter] = useState<ResultadoAcessoEvento | 'TODOS'>('TODOS')
  const [origemFilter, setOrigemFilter] = useState<OrigemEventoAcesso | 'TODAS'>('TODAS')
  const [modoFilter, setModoFilter] = useState<ModoEventoAcesso | 'TODOS'>('TODOS')
  const filteredEvents = useMemo(
    () =>
      eventos.filter((evento) => {
        const byResultado = resultadoFilter === 'TODOS' || evento.resultado === resultadoFilter
        const byOrigem = origemFilter === 'TODAS' || evento.origem === origemFilter
        const byModo = modoFilter === 'TODOS' || evento.modo === modoFilter

        return byResultado && byOrigem && byModo
      }),
    [eventos, modoFilter, origemFilter, resultadoFilter],
  )

  return (
    <>
      <PageHeader
        eyebrow="Controle de acesso"
        title="Eventos de acesso"
        description="Acompanhe eventos liberados e bloqueados recebidos do contrato real do backend."
        action={
          <button type="button" className="ghost-button compact" onClick={() => void reload()}>
            Atualizar
          </button>
        }
      />

      <section className="content-panel access-filters" aria-label="Filtros de eventos">
        <label>
          <span>Resultado</span>
          <select value={resultadoFilter} onChange={(event) => setResultadoFilter(event.target.value as ResultadoAcessoEvento | 'TODOS')}>
            <option value="TODOS">Todos</option>
            <option value="LIBERADO">Liberado</option>
            <option value="BLOQUEADO">Bloqueado</option>
          </select>
        </label>
        <label>
          <span>Origem</span>
          <select value={origemFilter} onChange={(event) => setOrigemFilter(event.target.value as OrigemEventoAcesso | 'TODAS')}>
            <option value="TODAS">Todas</option>
            <option value="QR_CODE">QR Code</option>
            <option value="RECEPCAO">Recepção</option>
            <option value="GATEWAY">Gateway</option>
            <option value="SISTEMA">Sistema</option>
            <option value="MANUAL">Manual</option>
          </select>
        </label>
        <label>
          <span>Modo</span>
          <select value={modoFilter} onChange={(event) => setModoFilter(event.target.value as ModoEventoAcesso | 'TODOS')}>
            <option value="TODOS">Todos</option>
            <option value="ONLINE">Online</option>
            <option value="OFFLINE">Offline</option>
          </select>
        </label>
      </section>

      <section className="content-panel">
        {isLoading && <StateMessage title="Carregando eventos de acesso..." />}
        {!isLoading && errorMessage && (
          <StateMessage
            title="Não foi possível carregar os eventos de acesso."
            description={errorMessage}
          />
        )}
        {!isLoading && !errorMessage && eventos.length === 0 && (
          <StateMessage
            title="Nenhum evento de acesso registrado."
            description="Os acessos liberados ou bloqueados aparecerão aqui."
          />
        )}
        {!isLoading && !errorMessage && eventos.length > 0 && filteredEvents.length === 0 && (
          <StateMessage
            title="Nenhum evento encontrado para os filtros atuais."
            description="Ajuste os filtros para visualizar outros registros."
          />
        )}
        {!isLoading && !errorMessage && filteredEvents.length > 0 && (
          <DataTable headers={['Data/hora', 'Aluno', 'Dispositivo', 'Resultado', 'Origem', 'Modo', 'Sincronização', 'Motivo']}>
            {filteredEvents.map((evento) => (
              <tr key={evento.id}>
                <td>{evento.dataHoraEventoLabel}</td>
                <td>{evento.alunoNome}</td>
                <td>{evento.dispositivoNome}</td>
                <td><StatusBadge status={evento.resultado}>{evento.resultadoLabel}</StatusBadge></td>
                <td>{evento.origemLabel}</td>
                <td><StatusBadge status={evento.modo}>{evento.modoLabel}</StatusBadge></td>
                <td>{evento.sincronizacaoLabel}</td>
                <td>{evento.motivo}</td>
              </tr>
            ))}
          </DataTable>
        )}
      </section>
    </>
  )
}
