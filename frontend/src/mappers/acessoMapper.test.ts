import { describe, expect, it } from 'vitest'
import { acessoApiToViewModel, acessoViewModelToAPI } from './acessoMapper'

describe('acessoMapper', () => {
  it('maps released access response safely', () => {
    expect(
      acessoApiToViewModel({
        alunoId: 4,
        alunoNome: 'Carla',
        acessoLiberado: true,
        motivo: 'Acesso liberado',
        matriculaId: 9,
        dataValidadeMatricula: '2026-04-20',
      }),
    ).toEqual({
      alunoId: 4,
      alunoNome: 'Carla',
      acessoLiberado: true,
      motivo: 'Acesso liberado',
      matriculaId: 9,
      dataValidadeMatricula: '2026-04-20',
    })
  })

  it('uses safe defaults for blocked or incomplete responses', () => {
    expect(acessoApiToViewModel({})).toEqual({
      alunoId: 0,
      alunoNome: '',
      acessoLiberado: false,
      motivo: '',
      matriculaId: null,
      dataValidadeMatricula: null,
    })
  })

  it('creates validation payload with alunoId only', () => {
    expect(acessoViewModelToAPI(22)).toEqual({ alunoId: 22 })
  })
})
