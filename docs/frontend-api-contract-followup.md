# Frontend API Contract Follow-up

## Contexto

Esta evolução consolida o padrão de separação entre DTOs da API, mappers e ViewModels do frontend no Extreme Gym Web. O foco foi blindar a UI contra divergências de contrato, sem redesign, nova feature, mudança de regra de negócio, alteração de endpoint ou ajuste no backend.

O projeto já possui Vitest configurado e uma estrutura consistente em `frontend/src/types`, `frontend/src/mappers` e `frontend/src/services`.

## Contratos Já Corrigidos

### Plano

- API: `valorMensal`, `duracaoEmDias`, `ativo`.
- UI: `valor`, `duracaoDias`, `status`.
- Mapper: `planoApiToViewModel` converte nomes e status.
- Payload: `planoViewModelToAPI` envia os nomes esperados pelo backend.
- Ajuste desta evolução: o ViewModel `Plano` deixou de expor `ativo`; a UI usa `status`.

### Check-in

- API: `permitido`, `motivo`.
- UI: `status`, `motivoBloqueio`.
- Mapper: `checkinApiToViewModel` converte permissão para status e só preenche motivo de bloqueio quando o acesso é bloqueado.
- Payload: `checkinViewModelToAPI` envia apenas `alunoId`.
- Ajuste desta evolução: o ViewModel `Checkin` deixou de expor `permitido` e `motivo`; componentes usam `status` e `motivoBloqueio`.

## Entidades Auditadas

- Aluno
- Matrícula
- Pagamento
- Acesso
- Auth/Usuário
- Dispositivos de Acesso
- Eventos de Acesso
- Credenciais de Acesso
- Busca Global
- Plano
- Check-in

## Resultado da Auditoria

| Entidade | DTO separado | Mapper | Service retorna ViewModel | Observações |
| --- | --- | --- | --- | --- |
| Aluno | Sim, `AlunoAPI` | Sim | Sim | Campos opcionais normalizados para strings seguras. |
| Matrícula | Sim, `MatriculaAPI` | Sim | Sim | `dataFim` da API é mapeado para `dataVencimento` na UI. |
| Pagamento | Sim, `PagamentoAPI` | Sim | Sim | Status restrito a `PAGO`, `PENDENTE`, `CANCELADO`; `dataVencimento` não é assumido da API. |
| Acesso | Sim, `AcessoAPI` | Sim | Sim | `motivo` é parte do contrato de resposta de validação, não DTO bruto de Check-in. |
| Auth/Usuário | Sim, `LoginResponseAPI` | Sim | Sim | Compatível com token direto, `accessToken`, `jwt`, `user` e `usuario`. |
| Dispositivos de Acesso | Sim, `DispositivoAcessoAPI` | Sim | Sim | Labels e datas são derivados no mapper. |
| Eventos de Acesso | Sim, `EventoAcessoAPI` | Sim | Sim | Labels, máscaras e fallback de datas são derivados no mapper. |
| Credenciais de Acesso | Sim, `CredencialAcessoAPI` | Sim | Sim | Identificador mascarado e labels são derivados no mapper. |
| Busca Global | Sim, `GlobalSearchAPI` | Sim | Sim | Rotas inválidas são descartadas no mapper. |
| Plano | Sim, `PlanoAPI` | Sim | Sim | Campos crus removidos do ViewModel. |
| Check-in | Sim, `CheckinAPI` | Sim | Sim | Campos crus removidos do ViewModel. |

## Divergências Encontradas

- `Plano` ainda expunha `ativo` no ViewModel, apesar de a UI já trabalhar com `status`.
- `Checkin` ainda expunha `permitido` e `motivo` no ViewModel, apesar de a UI já ter `status` e `motivoBloqueio`.
- `AcessoPage` consumia `response.permitido` ao registrar check-in.
- `PlanosPage` atualizava `ativo` manualmente após inativar um plano.
- `MatriculasPage` renderizava `dataFim`, campo de origem do backend, em vez do alias estabilizado `dataVencimento`.
- `PaymentsStatusChart` ainda continha cor para `ATRASADO`, status que não pertence ao contrato atual de pagamento.

## Divergências Corrigidas

- Removido `ativo` do ViewModel `Plano`.
- Removidos `permitido` e `motivo` do ViewModel `Checkin`.
- `AcessoPage` passou a usar `response.status` e `response.motivoBloqueio`.
- `PlanosPage` passou a atualizar somente `status: 'INATIVO'` no estado local.
- `MatriculasPage` passou a renderizar `dataVencimento`.
- `CatracaPage` passou a priorizar `dataVencimento` antes de `dataFim` no helper local.
- Removida referência visual a `ATRASADO` no gráfico de status de pagamentos.

## Divergências Não Corrigidas

- `Matricula` ainda mantém `dataFim` no ViewModel por compatibilidade com telas e testes existentes. O mapper já estabiliza `dataVencimento`, e a tela principal de matrículas foi ajustada para usar esse campo.
- `Pagamento` mantém `dataVencimento?: string` no ViewModel porque o dashboard e a catraca já aceitam esse campo como opcional. O mapper não lê esse campo da API e o define como `undefined`.
- `AcessoResponse.motivo` foi mantido porque é o contrato da validação de acesso, usado para orientar atendimento na recepção/catraca.
- Tipos `unknown` foram mantidos em pontos legítimos de erro HTTP, storage e metadata de busca global.

## Services Ajustados

Nenhum endpoint foi alterado. Os services auditados já recebem DTOs via `httpClient`, aplicam mapper e retornam ViewModels:

- `alunoService`
- `matriculaService`
- `pagamentoService`
- `acessoService`
- `authService`
- `accessDeviceService`
- `accessEventService`
- `accessCredentialService`
- `globalSearchService`
- `planoService`
- `checkinService`

## Mappers Criados ou Alterados

- `planoMapper`: removido repasse de `ativo` para o ViewModel.
- `checkinMapper`: removido repasse de `permitido` e `motivo` para o ViewModel.

Nenhum mapper novo foi necessário porque as entidades críticas já possuíam mapeamento.

## Testes Adicionados ou Ajustados

- `planoMapper.test.ts`: ampliado para validar conversões `valorMensal -> valor`, `duracaoEmDias -> duracaoDias`, `ativo -> status`, defaults seguros e ausência de campos crus no ViewModel.
- `checkinMapper.test.ts`: ampliado para validar `permitido -> status`, `motivo -> motivoBloqueio` apenas em bloqueio, preservação de identificadores/data e ausência de campos crus no ViewModel.
- Testes de páginas ajustados para mockar ViewModels estáveis de Check-in.

## Pendências Reais

- Confirmar se `Matricula.dataFim` deve ser removido definitivamente do ViewModel ou mantido como compatibilidade temporária.
- Confirmar se o backend algum dia retornará vencimentos de pagamento; hoje o mapper de pagamento não assume `dataVencimento`.
- Padronizar nomes dos DTOs de `*API` para `*ApiDTO` apenas se o time quiser essa convenção. A mudança seria mecânica e de baixo risco, mas geraria churn sem ganho funcional imediato.
- Avaliar extração de helpers de data/label para reduzir duplicação entre mappers de acesso.

## Próximos Passos Recomendados

- Remover `dataFim` de `Matricula` quando todas as telas e testes estiverem usando somente `dataVencimento`.
- Adicionar testes de service com mocks de `httpClient` se houver necessidade de garantir que nenhum DTO bruto escape do service.
- Manter qualquer novo endpoint seguindo o fluxo: `EntidadeAPI` ou `EntidadeApiDTO` -> mapper -> ViewModel -> UI.
