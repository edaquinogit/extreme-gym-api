import { AdminLayout } from '../../components/layout/AdminLayout'
import { useAuth } from '../../hooks/useAuth'
import { AcessoPage } from '../../pages/Acesso/AcessoPage'
import { AlunosPage } from '../../pages/Alunos/AlunosPage'
import { CatracaPage } from '../../pages/Catraca/CatracaPage'
import { CheckinsPage } from '../../pages/Checkins/CheckinsPage'
import { DashboardPage } from '../../pages/Dashboard/DashboardPage'
import { LoginPage } from '../../pages/Login/LoginPage'
import { MatriculasPage } from '../../pages/Matriculas/MatriculasPage'
import { PagamentosPage } from '../../pages/Pagamentos/PagamentosPage'
import { PlanosPage } from '../../pages/Planos/PlanosPage'
import { appPaths, privatePaths, type AppPath } from './paths'
import { useCurrentPath, useRedirect } from './router'

export function AppRoutes() {
  const path = useCurrentPath()
  const { isAuthenticated } = useAuth()
  const isPrivatePath = privatePaths.includes(path as AppPath)

  useRedirect(!isAuthenticated && isPrivatePath, appPaths.login)
  useRedirect(isAuthenticated && path === appPaths.login, appPaths.dashboard)

  if (!isAuthenticated && isPrivatePath) {
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
    default:
      return <DashboardPage />
  }
}
