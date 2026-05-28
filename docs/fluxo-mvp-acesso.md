# Fluxo MVP de Acesso

## Objetivo

Explicar como o Extreme Gym API valida se um aluno pode acessar a academia.

## Fluxo

Aluno -> Plano -> Matricula -> Pagamento -> Validacao de Acesso -> Check-in

## Regras de negocio

- aluno ativo;
- matricula ativa;
- plano cadastrado e ativo para permitir matricula;
- pagamento valido com status `PAGO`;
- acesso liberado ou bloqueado com motivo claro;
- check-in registrado com historico da tentativa.

## Camadas envolvidas

Controller, DTO, Service, Repository, Entity e Exception.

## Classes principais

- `AlunoService`
- `PlanoService`
- `MatriculaService`
- `PagamentoService`
- `AcessoService`
- `CheckInService`

## Endpoints para demonstracao

- `POST /alunos`
- `POST /planos`
- `POST /matriculas`
- `POST /pagamentos`
- `POST /acessos/validar`
- `POST /checkins`

## Testes relacionados

- `AlunoServiceTest`
- `PlanoServiceTest`
- `MatriculaServiceTest`
- `PagamentoServiceTest`
- `AcessoServiceTest`
- `CheckInServiceTest`
- `AlunoControllerIntegrationTest`
- `PlanoControllerIntegrationTest`
- `MatriculaControllerIntegrationTest`
- `PagamentoControllerIntegrationTest`
- `AcessoControllerIntegrationTest`
- `CheckInControllerIntegrationTest`

## Evidencia tecnica

O fluxo possui regras reais de negocio na camada de service. A validacao de acesso fica em `AcessoService`, enquanto `CheckInService` reutiliza essa decisao para registrar tentativas permitidas e bloqueadas.
