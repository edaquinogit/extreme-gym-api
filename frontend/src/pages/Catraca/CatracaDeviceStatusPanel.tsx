import type {
  DispositivoAcessoViewModel,
  ModoOperacaoDispositivo,
  StatusDispositivoAcesso,
} from '../../types/accessDevice'

type CatracaDeviceStatusPanelProps = {
  dispositivo: DispositivoAcessoViewModel | null
}

export function CatracaDeviceStatusPanel({
  dispositivo,
}: CatracaDeviceStatusPanelProps) {
  if (!dispositivo) {
    return (
      <section className="catraca-device-panel" aria-label="Status da catraca">
        <div>
          <span className="overview-label">Dispositivo</span>
          <h2>Nenhum dispositivo configurado</h2>
        </div>
        <p>Nenhum dispositivo cadastrado para esta estação.</p>
        <p className="catraca-device-warning">
          Cadastre uma catraca ou ponto de acesso para acompanhar o status
          operacional. A integração com gateway físico continua pendente.
        </p>
      </section>
    )
  }

  return (
    <section className="catraca-device-panel" aria-label="Status da catraca">
      <div className="catraca-device-header">
        <div>
          <span className="overview-label">Dispositivo</span>
          <h2>{dispositivo.nome}</h2>
        </div>
        <span
          className={`catraca-device-status is-${dispositivo.status.toLowerCase()}`}
        >
          {formatDeviceStatus(dispositivo.status)}
        </span>
      </div>

      <dl className="catraca-device-grid">
        <div>
          <dt>Modo</dt>
          <dd>{formatOperationMode(dispositivo.modoOperacao)}</dd>
        </div>
        <div>
          <dt>Última comunicação</dt>
          <dd>{formatLastCommunication(dispositivo.ultimaComunicacaoEm)}</dd>
        </div>
        <div>
          <dt>Eventos pendentes</dt>
          <dd>{dispositivo.eventosPendentes}</dd>
        </div>
        <div>
          <dt>Unidade</dt>
          <dd>{dispositivo.unidade}</dd>
        </div>
      </dl>

      {dispositivo.status === 'OFFLINE' && (
        <p className="catraca-device-warning">
          Catraca offline. A validação automática pode estar indisponível. Use a
          operação manual autorizada.
        </p>
      )}

      {dispositivo.status === 'MANUTENCAO' && (
        <p className="catraca-device-warning">
          Dispositivo em manutenção. Não use este ponto como referência de
          validação automática.
        </p>
      )}
    </section>
  )
}

function formatDeviceStatus(status: StatusDispositivoAcesso) {
  const labels: Record<StatusDispositivoAcesso, string> = {
    ATIVO: 'Ativo',
    INATIVO: 'Inativo',
    OFFLINE: 'Catraca offline',
    MANUTENCAO: 'Em manutenção',
  }

  return labels[status]
}

function formatOperationMode(mode: ModoOperacaoDispositivo) {
  const labels: Record<ModoOperacaoDispositivo, string> = {
    ONLINE: 'Online',
    OFFLINE: 'Offline',
    HIBRIDO: 'Híbrido',
  }

  return labels[mode]
}

function formatLastCommunication(value: string | null) {
  if (!value) {
    return 'Última comunicação não registrada.'
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value))
}
