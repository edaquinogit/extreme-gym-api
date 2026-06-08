import { type FormEvent, useEffect, useState } from 'react'
import { useApiError } from '../../hooks/useApiError'
import { useAuth } from '../../hooks/useAuth'
import { accessCredentialService } from '../../services/accessCredentialService'
import type {
  AccessCredential,
  AccessCredentialCreatePayload,
  AccessCredentialType,
} from '../../types/accessCredential'
import { formatDate } from '../../utils/formatDate'
import { hasRole } from '../../utils/permissions'
import { FormField } from '../ui/FormField'
import { StateMessage } from '../ui/StateMessage'
import { StatusBadge } from '../ui/StatusBadge'

type CredenciaisAcessoAlunoProps = {
  alunoId: number
}

const initialFormState: AccessCredentialCreatePayload = {
  tipo: 'QR_CODE',
  identificadorExterno: '',
  fornecedor: '',
  termoAceitoEm: '',
  versaoTermo: '',
}

export function CredenciaisAcessoAluno({ alunoId }: CredenciaisAcessoAlunoProps) {
  const { user } = useAuth()
  const { getErrorMessage } = useApiError()
  const canManageCredentials = hasRole(user, ['ADMIN', 'RECEPCAO'])
  const [credentials, setCredentials] = useState<AccessCredential[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [formState, setFormState] = useState<AccessCredentialCreatePayload>(initialFormState)
  const [formError, setFormError] = useState<string | null>(null)
  const [isSaving, setIsSaving] = useState(false)
  const [revokingId, setRevokingId] = useState<number | null>(null)

  useEffect(() => {
    let isMounted = true

    async function loadCredentials() {
      try {
        setIsLoading(true)
        setErrorMessage(null)
        const data = await accessCredentialService.listarPorAluno(alunoId)
        if (isMounted) {
          setCredentials(data)
        }
      } catch (error) {
        if (isMounted) {
          setErrorMessage(getErrorMessage(error))
        }
      } finally {
        if (isMounted) {
          setIsLoading(false)
        }
      }
    }

    void loadCredentials()

    return () => {
      isMounted = false
    }
  }, [alunoId, getErrorMessage])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!canManageCredentials) {
      return
    }

    if (!formState.identificadorExterno.trim()) {
      setFormError('Identificador externo é obrigatório.')
      return
    }

    try {
      setIsSaving(true)
      setFormError(null)
      const created = await accessCredentialService.criar(alunoId, formState)
      setCredentials((current) => [created, ...current])
      setFormState(initialFormState)
    } catch (error) {
      setFormError(getErrorMessage(error))
    } finally {
      setIsSaving(false)
    }
  }

  async function revokeCredential(credential: AccessCredential) {
    if (!canManageCredentials || credential.status === 'REVOGADA') {
      return
    }

    try {
      setRevokingId(credential.id)
      setErrorMessage(null)
      const revoked = await accessCredentialService.revogar(credential.id)
      setCredentials((current) =>
        current.map((item) => (item.id === revoked.id ? revoked : item)),
      )
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    } finally {
      setRevokingId(null)
    }
  }

  return (
    <div className="credential-manager">
      {isLoading && <StateMessage title="Carregando credenciais de acesso..." />}

      {!isLoading && errorMessage && (
        <StateMessage title="Não foi possível carregar as credenciais." description={errorMessage} />
      )}

      {!isLoading && !errorMessage && credentials.length === 0 && (
        <StateMessage
          title="Nenhuma credencial de acesso cadastrada."
          description="Credenciais QR, PIN ou cartão serão exibidas aqui."
        />
      )}

      {!isLoading && !errorMessage && credentials.length > 0 && (
        <div className="credential-list" aria-label="Credenciais de acesso">
          {credentials.map((credential) => (
            <div className="credential-row" key={credential.id}>
              <div>
                <strong>{formatCredentialType(credential.tipo)}</strong>
                <span>{credential.identificadorExternoMascarado}</span>
              </div>
              <div>
                <span>{credential.fornecedor}</span>
                <small>{formatRegisteredDate(credential.cadastradoEm)}</small>
              </div>
              <StatusBadge status={credential.status} />
              {canManageCredentials && (
                <button
                  className="ghost-button compact"
                  type="button"
                  disabled={credential.status === 'REVOGADA' || revokingId === credential.id}
                  onClick={() => void revokeCredential(credential)}
                >
                  {revokingId === credential.id ? 'Revogando...' : 'Revogar'}
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      {canManageCredentials && (
        <form className="credential-form" onSubmit={handleSubmit}>
          <FormField label="Tipo" required>
            <select
              value={formState.tipo}
              onChange={(event) =>
                setFormState((current) => ({
                  ...current,
                  tipo: event.target.value as AccessCredentialType,
                }))
              }
            >
              <option value="QR_CODE">QR Code</option>
              <option value="PIN">PIN</option>
              <option value="CARTAO">Cartão</option>
            </select>
          </FormField>
          <FormField label="Identificador externo" required>
            <input
              value={formState.identificadorExterno}
              onChange={(event) =>
                setFormState((current) => ({
                  ...current,
                  identificadorExterno: event.target.value,
                }))
              }
            />
          </FormField>
          <FormField label="Fornecedor">
            <input
              value={formState.fornecedor ?? ''}
              onChange={(event) =>
                setFormState((current) => ({ ...current, fornecedor: event.target.value }))
              }
            />
          </FormField>
          <FormField label="Versão do termo">
            <input
              value={formState.versaoTermo ?? ''}
              onChange={(event) =>
                setFormState((current) => ({ ...current, versaoTermo: event.target.value }))
              }
            />
          </FormField>

          {formError && <div className="field-error">{formError}</div>}

          <div className="form-actions">
            <button className="primary-button" type="submit" disabled={isSaving}>
              {isSaving ? 'Salvando...' : 'Cadastrar credencial'}
            </button>
          </div>
        </form>
      )}
    </div>
  )
}

function formatCredentialType(type: AccessCredentialType) {
  const labels: Record<AccessCredentialType, string> = {
    CARTAO: 'Cartão',
    FACE_TEMPLATE: 'Template facial',
    PIN: 'PIN',
    QR_CODE: 'QR Code',
  }
  return labels[type]
}

function formatRegisteredDate(value: string) {
  return value ? formatDate(value) : 'Cadastro não informado'
}
