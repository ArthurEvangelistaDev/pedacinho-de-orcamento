# Contratos da API — produtos e variações

Versão: 0.12  
Atualizado: 2026-10-31  
Estado: cadastro, consulta, edição, listagem e arquivamento/reativação de produtos/variações aprovados; sem implementação.

Referências: [spec.md](spec.md), [modelo de dados](modelo-de-dados.md), [convenções da API](contratos-api.md) e [receitas](contratos-receitas.md).

## Produto, opções e variação

Decisão já confirmada: o usuário abre um produto e seleciona tamanho e sabor. Produto é o agrupador; variação é a combinação vendida, com composição e precificação próprias.

Cadastro do agrupador e suas opções aprovado em 2026-10-29. Criação explícita de uma combinação com consumo de receitas/insumos e preço praticado aprovada em 2026-10-30. Não gerar todas as combinações automaticamente.

| Conceito | Exemplo ilustrativo | Responsabilidade |
| --- | --- | --- |
| Produto | Brownie recheado | Nome e opções próprias |
| Tamanho | Pequeno | Opção comercial deste produto |
| Sabor | Ninho | Opção comercial deste produto |
| Variação | Pequeno + Ninho | Composição e preço daquela combinação |

Tamanho comercial não é automaticamente uma opção de rendimento de receita. A composição da variação deve escolher explicitamente a receita e, em consumo por unidade, seu rendimento. Um tamanho chamado Pequeno não autoriza procurar rendimento por nome e assumir que é a mesma entidade.

## Cadastrar produto — aprovado

`POST /api/v1/produtos`, `Content-Type: application/json`.

### Entrada

Opções ilustrativas; não representam catálogo obrigatório nem disponibilidade real de sabores já confirmada.

```json
{
  "nome": "Brownie recheado",
  "descricao": "Brownie pequeno com recheio.",
  "tamanhos": [
    {
      "nome": "Pequeno"
    }
  ],
  "sabores": [
    {
      "nome": "Ninho"
    },
    {
      "nome": "Chocolate"
    }
  ]
}
```

### Validação

| Campo | Regra |
| --- | --- |
| nome | Obrigatório, trim, 1 a 150 caracteres |
| descricao | Opcional, até 10.000 caracteres; null ou vazio após trim significa sem descrição |
| tamanhos | Array opcional, padrão vazio; cada item possui nome obrigatório de 1 a 100 caracteres |
| sabores | Array opcional, padrão vazio; cada item possui nome obrigatório de 1 a 100 caracteres |

Array/item null é inválido. Nomes iguais de produto não serão chave de identidade; produtos distintos têm IDs distintos. Dentro do mesmo produto, nomes de tamanhos são únicos e nomes de sabores são únicos, com comparação de nomes conforme collation adotada. Um mesmo texto pode existir em tamanho e sabor sem conflito, pois são dimensões diferentes.

Não aceitar IDs de produto/opção, datas, versão, custo calculado, preço praticado ou uma lista de variações neste comando. O campo da API de criação não deve referenciar uma opção já existente de outro produto. Propriedades desconhecidas retornam 400 CAMPO_NAO_PERMITIDO.

Cada opção recebe ID próprio e pertence apenas ao produto criado. Ordem inicial é a posição no array, iniciada em 0. Preservar a ordem escolhida pelo usuário. Não deduplicar por nome nem transformar opções de outros produtos em catálogo global.

Persistir produto e suas opções numa única transação. Falha em um tamanho/sabor rejeita todo o cadastro sem deixar produto parcial. Ausência de receitas, preços ou configuração global não impede criar esse agrupador.

## Produto a completar

Aprovado permitir cadastrar somente nome, com tamanhos/sabores vazios. Isso não cria uma variação implícita nem um produto pronto para calcular preço.

Quando só houver uma opção real, cadastrar explicitamente um tamanho ou sabor, por exemplo Único e Tradicional, em vez de usar null na futura combinação. Não inserir esses valores silenciosamente: o usuário informa os rótulos adequados.

Configurações próprias de precificação e herança estão aprovadas em [contratos-precificacao.md](contratos-precificacao.md). Campos opcionais próprios no banco iniciam null, sem inventar padrão de margem/indiretos. Não exigir que o usuário configure lucro antes de salvar nome/opções.

## Resposta de sucesso

HTTP `201 Created`, `Location: /api/v1/produtos/201`. Exemplo com IDs e datas fictícios:

```json
{
  "id": "201",
  "nome": "Brownie recheado",
  "descricao": "Brownie pequeno com recheio.",
  "tamanhos": [
    {
      "id": "301",
      "nome": "Pequeno",
      "ordem": 0,
      "arquivadoEm": null
    }
  ],
  "sabores": [
    {
      "id": "401",
      "nome": "Ninho",
      "ordem": 0,
      "arquivadoEm": null
    },
    {
      "id": "402",
      "nome": "Chocolate",
      "ordem": 1,
      "arquivadoEm": null
    }
  ],
  "variacoes": [],
  "criadoEm": "2026-10-20T15:00:00Z",
  "atualizadoEm": "2026-10-20T15:00:00Z",
  "arquivadoEm": null,
  "versao": "1"
}
```

IDs e versão como strings; ordem como inteiro pequeno; datas UTC. Nenhuma variação é criada só porque tamanho e sabor foram cadastrados. Sem variações, não existe um preço único calculado para o produto agrupador; não exibir R$ 0 como substituto.

GET /produtos/{id} e edição de produto/opções estão aprovados adiante; o Location indica o identificador do recurso criado, sem presumir implementação pronta.

## Erros do cadastro de produto

| Situação | HTTP / código |
| --- | --- |
| Nome de produto ausente ou só espaços | 400 / NOME_OBRIGATORIO |
| Nome acima do limite | 400 / NOME_MUITO_LONGO |
| Descrição acima do limite | 400 / TEXTO_MUITO_LONGO |
| Nome vazio de tamanho/sabor | 400 / NOME_OBRIGATORIO no campo da opção |
| Tamanho repetido no comando | 400 / TAMANHO_DUPLICADO |
| Sabor repetido no comando | 400 / SABOR_DUPLICADO |
| Lista/item null | 400 / LISTA_INVALIDA ou ITEM_INVALIDO |
| Campo inesperado ou somente leitura | 400 / CAMPO_NAO_PERMITIDO |

Problem Details conforme aprovado; erros de domínio com VALIDACAO_FALHOU no topo e campos como tamanhos[0].nome e sabores[1].nome. JSON malformado/tipo incompatível usa REQUISICAO_INVALIDA; falhas técnicas usam 500/503 conforme convenções. Não rejeitar nome de produto repetido como se fosse combinação duplicada: unicidade da combinação pertence ao cadastro da variação.

## Verificação futura

| Cenário | Resultado esperado |
| --- | --- |
| Produto, um tamanho e dois sabores válidos | Produto e opções salvos com IDs; variacoes vazio |
| Produto apenas com nome | Permitido para completar depois |
| Dois tamanhos de nome equivalente | 400 TAMANHO_DUPLICADO; sem produto parcial |
| Dois sabores de nome equivalente | 400 SABOR_DUPLICADO; sem produto parcial |
| Mesmo nome de opção em produtos diferentes | Permitido, IDs independentes |
| Nome repetido entre tamanho e sabor | Permitido, dimensões independentes |
| Ausência de receitas/configuração de margem | Cadastro do agrupador permitido |
| Tentativa de passar ID de opção de outro produto | Campo não permitido no POST |

## Criar variação com composição — aprovada

`POST /api/v1/produtos/{produtoId}/variacoes`, `Content-Type: application/json`.

Uma variação representa uma combinação de tamanho e sabor já cadastrados no produto. Sua composição descreve o consumo por uma unidade vendida, sem representar produção ou venda efetiva.

### Exemplo de entrada

IDs fictícios: produto 201, tamanho 301 Pequeno, sabor 401 Ninho; receita 101 Massa de brownie com rendimento 501 Pequeno; receita 80 Recheio; insumo 60 Embalagem. Os valores não fixam rendimentos ou preços reais do negócio.

```json
{
  "versaoProduto": "1",
  "tamanhoId": "301",
  "saborId": "401",
  "receitas": [
    {
      "receitaId": "101",
      "modoConsumo": "UNIDADES",
      "rendimentoId": "501",
      "quantidadeReceita": "1",
      "quantidadeProdutos": "1"
    },
    {
      "receitaId": "80",
      "modoConsumo": "FRACAO",
      "rendimentoId": null,
      "quantidadeReceita": "0.5",
      "quantidadeProdutos": "8"
    }
  ],
  "insumos": [
    {
      "insumoId": "60",
      "quantidade": "1",
      "unidadeCodigo": "un"
    }
  ],
  "precoPraticado": "15.00"
}
```

### Identidade da combinação

- produtoId da rota deve existir e estar ativo; inexistente retorna 404 RECURSO_NAO_ENCONTRADO, arquivado retorna 409 PRODUTO_ARQUIVADO.
- versaoProduto obrigatória, como string da versão atual do agrupador. Ausente/inválida: 400 VERSAO_OBRIGATORIA/VERSAO_INVALIDA no campo; antiga: 409 CONFLITO_DE_EDICAO.
- tamanhoId e saborId obrigatórios, existentes, ativos e pertencentes ao produto da rota. Opção de outro produto/inexistente: 400 TAMANHO_INVALIDO ou SABOR_INVALIDO; opção arquivada: 409 OPCAO_ARQUIVADA no campo correspondente.
- Não criar opções a partir de nome neste comando. O frontend usa os IDs devolvidos no cadastro/consulta do produto.
- Combinação `(produtoId, tamanhoId, saborId)` é única, inclusive se já arquivada. Duplicata: 409 VARIACAO_JA_EXISTE, sem nova composição; editar/reativar o cadastro existente por contratos próprios.
- Só a combinação solicitada é criada. Um tamanho e dois sabores não criam automaticamente as duas variações.

### Consumo de receitas

Cada item de receitas possui receitaId, modoConsumo, quantidadeReceita e quantidadeProdutos. O backend atribui ID da linha e ordem conforme posição no array. Não enviar custo nem nome da receita como autoridade.

| Modo | Dados | Interpretação |
| --- | --- | --- |
| UNIDADES | rendimentoId obrigatório | quantidadeReceita unidades do rendimento escolhido atendem quantidadeProdutos unidades vendidas |
| FRACAO | rendimentoId omitido ou null | quantidadeReceita lotes/frações da receita atendem quantidadeProdutos unidades vendidas |

QuantidadeReceita e quantidadeProdutos devem ser positivas, em strings decimais com até 6 casas e limites DECIMAL(18,6). Não dividir e salvar um único fator aproximado: guardar as duas entradas originais.

Em UNIDADES, o rendimento precisa pertencer à receita e estar ativo. Não escolher por igualdade de nome. Rendimento de outra receita: 400 RENDIMENTO_INVALIDO; arquivado: 409 RENDIMENTO_ARQUIVADO. Em FRACAO, rendimento não participa; ID não null é 400 RENDIMENTO_NAO_APLICAVEL.

Receita deve existir e estar ativa para o novo uso: inexistente 400 RECEITA_INVALIDA; arquivada 409 RECEITA_ARQUIVADA. Receita incompleta pode ser usada, mantendo custo pendente. Uma receita sem rendimento serve para FRACAO, mas não para UNIDADES sem opção válida.

Permitir mais de uma linha da mesma receita, preservando consumos distintos, sem deduplicação automática.

### Consumo direto de insumos

Cada item de insumos possui insumoId, quantidade e unidadeCodigo. Quantidade é por uma unidade vendida, deve ser positiva e usar escala/limites de quantidade já definidos.

Exigir insumo existente e ativo, unidade disponível e dimensão compatível com a compra. Aplicar códigos INSUMO_INVALIDO, INSUMO_ARQUIVADO, UNIDADE_INVALIDA ou UNIDADE_INCOMPATIVEL conforme convenções anteriores. Custo ausente não impede cadastro; não é zero.

Permitir ingredientes, embalagem e decoração direta; não restringir a enum fixa de tipo nem incluir embalagens implicitamente por tamanho. Se embalagem já estiver no custo de uma receita, sua adição direta será outro consumo explícito; interface deve deixar os componentes visíveis para evitar duplicação acidental.

### Cadastro progressivo e preço praticado

- receitas e insumos são arrays opcionais, padrão vazio; null/item null é inválido. Sem ambas as composições, permitir salvar combinação para completar depois, com custo indisponível e pendência COMPOSICAO_VAZIA.
- Cadastro da combinação exige tamanho e sabor, mesmo quando composição está vazia; não gerar opções Único/Tradicional automaticamente.
- precoPraticado é opcional: omissão/null significa ainda não definido. Se informado, valor estritamente positivo, string monetária com até 2 casas em DECIMAL(18,2).
- A confirmação de preço zero aprovada vale para preço de compra de insumo. Não estender implicitamente à venda: precoPraticado zero/negativo retorna 400 PRECO_PRATICADO_INVALIDO.
- Preço praticado informado recebe data UTC; ausente recebe data null. É permitido guardar preço praticado mesmo com custo incompleto, sem inventar margem efetiva.
- Configurações próprias de lucro e indiretos iniciam null para herança; configurar/consultar precificação segue o contrato próprio aprovado. Não exigir configuração global para criar a variação.

### Custo direto por unidade

Calcular com valores atuais e leitura consistente:

```text
UNIDADES: custo = custo lote × quantidadeReceita / (rendimento × quantidadeProdutos)
FRACAO: custo = custo lote × quantidadeReceita / quantidadeProdutos
INSUMO: custo = custo por unidade base × quantidade convertida
custo direto unitário da variação = soma dos consumos acima
```

O custo direto aqui não aplica indiretos nem lucro. situacaoCusto indica disponibilidade do custo direto, não completude da precificação inteira. Se faltar componente, total direto é null com pendências, sem somar apenas partes conhecidas como total.

Exemplo hipotético: massa R$ 60/rendimento pequeno 30; recheio R$ 32 × 0,5/8; embalagem R$ 1/un. Custo direto por produto = R$ 2 + R$ 2 + R$ 1 = R$ 5. Preço praticado R$ 15 não permite chamar a diferença de lucro líquido; indiretos e regra de preço sugerido serão tratados na precificação.

### Salvamento, versões e resposta

Salvar variação e todos os seus componentes numa única transação. Validar opções, estados, dimensões e versão do produto no mesmo fluxo de gravação; restrição única no banco protege a combinação concorrente. Nenhuma falha pode deixar variação/composição parcial.

Regra de versão aprovada: criar variação incrementa versao do produto uma vez, pois muda seu conjunto de combinações, e atualizadoEm. A variação nova inicia versao "1", com criadoEm/atualizadoEm UTC e arquivadoEm null. Alterar composição de uma variação existente futuramente usará sua própria versão; não definir esse contrato de edição por antecipação.

Retornar 201 Created e Location `/api/v1/produtos/{produtoId}/variacoes/{id}`. Corpo com:

| Campo | Conteúdo |
| --- | --- |
| id, produtoId, tamanhoId, saborId | IDs como strings; opções pertencentes ao produto |
| receitas | Linhas salvas com id, referência, modo, rendimento, quantidades e ordem |
| insumos | Linhas salvas com id, referência, quantidade, unidadeCodigo e ordem |
| precoPraticado, precoPraticadoAtualizadoEm | Valor manual e data ou null |
| custoDiretoUnitario | String decimal de precisão de cálculo ou null |
| situacaoCusto, pendencias | Disponibilidade do custo direto e causas identificadas |
| criadoEm, atualizadoEm, arquivadoEm, versao | Estado da variação |
| versaoProduto | Nova versão do agrupador para o cliente continuar cadastrando combinações |

Sem preço sugerido/margem efetiva nesta resposta inicial; consulta desses indicadores definida no contrato aprovado de precificação. Campos desconhecidos e IDs de componentes enviados para criação retornam 400 CAMPO_NAO_PERMITIDO. Formato/erros seguem as convenções aprovadas.

### Verificação futura

| Cenário | Resultado esperado |
| --- | --- |
| Pequeno/Ninho com massa, recheio e embalagem | Uma combinação criada com composição e IDs próprios |
| Tamanho/sabor de outro produto | 400; nenhuma gravação parcial |
| Combinação já existente, ativa ou arquivada | 409 VARIACAO_JA_EXISTE |
| Rendimento da receita errada | 400 RENDIMENTO_INVALIDO |
| Meia receita para 8 produtos | Preservar 0,5 e 8; custo unitário usa divisão no cálculo |
| Receita/insumo sem custo completo | Cadastro permitido; custo direto indisponível |
| Composição vazia | Cadastro permitido para completar, custo indisponível |
| Preço praticado zero/negativo | Rejeitar, sem reutilizar confirmação de insumo |
| Versão antiga do produto | 409; não criar combinação com catálogo antigo |
| Criação efetiva em produto versão 1 | Variação versão 1 e produto versão 2 |

## Consultar variação individual — aprovada

`GET /api/v1/produtos/{produtoId}/variacoes/{variacaoId}`.

Retornar 200 com a representação da criação: IDs da combinação, linhas de receitas/insumos com IDs estáveis e consumos originais, preço praticado/data, datas de cadastro, arquivadoEm, versao da variação, versaoProduto atual e custo direto com situacaoCusto/pendencias. Incluir identificação e nomes de tamanho/sabor com seus estados de arquivamento, além dos nomes e estados das referências usadas nos componentes, sem inferir vínculos por nome.

Consulta usa leitura consistente e dados atuais; não altera versão, datas ou preço manual. Composição vazia ou custo pendente retorna 200 com custo null e causas. Referências arquivadas já vinculadas continuam visíveis e participam do cálculo. Variação/produto arquivados são consultáveis por ID.

Rota com ID inválido: 400 ID_INVALIDO. Produto/variação inexistente ou variação pertencente a outro produto: 404 RECURSO_NAO_ENCONTRADO. Não apresentar os dados de uma variação sob a rota de outro produto. Percentuais efetivos, preço sugerido e margem efetiva continuam na consulta própria de precificação.

## Editar composição e preço da variação — aprovada

`PATCH /api/v1/produtos/{produtoId}/variacoes/{variacaoId}`, corpo JSON.

Consulta e edição da variação aprovadas em 2026-10-31. Editar somente a composição e o preço praticado. Produto, tamanho e sabor da combinação permanecem os mesmos; trocar a combinação exige outro cadastro. Edição de nome/opções do agrupador, listagem e arquivamento/reativação de produtos/variações estão aprovados em seções próprias. Personalizações de lucro/indiretos usam o PATCH de precificação já aprovado.

### Campos e presença

| Campo | Regra aprovada |
| --- | --- |
| versao | Obrigatória; string de long positivo da versão atual da variação |
| receitas | Omitido preserva; array presente é a lista final; vazio remove todas as linhas de receitas |
| insumos | Omitido preserva; array presente é a lista final; vazio remove todos os consumos diretos |
| precoPraticado | Omitido preserva; null limpa; string positiva até 2 casas em DECIMAL(18,2) define o preço |

Exigir ao menos um campo editável além de versao. Rejeitar listas/item null, propriedades desconhecidas, produtoId/tamanhoId/saborId no corpo, datas, custos, versões geradas e campos de precificação. Não confundir campo omitido com null/array vazio na desserialização.

### Identidade das linhas e referências

- Cada item enviado representa uma linha completa, com os mesmos campos de consumo da criação. Item com id preserva a identidade de uma linha desta variação e desta lista. ID desconhecido, de outra lista/variação ou duplicado é rejeitado; id=null é inválido.
- Item sem id cria linha com ID gerado. Linha antiga omitida de uma lista presente é removida da composição. Isso não exclui insumo, receita, rendimento ou variação e não implementa FUT-01/FUT-02.
- Em receitas, a referência completa de uma linha existente — receitaId, modoConsumo e rendimentoId — é imutável. Para substituir receita ou modo/rendimento, retirar a linha e adicionar outra sem id. Em FRACAO, rendimentoId omitido/null são equivalentes. Quantidades podem ser ajustadas preservando a linha.
- Em insumos, insumoId de linha existente é imutável; quantidade e unidade compatível podem mudar. Trocar o insumo exige nova linha.
- Permitir múltiplas linhas da mesma referência, com IDs distintos. Posição no array determina ordem final, iniciada em 0. Não deduplicar nem persistir a divisão das quantidades originais.
- Nova linha exige receita/insumo/rendimento ativos e unidade disponível, com as validações da criação. Referência arquivada/inativa já vinculada pode permanecer e receber ajuste de quantidade. Nova unidade escolhida precisa estar disponível; revalidar sempre compatibilidade dimensional. A exceção de vínculo preservado não permite criar novo vínculo arquivado.

Opções comerciais de tamanho/sabor arquivadas, mas já vinculadas, permanecem na combinação; este PATCH não as reativa nem troca seus IDs. Produto ou variação arquivados bloqueiam a escrita, como no contrato de precificação.

### Preço praticado e cadastro incompleto

Preço zero/negativo é inválido; não usar confirmação de preço zero de insumo na venda. Mudar efetivamente o preço para outro valor positivo registra nova data UTC. Limpar o preço por null também limpa a data. Mesmo valor numérico, como "15" e "15.00", preserva a data. Alterar apenas composição preserva preço e data.

É permitido esvaziar a composição ou usar receita/insumo com custo pendente. Retornar custo direto null com COMPOSICAO_VAZIA ou causas propagadas, sem total parcial artificial. Preço manual pode permanecer ou ser definido mesmo sem custo completo. Alterações de custo não substituem o preço manual nem a regra de lucro aprovada.

### Exemplo de edição

IDs/valores fictícios: variação versão 1, consumo de receita id 701 e insumo id 801. O comando mantém a linha da massa, substitui a lista de receitas por essa única linha, preserva a lista de insumos por omissão e define novo preço:

```json
{
  "versao": "1",
  "receitas": [
    {
      "id": "701",
      "receitaId": "101",
      "modoConsumo": "UNIDADES",
      "rendimentoId": "501",
      "quantidadeReceita": "2",
      "quantidadeProdutos": "1"
    }
  ],
  "precoPraticado": "16.00"
}
```

### Salvamento, versões e resposta

Validar o estado final inteiro e salvar composição/preço numa única transação, comparando atomicamente a versão esperada. Falha rejeita todo o comando. Coordenar novas associações com arquivamento e alterações de referência, conforme os contratos anteriores.

Alteração efetiva incrementa versao da variação e atualizadoEm uma vez. Produto e suas opções conservam suas versões/datas: ajustar composição/preço não muda o conjunto de combinações. Não exigir versaoProduto neste PATCH. O produto precisa continuar ativo na gravação.

Com versão atual e estado final igual, retornar 200 sem mudar versão/datas. Comparar quantidades/preço numericamente; IDs, referências e ordem também participam da comparação. Substituir linha por outra sem id conta como mudança, mesmo com consumos iguais. Versão antiga sempre retorna 409 CONFLITO_DE_EDICAO.

Responder 200 com a mesma representação da consulta individual, incluindo novas linhas e versão atual. Custo é calculado com dados consistentes; a versão da variação não congela preços de insumos. Após resposta incerta, consultar antes de repetir; não reenviar automaticamente com versão mais nova.

### Erros da edição

| Situação | HTTP / código |
| --- | --- |
| ID de rota inválido / recurso inexistente ou de outro produto | 400 ID_INVALIDO / 404 RECURSO_NAO_ENCONTRADO |
| Produto arquivado / variação arquivada | 409 PRODUTO_ARQUIVADO / VARIACAO_ARQUIVADA |
| Versão ausente/inválida/antiga | 400 VERSAO_OBRIGATORIA ou VERSAO_INVALIDA / 409 CONFLITO_DE_EDICAO |
| Nenhum campo editável | 400 ALTERACAO_VAZIA |
| Lista/item null | 400 LISTA_INVALIDA ou ITEM_INVALIDO |
| ID de linha inválido, desconhecido ou de outra lista/variação | 400 COMPONENTE_INVALIDO |
| ID de linha duplicado na lista | 400 COMPONENTE_DUPLICADO |
| Referência imutável de linha existente alterada | 400 COMPONENTE_REFERENCIA_IMUTAVEL |
| Preço negativo, zero ou fora de formato/escala/limite | 400 PRECO_PRATICADO_INVALIDO |
| Campo não permitido | 400 CAMPO_NAO_PERMITIDO |

Validação de referências, quantidades, modos e dimensões segue os códigos da criação. Problem Details conforme aprovado; 400 de domínio usa VALIDACAO_FALHOU no topo e campo como receitas[0].id. JSON/tipo incompatível usa REQUISICAO_INVALIDA; Content-Type incorreto usa 415; falhas técnicas seguem 500/503. Nenhuma falha de campo deve deixar edição parcial.

### Verificação futura

Critérios VRE-01 a VRE-10 de spec.md aprovados em 2026-10-31, pendentes de implementação. Conferir também: referência de linha imutável, referência arquivada preservada versus novo vínculo rejeitado, preço zero rejeitado, listas null rejeitadas, mesmo preço sem mudança de data e versão atual com edição sem efeito.

## Consultar produto e opções — aprovada

`GET /api/v1/produtos/{produtoId}` retorna 200 com id, nome, descricao, tamanhos, sabores, variacoes, criadoEm, atualizadoEm, arquivadoEm e versao do agrupador. A leitura é consistente e não grava nem altera datas/versões.

Tamanhos/sabores incluem todas as opções, ativas e arquivadas, com id, nome, ordem e arquivadoEm. Ordenar cada lista por ordem e id crescentes; não unir opções por nome nem procurar rendimentos automaticamente. A interface usa somente opções ativas ao montar novas combinações.

variacoes contém somente combinações efetivamente cadastradas, inclusive arquivadas, em resumos com id, tamanhoId, saborId, precoPraticado, arquivadoEm e versao próprios, ordenados por id. Não incluir composições completas nem calcular um preço único do produto. Consulta individual e precificação da variação usam as rotas já aprovadas. Sem combinações, retornar lista vazia, sem criar variação implícita.

Produto arquivado é consultável por ID; custo/configuração incompletos não impedem a leitura. ID inválido retorna 400 ID_INVALIDO; ID válido inexistente retorna 404 RECURSO_NAO_ENCONTRADO. Listagem/busca de produtos está aprovada adiante.

## Editar produto e opções — aprovada

`PATCH /api/v1/produtos/{produtoId}`, corpo JSON. Consulta e edição do agrupador/opções aprovadas em 2026-10-31. Editar nome, descrição e opções locais, preservando as identidades usadas pelas variações. Este comando não altera composições, preços manuais ou personalizações de precificação.

### Presença dos campos

| Campo | Regra aprovada |
| --- | --- |
| versao | Obrigatória, string da versão atual do produto |
| nome | Omitido preserva; presente exige nome válido conforme criação, até 150 caracteres |
| descricao | Omitido preserva; null ou vazio após trim limpa; máximo 10.000 caracteres |
| tamanhos | Omitido preserva; array presente define opções e arquivamentos conforme regras abaixo |
| sabores | Omitido preserva; array presente define opções e arquivamentos conforme regras abaixo |

Exigir pelo menos um campo editável além de versao. Listas/item null são inválidos. Rejeitar id do produto, variacoes, custos, datas geradas e configurações de precificação como campos não permitidos neste PATCH.

### Identidade, nomes e arquivamento das opções

- Item com id identifica opção existente deste produto e da dimensão correta. Enviar nome e arquivado booleano explícito. id desconhecido, de outro produto ou de outra dimensão é inválido; id=null é inválido. IDs repetidos na mesma lista são rejeitados.
- Item sem id cria opção com nome obrigatório e ID gerado. arquivado ausente equivale a false; não permitir criar opção já arquivada. Nome de opção mantém os limites da criação, inclusive trim e máximo de 100 caracteres.
- Opção existente omitida de uma lista enviada é arquivada, preservando ID/nome e vínculos. Lista vazia arquiva todas as opções daquela dimensão. Lista omitida não arquiva nada.
- Item existente com arquivado=true arquiva; false mantém ativo ou reativa pelo mesmo ID. O servidor define arquivadoEm numa transição efetiva; preservar a data se já estava arquivado e limpar ao reativar.
- Nome é único em cada dimensão do produto, inclusive entre arquivados, segundo a comparação de nomes já prevista. Renomear/reativar a opção existente em vez de criar duplicata. Nome igual em tamanho e sabor é permitido; produtos distintos continuam independentes.
- A posição de cada item enviado define ordem, iniciada em 0. Opções omitidas e arquivadas preservam ordem anterior; eventuais empates são resolvidos por id na consulta. Validar nomes no estado final do comando, permitindo renomeações sem conflito final.
- Renomear preserva o significado e o ID da opção. Se for uma alternativa comercial diferente, criar outra opção. Nome comercial continua independente do rendimento de receita; renomear tamanho não troca rendimento nem recalcula quantidades da composição.

Arquivar opção impede novas associações na criação de variações, conforme contrato aprovado. Não arquivar, apagar ou recriar variações existentes em cascata. Essas variações continuam consultáveis e calculáveis com seus vínculos; a edição da composição segue seu contrato aprovado. Reativar opção não reativa automaticamente variações arquivadas.

Criar uma opção não gera combinações. Somente um POST explícito de variação cria uma combinação. Remover opção da seleção não é exclusão física nem amplia FUT-01/FUT-02 para produtos.

### Exemplo de edição

Produto versão 3 com tamanhos 301 Pequeno e 302 Normal. O comando renomeia a primeira opção, arquiva a segunda por omissão e preserva sabores e variações:

```json
{
  "versao": "3",
  "nome": "Brownie recheado artesanal",
  "tamanhos": [
    {
      "id": "301",
      "nome": "Pequeno individual",
      "arquivado": false
    }
  ]
}
```

### Transação, versões e resposta

Validar estado final e salvar cabeçalho/opções numa única transação. Revalidar versão e estado ativo do produto na gravação; produto arquivado retorna 409 PRODUTO_ARQUIVADO. Uma falha rejeita todo o PATCH, sem renomeações ou arquivamentos parciais.

Alteração efetiva incrementa versao e atualizadoEm do produto uma vez. Opções usam a versão do agrupador; variações preservam seus IDs, versões, datas, composições e preços. A resposta e consultas posteriores mostram os nomes/estados atuais das opções compartilhadas.

Com versão atual e estado final idêntico, responder 200 sem mudar datas/versão. Renomear após normalização, reordenar, adicionar ou mudar estado de opção conta como alteração quando o estado final muda. Versão antiga retorna 409 CONFLITO_DE_EDICAO mesmo em comando sem efeito.

Coordenar este PATCH com criação de variação, que também usa/incrementa a versão do produto. Não permitir que uma nova combinação valide uma opção ativa e seja gravada após seu arquivamento com estado antigo. Após perda de resposta, consultar antes de repetir; não substituir automaticamente a versão do cliente pela mais recente.

Responder HTTP 200 com a representação da consulta do produto. Não exigir configuração de lucro, preço ou composição para editar cabeçalho/opções; produto continua podendo ser completado depois.

### Erros da edição do produto

| Situação | HTTP / código |
| --- | --- |
| ID inválido / produto inexistente | 400 ID_INVALIDO / 404 RECURSO_NAO_ENCONTRADO |
| Produto arquivado | 409 PRODUTO_ARQUIVADO |
| Versão ausente/inválida/antiga | 400 VERSAO_OBRIGATORIA ou VERSAO_INVALIDA / 409 CONFLITO_DE_EDICAO |
| Nenhum campo editável | 400 ALTERACAO_VAZIA |
| Nome vazio/acima do limite; descrição acima do limite | Códigos da criação, nos respectivos campos |
| ID de opção inválido, de outro produto ou de outra dimensão | 400 TAMANHO_INVALIDO ou SABOR_INVALIDO |
| ID repetido na mesma lista | 400 OPCAO_DUPLICADA |
| Nome duplicado no estado final, inclusive arquivados | 400 TAMANHO_DUPLICADO ou SABOR_DUPLICADO |
| arquivado omitido em opção existente ou true em opção nova | 400 OPCAO_ESTADO_INVALIDO |
| Lista/item null | 400 LISTA_INVALIDA ou ITEM_INVALIDO |
| Campo desconhecido/somente leitura | 400 CAMPO_NAO_PERMITIDO |

Problem Details aprovado, com VALIDACAO_FALHOU no topo dos 400 de domínio e campos como tamanhos[0].id. JSON/tipo incompatível, inclusive arquivado null ou não booleano, usa REQUISICAO_INVALIDA. Content-Type e falhas técnicas seguem 415/500/503 conforme convenções. Nenhuma operação de exclusão física é criada nesta etapa.

### Verificação futura

Critérios PED-01 a PED-12 de spec.md aprovados em 2026-10-31, pendentes de implementação. Conferir consulta de opções arquivadas, listas omitidas versus vazias, identidade em renomeação/reativação, duplicatas inclusive arquivadas, ID de outro produto/dimensão, versão antiga, falha sem gravação parcial e nenhuma variação gerada ou arquivada automaticamente.

## Listar produtos e variações — aprovada

Listagem aprovada em 2026-10-31, sem implementação.

| Rota | Conteúdo |
| --- | --- |
| GET /api/v1/produtos | Resumos dos agrupadores |
| GET /api/v1/produtos/{produtoId}/variacoes | Resumos das combinações cadastradas naquele produto |

### Filtros e paginação

Ambas aceitam status ATIVOS (padrão), ARQUIVADOS ou TODOS, pagina de 1 a 2147483647 (padrão 1) e tamanhoPagina de 1 a 100 (padrão 20). status considera o arquivadoEm do recurso listado. Listar variações de produto arquivado é permitido; nesse caso, ATIVOS significa variações não arquivadas individualmente, sem tornar o produto ativo.

Produtos aceitam busca por trecho do nome, com trim, até 150 caracteres; vazio equivale a sem filtro. Busca sem distinguir maiúsculas/minúsculas ou acentos e com curingas tratados como texto literal, conforme insumos/receitas. Ordenar por nome e id crescentes.

Variações aceitam tamanhoId e saborId opcionais, IDs positivos de opções daquele produto, inclusive opções arquivadas para localizar vínculos antigos. Filtros de opções inexistentes/de outro produto retornam 400 TAMANHO_INVALIDO ou SABOR_INVALIDO. Não aceitam busca textual: os seletores usam as opções consultadas do produto.

Variações também aceitam situacaoCusto DISPONIVEL/INDISPONIVEL e composicaoVazia true/false, sem padrão de filtro. composicaoVazia considera ausência simultânea de receitas e insumos diretos; false não garante custo completo. situacaoCusto refere-se somente ao custo direto, sem depender de lucro, indiretos ou preço praticado. Custo conhecido zero é DISPONIVEL. Ordenar por ordem/id do tamanho, ordem/id do sabor e id da variação, todos crescentes.

Filtros se combinam com AND. Aplicar filtros, inclusive de custo, antes de contar e paginar. Itens, cálculos e contagens usam o mesmo snapshot por requisição; não prometer snapshot entre páginas. Arquivamento de referência já vinculada não a exclui do cálculo.

### Respostas e falhas

Envelope de ambas: itens, pagina e tamanhoPagina como inteiros pequenos, totalItens como string de inteiro não negativo e temProximaPagina booleano. totalItens conta todos os resultados filtrados antes da paginação. Calcular temProximaPagina por pagina × tamanhoPagina < totalItens, com inteiro amplo, sem overflow de int.

| Lista | Campos de cada item |
| --- | --- |
| Produtos | id, nome, arquivadoEm, versao, variacoesAtivas e variacoesArquivadas; contagens como strings não negativas |
| Variações | id, produtoId, tamanhoId, saborId, nomes/ordens/arquivadoEm das opções, arquivadoEm e versao da variação, precoPraticado, custoDiretoUnitario, situacaoCusto e composicaoVazia |

As contagens de variações no resumo do produto consideram seus estados individuais; não são custo, preço nem garantia de cadastro completo. Sem variações, ambas as contagens são "0". Não inventar preço único para o agrupador.

Lista de variações inclui produtoArquivadoEm no envelope, para distinguir estado do produto e do filho. Pendências detalhadas, composição e precificação completa permanecem nas consultas individuais aprovadas.

Sem resultados ou página além do fim: 200 com itens vazio, total real e página pedida. Produto válido inexistente na rota aninhada retorna 404 RECURSO_NAO_ENCONTRADO; ID de rota inválido retorna 400 ID_INVALIDO. Produto existente sem variações retorna 200 vazio.

Query desconhecida/repetida, status/custo/booleano inválido e paginação inválida seguem convenções de insumos/receitas: 400 com PARAMETRO_INVALIDO, STATUS_INVALIDO, SITUACAO_CUSTO_INVALIDA, COMPOSICAO_FILTRO_INVALIDO, PAGINA_INVALIDA ou TAMANHO_PAGINA_INVALIDO no campo correspondente. Busca acima do limite usa BUSCA_INVALIDA. Nenhuma listagem altera datas, versões ou cadastros.

## Arquivar e reativar produtos/variações — aprovado

Arquivamento/reativação aprovados em 2026-10-31, sem implementação.

| Método / rota | Transição |
| --- | --- |
| POST /api/v1/produtos/{produtoId}/arquivar | Arquivar o agrupador |
| POST /api/v1/produtos/{produtoId}/reativar | Reativar o mesmo agrupador |
| POST /api/v1/produtos/{produtoId}/variacoes/{variacaoId}/arquivar | Arquivar somente a combinação |
| POST /api/v1/produtos/{produtoId}/variacoes/{variacaoId}/reativar | Reativar a mesma combinação |

Corpo somente com versao obrigatória: a versão do produto nas rotas do produto e a versão da variação nas rotas da variação. Exemplo ilustrativo:

```json
{
  "versao": "4"
}
```

### Produto

Arquivar registra arquivadoEm UTC, atualiza atualizadoEm e incrementa versão do produto uma vez. Preservar todas as opções, variações, vínculos, configurações e preços, inclusive seus estados, datas e versões individuais. Não arquivar descendentes em cascata.

Produto arquivado sai da listagem padrão e das seleções usuais de produtos ativos; continua acessível por consulta direta ou filtro de arquivados. Bloquear criação de variação e escrita no produto/variações, inclusive seus PATCH de precificação, conforme contratos aprovados. Consultas e cálculos continuam usando os custos/configurações atuais; arquivamento não congela custos nem transforma custo conhecido em indisponível.

Reativar limpa arquivadoEm e atualiza data/versão do produto. Preservar o mesmo ID e todos os estados individuais: opção ou variação anteriormente arquivada continua arquivada. Produto incompleto ou sem precificação pode ser reativado para preenchimento posterior.

### Variação

Arquivar registra arquivadoEm UTC, atualiza atualizadoEm e incrementa somente a versão da variação. Preservar composição, preço praticado e data do preço, configurações e referências. A combinação continua reservada pela unicidade, inclusive arquivada: reativar/editar o cadastro existente em vez de criar outra variação igual.

Reativar preserva ID, referências, composição e preço; limpa arquivadoEm e atualiza data/versão próprias. Não exigir custo completo, composição preenchida ou configuração global. Referências/opções arquivadas já vinculadas não impedem a transição, pois os vínculos são preservados; permanecem sinalizadas e indisponíveis para novas associações. Não reativar opções, receitas, insumos ou qualquer outra entidade em cascata.

Ambas as transições de variação exigem produto ativo. Se estiver arquivado, retornar 409 PRODUTO_ARQUIVADO mesmo quando a variação já está no estado solicitado; reativar o produto antes de gerenciar a variação. Isso não impede consultar suas variações durante o arquivamento do agrupador.

Transição de variação não altera versão/data do produto nem de opções; o conjunto de combinações cadastradas é preservado. A versão do agrupador não representa todas as mudanças das variações embutidas em sua consulta.

### Versões, resposta e integridade

Após validar existência, pertencimento, guardas e versão, estado já coincidente com versão atual retorna 200 sem mudar datas/versão. Versão antiga sempre retorna 409 CONFLITO_DE_EDICAO, mesmo quando outra operação já alcançou o estado solicitado. Nenhum comando ajusta automaticamente a versão enviada pelo cliente.

Transição e comparação de versão são atômicas. Coordenar arquivamento do produto com criação/edição/ciclo de vida de variações para que nenhuma escrita seja confirmada com estado antigo do pai. Arquivamento de variação e seus PATCH compartilham sua versão. Falha rejeita toda a transição.

Responder 200 com a representação aprovada da consulta individual do recurso alterado. Após resultado incerto, consultar antes de repetir. Somente uma transição efetiva altera data/versão; não prometer sucesso em retry com versão antiga.

ID de rota inválido retorna 400 ID_INVALIDO; recurso inexistente ou variação de outro produto, 404 RECURSO_NAO_ENCONTRADO. Versão ausente/inválida usa 400 VERSAO_OBRIGATORIA/VERSAO_INVALIDA; campo extra usa 400 CAMPO_NAO_PERMITIDO. Problem Details e 415/500/503 seguem convenções aprovadas.

Não criar endpoint de exclusão física de produto/variação. FUT-01/FUT-02 continuam exclusivos de insumos/receitas e fora da primeira versão; este planejamento não solicita uma evolução de exclusão para produtos.

### Verificação futura

PLV-01 a PLV-14 de spec.md aprovados em 2026-10-31, pendentes de implementação. Conferir filtros antes de paginar/contar, estado próprio versus estado do pai, listas vazias, custo zero, transições sem cascata, referências preservadas, composição incompleta, versão antiga, comando sem efeito e corrida entre arquivamento do produto e escrita da variação.

## Próximo passo

Contratos principais, premissas de domínio e modelo de dados aprovados. Próximo passo: fechar versões/persistência e preparar o primeiro incremento do backend de insumos; produtos/variações serão implementados em entrega própria. Formatos/contratos de importação/backup continuam pendentes. FUT-01/FUT-02 continuam evoluções de insumos/receitas, sem extensão automática para produtos.
