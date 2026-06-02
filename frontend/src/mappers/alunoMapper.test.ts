import { describe, expect, it } from 'vitest'
import { alunoApiToViewModel, alunoStatusToAPI, alunoViewModelToAPI } from './alunoMapper'

describe('alunoMapper', () => {
  it('uses safe defaults when optional fields are missing', () => {
    expect(alunoApiToViewModel({ id: 7, nome: 'Ana' })).toEqual({
      id: 7,
      nome: 'Ana',
      email: '',
      telefone: '',
      status: 'ATIVO',
      dataCadastro: '',
    })
  })

  it('trims create and edit payload fields', () => {
    expect(
      alunoViewModelToAPI({
        nome: ' Ana ',
        email: ' ana@example.com ',
        telefone: ' 71999990000 ',
      }),
    ).toEqual({
      nome: 'Ana',
      email: 'ana@example.com',
      telefone: '71999990000',
    })
  })

  it('creates status payload compatible with PATCH /alunos/{id}/status', () => {
    expect(alunoStatusToAPI('INATIVO')).toEqual({ status: 'INATIVO' })
  })
})
