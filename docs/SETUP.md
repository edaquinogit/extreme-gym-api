# Setup

## Pre-requisitos

- Java 21
- Docker
- Maven Wrapper incluso no projeto

O projeto nao exige Maven instalado globalmente, pois usa `mvnw` e `mvnw.cmd`.

## Verificar Java

No WSL ou PowerShell:

```bash
java -version
```

O resultado esperado deve indicar Java 21.

Tambem e recomendado verificar o `JAVA_HOME`:

No WSL:

```bash
echo $JAVA_HOME
```

No PowerShell:

```powershell
echo $env:JAVA_HOME
```

## Subir PostgreSQL

Entre na raiz real do projeto, onde estao `pom.xml` e `docker-compose.yml`.

No WSL:

```bash
cd /mnt/c/Users/ednal/Documents/Projetos/extreme-gym-api/extreme-gym-api
docker compose up -d
docker ps
```

No PowerShell:

```powershell
cd C:\Users\ednal\Documents\Projetos\extreme-gym-api\extreme-gym-api
docker compose up -d
docker ps
```

O container esperado e `extreme-postgres`.

## Rodar a aplicacao

No WSL:

```bash
./mvnw spring-boot:run
```

No PowerShell:

```powershell
.\mvnw spring-boot:run
```

Quando a aplicacao estiver rodando, valide o endpoint raiz:

```bash
curl http://localhost:8080/
```

Resposta esperada:

```json
{
  "message": "Extreme Gym API is running"
}
```

## Acessar Swagger

Com a aplicacao rodando em `localhost:8080`, a documentacao interativa da API fica disponivel em:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Swagger UI alternativo: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

O Swagger permite visualizar e testar endpoints da API pelo navegador. Ele nao substitui autenticacao, deploy ou configuracoes de producao.

## Erro: porta 8080 ja esta em uso

Se a aplicacao falhar com a mensagem `Port 8080 was already in use`, ja existe outro processo usando a porta `8080`.

No WSL, identifique o processo:

```bash
lsof -i :8080
```

O comando mostra uma linha com o processo e o PID real. Exemplo:

```bash
COMMAND  PID  USER   FD   TYPE DEVICE SIZE/OFF NODE NAME
java    3418 ednal   78u  IPv6  48343      0t0  TCP *:http-alt (LISTEN)
```

Nesse exemplo, o PID real e `3418`.

Quando uma instrucao mostrar `PID_REAL` ou `NUMERO_DO_PID`, isso e apenas um placeholder. Substitua pelo numero real exibido no seu terminal.

Para encerrar o processo:

```bash
kill -9 PID_REAL
```

Usando o exemplo acima:

```bash
kill -9 3418
```

Confirme que a porta ficou livre:

```bash
lsof -i :8080
```

Se o comando nao retornar nenhum processo, rode a aplicacao novamente:

```bash
./mvnw spring-boot:run
```

A aplicacao deve subir exibindo mensagens como `Tomcat started on port 8080` e `Started ExtremeGymApiApplication`.

Se a porta continuar ocupada, repita o processo:

```bash
lsof -i :8080
kill -9 PID_REAL
```

## Rodar os testes

No WSL:

```bash
./mvnw test
```

No PowerShell:

```powershell
.\mvnw test
```

Na validacao atual do projeto, a suite automatizada passou com 62 testes e 0 falhas.

Antes de um commit, tambem e possivel rodar o script manual:

```bash
./scripts/pre-commit-check.sh
```

Esse script nao instala hooks e nao altera o comportamento do Git. Ele apenas executa a verificacao basica do projeto.

## Diferenca entre WSL e PowerShell

No WSL, o caminho do projeto usa o formato Linux:

```bash
/mnt/c/Users/ednal/Documents/Projetos/extreme-gym-api/extreme-gym-api
```

No PowerShell, o caminho usa o formato Windows:

```powershell
C:\Users\ednal\Documents\Projetos\extreme-gym-api\extreme-gym-api
```

Se o Docker Desktop estiver instalado no Windows, o WSL precisa estar integrado a ele. Caso contrario, comandos Docker podem falhar dentro do WSL mesmo funcionando no PowerShell.

## Erros conhecidos

### `docker compose up -d` retorna `unknown shorthand flag: 'd' in -d`

Esse erro normalmente indica problema na instalacao, versao ou integracao do Docker Compose no ambiente onde o comando foi executado.

Verifique:

```bash
docker --version
docker compose version
```

Possiveis solucoes:

- Atualizar o Docker Desktop.
- Habilitar a integracao do Docker Desktop com a distribuicao WSL usada.
- Fechar e abrir novamente o terminal WSL.
- Executar o comando pelo PowerShell, na raiz do projeto.
- Se estiver usando Compose legado, testar `docker-compose up -d`.

### `Connection refused` em `localhost:5432`

Esse erro significa que a aplicacao tentou acessar o PostgreSQL, mas nao encontrou nenhum banco aceitando conexao nessa porta.

Verifique se o container esta rodando:

```bash
docker ps
```

Se o container nao aparecer, suba o banco:

```bash
docker compose up -d
```

Se a porta `5432` ja estiver ocupada por outro PostgreSQL local, pare o servico conflitante ou ajuste o mapeamento de porta do Docker Compose.

### `JAVA_HOME` nao configurado

Se o Maven Wrapper nao encontrar o Java corretamente, configure o `JAVA_HOME` apontando para uma instalacao do Java 21.

No PowerShell, confirme:

```powershell
echo $env:JAVA_HOME
java -version
```

No WSL, confirme:

```bash
echo $JAVA_HOME
java -version
```

Depois de corrigir o ambiente, rode novamente:

```bash
./mvnw spring-boot:run
```
