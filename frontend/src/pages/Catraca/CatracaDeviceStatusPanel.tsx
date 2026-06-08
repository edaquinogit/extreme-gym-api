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
          <h2>Gateway externo ainda não validado</h2>
        </div>
        <p>Nenhum dispositivo de acesso foi retornado pelo backend.</p>
        <p className="catraca-device-warning">
          Use a operação manual autorizada até cadastrar e validar o gateway.
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
          <dt>Tipo</dt>
          <dd>{formatDeviceType(dispositivo.tipo)}</dd>
        </div>
        <div>
          <dt>Unidade</dt>
          <dd>{dispositivo.unidade || 'Unidade não informada'}</dd>
        </div>
      </dl>

      {(dispositivo.status === 'OFFLINE' || dispositivo.status === 'INATIVO') && (
        <p className="catraca-device-warning">
          Dispositivo sem operação ativa. Use a operação manual autorizada.
        </p>
      )}
    </section>
  )
}

function formatDeviceStatus(status: StatusDispositivoAcesso) {
  const labels: Record<StatusDispositivoAcesso, string> = {
    ATIVO: 'Dispositivo ativo',
    INATIVO: 'Dispositivo inativo',
    OFFLINE: 'Catraca offline',
    MANUTENCAO: 'Em manutenção',
  }

  return labels[status]
}

function formatDeviceType(type: DispositivoAcessoViewModel['tipo']) {
  const labels: Record<DispositivoAcessoViewModel['tipo'], string> = {
    CATRACA_FACIAL: 'Catraca facial',
    CATRACA_QR: 'Catraca QR',
    GATEWAY: 'Gateway',
    OUTRO: 'Outro',
    RECEPCAO: 'Recepção',
  }
  return labels[type]
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
    return 'Sem comunicação registrada'
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value))
}
