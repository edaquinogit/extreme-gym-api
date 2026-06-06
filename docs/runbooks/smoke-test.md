# Smoke Test Operacional

Execute apos deploy, restore ou mudanca de ambiente.

## API

- [ ] `GET /` retorna mensagem da API.
- [ ] Logs nao mostram erro de startup.
- [ ] Flyway terminou sem erro.

## Frontend

- [ ] URL abre.
- [ ] Tela de login carrega.
- [ ] Login ADMIN funciona.
- [ ] Dashboard carrega.

## Dominio

- [ ] Listar alunos.
- [ ] Criar aluno teste ou validar aluno existente.
- [ ] Criar/validar plano.
- [ ] Criar matricula.
- [ ] Registrar pagamento.
- [ ] Validar acesso liberado.
- [ ] Validar acesso bloqueado.
- [ ] Registrar check-in.
- [ ] Consultar eventos de acesso.
- [ ] Consultar dispositivos de acesso.
- [ ] Rodar busca global.

## Gateway

- [ ] `GET /health` retorna `status=ok`.
- [ ] `GET /status` mostra `pendingEvents`.
- [ ] `POST /admin/heartbeat` com `x-admin-api-key` funciona.
- [ ] `POST /admin/snapshot-refresh` com `x-admin-api-key` funciona em ambiente controlado.
- [ ] `POST /admin/sync` com `x-admin-api-key` funciona.

## Operacao

- [ ] Backup recente existe.
- [ ] `scripts/verify-backup.sh` passa.
- [ ] Logs de backup existem.
- [ ] Plano manual da recepcao esta disponivel.
- [ ] Responsavel tecnico foi definido.
