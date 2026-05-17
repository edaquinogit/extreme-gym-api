# Paginacao Obrigatoria Em Listagens

## Status

Aceito

## Contexto

Listagens como alunos, matriculas, pagamentos e check-ins tendem a crescer rapidamente em um sistema de academia.

Retornar `List<T>` sem limite e simples no MVP, mas pode causar lentidao, consumo excessivo de memoria e contratos ruins para dashboards e frontend.

## Decisao

Adotar paginacao com `Pageable` nas listagens principais.

Endpoints com crescimento esperado devem retornar respostas paginadas e suportar filtros simples por campos relevantes.

A mudanca deve ser feita por modulo, com testes e documentacao atualizados.

## Consequencias

- A API fica mais adequada para frontend e dashboards.
- Consultas passam a ser mais previsiveis.
- Contratos de listagem mudam e precisam ser documentados.
- Consumidores devem informar ou aceitar parametros padrao de pagina e tamanho.
