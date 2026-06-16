import { describe, expect, it } from 'vitest'
import { globalSearchApiToViewModel } from './globalSearchMapper'

describe('globalSearchMapper', () => {
  it('maps grouped global search response', () => {
    expect(
      globalSearchApiToViewModel({
        alunos: [
          {
            id: 1,
            tipo: 'ALUNO',
            titulo: 'Ana Silva',
            subtitulo: 'ATIVO',
            status: 'ATIVO',
            rota: '/alunos/1',
            metadata: { alunoId: 1 },
          },
        ],
      }),
    ).toMatchObject({
      alunos: [
        {
          id: 1,
          tipoLabel: 'Aluno',
          titulo: 'Ana Silva',
          rota: '/alunos/1',
        },
      ],
      matriculas: [],
      pagamentos: [],
      planos: [],
    })
  })

  it('drops invalid route values instead of creating fake navigation', () => {
    expect(
      globalSearchApiToViewModel({
        planos: [{ id: 2, tipo: 'PLANO', rota: 'planos/2' }],
      }).planos[0].rota,
    ).toBeNull()
  })
})
