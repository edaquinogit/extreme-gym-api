import { useState, type FormEvent } from 'react'
import { appPaths } from '../../app/routes/paths'
import { navigateTo } from '../../app/routes/router'
import { ApiStatus } from '../../components/ApiStatus'
import { AUTH_REGISTRATION_ENABLED } from '../../config/api'
import { useAuth } from '../../hooks/useAuth'
import { HttpError } from '../../services/httpClient'

type AuthMode = 'login' | 'register'

export function LoginPage() {
  const [mode, setMode] = useState<AuthMode>('login')
  const [name, setName] = useState('')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const { isLoading, login, register } = useAuth()

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErrorMessage(null)
    const normalizedUsername = username.trim()

    if (!normalizedUsername) {
      setErrorMessage('Informe seu email ou usuario.')
      return
    }

    try {
      if (mode === 'register' && AUTH_REGISTRATION_ENABLED) {
        const normalizedName = name.trim()

        if (!normalizedName) {
          setErrorMessage('Informe o nome do usuario.')
          return
        }

        await register({
          nome: normalizedName,
          email: normalizedUsername,
          senha: password,
        })
      } else {
        await login({ username: normalizedUsername, password })
      }

      navigateTo(appPaths.dashboard)
    } catch (error) {
      setErrorMessage(getLoginError(error))
    }
  }

  function handleModeChange(nextMode: AuthMode) {
    setMode(nextMode)
    setErrorMessage(null)
  }

  return (
    <main className="login-page">
      <div className="login-left-panel">
        <div className="login-brand-mark">
          <span className="brand-mark">EG</span>
          <strong>ExtremeGym</strong>
        </div>

        <div className="login-hero">
          <h2>Gestão moderna para academias que crescem</h2>
          <ul className="login-hero-bullets">
            <li>Controle de alunos, planos e matrículas</li>
            <li>Pagamentos e inadimplência em um só lugar</li>
            <li>Check-in e validação de acesso em tempo real</li>
          </ul>
        </div>

        <footer className="login-left-footer">Extreme Gym · Painel administrativo</footer>
      </div>

      <div className="login-right-panel">
        <section className="login-panel">
          <form className="login-form" onSubmit={handleSubmit}>
            <div className="login-brand">
              <span className="brand-mark">EG</span>
              <div>
                <strong>Extreme Gym</strong>
                <small>Painel administrativo</small>
              </div>
            </div>

            <div className="auth-form-header">
              <p className="eyebrow">{mode === 'register' ? 'Novo acesso' : 'Acesso seguro'}</p>
              <h2>{mode === 'register' ? 'Criar acesso' : 'Entrar no painel'}</h2>
              <p>
                {mode === 'register'
                  ? 'Cadastre um novo usuario autorizado para o painel.'
                  : 'Acesse sua conta para acompanhar a operação da academia.'}
              </p>
            </div>

            {AUTH_REGISTRATION_ENABLED && (
              <div className="auth-mode-switch" aria-label="Modo de autenticação">
                <button
                  className={mode === 'login' ? 'is-active' : ''}
                  type="button"
                  onClick={() => handleModeChange('login')}
                >
                  Entrar
                </button>
                <button
                  className={mode === 'register' ? 'is-active' : ''}
                  type="button"
                  onClick={() => handleModeChange('register')}
                >
                  Criar acesso
                </button>
              </div>
            )}

            {mode === 'register' && AUTH_REGISTRATION_ENABLED && (
              <div>
                <label htmlFor="name">Nome</label>
                <input
                  autoComplete="name"
                  id="name"
                  name="name"
                  placeholder="Ex.: Administrador"
                  required
                  type="text"
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                />
              </div>
            )}

            <div>
              <label htmlFor="username">Email ou usuario</label>
              <input
                autoComplete="username"
                id="username"
                name="username"
                placeholder="admin@extremegym.local"
                required
                type="text"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
              />
            </div>

            <div>
              <label htmlFor="password">Senha</label>
              <input
                autoComplete="current-password"
                id="password"
                name="password"
                minLength={mode === 'register' ? 12 : 1}
                placeholder="Informe sua senha"
                required
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
            </div>

            {errorMessage && <p className="form-error">{errorMessage}</p>}

            <button className="primary-button" disabled={isLoading} type="submit">
              {getSubmitLabel(mode, isLoading)}
            </button>

            {mode === 'register' && AUTH_REGISTRATION_ENABLED && (
              <p className="form-hint">
                O cadastro publico deve ficar habilitado apenas em ambiente de
                desenvolvimento ou configuração inicial.
              </p>
            )}

            <ApiStatus />
          </form>
        </section>
      </div>
    </main>
  )
}

function getSubmitLabel(mode: AuthMode, isLoading: boolean) {
  if (isLoading) {
    return mode === 'register' ? 'Criando...' : 'Entrando...'
  }

  return mode === 'register' ? 'Criar acesso' : 'Entrar'
}

function getLoginError(error: unknown) {
  if (error instanceof HttpError && error.status === 401) {
    return 'Email/usuário ou senha inválidos.'
  }

  if (error instanceof HttpError && error.status >= 500) {
    return 'A API está indisponível no momento. Tente novamente em instantes.'
  }

  if (error instanceof HttpError && error.status === 400) {
    return error.message || 'Revise os dados informados.'
  }

  if (error instanceof Error) {
    return error.message || 'Não foi possível autenticar.'
  }

  return 'Não foi possível autenticar.'
}
