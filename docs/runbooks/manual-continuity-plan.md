# Plano de continuidade manual

Use este plano quando o sistema estiver indisponivel durante o piloto controlado.

## Operacao manual

- Registrar entrada/saida em planilha ou livro fisico com data, hora, nome do aluno e responsavel.
- Validar mensalidade por lista previamente exportada quando o backend estiver indisponivel.
- Marcar excecoes para conciliacao posterior.
- Nao coletar senha, token, API key ou dado sensivel desnecessario.

## Retorno do sistema

- Conferir saude da API.
- Executar smoke autenticado.
- Reconciliar registros manuais com o sistema.
- Registrar incidente e janela de indisponibilidade.

## Criterio de parada

Se login administrativo ou validacao operacional principal falhar durante o piloto, pausar o uso operacional completo e voltar ao processo manual ate correcao.
