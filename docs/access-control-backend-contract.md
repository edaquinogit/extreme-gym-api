# Contrato backend de controle de acesso fisico

## Visao geral

Esta sprint cria a base backend real para futura operacao hibrida com catraca fisica e gateway local. O backend passa a modelar dispositivos de acesso, credenciais de acesso, eventos de catraca/acesso, validacao por dispositivo, snapshot minimo de autorizados, auditoria operacional e busca global server-side.

O contrato e deliberadamente conservador: nao ha integracao direta com catraca, nao ha payload de fabricante, nao ha reconhecimento facial proprio e nao ha armazenamento de imagem facial ou template biometrico real.

## Limites desta sprint

- Implementado contrato backend real e persistente.
- Implementada API key tecnica por dispositivo, armazenada como hash.
- Implementado snapshot minimo para gateway futuro.
- Implementada busca global server-side com limite por grupo.
- Nao implementado gateway local.
- Nao implementada integracao com fabricante.
- Nao implementado cache offline local.
- Nao implementada biometria real.

## Entidades criadas

- `DispositivoAcesso`: representa catracas, recepcao ou dispositivos futuros.
- `EventoAcesso`: registra eventos online/offline recebidos do canal tecnico.
- `CredencialAcesso`: referencia credenciais externas sem armazenar biometria bruta.
- `AuditoriaAcesso`: registra acoes sensiveis do modulo.

## Endpoints criados

### Dispositivos

- `GET /dispositivos-acesso`
- `GET /dispositivos-acesso/{id}`
- `POST /dispositivos-acesso`
- `PUT /dispositivos-acesso/{id}`
- `PATCH /dispositivos-acesso/{id}/status`
- `POST /dispositivos-acesso/{id}/heartbeat`

### Eventos

- `GET /eventos-acesso`
- `GET /eventos-acesso/hoje`
- `GET /eventos-acesso/aluno/{alunoId}`
- `GET /eventos-acesso/dispositivo/{dispositivoId}`
- `POST /eventos-acesso`
- `POST /eventos-acesso/sincronizar-lote`

### Credenciais

- `GET /alunos/{alunoId}/credenciais-acesso`
- `POST /alunos/{alunoId}/credenciais-acesso`
- `PATCH /credenciais-acesso/{id}/revogar`

### Controle de acesso

- `POST /controle-acesso/validar-dispositivo`
- `GET /controle-acesso/snapshot-autorizados`
- `GET /controle-acesso/snapshot-autorizados/{dispositivoId}`

### Busca global

- `GET /busca-global?termo={termo}&limit=5`

## Seguranca

Os endpoints novos continuam protegidos pelo Spring Security/JWT conforme perfil de usuario. Endpoints tecnicos de dispositivo tambem suportam `X-Device-Api-Key` quando `ACCESS_DEVICE_API_KEY_REQUIRED=true`.

A API key nao e retornada em respostas e e armazenada somente como hash BCrypt em `dispositivos_acesso.api_key_hash`.

Regras:

- `ADMIN` administra dispositivos.
- `RECEPCAO` consulta dados operacionais.
- `CATRACA` pode validar acesso e registrar eventos tecnicos.
- `CATRACA` nao recebe dados financeiros na busca global.
- Dispositivo `INATIVO`, `MANUTENCAO` ou `OFFLINE` nao e considerado operacional para validacao automatica.

## Auditoria

Foi criada auditoria minima para:

- criacao e alteracao de dispositivo;
- alteracao de status;
- heartbeat;
- validacao por dispositivo;
- registro de evento;
- sincronizacao de evento;
- criacao e revogacao de credencial;
- falha de autenticacao tecnica.

## Snapshot

O snapshot retorna somente:

- `alunoId`;
- tipo da credencial;
- identificador externo;
- liberado/bloqueado;
- motivo de bloqueio;
- validade;
- data de atualizacao da credencial.

Nao retorna CPF, e-mail, telefone, endereco, dados financeiros, imagem facial, template biometrico ou historico completo.

## Busca global

A busca global e server-side, exige termo minimo de 2 caracteres e aplica limite por grupo. O resultado retorna apenas dados resumidos e metadados minimos.

## Flags

- `ACCESS_DEVICE_MODULE_ENABLED`
- `ACCESS_DEVICE_API_KEY_REQUIRED`
- `ACCESS_SNAPSHOT_ENABLED`
- `ACCESS_SNAPSHOT_VALIDITY_MINUTES`
- `GLOBAL_SEARCH_ENABLED`

## Pendencias para gateway local

- Definir formato final de lotes offline.
- Definir politica de retry e reconciliacao.
- Definir cache local e expurgo.
- Definir assinatura HMAC e janela de timestamp.
- Definir monitoramento de eventos pendentes.

## Pendencias para fabricante da catraca

- Fabricante e modelo.
- API/protocolo oficial.
- Contrato de credenciais externas.
- Formato de eventos offline.
- Regras de acionamento fisico de liberacao/bloqueio.

## Pendencias LGPD

- Termo especifico para biometria, se houver.
- Decisao formal sobre armazenamento ou delegacao de template biometrico.
- Politica de retencao de eventos.
- Processo de revogacao e eliminacao de credencial.

## Proximos passos de homologacao

1. Escolher fabricante/modelo.
2. Obter documentacao oficial da API.
3. Implementar gateway local isolado.
4. Validar API key/HMAC em ambiente controlado.
5. Homologar eventos online/offline com catraca real.
