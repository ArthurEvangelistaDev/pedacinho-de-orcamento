# Especificação — Pedacinho de Orçamento

Versão: 0.30  
Data de consolidação: 2026-10-06; atualização: 2026-10-31  
Estado: decisões consolidadas; primeiro incremento do backend Java em implementação.

Este é o documento de referência do aplicativo. **Confirmado** indica decisão explícita do usuário. **Proposto** indica recomendação discutida ou premissa técnica ainda sujeita a revisão. Exemplos numéricos não são cadastros reais nem valores padrão aprovados.

Nome confirmado do app: **Pedacinho de Orçamento**. Pasta do projeto: `pedacinhoDeOrcamento` em `C:/Users/arthu/OneDrive/Documentos/Projetos` (definidos em 2026-10-30).

## Problem Statement

O usuário gerencia ingredientes, embalagens, receitas e preços de produtos de confeitaria em uma planilha do Google Sheets. Deseja uma aplicação que facilite acesso, inclusão, edição e consulta, preserve as relações de custo e ofereça maior clareza na formação de preços.

O primeiro objetivo é gerenciar receitas e precificação para uso individual, executando tudo localmente e sem contratar serviços. Uma API independente permitirá que o backend seja consumido futuramente por um aplicativo mobile e hospedado na nuvem.

## Goals

- Cadastrar insumos e seus preços atuais, com data de atualização do preço.
- Compor receitas e preparos reutilizáveis, com rendimentos identificados por tamanho.
- Abrir um produto e selecionar tamanho e sabor entre combinações cadastradas.
- Consultar custos atuais e preços sugeridos por variação.
- Editar margem ou acréscimo sobre o custo e visualizar a conversão entre eles.
- Definir preço praticado e acompanhar sua margem efetiva estimada.
- Manter documentação suficiente para retomar o desenvolvimento sem reconstruir decisões da conversa.

## Decisões confirmadas

| ID | Decisão | Observação |
| --- | --- | --- |
| DEC-01 | Uso individual inicialmente | Apenas o usuário, sem necessidade atual de colaboração |
| DEC-02 | Foco inicial em receitas e precificação | Gestão financeira completa não é prioridade da primeira versão |
| DEC-03 | Execução local e orçamento inicial zero | Não depender de serviços pagos ou contas na nuvem |
| DEC-04 | Backend ASP.NET Core, linguagem C# — supersedida por DEC-42 | Decisão inicial preservada como histórico; stack atual Java/Spring Boot |
| DEC-05 | Banco MySQL | Escolhido após comparação com PostgreSQL e SQLite |
| DEC-06 | Dados e regras expostos por API independente | Preparar clientes web/mobile e futura hospedagem |
| DEC-07 | Atualização de preço de insumo afeta todos os dependentes | Inclui receitas e produtos, diretamente e por preparos intermediários |
| DEC-08 | Guardar data de atualização do preço | Histórico de preços não é necessário inicialmente |
| DEC-09 | Produção proporcional à receita base | Aumentar lote multiplica ingredientes e rendimento; não modifica a receita cadastrada |
| DEC-10 | Considerar tamanho no rendimento | Existem brownies pequenos e normais; recheado utiliza o pequeno |
| DEC-11 | Produto com seleção de tamanho e sabor | Não obrigar a procurar cada combinação como produto separado |
| DEC-12 | Percentuais globais e personalizações | Custos indiretos e lucro devem ser configuráveis |
| DEC-13 | Margem sobre venda e acréscimo sobre custo vinculados | Editar um recalcula o outro; acréscimo pode superar 100% |
| DEC-14 | Preço praticado manual por variação | Manter separado do preço sugerido |
| DEC-15 | Atualizar margem efetiva sem mudar preço praticado | Mudança de custo não reajusta automaticamente o valor manual |
| DEC-16 | Permitir insumo sem preço e sinalizar indisponibilidade | Custos dependentes não podem tratar o preço ausente como zero; confirmado em 2026-10-19 |
| DEC-17 | Nome obrigatório e quantidade estritamente positiva | Validações confirmadas em 2026-10-19 |
| DEC-18 | Tipos e unidades em tabelas de referência, enviados por código na API | Valores iniciais cadastrados no banco; não enviar nome/objeto completo |
| DEC-19 | Preço normalmente positivo; zero exige confirmação explícita | Confirmado confirmarPrecoZero = true no cadastro; zero é conhecido e diferente de preço ausente |
| DEC-20 | Padrão de erro Problem Details com códigos e erros por campo | Usuário aprovou a proposta; código estável orienta o cliente sem interpretar mensagens |
| DEC-21 | IDs/versões e valores decimais como strings no JSON | Aprovado; preservar dígitos no transporte, usando long/decimal no backend e BIGINT/DECIMAL no banco |
| DEC-22 | Consulta/listagem de insumos com busca, filtros e paginação | Aprovados GET individual e lista, ativos por padrão, 20 itens por página e máximo 100 |
| DEC-23 | Edição parcial com versão esperada e referência de compra completa | PATCH aprovado; mudar preço/quantidade/unidade atualiza data da referência; não sobrescrever versão antiga |
| DEC-24 | Arquivamento/reativação de insumos aprovados para a primeira versão | Preservar usos existentes e bloquear novas associações; exclusão definitiva fica planejada para versão futura |
| DEC-25 | Consulta dos catálogos de tipo/unidade aprovada | Ativos por padrão, leitura por código de inativos, dimensão/fator em unidades, carga inicial por seed e sem manutenção por tela nesta etapa |
| DEC-26 | Cadastro transacional de receita e receita incompleta aprovados | Permitir salvar nome e completar composição/rendimentos depois; custo indisponível até haver dados suficientes |
| DEC-27 | Consulta de receitas/custos e simulação por fator aprovadas | Custos atuais por componente/tamanho; escalar quantidades sem alterar cadastro, versão ou datas |
| DEC-28 | Edição de receita aprovada com identidade de componentes e rendimentos | PATCH com versão; manter IDs; arquivar rendimento retirado de lista enviada para preservar produtos existentes |
| DEC-29 | Listagem e arquivamento/reativação de receitas aprovados | Preservar vínculos e estados dos rendimentos; exclusão definitiva de receitas planejada para versão futura |
| DEC-30 | Cadastro do produto e opções aprovado | Opções locais com IDs próprios; sem variações automáticas; permitir produto a completar |
| DEC-31 | Nome do app: Pedacinho de Orçamento; pasta pedacinhoDeOrcamento | Local definido pelo usuário: C:/Users/arthu/OneDrive/Documentos/Projetos; confirmado em 2026-10-30 |
| DEC-32 | Criação de variação com composição aprovada | Combinação explícita, consumo UNIDADES/FRACAO com entradas originais, insumos diretos, cadastro incompleto e preço praticado opcional positivo; versão do produto protegida e incrementada |
| DEC-33 | Configuração e consulta da precificação aprovadas | Herança independente global → produto → variação; par de lucro autoritativo; limites, arredondamento final e indicadores conforme contratos-precificacao.md; confirmado em 2026-10-30 |
| DEC-34 | Consulta e edição de variação aprovadas | Identidade da combinação preservada, listas finais com IDs, preço praticado limpável, versão própria e edição atômica; confirmado em 2026-10-31 |
| DEC-35 | Consulta e edição do produto e opções aprovadas | Identidade ao renomear, arquivamento/reativação das opções sem cascata, novas opções sem gerar combinações e versão do agrupador; confirmado em 2026-10-31 |
| DEC-36 | Listagem e arquivamento/reativação de produtos/variações aprovados | Filtros/paginação, estados individuais preservados, transições sem cascata e controle de versão; produto arquivado bloqueia escrita nas variações; confirmado em 2026-10-31 |
| DEC-37 | Uso inicial em português e BRL, MySQL como fonte principal e acesso somente local sem login | Planilha reservada para futura importação validada, sem sincronização; navegador no próprio computador, sem conta de usuário inicialmente; confirmado em 2026-10-31 |
| DEC-38 | Receita vendida diretamente usa produto/variação ligado à receita original | Consumir unidade do rendimento ou lote inteiro, reutilizando composição e atualização de custos; precificação no fluxo já aprovado; confirmado em 2026-10-31 |
| DEC-39 | Simulação mostra aviso de rendimento fracionado e mantém os valores matemáticos | Ajuste manual do fator; sem ajuste automático para lotes inteiros nem arredondamento automático de ingredientes/rendimento na primeira versão; confirmado em 2026-10-31 |
| DEC-40 | Ordem e cobertura de importação/backup aprovadas | Validar cadastros manuais com dados de teste; backup/restauração local completo antes da adoção com dados reais; importação posterior com prévia/validação; backup manual com destino escolhido pelo usuário; confirmado em 2026-10-31 |
| DEC-41 | Modelo de 14 tabelas e relacionamentos aprovado | Estrutura revisada em modelo-de-dados.md, com entradas persistidas, vínculos protegidos e custos derivados; confirmação em 2026-10-31 não executa DDL nem escolhe ferramentas/versões |
| DEC-42 | Java 25 + Spring Boot 4.1 + Maven Wrapper + Spring Data JPA/Hibernate e MySQL | Troca de backend/API confirmada em 2026-10-31; primeiro incremento limitado a cadastro/consulta individual de insumos |

## Base técnica e decisões ainda abertas

| Tema | Estado | Direção atual |
| --- | --- | --- |
| Backend | Confirmado | Java 25 + Spring Boot 4.1; patch inicial 4.1.2 |
| Banco | Confirmado | MySQL |
| Modelo físico do banco | Confirmado | Desenho de 14 tabelas e relacionamentos aprovado em 2026-10-31; versões/provedor, collation e validação no ambiente real pendentes |
| Cliente inicial | Confirmado no objetivo | Aplicação web acessada pelo navegador |
| Frontend | Proposto | React + TypeScript + Vite |
| Persistência | Confirmado | Spring Data JPA/Hibernate e MySQL Connector/J; dependências gerenciadas pelo Spring Boot |
| Contrato HTTP | Proposto | REST/JSON e documentação OpenAPI |
| Organização | Proposto | Backend único organizado por módulos, com regras separadas de HTTP e persistência |
| Repositório | Proposto | Frontend e backend juntos, com Git |
| Testes | Escolha técnica do incremento | JUnit Jupiter e integração com MySQL real isolado; ferramenta do frontend a decidir |
| Execução | Proposto | Instalações locais; containers opcionais posteriormente |
| Versões | Definidas para o incremento | Java 25, Spring Boot 4.1.2, Maven 3.9.16 e MySQL 8.4.13 LTS; validar compatibilidade no build/testes |
| Interface visual | Pendente | Componentes, estilos, biblioteca e eventual PWA |

## Out of Scope

As exclusões abaixo limitam a primeira versão; não impedem evolução futura.

| Funcionalidade | Motivo / estado |
| --- | --- |
| Histórico de preços de insumos e custos antigos | Usuário informou não precisar inicialmente |
| Aplicativo mobile nativo | Evolução futura; preservar acesso por API |
| Hospedagem na nuvem | Evolução futura; execução atual será local |
| Sincronização bidirecional com Google Sheets | Fora do uso inicial aprovado; planilha reservada para futura importação validada, com contrato/execução próprios ainda pendentes |
| Acesso de outros dispositivos pela rede | Fora do acesso inicial aprovado, restrito ao próprio computador; configuração de rede futura exige revisão própria |
| Compras, vendas, pedidos, clientes e caixa | Prioridade atual é receitas e precificação |
| Estoque e movimentação de produção | Existe na planilha, mas não foi incluído no escopo inicial |
| Lançamento real de gás, água, limpeza e mão de obra | Primeira abordagem usa percentual estimado de custos indiretos |
| Múltiplas empresas, contas e colaboração | Uso individual confirmado |
| Funcionamento offline com sincronização entre dispositivos | Não definido; execução local não implica essa funcionalidade |
| Exclusão definitiva de insumos | Solicitada para versão futura; primeira versão usa arquivamento/reativação |
| Exclusão definitiva de receitas | Solicitada para versão futura em 2026-10-20; primeira versão usa arquivamento/reativação |

## Assumptions & Open Questions

Premissas não confirmadas são registradas explicitamente para revisão antes da implementação correspondente. A especificação não transforma recomendações em decisões do usuário.

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Moeda inicial | BRL, apresentação em português | Uso inicial aprovado em 2026-10-31 | Confirmado |
| Fonte principal | MySQL; planilha reservada para futura importação validada, sem sincronização | Uso inicial aprovado em 2026-10-31; formato/contrato de importação ainda pendentes | Confirmado |
| Preparos intermediários | Custo direto; aplicar indiretos e lucro uma vez no produto final | Precificação aprovada em 2026-10-30; evitar aplicação repetida em receitas compostas | Confirmado |
| Hierarquia dos percentuais | Global → produto → variação, independente por configuração | Precificação aprovada em 2026-10-30; permitir ajustes sem repetir todos os parâmetros | Confirmado |
| Percentuais por receita | Não aplicar na composição; receita vendida diretamente terá produto/variação associado à receita original | Fluxo aprovado em 2026-10-31; precificação final usa o produto/variação e herança já aprovados | Confirmado |
| Produção fracionada | Mostrar quantidades matemáticas e aviso de rendimento fracionado; ajuste manual do fator, sem arredondamento automático de ingredientes/rendimento na primeira versão | Aprovado em 2026-10-31; preservar proporções e manter execução de produção fora do escopo | Confirmado |
| Unidade e fração no consumo | Permitir unidade de uma opção de rendimento ou fração fixa da receita | Criação de variação aprovada em 2026-10-30; consumos com entradas originais preservadas | Confirmado |
| Reutilização de massas | Uma receita base com opções alternativas de rendimento por tamanho | Confirmado nos contratos aprovados de receita/rendimentos e consumo da variação; receitas distintas continuam permitidas | Confirmado |
| Campos de sabor | Rótulos por produto; composição definida na variação | Confirmado nos contratos aprovados de produto/opções e variação; rótulo não define consumo automaticamente | Confirmado |
| Limites do lucro desejado | Margem de 0 até menos de 100%; acréscimo não negativo sem teto de negócio de 100% | Precificação aprovada em 2026-10-30; limites físicos no contrato, perdas permitidas na margem efetiva | Confirmado |
| Preço praticado | Opcional e positivo; ausência mostra margem efetiva indisponível | Criação aprovada em 2026-10-30; edição/limpeza do preço aprovada em 2026-10-31 | Confirmado |
| Insumo com preço zero | Aceitar como exceção com validação adicional; preço usual maior que zero | Usuário confirmou zero com proteção contra preenchimento acidental | Confirmado |
| Confirmação de preço zero | Exigir confirmarPrecoZero = true no cadastro com precoCompra = 0 | Confirmação consciente, validada também pela API; usuário aprovou a sugestão | Confirmado |
| Respostas de erro | Problem Details com código estável e lista de erros por campo | Interface pode apresentar mensagens e tratar confirmação sem interpretar texto | Confirmado |
| Representação de números no JSON | IDs/versões e decimais como strings; booleanos e status HTTP preservam tipos | Evitar perda de precisão no transporte para clientes JavaScript, com conversão explícita | Confirmado |
| Consulta/listagem de insumos | Busca por nome, filtros, paginação 20 por página e máximo 100, ordenação nome/id | Aprovado conforme contratos-api.md; ausência de resultados não é erro | Confirmado |
| Edição e custo de compra | PATCH parcial com versão esperada; preço/quantidade/unidade enviados juntos | Prevenir perda de dados e troca incoerente da referência do custo | Confirmado |
| Data da referência de preço | Atualizar ao mudar preço, quantidade ou unidade de compra; manter null enquanto preço ausente | Alterar embalagem/quantidade comprada pode alterar custo sem mudar o valor pago | Confirmado |
| Insumo sem preço | Permitir cadastro com preço ausente; sinalizar custo indisponível nos dependentes | Confirmação explícita do usuário em 2026-10-19 | Confirmado |
| Custos indiretos | Percentual não negativo sobre custo direto completo, incluindo embalagem | Base aprovada em 2026-10-30; aplicação única no produto final | Confirmado |
| Arredondamento | Precisão decimal interna; preço sugerido final em centavos, empate arredondado para cima | Regra aprovada em 2026-10-30; evitar arredondamento por ingrediente | Confirmado |
| Exclusão | Arquivar insumos, preservando usos existentes e bloqueando novas associações; reativar pelo mesmo ID | Aprovado para a primeira versão; exclusão definitiva solicitada para versão futura | Confirmado |
| Consulta de catálogos | Listas completas de referência, ativos por padrão; consulta por código permite ler inativos | Preencher seletores a partir do banco, sem perder rótulos de referências antigas | Confirmado |
| Cadastro de receita | Salvar cabeçalho, ingredientes, preparos e rendimentos em uma transação | Aprovado conforme contratos-receitas.md; manter custo calculado separado da composição | Confirmado |
| Receita incompleta | Permitir nome e composição vazia, com custo indisponível até completar | Usuário aprovou explicitamente cadastro progressivo sem inventar total zero | Confirmado |
| Consulta e simulação de receitas | Consultar custo direto atual e escalar lote por fator sem gravar produção | Aprovado em contratos-receitas.md; versão da receita não é versão dos preços dos insumos | Confirmado |
| Edição de receita | PATCH parcial, listas presentes substituem composição mantendo IDs de linhas existentes | Permitir edição atômica sem trocar identidades usadas pelos produtos | Confirmado |
| Retirada de opção de rendimento | Arquivar opção omitida de lista enviada, preservando seu ID e os vínculos existentes | Não apagar tamanho que já tem produtos dependentes | Confirmado |
| Listagem e ciclo de vida de receitas | Busca/filtros/paginação, arquivamento sem cascata e reativação no mesmo ID | Preservar custos e composições, separando estado do cadastro da disponibilidade de custo | Confirmado |
| Cadastro do produto agrupador | Nome, descrição e opções locais de tamanho/sabor, salvos juntos; sem gerar variações | Aprovado conforme contratos-produtos.md; composição e preço pertencem à combinação explícita | Confirmado |
| Produto a completar | Permitir nome sem opções/variações inicialmente | Facilitar cadastro progressivo, sem criar preço ou combinação artificial | Confirmado |
| Criação de variação | Combinação explícita, composição por unidade/fração e consumo direto, com versão do produto | Aprovado em contratos-produtos.md em 2026-10-30; preservar entradas originais | Confirmado |
| Variação a completar | Exigir tamanho/sabor, permitir composição vazia e preço praticado ainda ausente | Aprovado em 2026-10-30; montagem progressiva sem custo zero artificial | Confirmado |
| Configuração de precificação | GET/PUT global e GET/PATCH por produto/variação; versão esperada e alterações atômicas | Aprovado em contratos-precificacao.md em 2026-10-30; detectar edição concorrente | Confirmado |
| Regra de lucro persistida | Par modo/percentual; guardar somente a entrada autoritativa e derivar o equivalente | Aprovado em 2026-10-30; evitar deriva e mistura de níveis da herança | Confirmado |
| Ausência de configuração global | Não criar percentuais padrão; calcular cada indicador apenas com suas dependências disponíveis | Aprovado em 2026-10-30; personalizações completas sem global e margem efetiva sem objetivo de lucro | Confirmado |
| Precisão e limites da precificação | Percentuais até 6 casas em DECIMAL(18,6); preço sugerido em centavos, empate para cima; limites e pendências no contrato | Aprovado em 2026-10-30; não truncar entradas nem inventar valores após overflow | Confirmado |
| Consulta e edição da variação | GET individual e PATCH com versão própria, listas finais com IDs e preço praticado anulável | Aprovado em contratos-produtos.md em 2026-10-31; preservar a identidade da combinação | Confirmado |
| Consulta e edição do produto/opções | GET completo e PATCH com versão do agrupador; opções com IDs e arquivamento/reativação sem cascata | Aprovado em contratos-produtos.md em 2026-10-31; preservar variações existentes | Confirmado |
| Listagem de produtos/variações | Filtros e paginação conforme contratos-produtos.md; agrupador sem custo/preço único | Aprovado em 2026-10-31; distinguir estado do cadastro e disponibilidade de custo | Confirmado |
| Ciclo de vida de produtos/variações | Arquivar/reativar por versão própria, sem cascata; produto arquivado bloqueia escrita nas variações | Aprovado em 2026-10-31; preservar composições, preços e estados individuais, sem exclusão física | Confirmado |
| Prioridade da importação | Validar o fluxo com cadastros manuais e importar a planilha numa entrega posterior, com prévia e validação dos dados | Ordem/cobertura aprovadas em 2026-10-31; formato, mapeamento e contrato de aplicação continuam pendentes | Confirmado |
| Backup e restauração local | Recuperar o conjunto completo de dados antes da adoção com dados reais; backup manual e local com destino escolhido pelo usuário | Prioridade/cobertura aprovadas em 2026-10-31; formato, ferramenta, caminho concreto e procedimento ainda pendentes | Confirmado |
| Acesso inicial | Pelo navegador somente no próprio computador, sem conta/login inicialmente | Uso inicial aprovado em 2026-10-31; rede e exposição pública exigem revisão própria | Confirmado |
| Valores padrão | Usuário informa percentuais na configuração inicial | Ausência de padrões ocultos aprovada na precificação; 30% e 50% continuam apenas exemplos | Confirmado |

**Open questions: none unlogged — todas as pendências conhecidas estão registradas acima ou na seção de base técnica.** O documento consolida decisões; as propostas permanecem sujeitas a revisão. Não representa aprovação de implementação integral.

## Modelo conceitual

Este modelo organiza conceitos; não é ainda o esquema físico definitivo do MySQL.

| Entidade | Dados / relações principais |
| --- | --- |
| Insumo | Nome, tipo ingrediente/embalagem, preço da compra de referência, quantidade comprada, unidade, data de atualização do preço |
| Receita | Nome, composição do lote base, instruções/observações opcionais |
| Ingrediente de receita | Receita, insumo, quantidade e unidade de consumo |
| Preparo de receita | Receita principal, preparo utilizado e quantidade/fração |
| Opção de rendimento | Receita, identificação do tamanho/porção e quantidade obtida do lote completo |
| Produto | Nome, variações disponíveis, possíveis personalizações de precificação |
| Variação de produto | Produto, tamanho, sabor, composição, preço praticado e possíveis personalizações |
| Receita da variação | Variação, receita, modo de consumo, quantidade e opção de rendimento quando aplicável |
| Insumo da variação | Variação, insumo e quantidade; inclui embalagem e decoração direta |
| Configuração global | Percentual de indiretos e regra de lucro desejado |
| Regra de lucro | Modo margem/acréscimo e valor informado; o indicador equivalente é derivado |

Uma opção de rendimento representa todo o lote da receita cortado/porcionado daquele modo. Rendimento pequeno e normal são alternativas, não quantidades somadas nem promessa de produzir ambos no mesmo lote.

O usuário abrirá um produto e escolherá uma combinação disponível. Ter tamanho normal em outro produto não cria automaticamente brownie recheado normal. Não gerar o produto cartesiano de tamanhos e sabores sem composição cadastrada.

### Receita vendida diretamente — aprovada

Quando uma receita for vendida diretamente, cadastrar um produto e uma variação que consumam essa receita. A receita mantém composição, rendimentos e custo direto; o produto/variação recebe a configuração de precificação e o preço praticado pelo fluxo já aprovado. O vínculo reutiliza a receita original, preservando a atualização automática dos custos.

Exemplos ilustrativos, sem opções criadas automaticamente:

| Venda | Composição da variação |
| --- | --- |
| Um brigadeiro de uma receita com rendimento 40 | UNIDADES, rendimento escolhido, quantidadeReceita "1" para quantidadeProdutos "1" |
| Um bolo correspondente à receita inteira | FRACAO, quantidadeReceita "1" para quantidadeProdutos "1" |

Tamanho/sabor continuam opções explícitas informadas pelo usuário, como Único/Tradicional quando fizer sentido. Embalagem pode ser consumo direto adicional se ainda não estiver na receita. Aplicar indiretos e lucro uma única vez na variação final, conforme regra já aprovada.

Fluxo aprovado em 2026-10-31. Receita vendida diretamente usa o cadastro explícito de produto/variação; percentuais próprios não são criados no cadastro de receita e não há produto automático. Registrar vendas/pedidos continua fora da primeira versão. Esta aprovação não autoriza implementação integral.

## Regras de cálculo

### Insumos e preparos

Após converter quantidades para unidades compatíveis:

```text
custo unitário do insumo = preço de compra / quantidade comprada
custo consumido = custo unitário do insumo × quantidade utilizada
custo direto da receita = soma dos ingredientes + soma dos preparos utilizados
custo por unidade de rendimento = custo direto da receita / rendimento escolhido
custo por fração = custo direto da receita × fração utilizada
```

Conversões propostas: kg↔g, L↔ml e unidades. Peso↔volume ou peso↔unidade exige conversão específica cadastrada; não presumir que 1 ml equivale a 1 g.

### Produto e preço sugerido

Sejam `D` custo direto da variação, `i` indiretos em percentual, `C` custo total estimado, `m` margem percentual e `a` acréscimo percentual:

```text
C = D × (1 + i / 100)
preço por margem = C / (1 − m / 100)
preço por acréscimo = C × (1 + a / 100)
m = 100 × a / (100 + a)
a = 100 × m / (100 − m)
```

Custos indiretos estimam gás, água, limpeza e mão de obra. O acréscimo de lucro usa como base o custo total estimado, já com os indiretos. São campos diferentes.

Guardar qual modo foi editado por último e seu percentual informado foi aprovado em 2026-10-30 para impedir deriva causada por converter repetidamente valores arredondados. O segundo indicador e o preço sugerido são derivados.

### Preço praticado

Para preço praticado positivo `V`:

```text
resultado estimado por unidade = V − C
margem efetiva estimada (%) = 100 × (V − C) / V
```

A margem efetiva pode ser negativa. A estimativa considera somente os custos incluídos; não equivale a uma apuração contábil de lucro líquido. Impostos, taxas de pagamento, frete e comissões não foram modelados nesta versão.

Alterar o preço praticado atualiza a margem efetiva, sem substituir o objetivo de margem/acréscimo configurado. Alterar o objetivo muda o preço sugerido, sem sobrescrever o preço praticado.

### Produção fracionada — aprovada

O contrato de simulação mantém quantidades e rendimentos proporcionais ao fator informado, sem arredondar silenciosamente. Aprovado em 2026-10-31 fechar a primeira versão com exibição de aviso quando o rendimento simulado de uma opção não for inteiro, preservando seu valor matemático e o custo correspondente. Nenhum novo registro de produção é criado.

| Exemplo ilustrativo | Resultado mantido na simulação |
| --- | --- |
| Receita rende 31 unidades; fator 0,5 | Rendimento 15,5 unidades, com aviso de rendimento fracionado |
| Receita usa 3 ovos; fator 0,5 | Quantidade 1,5 na unidade de consumo cadastrada, sem substituir por 2 |
| Receita custa R$ 60; fator 0,5 | Custo simulado R$ 30, sem aumento por arredondamento de ingredientes |

O usuário pode editar explicitamente o fator e simular novamente; o sistema preserva receita base, quantidades originais e preço praticado. Não transformar resultado fracionário em erro de validação nem executar ajuste automático do lote para obter apenas unidades inteiras. Ajustes físicos de ingredientes e execução da produção permanecem a critério do usuário.

O escopo aprovado não inclui cálculo automático de lote para uma quantidade-alvo inteira, arredondamento de ovos/embalagens ou compra por pacotes. O aviso e esse limite operacional estão confirmados; não há novo endpoint nem alteração do contrato de fator. A implementação continua pendente.

## User Stories

Os critérios usam palavras-chave EARS (`WHEN`, `THEN`, `SHALL`, `IF`, `WHILE`) com conteúdo em português. Os IDs permitem ligar requisitos a testes e tarefas futuras.

### P1 — Insumos e preços atuais

Como usuário, quero cadastrar insumos e atualizar seus preços para conhecer os custos atuais.

**Acceptance Criteria**:
1. WHEN um insumo é salvo com compra de R$ 40 por 1.000 g THEN o sistema SHALL calcular custo por grama igual a R$ 0,04. [INS-01]
2. WHEN um novo preço é salvo THEN o sistema SHALL registrar o instante da atualização do preço. [INS-02]
3. WHEN o nome de um insumo é editado sem alterar seu preço THEN o sistema SHALL preservar a data de atualização do preço. [INS-03]
4. WHEN o preço de um insumo muda THEN o sistema SHALL usar o novo preço na próxima consulta de custos de todos os dependentes diretos e indiretos. [INS-04]
5. IF a quantidade comprada é menor ou igual a zero THEN o sistema SHALL rejeitar o cadastro dessa compra de referência. [INS-05]
6. WHEN um cadastro válido de insumo não contém preço de compra THEN o sistema SHALL permitir salvar esse insumo com preço ausente. [INS-06]
7. WHILE o preço do insumo está ausente THEN o sistema SHALL apresentar seu custo unitário como indisponível em vez de zero. [INS-07]
8. WHILE um componente direto ou indireto não tem preço THEN o sistema SHALL apresentar custo total, preço sugerido e margem efetiva dos dependentes como indisponíveis, identificando o insumo pendente. [INS-08]
9. IF o nome está ausente ou vazio após remover espaços das extremidades THEN o sistema SHALL rejeitar o cadastro. [INS-09]
10. IF o código de tipo não existe na tabela de tipos disponíveis THEN o sistema SHALL rejeitar o cadastro indicando o campo tipoCodigo. [INS-10]
11. IF o código de unidade não existe na tabela de unidades disponíveis THEN o sistema SHALL rejeitar o cadastro indicando o campo unidadeCompraCodigo. [INS-11]
12. IF o preço de compra informado é negativo THEN o sistema SHALL rejeitar o cadastro. [INS-12]
13. IF o preço de compra é zero e confirmarPrecoZero não é true THEN o sistema SHALL rejeitar a criação com código PRECO_ZERO_REQUER_CONFIRMACAO. [INS-13]
14. WHEN um cadastro válido informa preço zero e confirmarPrecoZero = true THEN o sistema SHALL salvar preço conhecido igual a zero, sem pendência de preço ausente. [INS-14]

INS-13 e INS-14 detalham a confirmação explícita aprovada pelo usuário em 2026-10-19. O padrão de respostas de erro e a representação numérica em contratos-api.md também foram aprovados.

**Independent Test**: cadastrar um insumo, utilizá-lo em um preparo e em um produto, alterar o preço e consultar os custos em cada nível.

### P1 — Receitas, rendimentos e lotes

Como usuário, quero montar receitas reutilizáveis e estimar diferentes quantidades de produção.

**Acceptance Criteria**:
1. WHEN uma receita de custo direto R$ 60 tem rendimento normal 20 e pequeno 30 THEN o sistema SHALL calcular custos unitários R$ 3 e R$ 2 nas opções respectivas. [REC-01]
2. WHEN o usuário simula fator de lote 0,5 THEN o sistema SHALL multiplicar cada quantidade de ingrediente da receita base por 0,5. [REC-02]
3. WHEN o usuário simula fator de lote 0,5 em um rendimento base de 20 THEN o sistema SHALL apresentar rendimento proporcional 10. [REC-03]
4. WHEN uma simulação de lote é concluída THEN o sistema SHALL preservar a composição e os rendimentos da receita cadastrada. [REC-04]
5. WHEN meia receita de preparo custa R$ 16 THEN o sistema SHALL atribuir R$ 16 ao consumo de fração 0,5 desse preparo. [REC-05]
6. IF uma alteração cria dependência circular direta ou indireta entre receitas THEN o sistema SHALL rejeitar essa alteração. [REC-06]
7. IF um rendimento é menor ou igual a zero THEN o sistema SHALL rejeitar essa opção de rendimento. [REC-07]

**Independent Test**: cadastrar uma massa com duas opções de rendimento e simular meia receita; usar outro preparo e tentar um ciclo.

### P1 — Produtos e variações

Como usuário, quero abrir um produto e escolher seu tamanho e sabor para consultar a composição correta.

**Acceptance Criteria**:
1. WHEN o usuário abre um produto THEN o sistema SHALL apresentar somente combinações de tamanho e sabor cadastradas para esse produto. [VAR-01]
2. WHEN uma variação utiliza uma unidade do rendimento pequeno THEN o sistema SHALL calcular sua porção de massa com o rendimento pequeno. [VAR-02]
3. WHEN uma variação utiliza 0,5 receita de recheio para 8 unidades THEN o sistema SHALL consumir 0,0625 receita de recheio por unidade do produto. [VAR-03]
4. WHEN uma variação usa massa, recheio e embalagem THEN o sistema SHALL somar o custo dessas três partes no custo direto da variação. [VAR-04]
5. IF uma combinação de tamanho e sabor já existe no mesmo produto THEN o sistema SHALL rejeitar outra variação com essa mesma combinação. [VAR-05]

**Independent Test**: produto com duas variações de recheio, ambas pequenas, sem gerar automaticamente uma variação normal.

### P1 — Custos indiretos, margem e acréscimo

Como usuário, quero configurar percentuais e visualizar suas relações para decidir o preço.

**Acceptance Criteria**:
1. WHEN o custo direto é R$ 10 e os indiretos são 30% THEN o sistema SHALL calcular custo total estimado R$ 13. [PRE-01]
2. WHEN a margem desejada é alterada para 50% THEN o sistema SHALL apresentar acréscimo equivalente de 100%. [PRE-02]
3. WHEN o acréscimo desejado é alterado para 300% THEN o sistema SHALL apresentar margem equivalente de 75%. [PRE-03]
4. WHEN o custo total é R$ 13 e a margem é 50% THEN o sistema SHALL apresentar preço sugerido R$ 26. [PRE-04]
5. WHEN o custo total é R$ 13 e o acréscimo é 300% THEN o sistema SHALL apresentar preço sugerido R$ 52. [PRE-05]
6. IF a margem desejada é maior ou igual a 100% THEN o sistema SHALL rejeitar esse valor. [PRE-06]
7. WHEN um preparo é utilizado dentro de outro preparo THEN o sistema SHALL transmitir seu custo direto sem aplicar novamente lucro ou indiretos. [PRE-07]
8. WHILE uma configuração da variação está em herança THEN o sistema SHALL usar o valor efetivo do produto ou, se este também herdar, da configuração global. [PRE-08]
9. WHEN um percentual próprio é definido como 0% THEN o sistema SHALL aplicar 0% em vez de herdar o percentual superior. [PRE-09]

**Independent Test**: testar conversões, indiretos e herança com custos sintéticos conhecidos. PRE-07 a PRE-09 foram confirmados na aprovação da precificação em 2026-10-30; implementação pendente.

### P1 — Preço praticado e margem efetiva

Como usuário, quero informar o preço cobrado e enxergar o efeito dos custos atuais.

**Acceptance Criteria**:
1. WHEN o custo total é R$ 13 e o preço praticado é R$ 20 THEN o sistema SHALL apresentar margem efetiva estimada de 35%. [VEN-01]
2. WHEN o custo total aumenta para R$ 15 com preço praticado R$ 20 THEN o sistema SHALL apresentar margem efetiva estimada de 25%. [VEN-02]
3. WHEN o custo total muda THEN o sistema SHALL preservar o preço praticado informado manualmente. [VEN-03]
4. WHEN o objetivo de lucro muda THEN o sistema SHALL preservar o preço praticado informado manualmente. [VEN-04]
5. WHEN o preço praticado fica abaixo do custo total THEN o sistema SHALL sinalizar resultado estimado negativo. [VEN-05]
6. WHEN a margem efetiva fica abaixo da margem desejada THEN o sistema SHALL sinalizar essa diferença na precificação. [VEN-06]
7. WHILE o preço praticado não foi informado THEN o sistema SHALL apresentar margem efetiva indisponível. [VEN-07]

**Independent Test**: fixar preço em R$ 20, atualizar custos e objetivo; verificar que preço manual permanece e margem acompanha os custos.

### P1 — Consultar e localizar insumos

Como usuário, quero consultar cadastros e filtrar a lista para localizar um insumo ou corrigir preços pendentes. Contrato aprovado em 2026-10-19; ainda não implementado.

**Acceptance Criteria**:
1. WHEN o usuário consulta um ID existente THEN o sistema SHALL retornar o cadastro atual com HTTP 200. [CON-01]
2. WHEN o usuário consulta um ID válido inexistente THEN o sistema SHALL retornar HTTP 404 com codigo RECURSO_NAO_ENCONTRADO. [CON-02]
3. WHEN a listagem é solicitada sem parâmetros THEN o sistema SHALL retornar a primeira página com tamanho 20, somente ativos e ordem nome/id crescentes. [CON-03]
4. WHEN nenhum insumo corresponde aos filtros THEN o sistema SHALL retornar HTTP 200 com itens vazio e totalItens "0". [CON-04]
5. WHEN a página solicitada ultrapassa o fim da lista THEN o sistema SHALL retornar HTTP 200 com itens vazio e o total real dos filtros. [CON-05]
6. IF tamanhoPagina é menor que 1 ou maior que 100 THEN o sistema SHALL retornar HTTP 400 com erro TAMANHO_PAGINA_INVALIDO. [CON-06]
7. WHEN o filtro INDISPONIVEL é aplicado THEN o sistema SHALL excluir da lista insumos com preço zero confirmado. [CON-07]
8. WHEN o filtro status é TODOS THEN o sistema SHALL incluir ativos e arquivados que satisfaçam os demais filtros. [CON-08]

**Independent Test**: carregar dados conhecidos, consultar IDs, filtrar insumos sem preço e testar primeira página, ausência de resultados e página além do fim.

### P1 — Editar insumos e referência de compra

Como usuário, quero corrigir um cadastro e atualizar preço/quantidade de compra sem perder edições ou alterar automaticamente meus preços de venda. Contrato aprovado em 2026-10-19; não implementado.

**Acceptance Criteria**:
1. WHEN apenas o nome muda numa edição com versão atual THEN o sistema SHALL preservar preço e data de atualização do preço. [EDI-01]
2. WHEN compra de R$ 40 por 1000 g muda para R$ 45 por 1000 g com versão atual THEN o sistema SHALL calcular custo por g igual a R$ 0,045. [EDI-02]
3. IF a versão esperada não corresponde à versão atual THEN o sistema SHALL rejeitar a edição com HTTP 409 e codigo CONFLITO_DE_EDICAO, sem sobrescrever dados. [EDI-03]
4. IF a edição envia apenas parte do trio preço/quantidade/unidade de compra THEN o sistema SHALL rejeitar a edição com HTTP 400 e erro REFERENCIA_COMPRA_INCOMPLETA nos campos obrigatórios ausentes. [EDI-04]
5. WHEN o trio completo é enviado com precoCompra null e versão atual THEN o sistema SHALL remover o preço conhecido e apresentar custos dependentes indisponíveis. [EDI-05]
6. WHEN compra de R$ 40 por 1000 g muda para R$ 40 por 500 g THEN o sistema SHALL calcular custo por g igual a R$ 0,08. [EDI-06]
7. WHEN a referência da compra muda com preço conhecido THEN o sistema SHALL registrar a data da alteração da referência em precoAtualizadoEm. [EDI-07]
8. IF uma mudança de unidade cria dimensão incompatível com usos existentes THEN o sistema SHALL rejeitar a alteração inteira com HTTP 409 e codigo UNIDADE_INCOMPATIVEL_COM_USOS. [EDI-08]
9. WHEN uma edição válida com versão atual contém valores iguais aos salvos THEN o sistema SHALL preservar versão e datas. [EDI-09]

**Independent Test**: editar nome e referência de compra separadamente; simular dois clientes com a mesma versão e verificar que somente um sobrescreve aquela versão. Edição de preço zero reutiliza a confirmação aprovada, sem exigir confirmação em edição apenas de nome.

### P1 — Arquivar e reativar insumos

Como usuário, quero retirar insumos da seleção e recuperá-los depois sem perder minhas receitas. Contrato de arquivamento/reativação aprovado em 2026-10-19; não implementado. Exclusão definitiva será tratada em uma versão futura.

**Acceptance Criteria**:
1. WHEN um insumo ativo é arquivado com versão atual THEN o sistema SHALL registrar arquivadoEm em UTC e incrementar a versão. [ARQ-01]
2. WHILE um insumo está arquivado THEN o sistema SHALL preservar seu consumo e custo nos componentes existentes. [ARQ-02]
3. IF um novo componente tenta usar insumo arquivado THEN o sistema SHALL rejeitar a associação com HTTP 409 e codigo INSUMO_ARQUIVADO. [ARQ-03]
4. WHEN um insumo arquivado é reativado com versão atual e referências disponíveis THEN o sistema SHALL restaurar o estado ativo no mesmo ID sem alterar preço/data do preço. [ARQ-04]
5. WHEN um comando solicita estado já existente com versão atual THEN o sistema SHALL retornar HTTP 200 sem alterar versão ou datas. [ARQ-05]
6. IF a versão esperada no comando de arquivar/reativar está desatualizada THEN o sistema SHALL retornar HTTP 409 CONFLITO_DE_EDICAO sem alteração. [ARQ-06]
7. IF uma transição de reativação depende de tipo ou unidade inativos THEN o sistema SHALL retornar HTTP 409 REFERENCIA_INATIVA e manter o insumo arquivado. [ARQ-07]

**Independent Test**: arquivar um insumo já utilizado, consultar o custo da receita, tentar novo vínculo e reativar pelo mesmo ID. Preço ausente não impede reativação.

### P1 — Consultar catálogos de tipos e unidades

Como usuário, quero escolher tipos e unidades válidos, com rótulos legíveis, mantendo a identificação de referências antigas. Contratos de consulta aprovados em 2026-10-19; não implementados.

**Acceptance Criteria**:
1. WHEN um catálogo é consultado sem filtros THEN o sistema SHALL retornar somente registros ativos do banco. [CAT-01]
2. WHEN um código inativo existente é consultado individualmente THEN o sistema SHALL retornar HTTP 200 com ativo=false. [CAT-02]
3. WHEN unidades são filtradas por dimensao=MASSA THEN o sistema SHALL retornar somente unidades dessa dimensão. [CAT-03]
4. WHEN o código kg é consultado THEN o sistema SHALL informar unidadeBaseCodigo g e fatorBase "1000.000000". [CAT-04]
5. WHEN um código inexistente é consultado individualmente THEN o sistema SHALL retornar HTTP 404 RECURSO_NAO_ENCONTRADO. [CAT-05]
6. WHEN uma listagem de catálogo não tem correspondências THEN o sistema SHALL retornar HTTP 200 com itens vazio. [CAT-06]

**Independent Test**: carregar tipos e unidades iniciais, desativar uma referência em ambiente de teste e comparar lista padrão, lista TODOS e consulta individual.

### P1 — Cadastrar receita com composição e rendimentos

Como usuário, quero salvar uma receita com seus componentes e rendimentos de referência sem gravar partes inconsistentes. Cadastro e receita incompleta aprovados conforme contratos-receitas.md; não implementados.

**Acceptance Criteria**:
1. WHEN um comando válido cadastra receita com componentes e rendimentos THEN o sistema SHALL salvar o conjunto numa única transação e retornar HTTP 201. [RCP-01]
2. IF um componente do comando é inválido THEN o sistema SHALL rejeitar a criação sem persistir cabeçalho ou componentes parciais. [RCP-02]
3. WHEN um cadastro válido usa insumo ativo sem preço THEN o sistema SHALL salvar a receita com custo indisponível e pendência que identifica o insumo. [RCP-03]
4. WHEN uma receita é criada com nome válido e composição vazia THEN o sistema SHALL apresentar custo indisponível com pendência COMPOSICAO_VAZIA. [RCP-04]
5. WHEN a mesma receita consome 100 g e 50 g em linhas separadas de insumo comprado por R$ 40 a cada 1000 g THEN o sistema SHALL contabilizar custo consumido de R$ 6 preservando as duas linhas. [RCP-05]
6. IF dois rendimentos no comando têm nome equivalente na comparação de nomes adotada THEN o sistema SHALL rejeitar a criação com HTTP 400 e erro RENDIMENTO_DUPLICADO. [RCP-06]
7. WHEN uma receita com composição precificável não possui opções de rendimento THEN o sistema SHALL permitir calcular o custo direto do lote e seu consumo por fração. [RCP-07]

**Independent Test**: criar receita com dados fictícios conhecidos, outra com preço pendente, outra incompleta e um comando com ingrediente inválido; conferir que a falha não deixa registros parciais.

### P1 — Consultar custos e simular lotes

Como usuário, quero consultar o custo direto e calcular quantidades proporcionais sem alterar minha receita. Contratos de consulta/custos e simulação aprovados; não implementados.

**Acceptance Criteria**:
1. WHEN uma receita de custo direto R$ 60 e rendimentos 30 pequenos ou 20 normais é consultada THEN o sistema SHALL apresentar custos unitários R$ 2 e R$ 3 nas opções respectivas. [SIM-01]
2. WHEN fatorLote é 0,5 para ingrediente de 200 g THEN o sistema SHALL apresentar quantidade simulada de 100 g. [SIM-02]
3. WHEN fatorLote é 2 para consumo base de 0,5 lote de preparo THEN o sistema SHALL apresentar consumo simulado de 1 lote desse preparo. [SIM-03]
4. WHEN uma receita de custo direto R$ 60 é simulada com fator 2 THEN o sistema SHALL apresentar custo direto simulado R$ 120. [SIM-04]
5. WHEN a simulação é concluída THEN o sistema SHALL preservar composição, rendimentos, versão e datas dos cadastros envolvidos. [SIM-05]
6. WHEN uma receita com insumo sem preço é simulada com fator válido THEN o sistema SHALL retornar HTTP 200 com quantidades ajustadas e custo indisponível. [SIM-06]
7. IF fatorLote é zero ou negativo THEN o sistema SHALL rejeitar a simulação com HTTP 400 e FATOR_LOTE_INVALIDO. [SIM-07]
8. IF a versão esperada da receita está desatualizada THEN o sistema SHALL rejeitar a simulação com HTTP 409 CONFLITO_DE_EDICAO. [SIM-08]

**Independent Test**: consultar massa hipotética de R$ 60, simular metade/dobro e conferir quantidades, custo e ausência de alterações persistidas; repetir com preço pendente e versão antiga.

### P1 — Editar composição e rendimentos

Como usuário, quero ajustar a receita preservando referências, com controle de versão e clareza sobre o impacto nos produtos. Contrato aprovado em 2026-10-20; não implementado.

**Acceptance Criteria**:
1. WHEN uma edição contém apenas nome e versão atual THEN o sistema SHALL preservar ingredientes, preparos e rendimentos. [RCE-01]
2. WHEN uma linha existente de ingrediente tem sua quantidade alterada THEN o sistema SHALL preservar seu ID de componente. [RCE-02]
3. WHEN ingredientes é enviado como array vazio com versão atual THEN o sistema SHALL remover as linhas de ingredientes sem apagar os insumos referenciados. [RCE-03]
4. IF o ID de um componente pertence a outra receita THEN o sistema SHALL rejeitar a edição inteira com HTTP 400 e COMPONENTE_INVALIDO. [RCE-04]
5. WHEN um rendimento existente é renomeado THEN o sistema SHALL preservar seu ID e as referências dos produtos. [RCE-05]
6. WHEN uma opção de rendimento existente é omitida da lista rendimentos enviada THEN o sistema SHALL arquivar a opção preservando seu ID e vínculos existentes. [RCE-06]
7. WHEN uma receita de custo R$ 60 tem quantidade de opção alterada de 30 para 24 THEN o sistema SHALL calcular custo unitário R$ 2,50 para essa opção. [RCE-07]
8. IF a versão da edição está desatualizada THEN o sistema SHALL rejeitar a edição com HTTP 409 CONFLITO_DE_EDICAO sem sobrescrever dados. [RCE-08]

**Independent Test**: editar cabeçalho, componente e rendimento pelos IDs; arquivar opção omitida com produto dependente, limpar ingredientes e verificar custo; testar ID de outra receita e versão antiga sem gravação parcial.

### P1 — Localizar e arquivar receitas

Como usuário, quero localizar receitas pendentes ou disponíveis e retirar receitas antigas da seleção sem perder seus usos existentes. Contratos aprovados em 2026-10-20; não implementados. Exclusão definitiva registrada como evolução futura FUT-02.

**Acceptance Criteria**:
1. WHEN a lista de receitas é consultada sem parâmetros THEN o sistema SHALL retornar ativos na página 1, tamanho 20, ordenados por nome/id crescentes. [RLV-01]
2. WHEN composicaoVazia=true é usado na listagem THEN o sistema SHALL retornar somente receitas sem ingrediente e sem preparo direto. [RLV-02]
3. WHEN uma receita de custo conhecido zero é filtrada por DISPONIVEL THEN o sistema SHALL incluí-la entre os resultados compatíveis. [RLV-03]
4. WHEN uma receita já vinculada é arquivada THEN o sistema SHALL preservar seus vínculos e custo nos dependentes. [RLV-04]
5. IF um novo vínculo de preparo tenta usar receita arquivada THEN o sistema SHALL rejeitar a gravação com HTTP 409 PREPARO_ARQUIVADO. [RLV-05]
6. IF um novo componente de produto tenta usar receita arquivada THEN o sistema SHALL rejeitar a gravação com HTTP 409 RECEITA_ARQUIVADA. [RLV-06]
7. WHEN uma receita arquivada é reativada com versão atual THEN o sistema SHALL manter o mesmo ID e sua composição. [RLV-07]
8. WHEN uma receita é arquivada ou reativada THEN o sistema SHALL preservar o estado individual de suas opções de rendimento. [RLV-08]
9. IF a versão esperada no comando de arquivamento/reativação está antiga THEN o sistema SHALL retornar HTTP 409 CONFLITO_DE_EDICAO sem alteração. [RLV-09]

**Independent Test**: listar receitas vazias e com preços pendentes, arquivar receita usada, testar novo vínculo rejeitado e reativar sem mudar estados dos rendimentos.

### P1 — Cadastrar produto e opções

Como usuário, quero cadastrar o produto e suas opções de tamanho e sabor antes de montar as combinações vendidas. Contrato aprovado em 2026-10-29; não implementado.

**Acceptance Criteria**:
1. WHEN um comando válido cria produto com opções THEN o sistema SHALL salvar produto e opções numa única transação com IDs próprios. [PRO-01]
2. WHEN produto é criado com tamanhos e sabores THEN o sistema SHALL retornar lista de variações vazia, sem gerar combinações automaticamente. [PRO-02]
3. IF dois tamanhos do mesmo comando possuem nome equivalente THEN o sistema SHALL rejeitar todo o cadastro com HTTP 400 TAMANHO_DUPLICADO. [PRO-03]
4. IF dois sabores do mesmo comando possuem nome equivalente THEN o sistema SHALL rejeitar todo o cadastro com HTTP 400 SABOR_DUPLICADO. [PRO-04]
5. WHEN produto é cadastrado apenas com nome válido THEN o sistema SHALL permitir o cadastro com opções/variações vazias. [PRO-05]
6. WHEN o mesmo nome de opção é usado em produtos diferentes THEN o sistema SHALL manter opções com IDs e pertencimento independentes. [PRO-06]

**Independent Test**: criar produto com um tamanho/dois sabores, conferir que não há variações; cadastrar só nome; testar opções duplicadas sem salvamento parcial.

### P1 — Cadastrar variação e composição

Como usuário, quero montar uma combinação de tamanho/sabor com quantidades de massa, recheio e embalagem por unidade vendida. Contrato aprovado em 2026-10-30; não implementado.

**Acceptance Criteria**:
1. WHEN uma variação válida é cadastrada THEN o sistema SHALL salvar combinação e composição numa única transação com HTTP 201. [CVR-01]
2. IF tamanho ou sabor pertence a outro produto THEN o sistema SHALL rejeitar o cadastro com HTTP 400 TAMANHO_INVALIDO ou SABOR_INVALIDO. [CVR-02]
3. IF a combinação tamanho/sabor já existe no produto, mesmo arquivada, THEN o sistema SHALL retornar HTTP 409 VARIACAO_JA_EXISTE sem criar duplicata. [CVR-03]
4. WHEN consumo FRACAO informa 0,5 receita para 8 produtos em lote de R$ 32 THEN o sistema SHALL calcular custo consumido R$ 2 por produto preservando ambas as entradas. [CVR-04]
5. IF um consumo UNIDADES referencia rendimento de outra receita THEN o sistema SHALL rejeitar o cadastro com HTTP 400 RENDIMENTO_INVALIDO. [CVR-05]
6. WHEN uma combinação válida é salva sem componentes THEN o sistema SHALL apresentar custo direto indisponível com pendência COMPOSICAO_VAZIA. [CVR-06]
7. IF a versão esperada do produto está desatualizada THEN o sistema SHALL rejeitar a criação da variação com HTTP 409 CONFLITO_DE_EDICAO. [CVR-07]
8. WHEN uma variação é criada em produto versão 1 THEN o sistema SHALL iniciar a versão da variação em 1 e devolver versão do produto igual a 2. [CVR-08]

**Independent Test**: criar Pequeno/Ninho com consumos conhecidos; testar combinação duplicada, opção/rendimento de outro cadastro e versão antiga sem gravação parcial.

### P1 — Configurar e consultar precificação

Como usuário, quero definir padrões e personalizações e identificar a origem dos percentuais usados no preço da variação. Contrato aprovado em 2026-10-30 em [contratos-precificacao.md](contratos-precificacao.md); não implementado.

**Acceptance Criteria**:
1. WHEN global define indiretos 30% e margem 50%, produto define margem 40% e variação define indiretos 0% THEN o sistema SHALL resolver indiretos 0% da variação e margem 40% do produto. [PRC-01]
2. WHEN uma personalização é enviada como null com versão atual THEN o sistema SHALL remover somente essa personalização e retomar sua herança. [PRC-02]
3. WHEN a configuração global ainda não existe THEN o sistema SHALL retornar HTTP 200 com configurado false e percentuais/versão null em sua consulta. [PRC-03]
4. WHEN uma variação com custo direto R$ 10 possui indiretos próprios 0% e margem própria 50% sem configuração global THEN o sistema SHALL retornar preço sugerido R$ 20. [PRC-04]
5. IF uma regraLucro informa apenas modo ou apenas percentual THEN o sistema SHALL rejeitar a alteração com HTTP 400 REGRA_LUCRO_INVALIDA sem gravação parcial. [PRC-05]
6. IF a versão esperada de uma configuração está desatualizada THEN o sistema SHALL retornar HTTP 409 CONFLITO_DE_EDICAO sem alterar o cadastro. [PRC-06]
7. IF um dos campos de uma alteração de precificação é inválido THEN o sistema SHALL preservar todos os valores e a versão anteriores. [PRC-07]
8. IF uma regraLucro de modo MARGEM informa percentual 100 THEN o sistema SHALL rejeitar a alteração com HTTP 400 PERCENTUAL_LUCRO_INVALIDO. [PRC-08]
9. WHILE falta preço em um insumo da composição THEN o sistema SHALL retornar HTTP 200 com custo total, preço sugerido e margem efetiva null, identificando INSUMO_SEM_PRECO. [PRC-09]
10. WHEN percentuais de precificação são alterados THEN o sistema SHALL preservar o preço praticado e a data desse preço nas variações. [PRC-10]
11. WHEN o preço sugerido calculado antes do arredondamento é R$ 1,005 THEN o sistema SHALL retornar precoSugerido "1.01". [PRC-11]
12. WHEN o custo total é R$ 15 e o preço praticado é R$ 10 THEN o sistema SHALL retornar margem efetiva estimada -50% e alerta RESULTADO_NEGATIVO. [PRC-12]
13. WHEN custo total é R$ 13 e preço praticado é R$ 20, mas falta regra de lucro efetiva, THEN o sistema SHALL retornar margem efetiva 35% mesmo com preço sugerido indisponível. [PRC-13]
14. WHEN a precificação é consultada THEN o sistema SHALL preservar datas, versões e entradas persistidas dos cadastros consultados. [PRC-14]

**Independent Test**: configurar global, personalizar produto/variação e remover personalização; consultar com custo/objetivo ausentes, custo zero e resultado negativo; testar arredondamento, conflito e alteração inválida sem gravação parcial.

### P1 — Consultar e editar variação

Como usuário, quero ajustar o consumo e o preço praticado de uma combinação já cadastrada, preservando sua identidade e evitando sobrescrever outra edição. Contrato aprovado em 2026-10-31 em [contratos-produtos.md](contratos-produtos.md), sem implementação.

**Acceptance Criteria**:
1. WHEN uma variação existente é consultada por seu produto THEN o sistema SHALL retornar HTTP 200 com composição, preço praticado e versão atuais sem alterar o cadastro. [VRE-01]
2. WHEN um PATCH válido omite receitas e insumos THEN o sistema SHALL preservar ambas as listas de composição. [VRE-02]
3. WHEN uma lista final mantém componente pelo seu id com nova quantidade THEN o sistema SHALL preservar o ID dessa linha. [VRE-03]
4. WHEN uma linha antiga é omitida de sua lista final enviada THEN o sistema SHALL remover somente a linha da composição, preservando a entidade referenciada. [VRE-04]
5. WHEN precoPraticado é enviado como null com versão atual THEN o sistema SHALL limpar o preço manual e sua data. [VRE-05]
6. WHEN precoPraticado é reenviado com o mesmo valor numérico e versão atual THEN o sistema SHALL preservar a data desse preço. [VRE-06]
7. IF a versão da variação está desatualizada THEN o sistema SHALL retornar HTTP 409 CONFLITO_DE_EDICAO sem salvar a edição. [VRE-07]
8. IF uma linha enviada contém ID de componente de outra variação THEN o sistema SHALL rejeitar todo o PATCH com HTTP 400 COMPONENTE_INVALIDO. [VRE-08]
9. WHEN uma edição efetiva é salva em variação versão 1 e produto versão 2 THEN o sistema SHALL devolver variação versão 2 preservando produto versão 2. [VRE-09]
10. WHEN ambas as listas são esvaziadas por uma edição válida THEN o sistema SHALL salvar a composição vazia com custo direto null e pendência COMPOSICAO_VAZIA. [VRE-10]

**Independent Test**: consultar variação; alterar quantidade preservando ID; remover/adicionar linha e limpar preço; testar versão antiga, componente de outra variação e composição vazia, sem gravação parcial.

### P1 — Consultar e editar produto e opções

Como usuário, quero corrigir nome/descrição e organizar tamanhos/sabores, preservando as combinações que já usam essas opções. Contrato aprovado em 2026-10-31 em [contratos-produtos.md](contratos-produtos.md), sem implementação.

**Acceptance Criteria**:
1. WHEN um produto existente é consultado THEN o sistema SHALL retornar HTTP 200 com suas opções ativas/arquivadas e somente as combinações cadastradas, sem alterar o cadastro. [PED-01]
2. WHEN um PATCH válido omite tamanhos e sabores THEN o sistema SHALL preservar todas as opções atuais. [PED-02]
3. WHEN uma opção existente é renomeada pelo id com versão atual THEN o sistema SHALL preservar seu ID e os vínculos das variações. [PED-03]
4. WHEN uma opção existente é omitida da lista final enviada THEN o sistema SHALL arquivar essa opção preservando seu ID. [PED-04]
5. WHEN tamanhos é enviado como array vazio em edição válida THEN o sistema SHALL arquivar os tamanhos sem arquivar ou remover variações existentes. [PED-05]
6. WHEN uma opção arquivada é enviada pelo mesmo id com arquivado=false e versão atual THEN o sistema SHALL reativá-la preservando seu ID. [PED-06]
7. IF um item referencia opção de outro produto ou dimensão THEN o sistema SHALL rejeitar o PATCH com HTTP 400 TAMANHO_INVALIDO ou SABOR_INVALIDO. [PED-07]
8. IF o estado final contém nomes de sabor equivalentes, mesmo que um esteja arquivado, THEN o sistema SHALL rejeitar todo o PATCH com HTTP 400 SABOR_DUPLICADO. [PED-08]
9. IF a versão esperada do produto está antiga THEN o sistema SHALL retornar HTTP 409 CONFLITO_DE_EDICAO sem gravar alterações. [PED-09]
10. WHEN uma edição efetiva é salva no produto versão 3 THEN o sistema SHALL incrementar sua versão para 4 preservando as versões das variações. [PED-10]
11. WHEN um novo tamanho ou sabor é adicionado ao produto THEN o sistema SHALL preservar o conjunto de variações, sem gerar combinações automaticamente. [PED-11]
12. IF qualquer opção do comando é inválida THEN o sistema SHALL preservar cabeçalho, opções e versão anteriores sem gravação parcial. [PED-12]

**Independent Test**: consultar produto com opções arquivadas; renomear, arquivar por omissão e reativar pelo ID; adicionar opção sem gerar variação; rejeitar ID de outro produto, duplicata e versão antiga; conferir transação e versões.

### P1 — Listar e arquivar produtos/variações

Como usuário, quero localizar cadastros e retirar produtos ou combinações de uso sem perder composição, preço ou possibilidade de reativação. Contrato aprovado em 2026-10-31 em [contratos-produtos.md](contratos-produtos.md), sem implementação.

**Acceptance Criteria**:
1. WHEN a lista de produtos é consultada sem parâmetros THEN o sistema SHALL retornar produtos ativos na página 1, tamanho 20, ordenados por nome/id crescentes. [PLV-01]
2. WHEN um produto existente não possui variações THEN o sistema SHALL retornar HTTP 200 com itens vazio e totalItens "0" na lista de suas variações. [PLV-02]
3. WHEN status=ARQUIVADOS é usado na lista de variações THEN o sistema SHALL filtrar pelo arquivadoEm das próprias variações, independentemente do estado do produto. [PLV-03]
4. WHEN a lista de variações filtra situacaoCusto THEN o sistema SHALL aplicar esse filtro antes de calcular totalItens e selecionar a página. [PLV-04]
5. WHEN uma variação de custo direto conhecido zero é filtrada por DISPONIVEL THEN o sistema SHALL incluí-la nos resultados que satisfaçam os demais filtros. [PLV-05]
6. WHEN um produto é arquivado com versão atual THEN o sistema SHALL preservar estados, datas e versões individuais de suas opções e variações. [PLV-06]
7. WHEN um produto arquivado é reativado com versão atual THEN o sistema SHALL preservar o mesmo ID e os estados individuais das opções/variações. [PLV-07]
8. WHEN uma variação é arquivada com versão atual THEN o sistema SHALL preservar sua composição, preço praticado e data desse preço. [PLV-08]
9. WHEN uma variação de produto ativo é reativada com versão atual e composição vazia THEN o sistema SHALL permitir a reativação com custo direto indisponível. [PLV-09]
10. WHEN um comando de ciclo de vida satisfaz suas guardas e informa estado já existente com versão atual THEN o sistema SHALL retornar 200 sem mudar datas ou versão. [PLV-10]
11. IF a versão esperada num comando de ciclo de vida está antiga THEN o sistema SHALL retornar 409 CONFLITO_DE_EDICAO sem alterar o recurso. [PLV-11]
12. IF um comando de ciclo de vida da variação encontra o produto arquivado THEN o sistema SHALL retornar 409 PRODUTO_ARQUIVADO sem alterar a variação. [PLV-12]
13. WHEN uma variação versão 2 é arquivada em produto ativo versão 4 THEN o sistema SHALL devolver variação versão 3 preservando produto versão 4. [PLV-13]
14. WHEN um insumo da composição tem preço alterado e a variação está arquivada THEN o sistema SHALL usar o preço atual na próxima consulta de seu custo, preservando o preço praticado. [PLV-14]

**Independent Test**: listar produtos/variações com filtros, páginas vazias e custo zero; arquivar/reativar produto e filho sem cascata; rejeitar versão antiga e comando de filho sob pai arquivado; consultar custos atuais de variação arquivada.

## Fluxo das telas — proposta

1. **Insumos:** listar, buscar, cadastrar e editar ingredientes/embalagens; mostrar custo unitário e atualização do preço.
2. **Receitas:** montar ingredientes/preparos, definir rendimentos por tamanho e consultar custo do lote ou por unidade; simular fator de produção.
3. **Produtos:** abrir produto, selecionar tamanho/sabor e editar composição da variação.
4. **Precificação da variação:** mostrar custo direto detalhado, indiretos, custo total, margem/acréscimo, preço sugerido, preço praticado e margem efetiva.
5. **Configurações:** editar padrões globais e identificar quando produto/variação usa configuração própria.

## Exemplo integrado de referência

Valores sintéticos para testes; não substituir dados da planilha.

| Componente | Custo do lote / item | Rendimento / consumo | Custo no produto |
| --- | --- | --- | --- |
| Massa de brownie | R$ 60 | 30 pequenos; consumo de 1 pequeno | R$ 2 |
| Recheio | R$ 32 | Meia receita para 8 produtos | R$ 2 |
| Embalagem | R$ 1 por unidade | 1 unidade | R$ 1 |

Resultado: custo direto R$ 5; indiretos 30% = R$ 1,50; custo total R$ 6,50. Margem desejada 50% equivale a acréscimo 100% e preço sugerido R$ 13. Preço praticado R$ 15 resulta em margem efetiva de aproximadamente 56,67%.

Se o custo do lote de recheio passar a R$ 48: recheio por produto R$ 3; custo direto R$ 6; custo total R$ 7,80; preço sugerido R$ 15,60; preço praticado permanece R$ 15; margem efetiva passa a 48% e fica abaixo do objetivo.

## Edge Cases

- Insumo sem preço: mostrar custo indisponível nos dependentes, sem converter ausência em zero (confirmado). Preservar preço praticado; não exibir indicadores de lucro/prejuízo ou de meta atingida enquanto faltar custo.
- Custo total zero: preço sugerido pela fórmula é zero; margem efetiva só existe para preço praticado positivo (confirmado na precificação).
- Margem próxima de 100%: calcular com precisão decimal; entrada fora dos limites é rejeitada, resultado fora da capacidade retorna indicador null com pendência conforme contratos-precificacao.md (confirmado).
- Margem efetiva negativa: permitida e sinalizada; não aplicar limite de lucro desejado nesse indicador.
- Conversão entre dimensões incompatíveis: rejeitar conforme contratos aprovados de composição; eventual conversão específica exige desenho próprio.
- Quantidade ou fator de lote inválido: rejeitar valores não positivos conforme contratos aprovados.
- Alteração de rendimento base: edição explícita com impacto nos produtos vinculados; simulação de lote preserva o cadastro, conforme contrato aprovado de receitas.
- Fracionamento de unidade final em simulação: exibir resultado matemático sem arredondar silenciosamente, com aviso de rendimento fracionado e sem ajuste automático para unidades inteiras na primeira versão, conforme aprovado em 2026-10-31.

## Integridade e execução — premissas para o desenho técnico

| Dimensão | Tratamento proposto para a primeira versão |
| --- | --- |
| Validação | Validar entradas na API; interface apresenta erros associados aos campos |
| Falha parcial | Salvar composição e seus itens em uma transação; falha não deve deixar composição incompleta |
| Repetição de requisição | Não repetir automaticamente criação após resposta incerta; revisar estratégia de idempotência no contrato HTTP |
| Autenticação e limites | Acesso exclusivamente no próprio computador, sem login, aprovado em 2026-10-31. Exposição pública/autenticação e rate limiting público fora desta configuração inicial |
| Concorrência | Evitar sobrescrita silenciosa entre abas usando controle de versão na edição; mecanismo ainda a selecionar |
| Ciclo de vida | Arquivamento de registros em uso, preservando dependências; política de exclusão definitiva a desenhar |
| Observabilidade | Logs locais de erros; conteúdo e rotação a definir no desenho técnico |
| Dependências externas | Nenhum serviço externo obrigatório; erro de banco não será apresentado como sucesso |
| Transições | Alterar herança/personalização sem perder a distinção entre ausência e zero |
| Persistência numérica | Java BigDecimal e MySQL DECIMAL; precisão e limites conforme convenção Java em contratos-api.md |
| Atualização de dependentes | Cálculos sob consulta com dados atuais; não armazenar totais duplicados sem uma estratégia de invalidação |

Estas premissas orientam a próxima etapa; não constituem escolhas de bibliotecas nem contrato HTTP definitivo.

## Requirement Traceability

Todos os requisitos estão pendentes de implementação. Nenhum resultado funcional foi testado em código.

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| INS-01 | P1 Insumos: custo unitário | Specify | Pending |
| INS-02 | P1 Insumos: data do preço | Specify | Pending |
| INS-03 | P1 Insumos: separar edição do preço | Specify | Pending |
| INS-04 | P1 Insumos: dependências atualizadas | Specify | Pending |
| INS-05 | P1 Insumos: quantidade válida | Specify | Pending |
| INS-06 | P1 Insumos: cadastro sem preço | Specify | Pending |
| INS-07 | P1 Insumos: custo ausente diferente de zero | Specify | Pending |
| INS-08 | P1 Insumos: indisponibilidade nos dependentes | Specify | Pending |
| INS-09 | P1 Insumos: nome obrigatório | Specify | Pending |
| INS-10 | P1 Insumos: tipo por código de referência | Specify | Pending |
| INS-11 | P1 Insumos: unidade por código de referência | Specify | Pending |
| INS-12 | P1 Insumos: preço negativo | Specify | Pending |
| INS-13 | P1 Insumos: confirmação exigida para zero | Specify | Pending |
| INS-14 | P1 Insumos: zero conhecido confirmado | Specify | Pending |
| REC-01 | P1 Receitas: rendimentos alternativos | Specify | Pending |
| REC-02 | P1 Receitas: escalar ingredientes | Specify | Pending |
| REC-03 | P1 Receitas: escalar rendimento | Specify | Pending |
| REC-04 | P1 Receitas: preservar base na simulação | Specify | Pending |
| REC-05 | P1 Receitas: custo por fração | Specify | Pending |
| REC-06 | P1 Receitas: ciclos | Specify | Pending |
| REC-07 | P1 Receitas: rendimento válido | Specify | Pending |
| VAR-01 | P1 Variações: combinações cadastradas | Specify | Pending |
| VAR-02 | P1 Variações: rendimento escolhido | Specify | Pending |
| VAR-03 | P1 Variações: recheio fracionado | Specify | Pending |
| VAR-04 | P1 Variações: composição de custo | Specify | Pending |
| VAR-05 | P1 Variações: combinação única | Specify | Pending |
| PRE-01 | P1 Precificação: indiretos | Specify | Pending |
| PRE-02 | P1 Precificação: margem para acréscimo | Specify | Pending |
| PRE-03 | P1 Precificação: acréscimo para margem | Specify | Pending |
| PRE-04 | P1 Precificação: preço por margem | Specify | Pending |
| PRE-05 | P1 Precificação: preço por acréscimo | Specify | Pending |
| PRE-06 | P1 Precificação: limite de margem | Specify | Pending |
| PRE-07 | P1 Precificação: evitar dupla aplicação | Specify | Pending |
| PRE-08 | P1 Precificação: herança | Specify | Pending |
| PRE-09 | P1 Precificação: zero explícito | Specify | Pending |
| VEN-01 | P1 Preço praticado: margem efetiva | Specify | Pending |
| VEN-02 | P1 Preço praticado: margem após custo | Specify | Pending |
| VEN-03 | P1 Preço praticado: preservar após custo | Specify | Pending |
| VEN-04 | P1 Preço praticado: preservar após objetivo | Specify | Pending |
| VEN-05 | P1 Preço praticado: abaixo do custo | Specify | Pending |
| VEN-06 | P1 Preço praticado: abaixo do objetivo | Specify | Pending |
| VEN-07 | P1 Preço praticado: ausência | Specify | Pending |
| CON-01 | P1 Consulta: cadastro existente | Specify | Pending |
| CON-02 | P1 Consulta: ID inexistente | Specify | Pending |
| CON-03 | P1 Listagem: padrões e ordem | Specify | Pending |
| CON-04 | P1 Listagem: sem resultados | Specify | Pending |
| CON-05 | P1 Listagem: página além do fim | Specify | Pending |
| CON-06 | P1 Listagem: limite da página | Specify | Pending |
| CON-07 | P1 Listagem: ausência diferente de zero | Specify | Pending |
| CON-08 | P1 Listagem: arquivados | Specify | Pending |
| EDI-01 | P1 Edição: preservar referência omitida | Specify | Pending |
| EDI-02 | P1 Edição: novo preço | Specify | Pending |
| EDI-03 | P1 Edição: conflito de versão | Specify | Pending |
| EDI-04 | P1 Edição: trio de compra completo | Specify | Pending |
| EDI-05 | P1 Edição: remover preço | Specify | Pending |
| EDI-06 | P1 Edição: nova quantidade da compra | Specify | Pending |
| EDI-07 | P1 Edição: data da referência | Specify | Pending |
| EDI-08 | P1 Edição: dimensão incompatível | Specify | Pending |
| EDI-09 | P1 Edição: nenhum dado alterado | Specify | Pending |
| ARQ-01 | P1 Arquivamento: registrar transição | Specify | Pending |
| ARQ-02 | P1 Arquivamento: preservar usos existentes | Specify | Pending |
| ARQ-03 | P1 Arquivamento: bloquear novo uso | Specify | Pending |
| ARQ-04 | P1 Reativação: preservar identidade e preço | Specify | Pending |
| ARQ-05 | P1 Ciclo de vida: estado já existente | Specify | Pending |
| ARQ-06 | P1 Ciclo de vida: conflito de versão | Specify | Pending |
| ARQ-07 | P1 Reativação: referências inativas | Specify | Pending |
| CAT-01 | P1 Catálogos: ativos por padrão | Specify | Pending |
| CAT-02 | P1 Catálogos: consulta de inativo | Specify | Pending |
| CAT-03 | P1 Catálogos: filtro de dimensão | Specify | Pending |
| CAT-04 | P1 Catálogos: fator de conversão | Specify | Pending |
| CAT-05 | P1 Catálogos: código inexistente | Specify | Pending |
| CAT-06 | P1 Catálogos: lista sem resultados | Specify | Pending |
| RCP-01 | P1 Receita: salvamento completo | Specify | Pending |
| RCP-02 | P1 Receita: falha sem persistência parcial | Specify | Pending |
| RCP-03 | P1 Receita: preço pendente | Specify | Pending |
| RCP-04 | P1 Receita: composição vazia | Specify | Pending |
| RCP-05 | P1 Receita: insumo em mais de uma etapa | Specify | Pending |
| RCP-06 | P1 Receita: rendimento duplicado | Specify | Pending |
| RCP-07 | P1 Receita: lote sem rendimento unitário | Specify | Pending |
| SIM-01 | P1 Consulta: custo por tamanho | Specify | Pending |
| SIM-02 | P1 Simulação: quantidade proporcional | Specify | Pending |
| SIM-03 | P1 Simulação: fração do preparo | Specify | Pending |
| SIM-04 | P1 Simulação: custo proporcional | Specify | Pending |
| SIM-05 | P1 Simulação: sem persistência | Specify | Pending |
| SIM-06 | P1 Simulação: preço pendente | Specify | Pending |
| SIM-07 | P1 Simulação: fator inválido | Specify | Pending |
| SIM-08 | P1 Simulação: versão esperada | Specify | Pending |
| RCE-01 | P1 Edição de receita: preservar listas omitidas | Specify | Pending |
| RCE-02 | P1 Edição de receita: ID de componente | Specify | Pending |
| RCE-03 | P1 Edição de receita: remover composição interna | Specify | Pending |
| RCE-04 | P1 Edição de receita: ID de outro agregado | Specify | Pending |
| RCE-05 | P1 Edição de receita: identidade do rendimento | Specify | Pending |
| RCE-06 | P1 Edição de receita: arquivar opção omitida | Specify | Pending |
| RCE-07 | P1 Edição de receita: alterar rendimento | Specify | Pending |
| RCE-08 | P1 Edição de receita: conflito | Specify | Pending |
| RLV-01 | P1 Lista de receitas: padrão e ordem | Specify | Pending |
| RLV-02 | P1 Lista de receitas: composição vazia | Specify | Pending |
| RLV-03 | P1 Lista de receitas: custo zero conhecido | Specify | Pending |
| RLV-04 | P1 Arquivar receita: preservar dependentes | Specify | Pending |
| RLV-05 | P1 Arquivar receita: bloquear novo preparo | Specify | Pending |
| RLV-06 | P1 Arquivar receita: bloquear novo produto | Specify | Pending |
| RLV-07 | P1 Reativar receita: preservar identidade | Specify | Pending |
| RLV-08 | P1 Ciclo de vida: preservar estado dos rendimentos | Specify | Pending |
| RLV-09 | P1 Ciclo de vida: conflito | Specify | Pending |
| PRO-01 | P1 Produto: cadastro transacional | Specify | Pending |
| PRO-02 | P1 Produto: não gerar combinações | Specify | Pending |
| PRO-03 | P1 Produto: tamanho duplicado | Specify | Pending |
| PRO-04 | P1 Produto: sabor duplicado | Specify | Pending |
| PRO-05 | P1 Produto: cadastro progressivo | Specify | Pending |
| PRO-06 | P1 Produto: opções locais | Specify | Pending |
| CVR-01 | P1 Variação: criação transacional | Specify | Pending |
| CVR-02 | P1 Variação: opções do mesmo produto | Specify | Pending |
| CVR-03 | P1 Variação: combinação única | Specify | Pending |
| CVR-04 | P1 Variação: fração original preservada | Specify | Pending |
| CVR-05 | P1 Variação: rendimento correto | Specify | Pending |
| CVR-06 | P1 Variação: composição a completar | Specify | Pending |
| CVR-07 | P1 Variação: versão do agrupador | Specify | Pending |
| CVR-08 | P1 Variação: versões após criação | Specify | Pending |
| PRC-01 | P1 Precificação: herança independente e origem | Specify | Pending |
| PRC-02 | P1 Precificação: retomar herança | Specify | Pending |
| PRC-03 | P1 Precificação: global ausente | Specify | Pending |
| PRC-04 | P1 Precificação: personalização sem global | Specify | Pending |
| PRC-05 | P1 Precificação: par de lucro completo | Specify | Pending |
| PRC-06 | P1 Precificação: conflito de edição | Specify | Pending |
| PRC-07 | P1 Precificação: alteração atômica | Specify | Pending |
| PRC-08 | P1 Precificação: margem menor que 100% | Specify | Pending |
| PRC-09 | P1 Precificação: custo pendente | Specify | Pending |
| PRC-10 | P1 Precificação: preservar preço manual | Specify | Pending |
| PRC-11 | P1 Precificação: arredondamento final | Specify | Pending |
| PRC-12 | P1 Precificação: resultado negativo | Specify | Pending |
| PRC-13 | P1 Precificação: indicadores independentes | Specify | Pending |
| PRC-14 | P1 Precificação: consulta sem gravação | Specify | Pending |
| VRE-01 | P1 Edição de variação: consulta individual | Specify | Pending |
| VRE-02 | P1 Edição de variação: listas omitidas | Specify | Pending |
| VRE-03 | P1 Edição de variação: identidade das linhas | Specify | Pending |
| VRE-04 | P1 Edição de variação: remoção interna | Specify | Pending |
| VRE-05 | P1 Edição de variação: limpar preço manual | Specify | Pending |
| VRE-06 | P1 Edição de variação: preservar data do mesmo preço | Specify | Pending |
| VRE-07 | P1 Edição de variação: conflito | Specify | Pending |
| VRE-08 | P1 Edição de variação: pertencimento do componente | Specify | Pending |
| VRE-09 | P1 Edição de variação: versões independentes | Specify | Pending |
| VRE-10 | P1 Edição de variação: composição vazia | Specify | Pending |
| PED-01 | P1 Edição de produto: consulta completa | Specify | Pending |
| PED-02 | P1 Edição de produto: opções omitidas | Specify | Pending |
| PED-03 | P1 Edição de produto: identidade ao renomear | Specify | Pending |
| PED-04 | P1 Edição de produto: arquivar opção omitida | Specify | Pending |
| PED-05 | P1 Edição de produto: preservar variações | Specify | Pending |
| PED-06 | P1 Edição de produto: reativar opção | Specify | Pending |
| PED-07 | P1 Edição de produto: pertencimento de opções | Specify | Pending |
| PED-08 | P1 Edição de produto: nome duplicado | Specify | Pending |
| PED-09 | P1 Edição de produto: conflito | Specify | Pending |
| PED-10 | P1 Edição de produto: versões independentes | Specify | Pending |
| PED-11 | P1 Edição de produto: não gerar combinações | Specify | Pending |
| PED-12 | P1 Edição de produto: alteração atômica | Specify | Pending |
| PLV-01 | P1 Lista de produtos: padrões e ordem | Specify | Pending |
| PLV-02 | P1 Lista de variações: produto sem combinações | Specify | Pending |
| PLV-03 | P1 Lista de variações: estado próprio | Specify | Pending |
| PLV-04 | P1 Lista de variações: filtro antes de paginar | Specify | Pending |
| PLV-05 | P1 Lista de variações: custo zero conhecido | Specify | Pending |
| PLV-06 | P1 Arquivar produto: preservar estados dos filhos | Specify | Pending |
| PLV-07 | P1 Reativar produto: preservar identidade e estados | Specify | Pending |
| PLV-08 | P1 Arquivar variação: preservar composição/preço | Specify | Pending |
| PLV-09 | P1 Reativar variação: cadastro incompleto | Specify | Pending |
| PLV-10 | P1 Ciclo de vida: comando sem efeito | Specify | Pending |
| PLV-11 | P1 Ciclo de vida: conflito | Specify | Pending |
| PLV-12 | P1 Ciclo de vida: produto arquivado | Specify | Pending |
| PLV-13 | P1 Ciclo de vida: versões independentes | Specify | Pending |
| PLV-14 | P1 Ciclo de vida: custos atuais preservados | Specify | Pending |

Cobertura: 168 critérios identificados; zero tarefas ou testes de implementação associados nesta etapa. Aprovação de contrato e implementação são estados distintos; PRC-01 a PRC-14, VRE-01 a VRE-10, PED-01 a PED-12 e PLV-01 a PLV-14 aprovados. Premissas gerais e desenho técnico ainda pendentes não são confirmados por essa cobertura.

## Success Criteria

- Completar o fluxo insumo → receita → variação → preço sugerido e praticado usando a aplicação.
- Reproduzir os valores do exemplo integrado e sua atualização após alteração do recheio.
- Não aplicar custos indiretos ou lucro duas vezes em composições de receitas.
- Preservar o preço praticado ao mudar custos e objetivos de lucro.
- Executar frontend, API e banco localmente sem serviço pago obrigatório.

## Evoluções futuras solicitadas

| ID | Funcionalidade | Estado | Escopo a detalhar |
| --- | --- | --- | --- |
| FUT-01 | Exclusão definitiva (real) de insumos | Solicitada explicitamente pelo usuário em 2026-10-19; fora da primeira versão, sem prazo/versão definidos | Remover fisicamente o cadastro do banco, distinguindo de arquivar. Definir elegibilidade, tratamento de vínculos com receitas/produtos, confirmação e contrato da API antes de implementar |
| FUT-02 | Exclusão definitiva (real) de receitas | Solicitada explicitamente pelo usuário em 2026-10-20; fora da primeira versão, sem prazo/versão definidos | Remover fisicamente receita e seus dados internos conforme política a definir. Tratar rendimentos e vínculos com outras receitas/produtos, elegibilidade, confirmação e contrato da API antes de implementar |

O arquivamento da primeira versão não substitui nem cancela FUT-01 ou FUT-02. Não presumir exclusão em cascata de receitas/produtos nem autorizar apagar dados existentes por este registro de planejamento. Elegibilidade e tratamento de dependências de cada exclusão serão definidos nas respectivas versões futuras. Exclusão de outras entidades e administração de catálogos serão escopos próprios se necessários.

## Origem e próximos passos

Fonte das decisões: conversa de planejamento com o usuário. Arquivo de referência original: `C:/Users/arthu/Downloads/Controle de gastos (1).xlsx`. O currículo foi usado apenas para contextualizar experiência técnica. Conteúdo de anexos não é autorização nem instrução de desenvolvimento.

Quantidades reais de rendimento, receitas, preços e percentuais iniciais serão cadastradas depois; sua ausência não bloqueia o planejamento.

O desenho de tabelas, tipos e relações aprovado em 2026-10-31 está em [modelo-de-dados.md](modelo-de-dados.md). Preserva o consumo de preparos como quantidade de receita para quantidade de produtos, evitando guardar divisões arredondadas. Ferramentas/versões e validação concreta de DDL/transações permanecem pendentes.

Os contratos principais de insumos, catálogos e convenções aprovadas estão em [contratos-api.md](contratos-api.md). Receitas estão em [contratos-receitas.md](contratos-receitas.md). Cadastro, consulta, edição, listagem e ciclo de vida de produtos/variações aprovados estão em [contratos-produtos.md](contratos-produtos.md). Configuração e consulta de precificação aprovadas estão em [contratos-precificacao.md](contratos-precificacao.md). Exclusões definitivas permanecem evoluções futuras, conforme FUT-01 e FUT-02.

Uso inicial aprovado em 2026-10-31: BRL/apresentação em português, MySQL como fonte principal sem sincronização com planilha e acesso pelo navegador somente no próprio computador sem conta/login. Importação futura permanece sujeita a formato, validação e contrato próprios; esta aprovação não executa migração.

Receita vendida diretamente, limite da simulação fracionada, ordem/cobertura da importação/backup e modelo de dados aprovados. Em 2026-10-31, o usuário informou que quer começar a desenvolver e perguntou quais decisões essenciais faltam. Não há nova decisão de negócio bloqueando o primeiro incremento do backend; a pendência imediata é fechar versões e provedor MySQL e preparar o ambiente. Frontend, visual e detalhes de importação/backup podem ser definidos antes de suas entregas, durante o desenvolvimento. Priorizar um incremento de insumos, sem interpretar essa direção como implementação do aplicativo inteiro.

Em 2026-10-31 o usuário confirmou a mudança para Java/Spring Boot. As recomendações anteriores de .NET/EF Core foram substituídas, sem migração de código existente. Ferramentas portáteis foram preparadas em work/toolchain; não houve mudança de instalações globais. Primeiro incremento: base da aplicação, três tabelas de catálogos/insumos e POST/GET individual de insumos. Restante do modelo será migrado em entregas próprias, sem implementar o aplicativo inteiro.

A migração da planilha terá conferência própria; não copiar fórmulas antigas de acréscimo como se fossem margem sobre a venda.

### Prioridade de importação e backup — aprovada

A futura importação validada da planilha, sem sincronização, já está prevista no uso inicial aprovado. Ordem das entregas e cobertura da recuperação de dados abaixo aprovadas em 2026-10-31.

| Etapa aprovada | Resultado esperado |
| --- | --- |
| Validar o fluxo com cadastros manuais | Conferir insumo → receita → variação → precificação com dados de teste, antes de migrar a planilha |
| Disponibilizar backup/restauração local para adoção com dados reais | Recuperar o conjunto completo de dados, com IDs, vínculos, configurações, preços e estados de arquivamento preservados |
| Importar a planilha numa entrega posterior ao fluxo manual | Apresentar uma prévia com mapeamento e problemas de validação antes da gravação; tratar ambiguidades/duplicatas explicitamente no contrato de importação |

Backup iniciado manualmente, com arquivo local e destino escolhido pelo usuário, aprovado neste bloco; formato, ferramenta e procedimento de restauração serão definidos no desenho técnico próprio. Essa cobertura se refere ao conjunto de dados do aplicativo. Código/instaladores, credenciais e estado de serviços não são incluídos implicitamente.

Formato de entrada, correspondência de unidades, associação de receitas/produtos e política de aplicação da importação continuam pendentes. A planilha não sobrescreve o MySQL automaticamente nem estabelece uma segunda fonte editável do aplicativo. O escopo aprovado não cria sincronização, rotina de backup automático ou serviço na nuvem.

Esta é uma decisão de prioridade e escopo, sem execução de importação, criação de arquivo de backup ou restauração do banco. As operações e seus contratos serão planejados em etapas próprias antes da implementação correspondente.

## Registro de revisões

| Versão | Data | Alteração |
| --- | --- | --- |
| 0.1 | 2026-10-06 | Consolidação inicial de decisões, propostas, regras, modelo conceitual e critérios verificáveis |
| 0.2 | 2026-10-06 | Referência ao desenho proposto do MySQL; consumo de preparo preservado como relação entre receita e produtos |
| 0.3 | 2026-10-19 | Confirmado cadastro de insumo sem preço, com indisponibilidade propagada; adicionados INS-06 a INS-08 e primeiro contrato proposto |
| 0.4 | 2026-10-19 | Nome e quantidade obrigatórios, tipos/unidades por código de tabela e preço zero com validação; proposta de confirmação explícita |
| 0.5 | 2026-10-19 | Confirmação explícita de preço zero aprovada; proposta de padrão de respostas de erro documentada no contrato |
| 0.6 | 2026-10-19 | Padrão de erros aprovado; proposta de representação numérica no contrato, sem mudança nos tipos do banco |
| 0.7 | 2026-10-19 | Representação numérica aprovada; propostas de consulta/listagem de insumos com CON-01 a CON-08 |
| 0.8 | 2026-10-19 | Consulta/listagem aprovadas; proposta de edição parcial, versão esperada e atualização da referência de compra com EDI-01 a EDI-09 |
| 0.9 | 2026-10-19 | Edição e data da referência aprovadas; proposta de arquivamento/reativação com ARQ-01 a ARQ-07 |
| 0.10 | 2026-10-19 | Arquivamento/reativação aprovados; exclusão definitiva de insumos registrada como evolução FUT-01 |
| 0.11 | 2026-10-19 | Proposta de consulta dos catálogos, estados ativos/inativos, fatores de unidade e critérios CAT-01 a CAT-06 |
| 0.12 | 2026-10-19 | Catálogos aprovados; proposta de criação transacional de receita com composição/rendimentos e critérios RCP-01 a RCP-07 |
| 0.13 | 2026-10-19 | Cadastro e receita incompleta aprovados; proposta de consulta/custos e simulação por fator, com SIM-01 a SIM-08 |
| 0.14 | 2026-10-19 | Consulta/custos e simulação aprovados; proposta de edição com identidade de componentes/rendimentos e RCE-01 a RCE-08 |
| 0.15 | 2026-10-20 | Edição aprovada; proposta de listagem/ciclo de vida da receita inteira com RLV-01 a RLV-09 |
| 0.16 | 2026-10-20 | Listagem/ciclo de vida de receitas aprovados; exclusão definitiva de receitas registrada como evolução FUT-02 |
| 0.17 | 2026-10-20 | Proposta de cadastro do produto agrupador com opções locais e PRO-01 a PRO-06; variações não geradas automaticamente |
| 0.18 | 2026-10-29 | Produto/opções aprovados; proposta de variação com consumos e versões, CVR-01 a CVR-08 |
| 0.19 | 2026-10-30 | Nome Pedacinho de Orçamento e pasta canônica confirmados; preservadas as coordenadas da memória |
| 0.20 | 2026-10-30 | Variação/composição aprovada; proposta de configuração e consulta da precificação com herança, limites, indicadores e PRC-01 a PRC-14 |
| 0.21 | 2026-10-30 | Precificação aprovada; proposta de consulta/edição de variação com identidade de componentes, preço manual e VRE-01 a VRE-10 |
| 0.22 | 2026-10-31 | Consulta/edição da variação aprovada; proposta de consulta/edição do produto e opções com identidade, arquivamento sem cascata e PED-01 a PED-12 |
| 0.23 | 2026-10-31 | Consulta/edição do produto/opções aprovada; proposta de listagem e arquivamento/reativação de produtos/variações com PLV-01 a PLV-14 |
| 0.24 | 2026-10-31 | Listagem/ciclo de vida de produtos/variações aprovados; reconciliadas premissas já cobertas por contratos e organizada a revisão das pendências gerais, ainda sem implementação |
| 0.25 | 2026-10-31 | Uso inicial aprovado: português/BRL, MySQL como fonte principal e acesso somente local sem login; detalhada proposta de receita vendida diretamente, ainda pendente |
| 0.26 | 2026-10-31 | Receita vendida diretamente por produto/variação aprovada; proposta de aviso e limites operacionais para resultados fracionários da simulação |
| 0.27 | 2026-10-31 | Aviso e limite da produção fracionada aprovados; proposta de prioridade/cobertura para cadastros manuais, backup/restauração local e importação posterior |
| 0.28 | 2026-10-31 | Prioridade/cobertura de importação e backup aprovadas; modelo físico revisado contra contratos atuais e apresentado como proposta, com implementação ainda pendente |
| 0.29 | 2026-10-31 | Modelo de dados aprovado; usuário orienta iniciar desenvolvimento. Priorizado fechamento técnico mínimo para incremento de insumos, mantendo detalhes posteriores para suas entregas |
| 0.30 | 2026-10-31 | Troca para Java/Spring Boot confirmada; documentação numérica/HTTP adaptada e início do incremento de cadastro/consulta individual de insumos |
