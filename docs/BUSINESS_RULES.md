# Business Rules

Regras de negocio atualmente documentadas para a Extreme Gym API.

## Alunos

- Nome e obrigatorio.
- Nome deve ter no minimo 3 caracteres.
- Email e obrigatorio.
- Email deve possuir formato valido.
- Telefone e obrigatorio.
- Telefone deve ter entre 8 e 15 caracteres.
- Email nao pode ser duplicado no cadastro.
- Na atualizacao, o aluno pode manter o proprio email.
- Na atualizacao, outro aluno nao pode usar email ja cadastrado.
- Todo aluno novo recebe status inicial `ATIVO`.
- A data de cadastro e definida automaticamente na persistencia.

## Planos

- Nome e obrigatorio.
- Nome deve ter entre 3 e 80 caracteres.
- Nome nao pode ser duplicado no cadastro.
- Na atualizacao, o plano pode manter o proprio nome.
- Na atualizacao, outro plano nao pode usar nome ja cadastrado.
- Valor mensal e obrigatorio.
- Valor mensal deve ser maior que zero.
- Duracao em dias e obrigatoria.
- Duracao em dias deve ser maior que zero.
- Todo plano novo inicia ativo.
- A data de cadastro e definida automaticamente na persistencia.
- A remocao de plano e logica, alterando `ativo` para `false`.
- Plano inativo nao deve ser usado em matriculas futuramente.

## Organizacao das regras

- Validacoes de formato e obrigatoriedade ficam nos DTOs de entrada.
- Regras de negocio ficam na camada de service.
- Controllers devem permanecer finos, apenas recebendo requisicoes, acionando o service e retornando respostas HTTP.
- Repository deve permanecer restrito a acesso a dados.

## Fora do escopo atual

Ainda nao existem regras implementadas para:

- Matriculas.
- Pagamentos.
- Check-ins.
- Autenticacao.
- Controle fisico de acesso.
