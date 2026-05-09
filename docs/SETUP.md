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

## Rodar os testes

No WSL:

```bash
./mvnw test
```

No PowerShell:

```powershell
.\mvnw test
```

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
