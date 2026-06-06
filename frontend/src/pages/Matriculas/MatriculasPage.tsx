import { useState } from 'react'
import { useForm, type FieldPath } from 'react-hook-form'
import { z } from 'zod'
import { DataTable } from '../../components/tables/DataTable'
import { ConfirmDialog } from '../../components/ui/ConfirmDialog'
import { FormField } from '../../components/ui/FormField'
import { Modal } from '../../components/ui/Modal'
import { PageHeader } from '../../components/ui/PageHeader'
import { StateMessage } from '../../components/ui/StateMessage'
import { StatusBadge } from '../../components/ui/StatusBadge'
import { useApiError } from '../../hooks/useApiError'
import { useAuth } from '../../hooks/useAuth'
import { useResourceList } from '../../hooks/useResourceList'
import { alunoService } from '../../services/alunoService'
import { matriculaService } from '../../services/matriculaService'
import { planoService } from '../../services/planoService'
import type { Matricula } from '../../types/matricula'
import { formatDate } from '../../utils/formatDate'
import { hasRole } from '../../utils/permissions'

const matriculaSchema = z.object({
  alunoId: z.coerce.number().positive('Selecione um aluno.'),
  planoId: z.coerce.number().positive('Selecione um plano.'),
  dataInicio: z.string().min(1, 'Informe a data de início.'),
})

type MatriculaFormValues = z.input<typeof matriculaSchema>

const matriculaDefaultValues: MatriculaFormValues = {
  alunoId: 0,
  planoId: 0,
  dataInicio: new Date().toISOString().slice(0, 10),
}

export function MatriculasPage() {
  const {
    data: matriculas,
    setData: setMatriculas,
    errorMessage,
    isLoading,
    reload,
  } = useResourceList({
    load: matriculaService.listar,
  })
  const { data: alunos, errorMessage: alunosError, isLoading: isLoadingAlunos } = useResourceList({
    load: alunoService.listar,
  })
  const { data: planos, errorMessage: planosError, isLoading: isLoadingPlanos } = useResourceList({
    load: planoService.listar,
  })
  const { user } = useAuth()
  const { getErrorMessage } = useApiError()
  const canManageMatriculas = hasRole(user, ['ADMIN', 'RECEPCAO'])
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [matriculaToCancel, setMatriculaToCancel] = useState<Matricula | null>(null)
  const [isSaving, setIsSaving] = useState(false)
  const [cancelLoadingId, setCancelLoadingId] = useState<number | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [actionMessage, setActionMessage] = useState<string | null>(null)
  const {
    clearErrors,
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setError,
  } = useForm<MatriculaFormValues>({
    defaultValues: matriculaDefaultValues,
  })
  const activeAlunos = alunos.filter((aluno) => aluno.status !== 'INATIVO' && aluno.status !== 'CANCELADO')
  const activePlanos = planos.filter((plano) => plano.status === 'ATIVO')
  const isAuxLoading = isLoadingAlunos || isLoadingPlanos
  const auxError = alunosError ?? planosError

  function openMatriculaForm() {
    setFormError(null)
    clearActionFeedback()
    clearErrors()
    reset(matriculaDefaultValues)
    setIsFormOpen(true)
  }

  function closeMatriculaForm() {
    setIsFormOpen(false)
    setFormError(null)
    clearErrors()
  }

  async function submitMatricula(values: MatriculaFormValues) {
    if (isSaving) {
      return
    }

    const parsed = matriculaSchema.safeParse(values)

    setFormError(null)
    clearActionFeedback()
    clearErrors()

    if (!parsed.success) {
      applyValidationErrors(parsed.error.issues)
      return
    }

    try {
      setIsSaving(true)
      const savedMatricula = await matriculaService.criar(parsed.data)

      setMatriculas((current) => [savedMatricula, ...current])
      setActionMessage('Matrícula criada com sucesso.')
      closeMatriculaForm()
      void reload()
    } catch (error) {
      setFormError(getErrorMessage(error))
    } finally {
      setIsSaving(false)
    }
  }

  function requestMatriculaCancel(matricula: Matricula) {
    setMatriculaToCancel(matricula)
    clearActionFeedback()
  }

  function closeCancelDialog() {
    setMatriculaToCancel(null)
  }

  async function cancelarMatricula() {
    if (!matriculaToCancel) {
      return
    }

    try {
      setCancelLoadingId(matriculaToCancel.id)
      clearActionFeedback()
      await matriculaService.cancelar(matriculaToCancel.id)
      setMatriculas((current) =>
        current.map((item) =>
          item.id === matriculaToCancel.id ? { ...item, status: 'CANCELADA' } : item,
        ),
      )
      setActionMessage('Matrícula cancelada com sucesso.')
      setMatriculaToCancel(null)
      void reload()
    } catch (error) {
      setActionError(getErrorMessage(error))
    } finally {
      setCancelLoadingId(null)
    }
  }

  function applyValidationErrors(issues: z.ZodIssue[]) {
    const firstIssue = issues[0]

    for (const issue of issues) {
      const field = issue.path[0]

      if (typeof field === 'string' && field in matriculaDefaultValues) {
        setError(field as FieldPath<MatriculaFormValues>, { message: issue.message })
      }
    }

    setFormError(firstIssue?.message ?? 'Revise os campos do formulário.')
  }

  function clearActionFeedback() {
    setActionError(null)
    setActionMessage(null)
  }

  return (
    <>
      <PageHeader
        eyebrow="Matrículas"
        title="Matrículas ativas e histórico"
        description="Relacione aluno, plano, data final e status da matrícula."
        action={
          <button
            className="primary-button compact"
            type="button"
            onClick={openMatriculaForm}
            disabled={!canManageMatriculas || isAuxLoading || Boolean(auxError)}
            title={!canManageMatriculas ? 'Ação restrita aos perfis ADMIN e RECEPÇÃO.' : undefined}
          >
            Nova matrícula
          </button>
        }
      />

      {auxError && (
        <StateMessage
            title="Não foi possível carregar dados para nova matrícula."
          description={auxError}
        />
      )}
      {actionError && <StateMessage title="Não foi possível concluir a ação." description={actionError} />}
      {actionMessage && <StateMessage title="Dados atualizados" description={actionMessage} />}

      <section className="content-panel">
        {isLoading && <StateMessage title="Carregando matrículas..." />}
        {!isLoading && errorMessage && (
          <StateMessage title="Não foi possível carregar as matrículas." description={errorMessage} />
        )}
        {!isLoading && !errorMessage && matriculas.length === 0 && (
          <StateMessage
            title="Nenhuma matrícula encontrada"
            description="Cadastre um aluno e um plano para iniciar a primeira matrícula."
          />
        )}
        {!isLoading && !errorMessage && matriculas.length > 0 && (
          <DataTable headers={['ID', 'Aluno', 'Plano', 'Status', 'Início', 'Vencimento', 'Ações']}>
            {matriculas.map((matricula) => {
              const isCanceled = matricula.status === 'CANCELADA'

              return (
                <tr key={matricula.id}>
                  <td>{matricula.id}</td>
                  <td>{getAlunoNome(matricula)}</td>
                  <td>{matricula.planoNome ?? matricula.plano?.nome ?? '-'}</td>
                  <td><StatusBadge status={matricula.status} /></td>
                  <td>{formatDate(matricula.dataInicio)}</td>
                  <td>{formatDate(matricula.dataVencimento)}</td>
                  <td>
                    {!isCanceled ? (
                      <button
                        type="button"
                        className="btn-danger compact"
                        disabled={!canManageMatriculas || cancelLoadingId === matricula.id}
                        onClick={() => requestMatriculaCancel(matricula)}
                      >
                        {cancelLoadingId === matricula.id ? 'Cancelando...' : 'Cancelar'}
                      </button>
                    ) : (
                      '-'
                    )}
                  </td>
                </tr>
              )
            })}
          </DataTable>
        )}
      </section>

      <Modal isOpen={isFormOpen} onClose={closeMatriculaForm} title="Cadastrar nova matrícula">
        <form onSubmit={(event) => void handleSubmit(submitMatricula)(event)}>
          <FormField label="Aluno" error={errors.alunoId?.message} required>
            <select {...register('alunoId')}>
              <option value={0}>Selecione um aluno</option>
              {activeAlunos.map((aluno) => (
                <option key={aluno.id} value={aluno.id}>
                  {aluno.nome}
                </option>
              ))}
            </select>
          </FormField>

          <FormField label="Plano" error={errors.planoId?.message} required>
            <select {...register('planoId')}>
              <option value={0}>Selecione um plano</option>
              {activePlanos.map((plano) => (
                <option key={plano.id} value={plano.id}>
                  {plano.nome}
                </option>
              ))}
            </select>
          </FormField>

          <FormField label="Data de início" error={errors.dataInicio?.message} required>
            <input type="date" {...register('dataInicio')} />
          </FormField>

          {formError && <div className="field-error">{formError}</div>}

          <div className="form-actions">
            <button type="button" className="ghost-button" onClick={closeMatriculaForm}>
              Cancelar
            </button>
            <button type="submit" className="primary-button" disabled={isSaving}>
              {isSaving ? 'Salvando...' : 'Cadastrar matrícula'}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        isOpen={Boolean(matriculaToCancel)}
        title="Cancelar matrícula"
        description={`Deseja cancelar a matrícula de ${getAlunoNome(matriculaToCancel)}? Essa ação não poderá ser desfeita automaticamente.`}
        confirmLabel="Cancelar matrícula"
        isLoading={Boolean(matriculaToCancel && cancelLoadingId === matriculaToCancel.id)}
        onCancel={closeCancelDialog}
        onConfirm={() => void cancelarMatricula()}
      />
    </>
  )
}

function getAlunoNome(matricula: Matricula | null) {
  return matricula?.alunoNome ?? matricula?.aluno?.nome ?? '-'
}
