import { useEffect, useState } from 'react'
import { ConfirmDialog } from '../ui/ConfirmDialog'
import { StateMessage } from '../ui/StateMessage'
import { StatusBadge } from '../ui/StatusBadge'
import { useApiError } from '../../hooks/useApiError'
import { accessCredentialService } from '../../services/accessCredentialService'
import type { CredencialAcesso } from '../../types/accessCredential'
import { hasRole } from '../../utils/permissions'
import { useAuth } from '../../hooks/useAuth'

type CredenciaisAcessoAlunoProps = {
  alunoId: number
}

export function CredenciaisAcessoAluno({ alunoId }: CredenciaisAcessoAlunoProps) {
  const [credenciais, setCredenciais] = useState<CredencialAcesso[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [credentialToRevoke, setCredentialToRevoke] = useState<CredencialAcesso | null>(null)
  const [isRevoking, setIsRevoking] = useState(false)
  const { getErrorMessage } = useApiError()
  const { user } = useAuth()
  const canRevoke = hasRole(user, ['ADMIN', 'RECEPCAO'])

  useEffect(() => {
    let mounted = true

    async function loadCredentials() {
      try {
        setIsLoading(true)
        const response = await accessCredentialService.listarPorAluno(alunoId)

        if (mounted) {
          setCredenciais(response)
          setError(null)
        }
      } catch (loadError) {
        if (mounted) {
          setError(getErrorMessage(loadError))
        }
      } finally {
        if (mounted) {
          setIsLoading(false)
        }
      }
    }

    void loadCredentials()

    return () => {
      mounted = false
    }
  }, [alunoId, getErrorMessage])

  async function revokeCredential() {
    if (!credentialToRevoke) {
      return
    }

    try {
      setIsRevoking(true)
      const revoked = await accessCredentialService.revogar(credentialToRevoke.id)
      setCredenciais((current) =>
        current.map((item) => (item.id === revoked.id ? revoked : item)),
      )
      setMessage('Credencial revogada com sucesso.')
      setCredentialToRevoke(null)
    } catch (revokeError) {
      setError(getErrorMessage(revokeError))
    } finally {
      setIsRevoking(false)
    }
  }

  return (
    <section className="content-panel access-credentials-panel">
      <div className="access-section-header">
        <div>
          <span className="overview-label">Controle de acesso</span>
          <h2>Credenciais de acesso</h2>
        </div>
      </div>

      {message && <StateMessage title="Dados atualizados" description={message} />}
      {isLoading && <StateMessage title="Carregando credenciais de acesso..." />}
      {!isLoading && error && (
        <StateMessage title="Não foi possível carregar as credenciais." description={error} />
      )}
      {!isLoading && !error && credenciais.length === 0 && (
        <StateMessage
          title="Nenhuma credencial de acesso cadastrada."
          description="A credencial permite identificar o aluno em dispositivos compatíveis."
        />
      )}
      {!isLoading && !error && credenciais.length > 0 && (
        <div className="access-credential-list">
          {credenciais.map((credencial) => (
            <article key={credencial.id} className="access-credential-item">
              <div>
                <strong>{credencial.tipoLabel}</strong>
                <span>{credencial.identificadorExternoMascarado}</span>
                <small>{credencial.fornecedor}</small>
              </div>
              <dl>
                <div>
                  <dt>Status</dt>
                  <dd><StatusBadge status={credencial.status}>{credencial.statusLabel}</StatusBadge></dd>
                </div>
                <div>
                  <dt>Cadastrada</dt>
                  <dd>{credencial.cadastradoEmLabel}</dd>
                </div>
                <div>
                  <dt>Revogada</dt>
                  <dd>{credencial.revogadoEmLabel}</dd>
                </div>
                <div>
                  <dt>Termo</dt>
                  <dd>{credencial.versaoTermo} · {credencial.termoAceitoEmLabel}</dd>
                </div>
              </dl>
              <button
                type="button"
                className="ghost-button compact"
                disabled={!canRevoke || credencial.status === 'REVOGADA'}
                onClick={() => setCredentialToRevoke(credencial)}
              >
                Revogar credencial
              </button>
            </article>
          ))}
        </div>
      )}

      <ConfirmDialog
        isOpen={Boolean(credentialToRevoke)}
        title="Revogar credencial"
        description="A credencial deixará de identificar o aluno em dispositivos compatíveis."
        confirmLabel={isRevoking ? 'Revogando...' : 'Revogar credencial'}
        cancelLabel="Cancelar"
        onCancel={() => setCredentialToRevoke(null)}
        onConfirm={() => void revokeCredential()}
        isLoading={isRevoking}
      />
    </section>
  )
}
