# Clock Injetavel Para Regras Temporais

## Status

Aceito

## Contexto

Algumas regras dependem da data ou hora atual, como vencimento de matricula, registro de pagamento e check-in.

Usar `LocalDate.now()` ou `LocalDateTime.now()` diretamente em services dificulta testes determinísticos e pode gerar inconsistencias de fuso horario.

## Decisao

Introduzir um bean de `Clock` compartilhado e usa-lo nos services que dependem de data ou hora atual.

Novas regras temporais devem receber data/hora a partir do `Clock` injetado.

## Consequencias

- Testes de regras temporais ficam mais previsiveis.
- O comportamento de data/hora fica centralizado.
- A migracao deve ser incremental para evitar alteracoes amplas desnecessarias.
