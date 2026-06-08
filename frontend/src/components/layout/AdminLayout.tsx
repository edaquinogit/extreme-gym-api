import { useState, type ReactNode } from 'react'
import { appPaths } from '../../app/routes/paths'
import { navigateTo, useCurrentPath } from '../../app/routes/router'
import { ApiStatus } from '../ApiStatus'
import { useAuth } from '../../hooks/useAuth'
import { hasRole, type AppRole } from '../../utils/permissions'

const navigationItems = [
  { compactLabel: 'IN', label: 'Início', path: appPaths.dashboard, roles: ['ADMIN', 'RECEPCAO', 'CATRACA'] },
  { compactLabel: 'AL', label: 'Alunos', path: appPaths.alunos, roles: ['ADMIN', 'RECEPCAO'] },
  { compactLabel: 'PL', label: 'Planos', path: appPaths.planos, roles: ['ADMIN', 'RECEPCAO'] },
  { compactLabel: 'MA', label: 'Matrículas', path: appPaths.matriculas, roles: ['ADMIN', 'RECEPCAO'] },
  { compactLabel: 'PG', label: 'Pagamentos', path: appPaths.pagamentos, roles: ['ADMIN', 'RECEPCAO'] },
  { compactLabel: 'CH', label: 'Check-ins', path: appPaths.checkins, roles: ['ADMIN', 'RECEPCAO', 'CATRACA'] },
  { compactLabel: 'AC', label: 'Acesso', path: appPaths.acessos, roles: ['ADMIN', 'RECEPCAO', 'CATRACA'] },
  { compactLabel: 'DV', label: 'Dispositivos', path: appPaths.dispositivosAcesso, roles: ['ADMIN'] },
  { compactLabel: 'EV', label: 'Eventos', path: appPaths.eventosAcesso, roles: ['ADMIN', 'RECEPCAO'] },
  { compactLabel: 'CA', label: 'Catraca', path: appPaths.catraca, roles: ['ADMIN', 'RECEPCAO', 'CATRACA'] },
] as const

type NavigationItem = (typeof navigationItems)[number] & { roles: readonly AppRole[] }

type AdminLayoutProps = {
  children: ReactNode
}

export function AdminLayout({ children }: AdminLayoutProps) {
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false)
  const currentPath = useCurrentPath()
  const { logout, user } = useAuth()
  const visibleNavigationItems = navigationItems.filter((item: NavigationItem) =>
    hasRole(user, item.roles),
  )

  function handleNavigate(path: string) {
    navigateTo(path)
    setIsMenuOpen(false)
  }

  function handleLogout() {
    logout()
    navigateTo(appPaths.login)
  }

  const userLabel = user?.nome ?? user?.name ?? user?.email ?? user?.username ?? 'Admin'
  const userInitials = userLabel
    .split(/[\s@]/)[0]
    .split('')
    .slice(0, 2)
    .map((char) => char.toUpperCase())
    .join('')
  const sidebarClassName = [
    'admin-sidebar',
    isMenuOpen ? 'is-open' : '',
    isSidebarCollapsed ? 'is-collapsed' : '',
  ]
    .filter(Boolean)
    .join(' ')

  const currentNavItem = visibleNavigationItems.find((i) => i.path === currentPath)

  return (
    <div className={`admin-layout ${isSidebarCollapsed ? 'is-sidebar-collapsed' : ''}`}>
      <button
        className={`sidebar-overlay ${isMenuOpen ? 'is-visible' : ''}`}
        type="button"
        aria-label="Fechar menu"
        onClick={() => setIsMenuOpen(false)}
      />

      <aside className={sidebarClassName} aria-label="Barra lateral principal">
        <div className="sidebar-top">
          <div className="brand-block">
            <span className="brand-mark">EG</span>
            <div className="brand-copy">
              <strong>Extreme Gym</strong>
              <small>Unidade principal</small>
            </div>
          </div>

          <button
            className="icon-button sidebar-toggle"
            type="button"
            aria-label={isSidebarCollapsed ? 'Expandir menu lateral' : 'Recolher menu lateral'}
            aria-pressed={isSidebarCollapsed}
            onClick={() => setIsSidebarCollapsed((isCollapsed) => !isCollapsed)}
          >
            <span />
            <span />
            <span />
          </button>
        </div>

        <div className="brand-block mobile-brand">
          <span className="brand-mark">EG</span>
          <div className="brand-copy">
            <strong>Extreme Gym</strong>
            <small>Unidade principal</small>
          </div>
        </div>

        <nav className="sidebar-nav" aria-label="Navegação principal">
          {visibleNavigationItems.map((item) => {
            const isActive = currentPath === item.path
            return (
              <button
                className={`nav-link ${isActive ? 'is-active' : ''}`}
                key={item.path}
                title={item.label}
                type="button"
                aria-current={isActive ? 'page' : undefined}
                onClick={() => handleNavigate(item.path)}
              >
                <span className="nav-icon" aria-hidden="true">
                  {item.compactLabel}
                </span>
                <span className="nav-label">{item.label}</span>
              </button>
            )
          })}
        </nav>

        <div className="sidebar-user-block" role="region" aria-label="Informacoes do usuario">
          <div className="sidebar-user-avatar" aria-hidden>
            {userInitials}
          </div>
          <div className="sidebar-user-info">
            <strong>{userLabel}</strong>
            <small>{user?.role ?? 'Admin'} · Polo único</small>
          </div>
        </div>
      </aside>

      <div className="admin-main">
        <header className="admin-header">
          <div className="header-start">
            <button
              className="icon-button menu-button"
              type="button"
              aria-label={isMenuOpen ? 'Fechar menu' : 'Abrir menu'}
              aria-expanded={isMenuOpen}
              onClick={() => setIsMenuOpen((isOpen) => !isOpen)}
            >
              <span />
              <span />
              <span />
            </button>

            <div className="header-context">
              <strong>{currentNavItem?.label ?? 'Painel administrativo'}</strong>
              <span>{currentNavItem ? 'Área de ' + currentNavItem.label.toLowerCase() : 'Operação Extreme Gym'}</span>
            </div>
          </div>

          <div className="header-user">
            <label className="global-search" aria-label="Busca global visual">
              <span aria-hidden="true">⌕</span>
              <input disabled placeholder="Busca global pendente" type="search" />
            </label>

            <ApiStatus />

            <div className="header-profile">
              <div className="sidebar-user-avatar header-avatar" aria-hidden>
                {userInitials}
              </div>
              <div>
                <span>{userLabel}</span>
                <small>{user?.role ?? 'Admin'}</small>
              </div>
            </div>

            <div className="header-actions">
              <button className="ghost-button" type="button" onClick={() => handleNavigate(appPaths.alunos)} title="Ir para Alunos">
                Alunos
              </button>
              <button className="primary-button" type="button" onClick={handleLogout} aria-label="Sair">
                Sair
              </button>
            </div>
          </div>
        </header>

        <main className="admin-content">{children}</main>
      </div>
    </div>
  )
}
