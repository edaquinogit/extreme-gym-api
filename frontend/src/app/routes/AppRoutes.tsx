import { AdminLayout } from '../../components/layout/AdminLayout'
import { useAuth } from '../../hooks/useAuth'
import { AcessoPage } from '../../pages/Acesso/AcessoPage'
import { AlunosPage } from '../../pages/Alunos/AlunosPage'
import { CatracaPage } from '../../pages/Catraca/CatracaPage'
import { CheckinsPage } from '../../pages/Checkins/CheckinsPage'
import { DashboardPage } from '../../pages/Dashboard/DashboardPage'
import { DispositivosAcessoPage } from '../../pages/DispositivosAcesso/DispositivosAcessoPage'
import { EventosAcessoPage } from '../../pages/EventosAcesso/EventosAcessoPage'
import { LoginPage } from '../../pages/Login/LoginPage'
import { MatriculasPage } from '../../pages/Matriculas/MatriculasPage'
import { PagamentosPage } from '../../pages/Pagamentos/PagamentosPage'
import { PlanosPage } from '../../pages/Planos/PlanosPage'
import { hasRole, type AppRole } from '../../utils/permissions'
import { appPaths, privatePaths, type AppPath } from './paths'
import { useCurrentPath, useRedirect } from './router'

const routeRoles: Partial<Record<AppPath, readonly AppRole[]>> = {
  [appPaths.home]: ['ADMIN', 'RECEPCAO', 'CATRACA'],
  [appPaths.dashboard]: ['ADMIN', 'RECEPCAO', 'CATRACA'],
  [appPaths.alunos]: ['ADMIN', 'RECEPCAO'],
  [appPaths.planos]: ['ADMIN', 'RECEPCAO'],
  [appPaths.matriculas]: ['ADMIN', 'RECEPCAO'],
  [appPaths.pagamentos]: ['ADMIN', 'RECEPCAO'],
  [appPaths.checkins]: ['ADMIN', 'RECEPCAO', 'CATRACA'],
  [appPaths.acessos]: ['ADMIN', 'RECEPCAO', 'CATRACA'],
  [appPaths.dispositivosAcesso]: ['ADMIN'],
  [appPaths.eventosAcesso]: ['ADMIN', 'RECEPCAO'],
  [appPaths.catraca]: ['ADMIN', 'RECEPCAO', 'CATRACA'],
}

export function AppRoutes() {
  const path = useCurrentPath()
  const { isAuthenticated, user } = useAuth()
  const isPrivatePath = privatePaths.includes(path as AppPath)
  const allowedRoles = routeRoles[path as AppPath]
  const isUnauthorizedPrivatePath =
    isAuthenticated &&
    isPrivatePath &&
    Boolean(allowedRoles) &&
    !hasRole(user, allowedRoles ?? [])

  useRedirect(!isAuthenticated && isPrivatePath, appPaths.login)
  useRedirect(isAuthenticated && path === appPaths.login, appPaths.dashboard)
  useRedirect(isUnauthorizedPrivatePath, appPaths.dashboard)

  if ((!isAuthenticated && isPrivatePath) || isUnauthorizedPrivatePath) {
    return null
  }

  if (path === appPaths.login) {
    return <LoginPage />
  }

  if (path === appPaths.catraca) {
    return <CatracaPage />
  }

  return <AdminLayout>{renderPrivatePage(path)}</AdminLayout>
}

function renderPrivatePage(path: string) {
  switch (path) {
    case appPaths.home:
    case appPaths.dashboard:
      return <DashboardPage />
    case appPaths.alunos:
      return <AlunosPage />
    case appPaths.planos:
      return <PlanosPage />
    case appPaths.matriculas:
      return <MatriculasPage />
    case appPaths.pagamentos:
      return <PagamentosPage />
    case appPaths.checkins:
      return <CheckinsPage />
    case appPaths.acessos:
      return <AcessoPage />
    case appPaths.dispositivosAcesso:
      return <DispositivosAcessoPage />
    case appPaths.eventosAcesso:
      return <EventosAcessoPage />
    default:
      return <DashboardPage />
  }
}
