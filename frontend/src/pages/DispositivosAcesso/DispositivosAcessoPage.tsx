import { type Dispatch, type FormEvent, type SetStateAction, useState } from 'react'
import { DataTable } from '../../components/tables/DataTable'
import { FormField } from '../../components/ui/FormField'
import { Modal } from '../../components/ui/Modal'
import { PageHeader } from '../../components/ui/PageHeader'
import { StateMessage } from '../../components/ui/StateMessage'
import { StatusBadge } from '../../components/ui/StatusBadge'
import { useApiError } from '../../hooks/useApiError'
import { useAuth } from '../../hooks/useAuth'
import { useResourceList } from '../../hooks/useResourceList'
import { accessDeviceService } from '../../services/accessDeviceService'
import type {
  AccessDevice,
  AccessDeviceCreatePayload,
  AccessDeviceOperationMode,
  AccessDeviceType,
} from '../../types/accessDevice'
import { hasRole } from '../../utils/permissions'
import { formatDate } from '../../utils/formatDate'

const initialFormState: AccessDeviceCreatePayload = {
  nome: '',
  tipo: 'GATEWAY',
  modoOperacao: 'HIBRIDO',
  identificadorExterno: '',
  fabricante: '',
  modelo: '',
  ipLocal: '',
  unidade: '',
}

export function DispositivosAcessoPage() {
  const { user } = useAuth()
  const canCreateDevice = hasRole(user, ['ADMIN'])
  const { getErrorMessage } = useApiError()
  const {
    data: devices,
    setData: setDevices,
    errorMessage,
    isLoading,
  } = useResourceList({ load: accessDeviceService.listar })
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [formState, setFormState] = useState<AccessDeviceCreatePayload>(initialFormState)
  const [formError, setFormError] = useState<string | null>(null)
  const [isSaving, setIsSaving] = useState(false)
  const [createdApiKey, setCreatedApiKey] = useState<string | null>(null)
  const [isApiKeyVisible, setIsApiKeyVisible] = useState(false)

  function closeForm() {
    setIsFormOpen(false)
    setFormError(null)
    setFormState(initialFormState)
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!formState.nome.trim()) {
      setFormError('Nome é obrigatório.')
      return
    }

    try {
      setIsSaving(true)
      setFormError(null)
      const created = await accessDeviceService.criar(formState)
      setDevices((current) => [created.dispositivo, ...current])
      setCreatedApiKey(created.apiKeyPlaintext)
      setIsApiKeyVisible(false)
      closeForm()
    } catch (error) {
      setFormError(getErrorMessage(error))
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Controle de acesso"
        title="Dispositivos de acesso"
        description="Acompanhe gateways e catracas cadastrados para a operação física."
        action={
          canCreateDevice ? (
            <button className="primary-button compact" type="button" onClick={() => setIsFormOpen(true)}>
              Novo dispositivo
            </button>
          ) : undefined
        }
      />

      <section className="content-panel">
        {isLoading && <StateMessage title="Carregando dispositivos..." />}

        {!isLoading && errorMessage && (
          <StateMessage title="Não foi possível carregar os dispositivos." description={errorMessage} />
        )}

        {!isLoading && !errorMessage && devices.length === 0 && (
          <StateMessage
            title="Nenhum dispositivo de acesso cadastrado."
            description="Cadastre um gateway ou catraca para acompanhar a operação."
          />
        )}

        {!isLoading && !errorMessage && devices.length > 0 && (
          <DataTable headers={['Nome', 'Tipo', 'Status', 'Modo', 'Última comunicação', 'Detalhes']}>
            {devices.map((device) => (
              <tr key={device.id}>
                <td>
                  <strong>{device.nome}</strong>
                  <div className="table-muted">{device.identificadorExterno || 'Sem identificador externo'}</div>
                </td>
                <td>{formatDeviceType(device.tipo)}</td>
                <td><StatusBadge status={device.status} /></td>
                <td>{formatMode(device.modoOperacao)}</td>
                <td>{formatLastSeen(device.ultimaComunicacaoEm)}</td>
                <td>{formatDeviceDetails(device)}</td>
              </tr>
            ))}
          </DataTable>
        )}
      </section>

      <Modal isOpen={isFormOpen} onClose={closeForm} title="Cadastrar dispositivo">
        <p className="modal-description">
          A API key será exibida uma única vez após a criação e deve ser configurada no ambiente do gateway.
        </p>
        <form onSubmit={handleSubmit}>
          <FormField label="Nome" required>
            <input value={formState.nome} onChange={(event) => updateField('nome', event.target.value, setFormState)} />
          </FormField>
          <FormField label="Tipo" required>
            <select value={formState.tipo} onChange={(event) => updateField('tipo', event.target.value as AccessDeviceType, setFormState)}>
              <option value="GATEWAY">Gateway</option>
              <option value="CATRACA_QR">Catraca QR</option>
              <option value="CATRACA_FACIAL">Catraca facial</option>
              <option value="RECEPCAO">Recepção</option>
              <option value="OUTRO">Outro</option>
            </select>
          </FormField>
          <FormField label="Modo de operação" required>
            <select value={formState.modoOperacao} onChange={(event) => updateField('modoOperacao', event.target.value as AccessDeviceOperationMode, setFormState)}>
              <option value="HIBRIDO">Híbrido</option>
              <option value="ONLINE">Online</option>
              <option value="OFFLINE">Offline</option>
            </select>
          </FormField>
          <FormField label="Identificador externo">
            <input value={formState.identificadorExterno ?? ''} onChange={(event) => updateField('identificadorExterno', event.target.value, setFormState)} />
          </FormField>
          <FormField label="Fabricante">
            <input value={formState.fabricante ?? ''} onChange={(event) => updateField('fabricante', event.target.value, setFormState)} />
          </FormField>
          <FormField label="Modelo">
            <input value={formState.modelo ?? ''} onChange={(event) => updateField('modelo', event.target.value, setFormState)} />
          </FormField>
          <FormField label="IP local">
            <input value={formState.ipLocal ?? ''} onChange={(event) => updateField('ipLocal', event.target.value, setFormState)} />
          </FormField>
          <FormField label="Unidade">
            <input value={formState.unidade ?? ''} onChange={(event) => updateField('unidade', event.target.value, setFormState)} />
          </FormField>

          {formError && <div className="field-error">{formError}</div>}

          <div className="form-actions">
            <button className="ghost-button" type="button" onClick={closeForm}>Cancelar</button>
            <button className="primary-button" type="submit" disabled={isSaving}>{isSaving ? 'Criando...' : 'Criar dispositivo'}</button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={Boolean(createdApiKey)} onClose={() => setCreatedApiKey(null)} title="API key gerada">
        <p className="modal-description">
          Copie esta chave agora para o ambiente seguro do gateway. Ela não será armazenada nem exibida novamente pelo frontend.
        </p>
        <div className="secret-box" aria-label="API key gerada">
          {isApiKeyVisible ? createdApiKey : '••••••••••••••••••••••••'}
        </div>
        <div className="form-actions">
          <button className="ghost-button" type="button" onClick={() => setIsApiKeyVisible((value) => !value)}>
            {isApiKeyVisible ? 'Mascarar' : 'Exibir'}
          </button>
          <button className="primary-button" type="button" onClick={() => setCreatedApiKey(null)}>
            Entendi
          </button>
        </div>
      </Modal>
    </>
  )
}

function updateField<TKey extends keyof AccessDeviceCreatePayload>(
  field: TKey,
  value: AccessDeviceCreatePayload[TKey],
  setFormState: Dispatch<SetStateAction<AccessDeviceCreatePayload>>,
) {
  setFormState((current) => ({ ...current, [field]: value }))
}

function formatLastSeen(value: string | null) {
  return value ? formatDate(value) : 'Última comunicação não registrada.'
}

function formatDeviceType(type: AccessDeviceType) {
  const labels: Record<AccessDeviceType, string> = {
    CATRACA_FACIAL: 'Catraca facial',
    CATRACA_QR: 'Catraca QR',
    RECEPCAO: 'Recepção',
    GATEWAY: 'Gateway',
    OUTRO: 'Outro',
  }
  return labels[type]
}

function formatMode(mode: AccessDeviceOperationMode) {
  const labels: Record<AccessDeviceOperationMode, string> = {
    HIBRIDO: 'Híbrido',
    ONLINE: 'Online',
    OFFLINE: 'Offline',
  }
  return labels[mode]
}

function formatDeviceDetails(device: AccessDevice) {
  const parts = [device.fabricante, device.modelo, device.ipLocal, device.unidade].filter(Boolean)
  return parts.length > 0 ? parts.join(' · ') : 'Sem detalhes adicionais'
}
