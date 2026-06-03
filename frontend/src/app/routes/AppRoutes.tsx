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
import { appPaths, privatePaths, type AppPath } from './paths'
import { useCurrentPath, useRedirect } from './router'
import { hasRole } from '../../utils/permissions'

export function AppRoutes() {
  const path = useCurrentPath()
  const { isAuthenticated, user } = useAuth()
  const isPrivatePath = privatePaths.includes(path as AppPath)
  const isAllowedPrivatePath = !isPrivatePath || canAccessPath(path, user)

  useRedirect(!isAuthenticated && isPrivatePath, appPaths.login)
  useRedirect(isAuthenticated && path === appPaths.login, appPaths.dashboard)
  useRedirect(isAuthenticated && isPrivatePath && !isAllowedPrivatePath, appPaths.dashboard)

  if (!isAuthenticated && isPrivatePath) {
    return null
  }

  if (isAuthenticated && isPrivatePath && !isAllowedPrivatePath) {
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

function canAccessPath(path: string, user: ReturnType<typeof useAuth>['user']) {
  if (path === appPaths.dispositivosAcesso) {
    return hasRole(user, ['ADMIN'])
  }

  if (path === appPaths.eventosAcesso) {
    return hasRole(user, ['ADMIN', 'RECEPCAO'])
  }

  if (path === appPaths.pagamentos || path === appPaths.planos || path === appPaths.matriculas || path === appPaths.alunos) {
    return hasRole(user, ['ADMIN', 'RECEPCAO'])
  }

  return hasRole(user, ['ADMIN', 'RECEPCAO', 'CATRACA'])
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
