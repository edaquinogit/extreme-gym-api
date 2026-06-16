# Auditoria de Prontidao para Producao Real

Data: 2026-06-06

Escopo: backend Spring Boot, frontend React/Vite, gateway local de acesso, PostgreSQL, Flyway, Docker, seguranca e operacao diaria da Extreme Gym.

## Decisao Executiva

Status atual: **NO-GO para producao real ampla**.

O sistema esta maduro para **staging operacional** e pode evoluir para **piloto controlado** depois da Phase 0. O bloqueio nao esta no dominio principal: alunos, planos, matriculas, pagamentos, check-ins, validacao de acesso, RBAC, frontend e gateway existem. O bloqueio esta em continuidade operacional.

Decisao atual:

- **GO para staging operacional**.
- **GO para piloto controlado** somente com backup, restore ensaiado, plano manual da recepcao, smoke test e responsavel tecnico definidos.
- **NO-GO para producao real ampla** enquanto faltar backup testado, observabilidade, rollback, plano manual e validacao operacional.

## Arquitetura Alvo Realista

```text
Internet
  |
HTTPS / Reverse proxy
  |
Spring Boot API + frontend estatico
  |
PostgreSQL
  |
Backup automatico + logs persistidos + monitoramento externo

Gateway local da catraca
  |
Snapshot local de autorizacoes
  |
Sincronizacao com backend
```

Esta Phase 0 nao exige Django, Gunicorn, Redis obrigatorio, Redis Sentinel, ELK obrigatorio, PostgreSQL replicado, multiplos app servers ou blue-green completo. Essas opcoes podem ser avaliadas depois que a operacao basica estiver estavel.

## Status Atual

| Area | Status | Leitura senior |
| --- | --- | --- |
| Dominio principal | Bom | Fluxos centrais existem e possuem testes. |
| Banco | Bom com pendencias | PostgreSQL, Flyway e `ddl-auto=validate`; falta restore testado. |
| Seguranca backend | Boa base | JWT, RBAC, secrets obrigatorios em prod e Swagger desabilitado. |
| Frontend | Bom para piloto | Contrato API/UI maduro; token em `localStorage` segue como risco documentado. |
| Gateway | Base arquitetural | Vendor-agnostic, snapshot e sync; ainda nao homologado com hardware real. |
| Deploy | Parcial | Docker e compose de producao existem; faltavam runbooks. |
| Observabilidade | Pendente | Healthcheck simples existe; faltam alertas e rotina de logs. |
| Continuidade manual | Pendente | Recepcao precisa caminho operacional quando sistema/catraca falhar. |

## Phase 0: Blindagem Operacional

Objetivo: permitir staging operacional e piloto controlado sem transformar o projeto em uma stack enterprise.

Criterios de aceite:

- Backup diario PostgreSQL configurado.
- Retencao entre 7 e 30 dias.
- Restore ensaiado em banco separado.
- Runbooks de backup, restore, deploy, rollback, observabilidade e continuidade criados.
- Staging com PostgreSQL real documentado ou criado.
- Logs de backend, gateway e backup persistidos ou coletados fora dos containers.
- Healthcheck externo ativo.
- Plano manual da recepcao pronto.
- Smoke test operacional aprovado.
- Checklist Go/No-Go preenchido.

## Phase 1: Piloto Controlado

Duracao sugerida: 1 a 2 semanas.

Como operar:

- Sistema roda em paralelo com controle manual.
- Acesso fisico nao depende exclusivamente do sistema.
- Pagamentos e check-ins sao conferidos diariamente.
- Divergencias viram ajuste de sistema ou procedimento.
- Gateway/catraca deve ser observado antes de virar caminho unico.

Criterio para avancar:

- Nenhuma perda de dados.
- Backup e restore confirmados.
- Recepcao consegue operar com pouca ajuda.
- Sem bloqueio indevido sem caminho manual.
- Logs e alertas detectam falhas antes do usuario reclamar.

## Phase 2: Producao Assistida

Duracao sugerida: 2 a 4 semanas.

Como operar:

- Sistema vira principal para cadastros, matriculas, pagamentos e acesso.
- Plano manual fica como contingencia.
- Deploys apenas em janela controlada.
- Responsavel tecnico acompanha incidentes e metricas.

Criterio para producao normal:

- Nenhum incidente P0.
- Restore testado pelo menos uma vez.
- Rollback ensaiado.
- Equipe treinada.
- Gestor aceita o escopo financeiro e operacional atual.

## Operacao Continua

Rotina diaria:

- Conferir API, frontend e gateway.
- Conferir backup da madrugada.
- Revisar erros e eventos pendentes.
- Validar pagamentos/check-ins do dia anterior.

Rotina semanal:

- Testar login de perfis principais.
- Revisar usuarios ativos.
- Verificar disco do PostgreSQL.
- Revisar incidentes e pendencias.

Rotina mensal:

- Testar restore em ambiente separado.
- Revisar permissoes.
- Atualizar dependencias com criterio.
- Revisar politica de dados e retencao.

## Riscos Reais

| Risco | Impacto | Mitigacao |
| --- | --- | --- |
| Falha de backup | Perda de dados financeiros e operacionais | Backup diario, alerta e restore testado. |
| API indisponivel | Recepcao nao valida aluno | Healthcheck externo e plano manual. |
| Gateway/catraca offline | Fila na entrada | Liberacao manual e sync posterior. |
| Deploy ruim | Sistema para em horario comercial | Janela, backup, smoke test e rollback. |
| Banco exposto | Vazamento/alteracao de dados | Sem porta publica e credenciais fortes. |
| Token roubado por XSS | Sessao comprometida | HTTPS, CSP futura, token curto e migracao planejada. |
| Operador sem treinamento | Pagamento/matricula incorretos | Treinamento, perfis e conferencia diaria. |

## Pendencias

- Executar restore real em staging.
- Ativar monitoramento externo.
- Definir canal de alerta.
- Homologar gateway com operacao fisica.
- Planejar evolucao do armazenamento de token.
- Melhorar auditoria administrativa de acoes sensiveis.
- Validar fluxo financeiro real com o gestor.
