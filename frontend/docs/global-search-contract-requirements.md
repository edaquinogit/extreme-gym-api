# Requisitos de contrato para busca global

Sprint 4 - especificacao do contrato real.

## Diagnostico

A busca global nao deve ser implementada no frontend enquanto nao houver endpoint real. Carregar alunos, matriculas, pagamentos e planos completos para filtrar no navegador prejudica performance, aumenta risco de vazamento de dados e ignora permissoes por perfil.

## Endpoint sugerido

```http
GET /busca-global?termo={termo}&limit=5
```

## Resposta sugerida

```json
{
  "alunos": [],
  "matriculas": [],
  "pagamentos": [],
  "planos": []
}
```

Cada item de resultado deve conter:

- `id`
- `tipo`
- `titulo`
- `subtitulo`
- `status`
- `rota`
- `metadata`

## Regras obrigatorias

- A busca deve ser server-side.
- O frontend nao deve carregar todas as entidades para simular busca global.
- O backend deve limitar resultados por grupo.
- O backend deve respeitar o perfil do usuario autenticado.
- Dados sensiveis nao devem ser expostos em resultados resumidos.
- `CATRACA` nao deve visualizar dados financeiros.
- `RECEPCAO` pode visualizar dados operacionais.
- `ADMIN` pode visualizar tudo que a regra de negocio permitir.

## Estados esperados no frontend

- Campo visual de busca disponivel na topbar.
- Estado de carregamento enquanto a API responde.
- Estado vazio quando nao houver resultado.
- Estado de erro amigavel quando a API falhar.
- Navegacao para `rota` retornada pelo backend.

## Pendencias

- Confirmar endpoint real.
- Confirmar payload final por tipo de resultado.
- Confirmar permissoes por perfil.
- Confirmar limites por grupo.
- Confirmar quais campos financeiros podem aparecer para `ADMIN` e `RECEPCAO`.
