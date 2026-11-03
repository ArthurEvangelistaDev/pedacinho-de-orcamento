# STATE

## Decisions

### AD-001
- **Decision**: Backend em C# com ASP.NET Core, expondo API independente da interface.
- **Reason**: Usuário escolheu ASP.NET Core após comparar com Java; API permite clientes web e futuro mobile.
- **Trade-off**: Manter contrato e execução de frontend/backend separados; futuras mudanças de linguagem exigirão migração de código.
- **Scope**: Backend e integração de clientes.
- **Date**: 2026-10-06 (registro da decisão anterior)
- **Status**: superseded by AD-012

### AD-002
- **Decision**: Banco MySQL desde a primeira versão local.
- **Reason**: Usuário conhece MySQL e o escolheu após comparação com PostgreSQL e SQLite.
- **Trade-off**: Administrar um serviço local e verificar compatibilidade do provedor .NET; não usar arquivo SQLite.
- **Scope**: Persistência de todas as funcionalidades.
- **Date**: 2026-10-06 (registro da decisão anterior)
- **Status**: active

### AD-003
- **Decision**: Erros da API em Problem Details com codigo, erros por campo e traceId, incluindo validação de preço zero.
- **Reason**: Usuário aprovou um contrato que permite à interface tratar falhas por identificador estável.
- **Trade-off**: Mapear erros de binding/validação do framework ao contrato próprio, em vez de expor respostas automáticas diferentes.
- **Scope**: Respostas de erro dos endpoints da API; operações futuras ainda precisam dos seus casos específicos.
- **Date**: 2026-10-19
- **Status**: active

### AD-004
- **Decision**: IDs/versões e valores decimais trafegam como strings no JSON; backend e banco preservam tipos numéricos.
- **Reason**: Usuário aprovou preservação dos dígitos para clientes JavaScript; cálculos financeiros são autoritativos no backend.
- **Trade-off**: Exigir conversão e validação explícitas no transporte, sem aceitar silenciosamente strings e números no mesmo campo.
- **Scope**: Contratos de entrada/saída da API; booleanos, status HTTP e inteiros pequenos de paginação mantêm tipos próprios.
- **Date**: 2026-10-19
- **Status**: active

### AD-005
- **Decision**: Arquivar/reativar insumos na primeira versão, preservando composições; planejar exclusão definitiva como evolução FUT-01.
- **Reason**: Usuário aprovou o ciclo de vida reversível e pediu explicitamente exclusão real futura.
- **Trade-off**: Manter registros arquivados até definir elegibilidade e tratamento de vínculos para exclusão física.
- **Scope**: Ciclo de vida de insumos e contratos correspondentes; não autoriza apagar dados atuais.
- **Date**: 2026-10-19
- **Status**: active

### AD-006
- **Decision**: Listagem e arquivamento/reativação de receitas aprovados; exclusão física de receitas planejada como FUT-02.
- **Reason**: Usuário aprovou o ciclo de vida reversível e pediu anotar a exclusão real em versão futura, também para receitas.
- **Trade-off**: Preservar receitas e rendimentos usados até definir elegibilidade e tratamento dos dependentes para exclusão física.
- **Scope**: Receitas, suas referências e evolução FUT-02; FUT-01 continua dedicado a insumos.
- **Date**: 2026-10-20
- **Status**: active

### AD-007
- **Decision**: Aplicar ai-memory ao projeto como memória local de recuperação, preservando a documentação canônica e separando aprovações de propostas.
- **Reason**: Usuário solicitou instalar a skill do repositório akitaonrails/ai-memory e usá-la neste projeto; em seguida perguntou se tudo já estava registrado.
- **Trade-off**: Serviço local e integração MCP/hooks adicionais; documentos importados são snapshots, precisam ser conferidos contra os arquivos atuais. Captura automática depende da confiança dos hooks pelo usuário.
- **Scope**: Ferramentas do agente e continuidade deste projeto; não muda a stack do aplicativo e não aprova novos contratos.
- **Date**: 2026-10-29
- **Status**: active

### AD-008
- **Decision**: Nome do app: Pedacinho de Orçamento. Diretório canônico: C:/Users/arthu/OneDrive/Documentos/Projetos/pedacinhoDeOrcamento.
- **Reason**: Nome e localização escolhidos explicitamente pelo usuário.
- **Trade-off**: A pasta anterior foi preservada como backup; continuar as edições somente na nova pasta.
- **Scope**: Identidade e localização dos arquivos; manter as coordenadas existentes do ai-memory para preservar a continuidade.
- **Date**: 2026-10-30
- **Status**: active

### AD-009
- **Decision**: Precificação herda indiretos e par de lucro independentemente em global → produto → variação; null herda, zero é próprio e somente modo/percentual informado é persistido como fonte do lucro.
- **Reason**: Usuário aprovou o contrato de precificação em 2026-10-30, com origem dos valores e conversão entre margem/acréscimo sem deriva por arredondamento.
- **Trade-off**: Resolver configurações efetivas por consulta e preservar sua origem; não duplicar valores herdados nem indicadores equivalentes persistidos. Preço manual permanece independente dos custos e objetivos.
- **Scope**: Configuração global, produto, variação e serviço de precificação; não aprova o esquema físico inteiro nem implementação do app.
- **Date**: 2026-10-30
- **Status**: active

### AD-010
- **Decision**: Uso inicial em português/BRL, MySQL como fonte principal, planilha para futura importação validada sem sincronização e acesso pelo navegador somente no próprio computador, sem conta/login.
- **Reason**: Usuário aprovou explicitamente as três premissas do bloco de uso inicial em 2026-10-31.
- **Trade-off**: Evitar duas fontes editáveis e fluxo de autenticação na instalação individual; acesso de outros dispositivos, exposição pública e sincronização exigem revisão própria. Formato e execução da importação continuam pendentes.
- **Scope**: Apresentação, fonte de dados e limites de acesso da primeira versão; não autoriza migração ou implementação integral.
- **Date**: 2026-10-31
- **Status**: active

### AD-011
- **Decision**: Validar primeiro o fluxo com cadastros manuais; disponibilizar backup/restauração local completo antes de adotar dados reais; importar a planilha depois, com prévia e validação. Backup manual em destino escolhido pelo usuário.
- **Reason**: Usuário aprovou a ordem e a cobertura do bloco de importação/backup em 2026-10-31.
- **Trade-off**: Adoção com dados reais depende da recuperação do conjunto de dados; migração da planilha fica posterior à validação do fluxo manual. Formatos, ferramentas, mapeamentos e procedimentos continuam pendentes.
- **Scope**: Sequência de entregas, importação e recuperação dos dados do aplicativo; não autoriza executar importação/restauração ou implementar o app inteiro.
- **Date**: 2026-10-31
- **Status**: active

### AD-012
- **Decision**: Backend/API em Java 25 com Spring Boot 4.1, Maven Wrapper e Spring Data JPA/Hibernate com driver MySQL.
- **Reason**: Usuário avaliou a alternativa Java e confirmou “vamos seguir com isso então” em 2026-10-31.
- **Trade-off**: Substituir ASP.NET Core/EF Core por Spring e JPA; adaptar serialização, Problem Details e precisão numérica sem mudar contratos ou tabelas aprovados. BigDecimal usa DECIMAL128 (34 dígitos, HALF_EVEN), com limites do transporte/banco preservados.
- **Scope**: Substitui AD-001. Primeiro incremento restrito a cadastro/consulta individual de insumos e base de catálogos; frontend e demais módulos permanecem entregas posteriores.
- **Date**: 2026-10-31
- **Status**: active

## Handoff

- **Feature**: Pedacinho de Orçamento — gestão de receitas e precificação.
- **Documento canônico**: `outputs/spec.md`. Manter este arquivo como referência única; não criar cópias editáveis concorrentes.
- **Phase / Task**: Pausa solicitada pelo usuário em 2026-11-01. Especificação v0.30; Java 25/Spring Boot 4.1/Maven/JPA confirmados em AD-012. Primeiro incremento backend/ em implementação; detalhes em .specs/features/insumos-inicial/execucao.md.
- **Completed**: Dois commits locais: 4e565b1 (documentação/stack) e 17a8d02 (base Java, Wrapper, migração e teste MySQL). POST /api/v1/insumos e GET /api/v1/insumos/{id} escritos e salvos, ainda sem commit do módulo. Último gate completo confirmado: 74 testes passando, 0 falhas/erros/ignorados. Após mais três casos, relatórios XML indicam 77 passando, mas a execução externa terminou com erro de PowerShell causado por aviso nativo; não equivale a gate completo confirmado.
- **Memory**: ai-memory 3.2.10 instalado; escopo default/controle-receitas-precificacao. Nove páginas: quatro resumos e cinco documentos completos conferidos por leitura. MCP autenticado testado. Relatório em outputs/memoria-do-projeto.md; hooks ainda dependem da autorização do usuário no Codex.
- **In-progress**: Correção de backend/scripts/test-local.ps1 já aplicada: preservar stderr nativo e usar o exit code do Maven, com ErrorActionPreference Continue somente nos trechos nativos. Sintaxe PowerShell verificada na pausa; nova execução após essa correção não está confirmada. Não reaplicar a correção nem alterar expectativas para obter sucesso.
- **Next step**: Conferir árvore/execucao.md e repetir o gate completo com o script atual, registrando exit code e BUILD SUCCESS. Depois concluir rastreabilidade, commit do módulo e revisão independente com mutações isoladas conforme skill tlc-spec-driven. Nenhuma revisão independente executada ainda; nenhum critério do módulo marcado Verified. Frontend e demais módulos ficam para entregas posteriores; FUT-01/FUT-02 fora desta versão.
- **Blockers**: Trabalho pausado por solicitação do usuário. Git .git é somente leitura no sandbox; add/commit exigiram execução aprovada com safe.directory restrito ao caminho deste repositório, sem alterar configuração global. Conector MCP ai-memory indisponível nesta sessão; continuidade salva nos arquivos canônicos locais.
- **Uncommitted files**: backend/README.md; backend/scripts/test-local.ps1; backend/src/test/java/br/com/pedacinhodeorcamento/SchemaIT.java; novos arquivos em backend/src/main/java/br/com/pedacinhodeorcamento/api/ e insumos/; novos testes em backend/src/test/java/br/com/pedacinhodeorcamento/insumos/; estes registros de pausa. Não descartar nem substituir o código por scaffold.
- **Environment**: JDK 25.0.5, Maven 3.9.16, MySQL 8.4.13 e Python portátil para validadores em work/toolchain, ignorado pelo Git. Teste usa somente 127.0.0.1:33306/pedacinho_insumos_test; credenciais locais em work/ não devem ser exibidas ou gravadas na memória. Nenhum processo java/mysqld encontrado na conferência da pausa.
- **Branch**: master, HEAD 17a8d02; repositório local sem remoto/publicação. Não fazer stash/reset nem commit do módulo como concluído antes do gate.
