# Sprint 4 - Preparacao operacional da catraca

## Diagnostico inicial

O frontend iniciou a Sprint 4 com `git status --ignored` limpo: nenhum arquivo pendente, `frontend/node_modules/`, `frontend/dist/`, `frontend/.env`, logs e artefatos de build continuam ignorados. A pasta `frontend/` ja esta versionavel com arquivos de fonte e documentacao rastreados.

A `CatracaPage` ja possuia fluxo real de validacao via `acessoService.validar()` e registro de check-in via `checkinService.registrar()`. O risco principal era falta de testes dedicados e microcopy sugerindo Face ID antes de existir contrato real.

## Situacao do Git/frontend

- Nenhum arquivo nao rastreado no inicio da sprint.
- Arquivos gerados continuam ignorados.
- Nenhum commit foi executado automaticamente.
- Novos arquivos de fonte, testes e documentacao devem ser adicionados no proximo commit.

## Testes criados

Foi criada cobertura dedicada para `CatracaPage`, cobrindo:

- estado inicial;
- orientacao operacional;
- painel de status de dispositivo sem telemetria falsa;
- loading durante validacao;
- acesso liberado;
- acesso bloqueado com motivo;
- erro amigavel de API;
- fallback para dados ausentes;
- chamada real de handler para registro de check-in.

## Componentes extraidos

- `CatracaDeviceStatusPanel`: componente puro para exibir status de dispositivo quando houver contrato real, ou avisar que o gateway ainda esta pendente.
- `CatracaEmptyState`: componente puro para estado inicial e historico vazio da estacao.

## Melhorias na CatracaPage

- Microcopy removeu sugestao operacional de Face ID enquanto nao existe integracao.
- Campo manual recebeu label acessivel.
- Estado inicial ficou mais claro.
- Loading passou a informar que a situacao do aluno esta sendo consultada.
- Acesso liberado e bloqueado usam texto em portugues operacional.
- Motivo de bloqueio ganhou prefixo explicito.
- Nome de aluno ausente recebe fallback seguro.

## Permissoes revisadas

Nao existe fluxo de liberacao manual administrativo nesta sprint. Portanto, nenhuma regra nova foi criada.

Requisito futuro documentado: liberacao manual deve exigir perfil permitido, motivo e auditoria.

## Contrato de dispositivo/gateway

Criado em `frontend/docs/access-device-contract-requirements.md`.

O documento define contrato minimo para:

- dispositivo de acesso;
- evento de acesso;
- resposta de validacao;
- snapshot/offline;
- permissoes e auditoria.

## Contrato de busca global

Criado em `frontend/docs/global-search-contract-requirements.md`.

A busca global continua nao implementada porque nao ha endpoint real. O documento define contrato server-side, payload sugerido, limites e regras por perfil.

## Pendencias para backend

- Endpoint real de status de dispositivo/gateway.
- Endpoint real de eventos de acesso.
- Contrato final de validacao para catraca fisica.
- Endpoint server-side de busca global.
- Regras de permissao para liberacao manual.
- Auditoria de liberacao manual.

## Pendencias para hardware real

- Fabricante e modelo da catraca.
- API/protocolo do equipamento.
- Estrategia do gateway local/cloud.
- Modelo de operacao offline.
- Reconciliacao de eventos pendentes.
- Politica de snapshot de credenciais.

## Garantias preservadas

- Nenhum endpoint falso foi criado.
- Nenhum mock de producao foi usado.
- Nenhum service ou mapper existente foi alterado.
- Nenhuma regra de negocio foi inventada.
- Fluxos de `AcessoPage` e CRUDs foram preservados.

## Proximo passo recomendado

Executar uma sprint de backend para definir o modelo hibrido de dispositivos, eventos de acesso, status de gateway e busca global server-side antes de qualquer integracao com fabricante.
