import {
  useCallback,
  useEffect,
  useRef,
  useState,
  type KeyboardEvent as ReactKeyboardEvent,
} from 'react'
import { useApiError } from '../../hooks/useApiError'
import { acessoService } from '../../services/acessoService'
import { alunoService } from '../../services/alunoService'
import { accessDeviceService } from '../../services/accessDeviceService'
import { accessEventService } from '../../services/accessEventService'
import { checkinService } from '../../services/checkinService'
import { HttpError } from '../../services/httpClient'
import { matriculaService } from '../../services/matriculaService'
import { pagamentoService } from '../../services/pagamentoService'
import type { AcessoResponse } from '../../types/acesso'
import type { Aluno, StatusAluno } from '../../types/aluno'
import type { Matricula } from '../../types/matricula'
import type { Pagamento } from '../../types/pagamento'
import { CatracaDeviceStatusPanel } from './CatracaDeviceStatusPanel'
import { CatracaEmptyState } from './CatracaEmptyState'
import type {
  DispositivoAcesso,
  DispositivoAcessoViewModel,
} from '../../types/accessDevice'
import type { EventoAcesso } from '../../types/accessEvent'

type CheckinStatus = 'idle' | 'registering' | 'registered' | 'error'

type StudentSnapshot = {
  aluno: Aluno | null
  matricula: Matricula | null
  pagamento: Pagamento | null
}

type CatracaResult =
  | {
      key: number
      type: 'loading'
    }
  | {
      key: number
      type: 'success'
      acesso: AcessoResponse
      snapshot: StudentSnapshot
      checkinStatus: CheckinStatus
      checkinMessage: string | null
    }
  | {
      key: number
      type: 'blocked'
      acesso: AcessoResponse
      snapshot: StudentSnapshot
    }
  | {
      key: number
      type: 'error'
      title: string
      description: string
    }

export function CatracaPage() {
  const [now, setNow] = useState(() => new Date())
  const [alunoId, setAlunoId] = useState('')
  const [result, setResult] = useState<CatracaResult | null>(null)
  const [countdown, setCountdown] = useState<number | null>(null)
  const [dispositivos, setDispositivos] = useState<DispositivoAcesso[]>([])
  const [selectedDeviceId, setSelectedDeviceId] = useState<number | null>(null)
  const [deviceError, setDeviceError] = useState<string | null>(null)
  const [recentEvents, setRecentEvents] = useState<EventoAcesso[]>([])
  const [eventsError, setEventsError] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)
  const resultRef = useRef<CatracaResult | null>(null)
  const { getErrorMessage } = useApiError()
  const resultKey = result?.key ?? null
  const resultType = result?.type ?? null
  const dispositivo =
    dispositivos.find((item) => item.id === selectedDeviceId) ??
    dispositivos[0] ??
    null
  const dispositivoViewModel: DispositivoAcessoViewModel | null = dispositivo
    ? {
        ...dispositivo,
        eventosPendentes: recentEvents.filter(
          (event) =>
            event.dispositivoId === dispositivo.id && !event.sincronizado,
        ).length,
      }
    : null
  const dispositivosOptions = dispositivos.filter((item) => item.id > 0)

  const clearResult = useCallback((nextValue = '') => {
    setResult(null)
    setCountdown(null)
    setAlunoId(nextValue)

    window.setTimeout(() => inputRef.current?.focus(), 0)
  }, [])

  useEffect(() => {
    inputRef.current?.focus()

    const intervalId = window.setInterval(() => setNow(new Date()), 1000)

    return () => window.clearInterval(intervalId)
  }, [])

  useEffect(() => {
    let mounted = true

    async function loadDevices() {
      try {
        const response = await accessDeviceService.listar()

        if (!mounted) {
          return
        }

        setDispositivos(response)
        setSelectedDeviceId((current) => current ?? response[0]?.id ?? null)
        setDeviceError(null)
      } catch {
        if (mounted) {
          setDeviceError('Não foi possível carregar os dispositivos de acesso.')
        }
      }
    }

    void loadDevices()

    return () => {
      mounted = false
    }
  }, [])

  useEffect(() => {
    let mounted = true

    async function loadRecentEvents() {
      try {
        const response = await accessEventService.listarHoje()

        if (mounted) {
          setRecentEvents(response.slice(0, 5))
          setEventsError(null)
        }
      } catch {
        if (mounted) {
          setEventsError('Não foi possível carregar os eventos de acesso.')
        }
      }
    }

    void loadRecentEvents()

    return () => {
      mounted = false
    }
  }, [])

  useEffect(() => {
    resultRef.current = result
  }, [result])

  useEffect(() => {
    if (!resultKey || !resultType || resultType === 'loading') {
      setCountdown(null)
      return
    }

    const seconds = getAutoClearSeconds(resultType)
    const expiresAt = Date.now() + seconds * 1000
    setCountdown(seconds)

    const intervalId = window.setInterval(() => {
      const remaining = Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000))
      setCountdown(remaining)
    }, 250)
    const timeoutId = window.setTimeout(() => clearResult(), seconds * 1000)

    return () => {
      window.clearInterval(intervalId)
      window.clearTimeout(timeoutId)
    }
  }, [clearResult, resultKey, resultType])

  useEffect(() => {
    function handleGlobalKeyDown(event: globalThis.KeyboardEvent) {
      const currentResult = resultRef.current

      if (!currentResult || currentResult.type === 'loading' || event.key === 'Tab') {
        return
      }

      event.preventDefault()

      const nextValue =
        event.key.length === 1 && /^\d$/.test(event.key) ? event.key : ''
      clearResult(nextValue)
    }

    window.addEventListener('keydown', handleGlobalKeyDown)

    return () => window.removeEventListener('keydown', handleGlobalKeyDown)
  }, [clearResult])

  async function validarAcesso() {
    const parsedAlunoId = Number(alunoId)

    if (!Number.isFinite(parsedAlunoId) || parsedAlunoId <= 0) {
      return
    }

    const currentResultKey = Date.now()

    try {
      setResult({ key: currentResultKey, type: 'loading' })
      setCountdown(null)

      const response = await acessoService.validar(parsedAlunoId)
      setAlunoId('')

      if (!response.acessoLiberado) {
        setResult({
          key: currentResultKey,
          type: 'blocked',
          acesso: response,
          snapshot: createEmptySnapshot(),
        })
        void carregarDadosAluno(currentResultKey, response)
        return
      }

      setResult({
        key: currentResultKey,
        type: 'success',
        acesso: response,
        snapshot: createEmptySnapshot(),
        checkinStatus: 'registering',
        checkinMessage: null,
      })

      void carregarDadosAluno(currentResultKey, response)
      void registrarCheckin(currentResultKey, response.alunoId)
    } catch (error) {
      setAlunoId('')

      setResult({
        key: currentResultKey,
        type: 'error',
        title:
          error instanceof HttpError && error.status === 404
            ? 'Aluno não encontrado'
            : 'Não foi possível validar o acesso',
        description:
          error instanceof HttpError && error.status === 404
            ? 'Nenhum aluno encontrado com os dados informados.'
            : 'Tente novamente em alguns instantes.',
      })
    }
  }

  async function carregarDadosAluno(
    currentResultKey: number,
    acesso: AcessoResponse,
  ) {
    const [alunoResult, matriculasResult, pagamentosResult] =
      await Promise.allSettled([
        alunoService.buscar(acesso.alunoId),
        matriculaService.listar(),
        pagamentoService.listar(),
      ])

    const aluno =
      alunoResult.status === 'fulfilled' ? alunoResult.value : null
    const matriculas =
      matriculasResult.status === 'fulfilled' ? matriculasResult.value : []
    const pagamentos =
      pagamentosResult.status === 'fulfilled' ? pagamentosResult.value : []
    const matricula =
      matriculas.find((item) => item.id === acesso.matriculaId) ??
      matriculas.find(
        (item) => item.alunoId === acesso.alunoId && item.status === 'ATIVA',
      ) ??
      null
    const pagamento = getLatestPayment(
      pagamentos.filter((item) => item.alunoId === acesso.alunoId),
      matricula?.id ?? acesso.matriculaId,
    )

    setResult((current) => {
      if (
        !current ||
        current.key !== currentResultKey ||
        (current.type !== 'success' && current.type !== 'blocked')
      ) {
        return current
      }

      return {
        ...current,
        snapshot: {
          aluno,
          matricula,
          pagamento,
        },
      }
    })
  }

  async function registrarCheckin(
    currentResultKey: number,
    alunoIdValue: number,
  ) {
    setResult((current) => {
      if (!current || current.key !== currentResultKey || current.type !== 'success') {
        return current
      }

      return {
        ...current,
        checkinStatus: 'registering',
        checkinMessage: null,
      }
    })

    try {
      await checkinService.registrar(alunoIdValue)

      setResult((current) => {
        if (!current || current.key !== currentResultKey || current.type !== 'success') {
          return current
        }

        return {
          ...current,
          checkinStatus: 'registered',
          checkinMessage: 'Check-in registrado.',
        }
      })
    } catch (error) {
      setResult((current) => {
        if (!current || current.key !== currentResultKey || current.type !== 'success') {
          return current
        }

        return {
          ...current,
          checkinStatus: 'error',
          checkinMessage: getErrorMessage(error),
        }
      })
    }
  }

  function handleInputKeyDown(event: ReactKeyboardEvent<HTMLInputElement>) {
    if (event.key !== 'Enter') {
      return
    }

    event.preventDefault()
    void validarAcesso()
  }

  const isLoading = result?.type === 'loading'
  const formattedTime = new Intl.DateTimeFormat('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  }).format(now)
  const formattedDate = new Intl.DateTimeFormat('pt-BR', {
    weekday: 'long',
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  }).format(now)

  return (
    <div className="catraca-page">
      <header className="catraca-topbar">
        <div className="catraca-brand">
          <span className="brand-mark">EG</span>
          <span>Controle de Acesso</span>
        </div>

        <div className="catraca-topbar__status">
          <div className="catraca-clock">
            <div className="catraca-clock-time">{formattedTime}</div>
            <div className="catraca-clock-date">{formattedDate}</div>
          </div>
          <span className="catraca-system-status">
            <span className="system-active-dot" aria-hidden="true" />
            Sistema ativo
          </span>
        </div>
      </header>

      <main className="catraca-body">
        {dispositivosOptions.length > 1 && (
          <label className="catraca-device-select">
            <span>Dispositivo da sessão</span>
            <select
              value={selectedDeviceId ?? ''}
              onChange={(event) => setSelectedDeviceId(Number(event.target.value))}
            >
              {dispositivosOptions.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.nome}
                </option>
              ))}
            </select>
          </label>
        )}

        {deviceError && (
          <section className="catraca-device-panel" aria-label="Falha ao carregar dispositivo">
            <h2>Dispositivo indisponível</h2>
            <p>{deviceError}</p>
          </section>
        )}

        <CatracaDeviceStatusPanel dispositivo={dispositivoViewModel} />

        <section className="catraca-input-card" aria-label="Identificação">
          <div className="catraca-input-copy">
            <h1>Identificação do aluno</h1>
            <p>Informe o ID do aluno para validação manual autorizada.</p>
          </div>

          <input
            ref={inputRef}
            type="number"
            min="1"
            inputMode="numeric"
            className="catraca-input-large"
            value={alunoId}
            disabled={isLoading}
            onChange={(event) => setAlunoId(event.target.value)}
            onKeyDown={handleInputKeyDown}
            autoFocus
            aria-label="ID do aluno"
          />

          <button
            type="button"
            className="catraca-btn-large"
            disabled={isLoading || !alunoId}
            onClick={() => void validarAcesso()}
          >
            {isLoading ? 'Verificando...' : 'Verificar acesso'}
          </button>

          <p className="catraca-key-hint">Pressione Enter para confirmar</p>
        </section>

        {result ? (
          <ResultPanel
            result={result}
            countdown={countdown}
            onRegisterCheckin={() => {
              if (result.type === 'success') {
                void registrarCheckin(result.key, result.acesso.alunoId)
              }
            }}
          />
        ) : (
          <CatracaEmptyState />
        )}

        <RecentAccessEvents events={recentEvents} error={eventsError} />
      </main>
    </div>
  )
}

function RecentAccessEvents({
  events,
  error,
}: {
  events: EventoAcesso[]
  error: string | null
}) {
  return (
    <section className="catraca-device-panel" aria-label="Eventos recentes de acesso">
      <div className="catraca-device-header">
        <div>
          <span className="overview-label">Eventos</span>
          <h2>Acessos de hoje</h2>
        </div>
      </div>

      {error && <p className="catraca-device-warning">{error}</p>}

      {!error && events.length === 0 && (
        <p>Os acessos liberados ou bloqueados aparecerão aqui.</p>
      )}

      {!error && events.length > 0 && (
        <div className="catraca-events-list">
          {events.map((event) => (
            <article key={event.id} className="catraca-event-item">
              <div>
                <strong>{event.alunoNome}</strong>
                <span>{event.motivo}</span>
              </div>
              <span className={`catraca-device-status is-${event.resultado.toLowerCase()}`}>
                {event.resultadoLabel}
              </span>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}

type ResultPanelProps = {
  result: CatracaResult
  countdown: number | null
  onRegisterCheckin: () => void
}

function ResultPanel({
  result,
  countdown,
  onRegisterCheckin,
}: ResultPanelProps) {
  if (result.type === 'loading') {
    return (
      <section className="catraca-result catraca-result--loading">
        <div className="spinner" aria-hidden="true" />
        <p className="catraca-loading-text">Validando acesso...</p>
        <p className="catraca-hint">Consultando a situação do aluno.</p>
      </section>
    )
  }

  if (result.type === 'success') {
    return (
      <section className="catraca-result is-liberado">
        <div className="catraca-icon ok" aria-hidden="true">
          &#10003;
        </div>
        <h2 className="catraca-result-title ok">Acesso liberado.</h2>
        <p className="catraca-hint">Entrada registrada com sucesso.</p>

        <StudentCard
          acesso={result.acesso}
          snapshot={result.snapshot}
          tone="success"
        />

        <button
          type="button"
          className="catraca-btn-large"
          disabled={
            result.checkinStatus === 'registering' ||
            result.checkinStatus === 'registered'
          }
          onClick={onRegisterCheckin}
        >
          {getCheckinButtonLabel(result.checkinStatus)}
        </button>

        {result.checkinMessage && (
          <p className="catraca-hint">{result.checkinMessage}</p>
        )}

        <Countdown seconds={countdown} />
      </section>
    )
  }

  if (result.type === 'blocked') {
    return (
      <section className="catraca-result is-bloqueado">
        <div className="catraca-icon block" aria-hidden="true">
          &#10005;
        </div>
        <h2 className="catraca-result-title block">Acesso bloqueado.</h2>
        <StudentCard
          acesso={result.acesso}
          snapshot={result.snapshot}
          tone="blocked"
        />
        <div className="catraca-motivo">
          Motivo: {result.acesso.motivo || 'não informado pela API.'}
        </div>
        <p className="catraca-hint">{getActionHint(result.acesso.motivo)}</p>
        <Countdown seconds={countdown} />
      </section>
    )
  }

  return (
    <section className="catraca-result is-erro">
      <div className="catraca-icon warn" aria-hidden="true">
        !
      </div>
      <h2 className="catraca-result-title warn">{result.title}</h2>
      <p className="catraca-hint">{result.description}</p>
      <Countdown seconds={countdown} />
    </section>
  )
}

type StudentCardProps = {
  acesso: AcessoResponse
  snapshot: StudentSnapshot
  tone: 'success' | 'blocked'
}

function StudentCard({ acesso, snapshot, tone }: StudentCardProps) {
  const { aluno, matricula, pagamento } = snapshot
  const status = aluno?.status ?? 'ATIVO'
  const alunoNome = acesso.alunoNome || aluno?.nome || 'Aluno sem nome informado'
  const whatsappUrl = aluno
    ? getWhatsAppUrl(aluno, acesso, matricula, pagamento)
    : null

  return (
    <div className="student-card student-card--detailed">
      <span className="student-avatar" aria-hidden="true">
        {getInitials(alunoNome)}
      </span>

      <div className="student-info">
        <div className="student-info-header">
          <div>
            <div className="student-name">{alunoNome}</div>
            <div className="student-email">
              {aluno?.email ?? 'Email indisponível'}
            </div>
          </div>
          <span className={`status-badge ${getStatusClass(status)}`}>
            {formatStatus(status)}
          </span>
        </div>

        <div className="student-meta-grid">
          <InfoItem label="Telefone" value={aluno?.telefone ?? 'Indisponível'} />
          <InfoItem
            label="Cadastro"
            value={aluno ? formatDateTime(aluno.dataCadastro) : 'Indisponível'}
          />
          <InfoItem
            label="Matrícula"
            value={
              matricula
                ? `#${matricula.id} - ${getMatriculaPlanoName(matricula)}`
                : 'Sem matrícula ativa'
            }
          />
          <InfoItem
            label="Vigência"
            value={
              matricula
                ? `${formatOptionalDateOnly(
                    matricula.dataInicio,
                  )} até ${formatOptionalDateOnly(
                    getMatriculaEndDate(matricula),
                  )}`
                : acesso.dataValidadeMatricula
                  ? `Válido até ${formatDateOnly(acesso.dataValidadeMatricula)}`
                  : '-'
            }
          />
          <InfoItem
            label="Pagamento"
            value={
              pagamento
                ? `${formatMoney(pagamento.valor)} - ${formatStatus(
                    pagamento.status,
                  )}`
                : 'Sem pagamento registrado'
            }
          />
          <InfoItem
            label="Data pagamento"
            value={
              pagamento
                ? formatOptionalDateTime(getPaymentDate(pagamento))
                : '-'
            }
          />
        </div>

        <div className="student-contact-actions">
          <span className={`student-access-chip is-${tone}`}>
            {tone === 'success' ? 'Entrada autorizada' : 'Precisa atendimento'}
          </span>
          {whatsappUrl ? (
            <a
              className="student-whatsapp-link"
              href={whatsappUrl}
              target="_blank"
              rel="noreferrer"
            >
              Enviar WhatsApp
            </a>
          ) : (
            <span className="student-whatsapp-link is-disabled">
              WhatsApp indisponível
            </span>
          )}
        </div>
      </div>
    </div>
  )
}

function InfoItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="student-meta-item">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  )
}

function Countdown({ seconds }: { seconds: number | null }) {
  if (seconds === null) {
    return null
  }

  return <p className="catraca-countdown">Limpando em {seconds}...</p>
}

function getCheckinButtonLabel(status: CheckinStatus) {
  if (status === 'registering') {
    return 'Registrando...'
  }

  if (status === 'registered') {
    return 'Registrado OK'
  }

  return 'Registrar check-in'
}

function getAutoClearSeconds(type: Exclude<CatracaResult['type'], 'loading'>) {
  if (type === 'success') {
    return 10
  }

  if (type === 'blocked') {
    return 12
  }

  return 5
}

function getActionHint(motivo: string) {
  const normalizedMotivo = motivo
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()

  if (
    normalizedMotivo.includes('inadimplente') ||
    normalizedMotivo.includes('pagamento')
  ) {
    return 'Direcione o aluno para regularizar o pagamento.'
  }

  if (normalizedMotivo.includes('bloqueado')) {
    return 'Contate o administrador para liberar o cadastro.'
  }

  if (normalizedMotivo.includes('sem matricula')) {
    return 'Contate a recepção para realizar a matrícula.'
  }

  return 'Direcione o aluno para a recepção.'
}

function createEmptySnapshot(): StudentSnapshot {
  return {
    aluno: null,
    matricula: null,
    pagamento: null,
  }
}

function getLatestPayment(pagamentos: Pagamento[], matriculaId: number | null) {
  const scopedPagamentos = matriculaId
    ? pagamentos.filter((pagamento) => pagamento.matriculaId === matriculaId)
    : pagamentos

  return (
    [...scopedPagamentos].sort((first, second) =>
      getPaymentDate(second).localeCompare(getPaymentDate(first)),
    )[0] ?? null
  )
}

function getPaymentDate(pagamento: Pagamento) {
  return (
    pagamento.dataPagamento ??
    pagamento.dataCadastro ??
    pagamento.dataVencimento ??
    ''
  )
}

function getWhatsAppUrl(
  aluno: Aluno,
  acesso: AcessoResponse,
  matricula: Matricula | null,
  pagamento: Pagamento | null,
) {
  const phoneDigits = aluno.telefone.replace(/\D/g, '')

  if (phoneDigits.length < 10) {
    return null
  }

  const whatsappPhone = phoneDigits.startsWith('55')
    ? phoneDigits
    : `55${phoneDigits}`
  const message = [
    `Olá, ${aluno.nome}. Aqui é da ExtremeGym.`,
    acesso.acessoLiberado
      ? 'Seu acesso foi liberado na catraca.'
      : `Seu acesso precisa de atendimento: ${acesso.motivo}.`,
    matricula
      ? `Matrícula ${getMatriculaPlanoName(
          matricula,
        )}, válida até ${formatOptionalDateOnly(
          getMatriculaEndDate(matricula),
        )}.`
      : 'Não encontramos matrícula ativa no momento.',
    pagamento
      ? `Último pagamento: ${formatMoney(pagamento.valor)} em ${formatOptionalDateTime(
          getPaymentDate(pagamento),
        )}.`
      : 'Não localizamos pagamento registrado para a matrícula atual.',
  ].join('\n')

  return `https://wa.me/${whatsappPhone}?text=${encodeURIComponent(message)}`
}

function getInitials(name: string) {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('')
}

function getStatusClass(status: StatusAluno) {
  return `is-${status.toLowerCase()}`
}

function formatStatus(status: string) {
  return status.replace('_', ' ')
}

function formatDateOnly(value: string) {
  const [year, month, day] = value.split('-')

  if (!year || !month || !day) {
    return value
  }

  return `${day}/${month}/${year}`
}

function formatOptionalDateOnly(value?: string | null) {
  return value ? formatDateOnly(value) : '-'
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value))
}

function formatOptionalDateTime(value?: string | null) {
  return value ? formatDateTime(value) : '-'
}

function getMatriculaEndDate(matricula: Matricula) {
  return matricula.dataFim ?? matricula.dataVencimento
}

function getMatriculaPlanoName(matricula: Matricula) {
  return matricula.planoNome ?? matricula.plano?.nome ?? 'Plano ativo'
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value)
}
