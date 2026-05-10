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
- Plano inativo nao deve ser usado em matriculas.

## Matriculas

- Matricula conecta um aluno a um plano.
- Aluno e obrigatorio.
- Plano e obrigatorio.
- Data de inicio e obrigatoria.
- Aluno deve existir para criar matricula.
- Plano deve existir para criar matricula.
- Plano precisa estar ativo para ser usado em matricula.
- Aluno nao pode ter mais de uma matricula `ATIVA` ao mesmo tempo.
- A data final da matricula e calculada com base na data de inicio mais a duracao em dias do plano.
- Toda matricula nova inicia com status `ATIVA`.
- A data de cadastro e definida automaticamente na persistencia.
- Cancelamento altera o status da matricula para `CANCELADA`.
- Matricula nao deve ser apagada fisicamente.

## Pagamentos

- Pagamento pertence a uma matricula.
- Matricula e obrigatoria.
- Matricula deve existir para registrar pagamento.
- Matricula precisa estar `ATIVA` para receber pagamento.
- Matricula `CANCELADA` nao recebe pagamento.
- Matricula `VENCIDA` nao recebe pagamento nesta versao inicial.
- Valor e obrigatorio.
- Valor deve ser maior que zero.
- Forma de pagamento e obrigatoria.
- Pagamento registrado inicia com status `PAGO`.
- Pagamento `PAGO` duplicado para a mesma matricula nao e permitido nesta versao.
- A data de pagamento e definida automaticamente no registro.
- A data de cadastro e definida automaticamente na persistencia.
- Cancelamento altera o status do pagamento para `CANCELADO`.
- Pagamento nao deve ser apagado fisicamente.

## Check-ins

- Check-in pertence a um aluno.
- Check-in permitido deve estar associado a uma matricula.
- Aluno deve existir para registrar check-in.
- Aluno `BLOQUEADO` nao pode entrar.
- Aluno `CANCELADO` nao pode entrar.
- Aluno `INADIMPLENTE` nao pode entrar.
- Aluno precisa possuir matricula `ATIVA`.
- Matricula ativa nao pode estar vencida pela data final.
- Matricula precisa possuir pagamento `PAGO`.
- Tentativa de check-in bloqueada tambem deve ser registrada.
- O motivo do bloqueio deve ser claro.
- Check-in permitido deve retornar o motivo `Check-in permitido`.
- Respostas de check-in nao devem expor entidades completas de aluno ou matricula.
- A data e hora do check-in sao definidas automaticamente no registro.

## Validacao de Acesso

- Aluno deve existir para validar acesso.
- Aluno `BLOQUEADO` nao pode acessar.
- Aluno `CANCELADO` nao pode acessar.
- Aluno `INADIMPLENTE` nao pode acessar.
- Aluno precisa possuir matricula `ATIVA`.
- Matricula ativa nao pode estar vencida pela data final.
- Matricula precisa possuir pagamento `PAGO`.
- Quando todas as regras passam, o acesso deve ser liberado com motivo `Acesso liberado`.
- Quando alguma regra falha, o acesso deve ser bloqueado com motivo claro.
- Validacao de acesso nao registra check-in.
- Validacao de acesso nao altera dados no banco.
- Respostas de validacao de acesso nao devem expor entidades completas de aluno ou matricula.
- Catraca, QR Code e Face ID sao integracoes futuras, ainda nao implementadas.

## Automacao de lembretes e retencao de alunos

Regras futuras, ainda nao implementadas:

- O sistema podera identificar alunos com matricula ativa e varios dias sem check-in.
- Aluno ha 7 dias sem frequentar podera receber lembrete de retorno.
- Aluno ha 10 dias sem frequentar e com mensalidade atrasada podera receber lembrete financeiro ou relacional.
- A primeira implementacao podera ser apenas simulacao ou registro interno de notificacoes.
- Envio real por WhatsApp fica fora do MVP atual.
- Integracao real devera usar WhatsApp Business Platform ou provedor autorizado.
- Mensagens automaticas deverao respeitar opt-in do aluno, templates aprovados e LGPD.
- A regra de frequencia dependera dos registros do modulo de Check-ins.
- A regra de inadimplencia dependera da evolucao do modulo de Pagamentos com vencimentos.
- Esta evolucao nao cria, no momento, entidades de notificacao, services, controllers, jobs ou integracoes externas.

## Organizacao das regras

- Validacoes de formato e obrigatoriedade ficam nos DTOs de entrada.
- Regras de negocio ficam na camada de service.
- Controllers devem permanecer finos, apenas recebendo requisicoes, acionando o service e retornando respostas HTTP.
- Repository deve permanecer restrito a acesso a dados.

## Fora do escopo atual

Ainda nao existem regras implementadas para:

- Autenticacao.
- Controle fisico de acesso.
- Envio real de mensagens por WhatsApp.
