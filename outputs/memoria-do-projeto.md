# Memória do projeto — instalação e verificação

Data: 2026-10-29.

## Resultado

ai-memory 3.2.10 instalado e serviço local respondendo em 127.0.0.1:49374. Seis skills oficiais instaladas para Codex. Conexão MCP configurada por ponte stdio, que lê a credencial do arquivo local protegido; não exige token em variável de ambiente global.

Escopo exclusivo deste projeto: workspace `default`, project `controle-receitas-precificacao`, definido em `.ai-memory.toml`. Provedores LLM e embeddings desativados, sem API paga configurada. Perfil entre projetos desativado para este projeto.

O banco interno do serviço de memória não altera a escolha MySQL do aplicativo planejado.

## Conhecimento registrado

Quatro notas semânticas fixadas:

- `decisions/arquitetura-local.md`: ASP.NET Core, MySQL, execução local e limites das escolhas confirmadas.
- `decisions/contratos-api.md`: representação numérica, validação, erros e confirmação de preço zero.
- `decisions/regras-custos-e-precificacao.md`: receitas, rendimentos, custos atuais, margem/acréscimo e preço praticado.
- `concepts/documentacao-e-evolucoes.md`: documentos canônicos, continuidade e FUT-01/FUT-02.

Cinco documentos completos, registrados como snapshots identificados por data e SHA-256:

| Arquivo canônico | Página na memória |
| --- | --- |
| [Especificação](spec.md) | project-docs/spec.md |
| [Modelo de dados](modelo-de-dados.md) | project-docs/modelo-de-dados.md |
| [Contratos de insumos e convenções](contratos-api.md) | project-docs/contratos-api.md |
| [Contratos de receitas](contratos-receitas.md) | project-docs/contratos-receitas.md |
| [Contratos de produtos](contratos-produtos.md) | project-docs/contratos-produtos.md |

Total inicial: nove páginas. Os arquivos do projeto continuam sendo a fonte principal. A cópia na memória é um snapshot, não uma sincronização contínua; propostas foram preservadas como propostas. Mudanças posteriores exigem conferir o arquivo atual e atualizar a nota quando apropriado.

Não foi importada a conversa inteira, o currículo nem dados pessoais nele contidos. O próximo passo do aplicativo continua sendo a revisão da proposta de criação de variação/composição, seguida da precificação. Instalar memória não aprovou essa proposta automaticamente.

## Verificações realizadas

- Download do binário oficial v3.2.10 validado pelo SHA-256 publicado junto ao lançamento.
- Skills instaladas do commit `4a903a9ae622f5cb4bd2b2c811491757d2e9c395` pelo instalador de skills do Codex.
- Serviço respondeu a health check e status autenticado.
- As cinco cópias de documentos foram lidas de volta e comparadas integralmente com o conteúdo enviado.
- Busca por MySQL recuperou as notas deste projeto.
- Ponte MCP inicializada com sucesso; 24 ferramentas anunciadas; busca `memory_query` no escopo correto recuperou conhecimento do projeto.
- Instruções oficiais de roteamento adicionadas a AGENTS.md, preservando as orientações locais do projeto.

## Ação pendente no Codex

O instalador informou sete hooks ainda não confiados: preToolUse, postToolUse, preCompact, sessionStart, sessionEnd, userPromptSubmit e stop. O Codex não executa esses hooks antes de autorização do usuário.

Reabrir o Codex para carregar a integração e autorizar os hooks pelo fluxo de revisão do aplicativo/cliente. No CLI, o diagnóstico oficial orienta usar `/hooks`. Nenhuma opção de bypass de confiança foi ativada.

As páginas registradas manualmente e a busca já funcionam. Captura automática de novas sessões ainda depende dessa autorização; portanto não há promessa de que toda nova mensagem será capturada antes dela.

## Origem

[Repositório ai-memory](https://github.com/akitaonrails/ai-memory), [instruções oficiais de instalação](https://github.com/akitaonrails/ai-memory/blob/main/INSTALL_FOR_AGENTS.md) e [suporte Windows](https://github.com/akitaonrails/ai-memory/blob/main/docs/windows.md).
