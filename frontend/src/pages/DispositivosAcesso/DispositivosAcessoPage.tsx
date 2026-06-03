import { DataTable } from '../../components/tables/DataTable'
import { PageHeader } from '../../components/ui/PageHeader'
import { StateMessage } from '../../components/ui/StateMessage'
import { StatusBadge } from '../../components/ui/StatusBadge'
import { useAuth } from '../../hooks/useAuth'
import { useResourceList } from '../../hooks/useResourceList'
import { accessDeviceService } from '../../services/accessDeviceService'
import { hasRole } from '../../utils/permissions'

export function DispositivosAcessoPage() {
  const { data: dispositivos, errorMessage, isLoading, reload } = useResourceList({
    load: accessDeviceService.listar,
  })
  const { user } = useAuth()
  const canManageDevices = hasRole(user, ['ADMIN'])

  return (
    <>
      <PageHeader
        eyebrow="Controle de acesso"
        title="Dispositivos de acesso"
        description="Acompanhe catracas e pontos de acesso cadastrados no backend real."
        action={
          <button type="button" className="ghost-button compact" onClick={() => void reload()}>
            Atualizar
          </button>
        }
      />

      <section className="content-panel">
        {isLoading && <StateMessage title="Carregando dispositivos de acesso..." />}
        {!isLoading && errorMessage && (
          <StateMessage
            title="Não foi possível carregar os dispositivos de acesso."
            description={errorMessage}
          />
        )}
        {!isLoading && !errorMessage && dispositivos.length === 0 && (
          <StateMessage
            title="Nenhum dispositivo cadastrado."
            description="Cadastre uma catraca ou ponto de acesso para acompanhar o status operacional."
          />
        )}
        {!isLoading && !errorMessage && dispositivos.length > 0 && (
          <DataTable
            headers={[
              'Nome',
              'Tipo',
              'Fabricante',
              'Modelo',
              'Status',
              'Modo',
              'Unidade',
              'Última comunicação',
              'Ações',
            ]}
          >
            {dispositivos.map((dispositivo) => (
              <tr key={dispositivo.id}>
                <td>
                  <strong>{dispositivo.nome}</strong>
                  {dispositivo.identificadorExterno && (
                    <div className="table-muted">{dispositivo.identificadorExterno}</div>
                  )}
                </td>
                <td>{dispositivo.tipoLabel}</td>
                <td>{dispositivo.fabricante ?? '-'}</td>
                <td>{dispositivo.modelo ?? '-'}</td>
                <td><StatusBadge status={dispositivo.status}>{dispositivo.statusLabel}</StatusBadge></td>
                <td><StatusBadge status={dispositivo.modoOperacao}>{dispositivo.modoOperacaoLabel}</StatusBadge></td>
                <td>{dispositivo.unidade}</td>
                <td>{dispositivo.ultimaComunicacaoLabel}</td>
                <td>
                  <div className="row-actions">
                    <button
                      type="button"
                      className="ghost-button compact"
                      disabled={!canManageDevices}
                      title={!canManageDevices ? 'Ação restrita ao perfil ADMIN.' : undefined}
                    >
                      Configurar
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </DataTable>
        )}
      </section>
    </>
  )
}
