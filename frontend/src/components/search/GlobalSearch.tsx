import { useEffect, useMemo, useRef, useState } from 'react'
import { appPaths } from '../../app/routes/paths'
import { navigateTo } from '../../app/routes/router'
import { globalSearchService } from '../../services/globalSearchService'
import type {
  GlobalSearchGroup,
  GlobalSearchItem,
  GlobalSearchResult,
} from '../../types/globalSearch'

const MIN_SEARCH_LENGTH = 3
const SEARCH_DELAY_MS = 350

const emptyResult: GlobalSearchResult = {
  alunos: [],
  matriculas: [],
  pagamentos: [],
  planos: [],
}

const groupLabels: Record<GlobalSearchGroup, string> = {
  alunos: 'Alunos',
  matriculas: 'Matrículas',
  pagamentos: 'Pagamentos',
  planos: 'Planos',
}

const knownRoutes = new Set<string>(Object.values(appPaths))

export function GlobalSearch() {
  const [term, setTerm] = useState('')
  const [result, setResult] = useState<GlobalSearchResult>(emptyResult)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isOpen, setIsOpen] = useState(false)
  const containerRef = useRef<HTMLDivElement>(null)
  const trimmedTerm = term.trim()
  const showMinHint = trimmedTerm.length > 0 && trimmedTerm.length < MIN_SEARCH_LENGTH
  const hasResults = Object.values(result).some((items) => items.length > 0)
  const groups = useMemo(
    () =>
      (Object.keys(groupLabels) as GlobalSearchGroup[])
        .map((group) => ({ group, items: result[group] }))
        .filter(({ items }) => items.length > 0),
    [result],
  )

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (!containerRef.current?.contains(event.target as Node)) {
        setIsOpen(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  useEffect(() => {
    if (trimmedTerm.length < MIN_SEARCH_LENGTH) {
      return
    }

    let cancelled = false
    const timeoutId = window.setTimeout(async () => {
      try {
        setIsLoading(true)
        setError(null)
        const response = await globalSearchService.buscar(trimmedTerm)

        if (!cancelled) {
          setResult(response)
          setIsOpen(true)
        }
      } catch {
        if (!cancelled) {
          setError('Não foi possível buscar agora.')
          setResult(emptyResult)
          setIsOpen(true)
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false)
        }
      }
    }, SEARCH_DELAY_MS)

    return () => {
      cancelled = true
      window.clearTimeout(timeoutId)
    }
  }, [trimmedTerm])

  function handleItemClick(item: GlobalSearchItem) {
    if (!item.rota || !knownRoutes.has(item.rota)) {
      return
    }

    navigateTo(item.rota)
    setIsOpen(false)
    setTerm('')
    setResult(emptyResult)
  }

  return (
    <div className="global-search" ref={containerRef}>
      <label className="global-search-field" aria-label="Busca global">
        <span aria-hidden="true">⌕</span>
        <input
          placeholder="Buscar alunos, matrículas, pagamentos ou planos"
          type="search"
          value={term}
          onFocus={() => setIsOpen(true)}
          onChange={(event) => {
            const nextTerm = event.target.value
            setTerm(nextTerm)
            if (nextTerm.trim().length < MIN_SEARCH_LENGTH) {
              setResult(emptyResult)
              setError(null)
              setIsLoading(false)
            }
            setIsOpen(true)
          }}
        />
      </label>

      {isOpen && (term || isLoading || error || hasResults) && (
        <div className="global-search-popover" role="region" aria-label="Resultados da busca global">
          {showMinHint && <p className="global-search-message">Digite pelo menos 3 caracteres.</p>}
          {isLoading && <p className="global-search-message">Buscando...</p>}
          {error && <p className="global-search-message">{error}</p>}
          {!showMinHint && !isLoading && !error && trimmedTerm.length >= MIN_SEARCH_LENGTH && !hasResults && (
            <p className="global-search-message">Nenhum resultado encontrado.</p>
          )}
          {!isLoading && !error && groups.map(({ group, items }) => (
            <section key={group} className="global-search-group">
              <h3>{groupLabels[group]}</h3>
              {items.map((item) => {
                const canNavigate = Boolean(item.rota && knownRoutes.has(item.rota))

                return (
                  <button
                    key={`${item.tipo}-${item.id}-${item.titulo}`}
                    type="button"
                    className="global-search-item"
                    disabled={!canNavigate}
                    title={!canNavigate ? 'Rota ainda não disponível no frontend.' : undefined}
                    onClick={() => handleItemClick(item)}
                  >
                    <span>
                      <strong>{item.titulo}</strong>
                      <small>{item.subtitulo}</small>
                    </span>
                    <em>{item.tipoLabel}</em>
                  </button>
                )
              })}
            </section>
          ))}
        </div>
      )}
    </div>
  )
}
