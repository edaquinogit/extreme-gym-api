import { useEffect, useRef, useState } from 'react'
import type { StatusAluno } from '../../types/aluno'
import { StatusBadge } from './StatusBadge'

type StatusDropdownProps = {
  status: StatusAluno
  isLoading?: boolean
  onChange: (status: StatusAluno) => void
}

const STATUS_OPTIONS: StatusAluno[] = [
  'ATIVO',
  'INADIMPLENTE',
  'BLOQUEADO',
  'CANCELADO',
]

const STATUS_TRANSITIONS: Record<StatusAluno, StatusAluno[]> = {
  ATIVO: ['INADIMPLENTE', 'BLOQUEADO', 'CANCELADO'],
  INADIMPLENTE: ['ATIVO', 'BLOQUEADO', 'CANCELADO'],
  BLOQUEADO: ['ATIVO', 'INADIMPLENTE', 'CANCELADO'],
  CANCELADO: [],
  INATIVO: [],
}

export function StatusDropdown({
  status,
  isLoading = false,
  onChange,
}: StatusDropdownProps) {
  const [isOpen, setIsOpen] = useState(false)
  const wrapperRef = useRef<HTMLDivElement>(null)
  const canOpen = status !== 'CANCELADO' && status !== 'INATIVO' && !isLoading

  useEffect(() => {
    if (!isOpen) {
      return
    }

    function handlePointerDown(event: MouseEvent) {
      if (!wrapperRef.current?.contains(event.target as Node)) {
        setIsOpen(false)
      }
    }

    document.addEventListener('mousedown', handlePointerDown)

    return () => document.removeEventListener('mousedown', handlePointerDown)
  }, [isOpen])

  function handleSelect(nextStatus: StatusAluno) {
    if (!STATUS_TRANSITIONS[status].includes(nextStatus)) {
      return
    }

    setIsOpen(false)
    onChange(nextStatus)
  }

  if (status === 'CANCELADO' || status === 'INATIVO') {
    return <StatusBadge status={status} />
  }

  return (
    <div className="status-dropdown-wrapper" ref={wrapperRef}>
      <button
        type="button"
        className={`status-badge status-badge--button ${getStatusClass(status)}`}
        aria-expanded={isOpen}
        aria-haspopup="menu"
        disabled={!canOpen}
        onClick={() => setIsOpen((current) => !current)}
      >
        {isLoading ? 'Alterando...' : formatStatus(status)}
      </button>

      {isOpen && (
        <div className="status-dropdown" role="menu">
          {STATUS_OPTIONS.map((option) => {
            const isCurrent = option === status
            const isAvailable = STATUS_TRANSITIONS[status].includes(option)

            return (
              <button
                key={option}
                type="button"
                className={`status-dropdown-item ${
                  isCurrent ? 'is-current' : ''
                }`}
                disabled={isCurrent || !isAvailable}
                role="menuitem"
                onClick={() => handleSelect(option)}
              >
                <span className={`status-dot ${getStatusClass(option)}`} />
                {formatStatus(option)}
              </button>
            )
          })}
        </div>
      )}
    </div>
  )
}

function getStatusClass(status: StatusAluno) {
  return `is-${status.toLowerCase()}`
}

function formatStatus(status: StatusAluno) {
  return status.replace('_', ' ')
}
