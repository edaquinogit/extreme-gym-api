# Integração frontend de controle de acesso

## Visão geral

Esta sprint integra o frontend aos contratos reais do backend para dispositivos de acesso, eventos de acesso, credenciais de acesso e busca global server-side.

O frontend continua sem integração direta com catraca física, sem reconhecimento facial próprio, sem upload de face e sem uso de API key de dispositivo no navegador.

## Endpoints consumidos

- `GET /dispositivos-acesso`
- `GET /dispositivos-acesso/{id}`
- `POST /dispositivos-acesso`
- `PUT /dispositivos-acesso/{id}`
- `PATCH /dispositivos-acesso/{id}/status`
- `GET /eventos-acesso`
- `GET /eventos-acesso/hoje`
- `GET /eventos-acesso/aluno/{alunoId}`
- `GET /eventos-acesso/dispositivo/{dispositivoId}`
- `GET /alunos/{alunoId}/credenciais-acesso`
- `POST /alunos/{alunoId}/credenciais-acesso`
- `PATCH /credenciais-acesso/{id}/revogar`
- `GET /busca-global`

O frontend administrativo nao chama `POST /dispositivos-acesso/{id}/heartbeat`, nao chama `POST /eventos-acesso/sincronizar-lote` e nao envia `X-Device-Api-Key`.

## Types criados

- `accessDevice.ts`
- `accessEvent.ts`
- `accessCredential.ts`
- `globalSearch.ts`

## Mappers criados

- `accessDeviceMapper.ts`
- `accessEventMapper.ts`
- `accessCredentialMapper.ts`
- `globalSearchMapper.ts`

Os mappers separam DTO bruto da API de ViewModel da UI, traduzem enums, formatam datas em pt-BR e mascaram identificadores externos quando necessario.

## Services criados

- `accessDeviceService.ts`
- `accessEventService.ts`
- `accessCredentialService.ts`
- `globalSearchService.ts`

Todos usam `httpClient` existente e dependem do JWT humano ja configurado no frontend.

## Telas e componentes

- `DispositivosAcessoPage`: lista dispositivos e status operacional.
- `EventosAcessoPage`: lista eventos de hoje e filtros locais por resultado, origem e modo.
- `CredenciaisAcessoAluno`: componente reutilizavel para perfil futuro do aluno.
- `GlobalSearch`: busca server-side com debounce e resultados agrupados.
- `CatracaPage`: agora consulta dispositivos reais e eventos recentes de hoje.

## Permissoes

- `ADMIN`: acessa dispositivos, eventos e busca conforme backend.
- `RECEPCAO`: acessa eventos e operacao de acesso.
- `CATRACA`: mantem acesso a validacao/catraca e nao ve telas administrativas de dispositivos.

O frontend apenas melhora UX; o backend continua sendo a camada obrigatoria de seguranca.

## Limitações

- Rotas de detalhe como `/alunos/{id}` ainda nao existem no SPA. A busca global exibe o resultado, mas nao cria rota falsa.
- Cadastro/edicao administrativa de dispositivo com segredo tecnico nao foi exposto em UI para evitar tratamento inseguro de API key no navegador.
- Credenciais exibem somente referencia mascarada.

## Nao implementado

- Reconhecimento facial.
- Upload de face.
- Armazenamento de biometria no frontend.
- Gateway local.
- Integracao com fabricante.
- Heartbeat tecnico pelo navegador.
- Mock de producao.

## Pendencias para gateway/hardware

- Definir fabricante/modelo/API oficial.
- Definir seleção de dispositivo padrao por estação.
- Definir contrato final de gateway local.
- Definir HMAC/timestamp para canal tecnico.
- Homologar evento online/offline com equipamento real.
