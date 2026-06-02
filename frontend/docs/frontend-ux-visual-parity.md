# Sprint 2 - Visual Parity, UX e Design System

## Diagnostico visual inicial

O frontend ja possuia uma base funcional consistente da Sprint 1: rotas principais, CRUDs, mappers, services, testes de mapper, componentes compartilhados e graficos com Recharts. A camada visual, porem, ainda tinha sinais de produto em amadurecimento:

- sidebar clara e pouco diferenciada do conteudo;
- topbar com estilos inline e sem busca visual;
- dashboard mais informativo do que operacional;
- `AcessoPage` usando classes inexistentes;
- tabelas e filtros com padroes visuais variados;
- `FormField` sem associacao acessivel entre label e controle;
- graficos corretos, mas com tooltips/eixos pouco refinados;
- alguns textos sem acentos e microcopy inconsistente.

## Decisoes de design

- Direcao premium operacional: fundo claro, superficies brancas, sidebar escura e verde usado como marca e sucesso.
- Paleta semantica: verde para sucesso/marca, amarelo para atencao, vermelho para erro/bloqueio e azul para informacao.
- Componentes mantidos simples, tipados e sem biblioteca nova.
- Recharts preservado, com lazy loading ja existente no dashboard.
- Services, mappers, contratos e regras de negocio preservados.

## Tokens definidos

Foram reforcadas variaveis em `src/styles/global.css` para:

- cores de fundo, surface, texto, borda e sidebar;
- cores semanticas `success`, `warning`, `danger` e `info`;
- shadows de card/painel/modal;
- radius de 8 a 18px;
- z-index de header, overlay e modal;
- foco visivel em inputs e botoes.

## Componentes criados ou refatorados

- `Button`: variantes `primary`, `secondary`, `ghost`, `danger` e `subtle`.
- `IconButton`: botao iconico com label acessivel.
- `FilterBar`: barra reutilizavel de filtros e resumo.
- `Skeleton`: estado visual de carregamento.
- `ErrorState`: wrapper simples sobre `StateMessage`.
- `MetricCard`: variantes semanticas, loading sem inline style, suporte a icon/trend/description.
- `FormField`: label associado ao input/select/textarea com `id`, `aria-describedby`, erro e hint.
- `ConfirmDialog`: removidos estilos inline e aplicado padrao visual compartilhado.

## Paginas ajustadas

- `AdminLayout`: sidebar escura premium, item ativo mais claro, topbar com busca visual, API status, perfil e acoes.
- `Dashboard`: header operacional com acoes rapidas, KPIs com variantes, atencao imediata, graficos refinados e vencimentos proximos sem inline styles.
- `Alunos`: filtro padronizado, resumo de resultados, tabela com utilitarios visuais e modal mais consistente.
- `Planos`: tabela e acoes de linha padronizadas.
- `Matriculas`: microcopy principal revisada, cabecalhos e botoes alinhados ao novo padrao.
- `Pagamentos`: filtro por matricula em `FilterBar`, cabecalhos revisados e status preservado do backend.
- `Check-ins`: status com `StatusBadge` e empty state revisado.
- `Acesso`: pagina reconstruida no padrao administrativo, com card operacional, input grande, resultado liberado/bloqueado e botao de check-in.

## Melhorias em graficos

- Tooltips formatados em pt-BR.
- Valores monetarios em BRL no grafico de receita.
- Eixos, grid e cores refinados.
- Legends com labels formatados.
- Empty/loading/error states preservados via `ChartCard` e `EmptyChartState`.

## Acessibilidade

- Foco visivel mantido em inputs, selects, textareas e botoes.
- Botoes iconicos com `aria-label`.
- Sidebar com `aria-current` no item ativo.
- `FormField` agora conecta label, erro e hint ao controle.
- Busca global visual usa input de `type="search"`.
- Layout responsivo evita sobreposicao em telas menores.

## Limitacoes conhecidas

- A busca global da topbar e visual/local; nao foi criado endpoint novo.
- `pagamentosVencidos` permanece como derivacao segura sem inventar dado que o backend nao retorna.
- A `CatracaPage` e extensa e ja tinha visual proprio; foram preservados fluxo e regras, com ajuste global indireto por tokens.
- Ainda ha strings antigas sem acento em partes nao centrais do fluxo, principalmente catraca/login, recomendadas para uma rodada especifica de microcopy.
- Lighthouse nao foi executado automaticamente; a validacao equivalente foi feita por build/lint/type-check/test.

## Proximos passos recomendados

- Sprint 3: consolidar formularios com validacao visual unificada, revisar toda a microcopy da catraca, adicionar testes de componentes para `FormField`, `MetricCard` e fluxos criticos, e evoluir busca global para uma busca real quando houver contrato de API.
