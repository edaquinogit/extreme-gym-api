# Access Gateway Architecture

## Objetivo do Gateway
O gateway local é um serviço independente que opera dentro da rede local da academia. Ele faz a mediação entre o dispositivo de controle de acesso físico e o backend Extreme Gym, permitindo:

- validação de acesso online e offline;
- cache local de snapshot de autorizações;
- registro de eventos locais;
- sincronização de eventos quando a conexão com o backend retorna;
- envio de heartbeat periódico;
- adaptação para múltiplos fabricantes futuros.

## Responsabilidades

- manter o snapshot local de pessoas/credenciais autorizadas;
- validar acessos usando o backend como autoridade principal;
- operar offline de forma conservadora quando o backend não está disponível;
- registrar todos os eventos locais com idempotência;
- sincronizar eventos pendentes ao backend;
- expor APIs de diagnóstico locais;
- proteger segredos do dispositivo e do gateway.

## O que o gateway faz

- consulta snapshot autorizado do backend;
- recebe leituras de credenciais do adapter;
- decide liberar ou bloquear acesso;
- registra evento local antes de liberar qualquer acesso;
- sincroniza eventos em lote com o backend;
- mantém heartbeat de status do gateway.

## O que o gateway não faz

- não implementa reconhecimento facial próprio;
- não armazena imagem facial;
- não armazena template biométrico real;
- não depende do frontend para validação de acesso;
- não expõe segredos técnicos ao navegador;
- não inventa APIs de fabricante real.

## Fluxo Online

1. O adapter entrega `credentialType` e `externalIdentifier`.
2. O gateway gera `idempotencyKey` para o evento.
3. O gateway tenta validação online no backend.
4. Se o backend responder, o gateway registra o evento local e retorna liberar/bloquear.
5. Se a validação online falhar, o gateway tenta o fluxo offline (se habilitado).
6. Se offline desabilitado, o gateway bloqueia por segurança.

## Fluxo Offline

1. O gateway verifica se há snapshot local válido.
2. O gateway busca a credencial no snapshot.
3. Libera acesso apenas se `allowed=true`, dados existirem e não estiverem expirados.
4. Registra evento local como `offline` e marca `synced=false`.
5. Se o snapshot estiver expirado ou ausente, o gateway bloqueia.

## Fluxo de Sincronização

1. O gateway coleta eventos não sincronizados do armazenamento local.
2. Envia lotes ao backend usando `SYNC_BATCH_SIZE`.
3. Usa `idempotencyKey` para evitar duplicação.
4. Marca eventos como sincronizados quando o backend confirma.
5. Em caso de falha, mantém o evento e aumenta tentativas.

## Fluxo de Heartbeat

- Periodicamente o gateway envia heartbeat ao backend.
- O heartbeat inclui `gatewayId`, `deviceId`, `status`, `modo` e `pendingEvents`.
- Falhas no heartbeat não derrubam o gateway.
- Falhas consecutivas colocam o backend em modo offline localmente.

## Política de Cache

- O snapshot local é obtido do backend ao inicializar e periodicamente.
- O snapshot é armazenado em SQLite.
- Apenas dados mínimos para decisão offline são mantidos.
- Não se armazenam imagens ou templates biométricos.

## Política de Snapshot Expirado

- Se o snapshot expirar, o gateway entra em modo conservador.
- A política padrão é bloquear todas as validações offline.
- O gateway pode continuar a operar online se o backend estiver disponível.

## Política Conservadora de Bloqueio

- ausência de credencial no cache = bloqueio;
- snapshot expirado = bloqueio;
- dados incompletos/ausentes = bloqueio;
- `allowed=false` = bloqueio;
- falha de leitura local = bloqueio.

## Estratégia de Adapters por Fabricante

- Um adapter vendor-agnostic define interface genérica:
  - `start()`;
  - `stop()`;
  - `onCredentialRead(callback)`;
  - `unlock()`;
  - `deny(reason)`;
  - `getStatus()`.
- Fabricantes específicos implementam essa interface.
- O gateway mantém apenas a interface comum.
- Um adapter de simulação é fornecido apenas para desenvolvimento.

## Variáveis de Ambiente

- `GATEWAY_ID`
- `GATEWAY_NAME`
- `BACKEND_BASE_URL`
- `DEVICE_ID`
- `DEVICE_API_KEY`
- `DEVICE_HMAC_SECRET`
- `GATEWAY_PORT`
- `SNAPSHOT_REFRESH_INTERVAL_SECONDS`
- `SNAPSHOT_TTL_SECONDS`
- `OFFLINE_MODE_ENABLED`
- `OFFLINE_STRICT_MODE`
- `SYNC_INTERVAL_SECONDS`
- `SYNC_BATCH_SIZE`
- `LOG_LEVEL`

## Estratégia de Segurança

- O gateway guarda `DEVICE_API_KEY` e `DEVICE_HMAC_SECRET` apenas no servidor local.
- O navegador nunca recebe esses segredos.
- As chamadas ao backend usam cabeçalhos seguros:
  - `X-Device-Api-Key`
  - `X-Gateway-Id`
  - `X-Request-Timestamp`
  - `X-Request-Signature`
- O gateway falha ao iniciar se configuração obrigatória estiver ausente.
- Logs mascaram segredos.

## Estratégia de Logs

- Logs estruturados para eventos de snapshot, sync e heartbeat.
- Logs de erro detalham causa, mas não expõem segredos.
- Logs de produção não contêm `DEVICE_API_KEY`, `DEVICE_HMAC_SECRET` ou dados biométricos.

## Plano de Homologação Futura

- validar leitura de credenciais com adapter mock no ambiente local;
- homologar fluxo de snapshot com backend real;
- testar sincronização de eventos offline/online;
- adicionar adapter real quando API de fabricante for disponibilizada;
- validar heartbeat e recuperação após perda de backend;
- executar testes end-to-end com dispositivo físico assim que disponível.

## Notas Importantes

- o frontend não conversa diretamente com a catraca;
- o backend continua autoridade principal online;
- o gateway usa apenas identificador externo do adapter;
- face ID depende do equipamento/fabricante e não será implementado aqui.
