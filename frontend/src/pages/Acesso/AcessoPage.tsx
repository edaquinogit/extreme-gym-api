import { useCallback, useEffect, useMemo, useRef, useState, type FormEvent } from 'react'
import { FormField } from '../../components/ui/FormField'
import { PageHeader } from '../../components/ui/PageHeader'
import { StateMessage } from '../../components/ui/StateMessage'
import { StatusBadge } from '../../components/ui/StatusBadge'
import { useApiError } from '../../hooks/useApiError'
import { acessoService } from '../../services/acessoService'
import { checkinService } from '../../services/checkinService'
import { HttpError } from '../../services/httpClient'
import type { AcessoResponse } from '../../types/acesso'
import type { Checkin } from '../../types/checkin'
import { formatDate } from '../../utils/formatDate'

type AccessErrorState = {
  kind: 'not-found' | 'api'
  message: string
}

export function AcessoPage() {
  const [alunoId, setAlunoId] = useState('')
  const [resultado, setResultado] = useState<AcessoResponse | null>(null)
  const [errorState, setErrorState] = useState<AccessErrorState | null>(null)
  const [checkinMessage, setCheckinMessage] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isRegisteringCheckin, setIsRegisteringCheckin] = useState(false)
  const [recentCheckins, setRecentCheckins] = useState<Checkin[]>([])
  const [recentCheckinsError, setRecentCheckinsError] = useState<string | null>(null)
  const [isLoadingRecentCheckins, setIsLoadingRecentCheckins] = useState(true)
  const inputRef = useRef<HTMLInputElement>(null)
  const hasResult = Boolean(resultado || errorState || isLoading)
  const { getErrorMessage } = useApiError()
  const trimmedAlunoId = alunoId.trim()
  const canValidate = trimmedAlunoId.length > 0 && !isLoading
  const latestCheckins = useMemo(() => recentCheckins.slice(0, 5), [recentCheckins])

  const loadRecentCheckins = useCallback(async () => {
    try {
      setIsLoadingRecentCheckins(true)
      setRecentCheckinsError(null)
      const response = await checkinService.listar()
      setRecentCheckins(response.slice(0, 5))
    } catch (error) {
      setRecentCheckinsError(getErrorMessage(error))
    } finally {
      setIsLoadingRecentCheckins(false)
    }
  }, [getErrorMessage])

  useEffect(() => {
    let mounted = true

    async function loadInitialRecentCheckins() {
      try {
        const response = await checkinService.listar()

        if (mounted) {
          setRecentCheckins(response.slice(0, 5))
        }
      } catch (error) {
        if (mounted) {
          setRecentCheckinsError(getErrorMessage(error))
        }
      } finally {
        if (mounted) {
          setIsLoadingRecentCheckins(false)
        }
      }
    }

    void loadInitialRecentCheckins()

    return () => {
      mounted = false
    }
  }, [getErrorMessage])

  async function validarAcesso(event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault()

    if (!canValidate) {
      return
    }

    try {
      setIsLoading(true)
      setResultado(null)
      setErrorState(null)
      setCheckinMessage(null)
      const response = await acessoService.validar(Number(trimmedAlunoId))
      setResultado(response)
    } catch (error) {
      setErrorState(getAccessErrorState(error))
    } finally {
      setIsLoading(false)
      window.setTimeout(() => inputRef.current?.focus(), 0)
    }
  }

  async function registrarCheckin() {
    if (!resultado) {
      return
    }

    try {
      setIsRegisteringCheckin(true)
      setCheckinMessage(null)
      const response = await checkinService.registrar(resultado.alunoId)
      setCheckinMessage(
        response.status === 'AUTORIZADO'
          ? `Check-in registrado com sucesso para ${response.alunoNome ?? resultado.alunoNome}.`
          : response.motivoBloqueio ?? 'Check-in registrado com acesso bloqueado.',
      )
      await loadRecentCheckins()
    } catch (error) {
      setCheckinMessage(getErrorMessage(error))
    } finally {
      setIsRegisteringCheckin(false)
      window.setTimeout(() => inputRef.current?.focus(), 0)
    }
  }

  return (
    <div className="acesso-page">
      <PageHeader
        eyebrow="Recepção"
        title="Validar acesso"
        description="Informe o ID do aluno para consultar a permissão de entrada e registrar check-in quando o acesso estiver liberado."
      />

      <section className="acesso-card content-panel">
        <form className="acesso-form" onSubmit={(event) => void validarAcesso(event)}>
          <FormField label="ID do aluno" hint="Use o identificador numérico do aluno. A busca por nome, telefone ou e-mail depende de endpoint ainda não disponível." required>
            <input
              ref={inputRef}
              type="number"
              min="1"
              value={alunoId}
              autoFocus
              className="input-lg"
              placeholder="Informe o ID do aluno"
              onChange={(event) => setAlunoId(event.target.value)}
            />
          </FormField>

          <button
            type="submit"
            className="primary-button button-full button-lg"
            disabled={!canValidate}
          >
            {isLoading ? 'Validando acesso...' : 'Validar acesso'}
          </button>
        </form>

        {hasResult && (
          <div
            className={`acesso-result ${
              resultado?.acessoLiberado ? 'is-liberado' : 'is-bloqueado'
            }`}
            aria-live="polite"
          >
            {isLoading && (
              <>
                <p className="acesso-result-title">Validando acesso...</p>
                <p>Consultando a situação do aluno.</p>
              </>
            )}

            {!isLoading && errorState && (
              <>
                <p className="acesso-result-title">
                  {errorState.kind === 'not-found' ? 'Aluno não encontrado' : 'Não foi possível validar o acesso'}
                </p>
                <p>{errorState.message}</p>
              </>
            )}

            {!isLoading && resultado && (
              <>
                <p className="acesso-result-title">
                  {resultado.acessoLiberado
                    ? 'Acesso liberado.'
                    : 'Acesso bloqueado.'}
                </p>
                <div className="result-details">
                  <strong>{resultado.alunoNome || 'Aluno sem nome informado'}</strong>
                  {resultado.matriculaId && <span>Matrícula #{resultado.matriculaId}</span>}
                  <span>{resultado.motivo || 'Motivo não informado pela API.'}</span>
                  {resultado.acessoLiberado &&
                    resultado.dataValidadeMatricula && (
                      <span>
                        Válido até{' '}
                        {formatDateOnly(resultado.dataValidadeMatricula)}
                      </span>
                    )}
                </div>

                {resultado.acessoLiberado && (
                  <button
                    type="button"
                    className="primary-button button-full"
                    disabled={isRegisteringCheckin}
                    onClick={() => void registrarCheckin()}
                  >
                    {isRegisteringCheckin ? 'Salvando...' : 'Registrar check-in'}
                  </button>
                )}

                {checkinMessage && (
                  <p className="field-hint acesso-checkin-message">
                    {checkinMessage}
                  </p>
                )}
              </>
            )}
          </div>
        )}
      </section>

      <section className="content-panel acesso-recent-panel" aria-label="Histórico recente de check-ins">
        <div className="acesso-recent-header">
          <div>
            <span className="overview-label">Histórico recente</span>
            <h2>Últimos check-ins</h2>
          </div>
          <button type="button" className="ghost-button compact" onClick={() => void loadRecentCheckins()}>
            Atualizar
          </button>
        </div>

        {isLoadingRecentCheckins && <StateMessage title="Carregando check-ins..." />}

        {!isLoadingRecentCheckins && recentCheckinsError && (
          <StateMessage
            title="Não foi possível carregar o histórico recente."
            description={recentCheckinsError}
          />
        )}

        {!isLoadingRecentCheckins && !recentCheckinsError && latestCheckins.length === 0 && (
          <StateMessage
            title="Nenhum check-in registrado."
            description="Os últimos acessos aparecerão aqui assim que forem registrados pela API."
          />
        )}

        {!isLoadingRecentCheckins && !recentCheckinsError && latestCheckins.length > 0 && (
          <div className="acesso-recent-list">
            {latestCheckins.map((checkin) => (
              <article key={checkin.id} className="acesso-recent-item">
                <div>
                  <strong>{checkin.alunoNome ?? checkin.aluno?.nome ?? 'Aluno sem nome'}</strong>
                  <span>{formatDate(checkin.dataHora)}</span>
                </div>
                <StatusBadge status={checkin.status} />
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}

function getAccessErrorState(error: unknown): AccessErrorState {
  if (error instanceof HttpError && error.status === 404) {
    return {
      kind: 'not-found',
      message: 'Nenhum aluno encontrado com os dados informados.',
    }
  }

  return {
    kind: 'api',
    message: 'Não foi possível validar o acesso agora. Tente novamente em alguns instantes.',
  }
}

function formatDateOnly(value: string) {
  const [year, month, day] = value.split('-')

  if (!year || !month || !day) {
    return value
  }

  return `${day}/${month}/${year}`
}
