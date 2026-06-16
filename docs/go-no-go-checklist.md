# Go/No-Go Checklist

## GO para Staging

- [ ] Docker Compose de staging/producao valida com `docker compose config`.
- [ ] PostgreSQL real em uso.
- [ ] H2 nao usado em staging.
- [ ] Flyway executa migrations.
- [ ] `SPRING_PROFILES_ACTIVE=prod` ou profile staging equivalente.
- [ ] Swagger desabilitado ou protegido.
- [ ] CORS aponta para origem de staging.
- [ ] `JWT_SECRET`, `ADMIN_PASSWORD` e senhas de banco definidos por ambiente.
- [ ] Frontend abre.
- [ ] Login funciona.
- [ ] Gateway inicia em modo controlado.
- [ ] Logs persistidos/coletados.

## GO para Piloto Controlado

- [ ] Backup diario funcionando.
- [ ] Restore ensaiado ou testado.
- [ ] Plano manual da recepcao pronto.
- [ ] Responsavel tecnico definido.
- [ ] Smoke test aprovado.
- [ ] Healthcheck externo ativo.
- [ ] Alertas basicos ativos.
- [ ] Equipe treinada minimamente.
- [ ] Conferencia diaria de pagamentos/check-ins definida.
- [ ] Gateway/catraca nao e caminho unico.

## NO-GO para Producao Ampla

Marque NO-GO se qualquer item abaixo for verdadeiro:

- [ ] Sem backup.
- [ ] Sem restore testado.
- [ ] Sem rollback.
- [ ] Sem plano manual.
- [ ] Sem alertas.
- [ ] Sem logs.
- [ ] Sem responsavel tecnico.
- [ ] Gateway/catraca nao homologado.
- [ ] Banco exposto publicamente.
- [ ] Secrets em codigo ou `.env` commitado.
- [ ] Swagger publico em producao.
- [ ] Equipe sem treinamento.
