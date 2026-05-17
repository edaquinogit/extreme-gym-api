# Monolito Modular

## Status

Aceito

## Contexto

O Extreme Gym API nasceu como um MVP em Spring Boot organizado por camadas globais. Essa estrutura e simples e adequada para o inicio, mas novos modulos como usuarios, seguranca, dashboards, relatorios e integracoes podem aumentar o acoplamento entre services, DTOs, repositories e regras de negocio.

Microsservicos adicionariam complexidade operacional desnecessaria para o momento atual do produto.

## Decisao

Evoluir o sistema como monolito modular.

Cada dominio deve ser organizado gradualmente em um modulo proprio, com separacao interna entre API, aplicacao, dominio e infraestrutura quando houver ganho real de clareza.

A reorganizacao nao deve ser feita de uma vez. Modulos serao movidos conforme receberem alteracoes relevantes.

## Consequencias

- O projeto continua simples para desenvolvimento local e deploy.
- O acoplamento entre dominios tende a diminuir ao longo do tempo.
- A evolucao fica preparada para crescimento sem exigir microsservicos.
- Durante a transicao, podera existir convivencia entre a estrutura atual por camadas e a nova estrutura por dominio.
