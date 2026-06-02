# Requisitos de contrato para dispositivo de acesso

Sprint 4 - preparacao para catraca fisica e gateway.

## Objetivo

Definir o contrato minimo que o frontend precisa do backend para operar uma catraca hibrida sem criar endpoint falso, mock de producao ou regra de negocio local.

Nesta sprint o frontend fica preparado apenas para exibir status quando o contrato existir. Nenhuma integracao com fabricante, Face ID ou gateway local foi implementada.

## Dispositivo de acesso

Endpoint sugerido para leitura administrativa:

```http
GET /dispositivos-acesso
GET /dispositivos-acesso/{id}
```

Campos minimos:

- `id`: identificador unico do dispositivo.
- `nome`: nome operacional visivel para recepcao/catraca.
- `tipo`: `CATRACA`, `GATEWAY`, `FACIAL` ou outro tipo suportado.
- `fabricante`: fabricante do equipamento, quando definido.
- `modelo`: modelo do equipamento, quando definido.
- `status`: `ONLINE`, `OFFLINE`, `MANUTENCAO` ou `DESCONHECIDO`.
- `modoOperacao`: `ONLINE`, `OFFLINE` ou `HIBRIDO`.
- `ultimaComunicacaoEm`: data/hora da ultima comunicacao recebida.
- `eventosPendentes`: quantidade de eventos aguardando sincronizacao.
- `unidade`: unidade/polo associado ao dispositivo.

## Evento de acesso

Endpoint sugerido para historico operacional:

```http
GET /eventos-acesso?dispositivoId={id}&data={yyyy-mm-dd}&limit=50
```

Campos minimos:

- `id`
- `alunoNome`
- `alunoId`
- `dispositivoNome`
- `origem`: `MANUAL`, `CATRACA`, `GATEWAY`, `FACIAL`.
- `modo`: `ONLINE`, `OFFLINE`, `HIBRIDO`.
- `resultado`: `LIBERADO` ou `BLOQUEADO`.
- `motivo`
- `dataHora`
- `sincronizado`

## Resposta de validacao

O endpoint atual `POST /acessos/validar` pode ser evoluido ou um endpoint especifico pode ser criado para eventos de catraca.

Campos minimos esperados:

- `permitido`
- `resultado`
- `motivo`
- `aluno`
- `matricula`
- `plano`
- `dispositivo`
- `dataHora`
- `eventoId`

## Snapshot/offline

Para operacao hibrida ou offline, o frontend administrativo precisa visualizar a saude do snapshot usado pelo gateway.

Campos minimos:

- `versaoSnapshot`
- `geradoEm`
- `validoAte`
- `totalCredenciais`
- `totalLiberados`
- `totalBloqueados`
- `ultimoSync`
- `statusSync`

## Regras de permissao e auditoria

- `ADMIN` pode autorizar operacoes manuais quando o fluxo existir.
- `RECEPCAO` pode operar manualmente somente se a regra do sistema permitir.
- `CATRACA` deve operar validacao, sem acesso a financeiro ou configuracoes.
- Toda liberacao manual futura deve exigir motivo.
- Toda liberacao manual futura deve gerar evento auditavel com usuario, data/hora e justificativa.

## Pendencias para a proxima sprint

- Definir fabricante, modelo e API do equipamento real.
- Definir se o gateway sera local, cloud ou hibrido.
- Confirmar formato de evento offline e reconciliacao.
- Definir endpoint de status do dispositivo.
- Definir endpoint de historico de eventos de acesso.
- Definir se `POST /acessos/validar` sera mantido ou especializado para catraca.
