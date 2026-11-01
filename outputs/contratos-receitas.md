# Contratos da API — receitas

Versão: 0.9  
Atualizado: 2026-10-31  
Estado: contratos iniciais de receitas aprovados; sem implementação.

Referências: [spec.md](spec.md), [modelo de dados](modelo-de-dados.md) e [convenções da API](contratos-api.md).

IDs/versões e decimais são strings; códigos de unidade vêm do catálogo; falhas seguem o Problem Details aprovado. Cadastro com composição/rendimentos, opção de receita incompleta, consulta/custos e simulação aprovados pelo usuário em 2026-10-19; edição, listagem e ciclo de vida aprovados em 2026-10-20. Exclusão definitiva solicitada para versão futura, conforme FUT-02 na especificação.

## Primeira operação: cadastrar receita base

`POST /api/v1/receitas`, `Content-Type: application/json`.

Uma receita representa um lote base de massa, recheio ou outro preparo. O mesmo cadastro pode ser consumido por outros preparos ou por produtos. Opções de rendimento descrevem maneiras alternativas de dividir esse lote, como 30 pequenos ou 20 normais; não somar essas quantidades.

Salvar cabeçalho, ingredientes, preparos e rendimentos em uma transação. Não exigir várias chamadas independentes para criar uma receita completa. O cadastro não produz estoque nem registra compra/venda.

## Entrada de cadastro aprovada

Exemplo reduzido de contrato, não receita culinária completa. IDs, ingredientes, quantidades e rendimentos são fictícios e não definem os cadastros reais do negócio.

```json
{
  "nome": "Massa de brownie — exemplo",
  "observacoes": "Exemplo reduzido para demonstrar o contrato.",
  "ingredientes": [
    {
      "insumoId": "42",
      "quantidade": "100",
      "unidadeCodigo": "g"
    },
    {
      "insumoId": "43",
      "quantidade": "50",
      "unidadeCodigo": "g"
    }
  ],
  "preparos": [],
  "rendimentos": [
    {
      "nome": "Pequeno",
      "descricaoPorcao": "Porção pequena de referência",
      "quantidade": "30"
    },
    {
      "nome": "Normal",
      "descricaoPorcao": "Porção normal de referência",
      "quantidade": "20"
    }
  ]
}
```

### Campos

| Campo | Regra |
| --- | --- |
| nome | Obrigatório, trim, 1 a 150 caracteres; não exigir nome globalmente único |
| observacoes | Opcional; string de até 10.000 caracteres ou null; vazio após trim normaliza para null |
| ingredientes | Array opcional, padrão vazio; cada item tem insumoId, quantidade positiva e unidadeCodigo |
| preparos | Array opcional, padrão vazio; cada item tem preparoId e fracaoLote positiva |
| rendimentos | Array opcional, padrão vazio; cada item tem nome, descricaoPorcao opcional e quantidade positiva |

Array null ou item null é inválido; omissão de array na criação significa array vazio. Rejeitar campos desconhecidos e campos somente de leitura como id, custoDiretoLote, versao e datas. Essa escolha é específica da criação de receitas; política de omissão da edição será definida no contrato de edição.

Quantidades e fracaoLote usam DECIMAL(18,6), com limites e sintaxe definidos nas convenções de transporte. Unidade UNIDADE permite quantidades fracionárias quando informadas conscientemente; não arredondar ingrediente automaticamente para inteiro.

O backend atribui ordem baseada na posição de cada item no array, começando em 0. Não exigir ordem nem IDs de componentes no cadastro; cada linha recebe ID estável para futuras edições. Não ordenar automaticamente os ingredientes por nome.

## Ingredientes

- insumoId obrigatório e existente; inexistente retorna 400 com INSUMO_INVALIDO em ingredientes[n].insumoId.
- Insumo arquivado não pode ser vinculado em nova receita: 409 INSUMO_ARQUIVADO, conforme contrato aprovado de arquivamento.
- Quantidade maior que zero; unidadeCodigo deve existir, estar disponível e ter dimensão compatível com a compra do insumo. Referência incompatível é 400 UNIDADE_INCOMPATIVEL no campo do item.
- O mesmo insumo pode aparecer em várias linhas, pois pode ser usado em etapas distintas; somar o consumo, sem eliminar linhas duplicadas por nome ou ID.
- Não enviar preço de compra, custo unitário ou nome do insumo como autoridade. Consultar as referências no banco.
- Insumo ativo sem preço não impede salvar receita: calcular custo como indisponível, com pendência de preço.
- Embalagem normalmente será componente do produto. O tipo do insumo não será uma enum fixa que impeça o uso de novos tipos cadastrados; se for necessário restringir componentes por tipo, definir essa regra separadamente.

## Preparos reutilizados

Exemplo alternativo de um item de preparos, usando um recheio já cadastrado:

```json
{
  "preparoId": "80",
  "fracaoLote": "0.5"
}
```

preparoId referencia outra receita. fracaoLote 0,5 significa metade do lote base desse preparo, sem depender de sua opção de rendimento. Custo consumido = custo direto atual do preparo × fração. A fração pode ser maior que 1, representando múltiplos do lote.

Preparos devem existir e estar disponíveis para novo uso; inexistente retorna 400 PREPARO_INVALIDO, arquivado retorna 409 PREPARO_ARQUIVADO. Não criar outra receita implicitamente a partir desse corpo. Preparo sem custo completo pode ser usado, propagando suas pendências.

Validar grafo sem ciclos diretos ou indiretos. Falha de ciclo é 409 DEPENDENCIA_CIRCULAR, sem gravar alterações parciais. Na criação o novo ID é do servidor e os preparos já existem; a validação continua sendo invariante do domínio e deverá cobrir a edição futura.

## Rendimentos por tamanho

- Cada opção descreve quantas unidades o lote inteiro rende nesse corte/tamanho.
- Nome obrigatório de 1 a 100 caracteres após trim, único dentro da receita conforme a collation de nomes adotada. Nome de rendimento duplicado no mesmo comando é 400 RENDIMENTO_DUPLICADO.
- descricaoPorcao opcional, até 200 caracteres, vazio após trim normaliza para null. É descrição livre, sem unidade de peso implícita.
- Quantidade positiva, sem somar rendimentos alternativos nem inferir massa/peso final.
- Sem cadastrar opções comerciais de produtos automaticamente: Pequeno em uma receita não cria variação de produto.
- Receita sem opção de rendimento ainda pode ter custo direto de lote e ser consumida por fração. Consumo por unidade exige uma opção de rendimento cadastrada.
- Mudar quantidade de produção futuramente será simulação por fator, sem alterar esta receita base.

## Cadastro incompleto

Aprovado permitir salvar nome e listas vazias para completar depois. Não criar estado persistido de publicação neste passo; a disponibilidade do custo é derivada dos dados.

- Sem ingredientes e sem preparos: situacaoCusto INDISPONIVEL e pendência COMPOSICAO_VAZIA; não apresentar custo zero artificial.
- Com composição válida e preços conhecidos: custo de lote disponível mesmo se rendimentos estiver vazio.
- Com componente sem preço ou preparo incompleto: custo total de lote e custos unitários indisponíveis. Não mostrar soma de partes conhecidas como total.
- Preço conhecido zero é válido e não causa pendência INSUMO_SEM_PRECO.

## Resposta de sucesso

HTTP `201 Created`, `Location: /api/v1/receitas/{id}`. Retornar receita salva com IDs gerados por linha, quantidades normalizadas, criadoEm/atualizadoEm UTC, arquivadoEm null e versao "1".

Modelo da resposta:

| Campo | Conteúdo |
| --- | --- |
| id, nome, observacoes | Cabeçalho cadastrado |
| ingredientes | Linhas salvas com id, insumoId, quantidade, unidadeCodigo e ordem |
| preparos | Linhas salvas com id, preparoId, fracaoLote e ordem |
| rendimentos | Opções salvas com id, nome, descricaoPorcao, quantidade e arquivadoEm null |
| custoDiretoLote | String decimal de precisão de cálculo ou null |
| situacaoCusto | DISPONIVEL ou INDISPONIVEL |
| pendencias | Lista de causas identificadas; vazia se custo completo |
| criadoEm, atualizadoEm, arquivadoEm, versao | Mesma convenção de datas/versão dos insumos |

Para criação com composição vazia, custoDiretoLote null e pendências incluindo `{"codigo":"COMPOSICAO_VAZIA","receitaId":"101","mensagem":"Adicione ingredientes ou preparos à receita."}`. O ID 101 é ilustrativo.

Para preço ausente, pendência contém codigo INSUMO_SEM_PRECO, insumoId e mensagem. Para preparo vazio, usar COMPOSICAO_VAZIA com receitaId do preparo. Deduplicar a mesma causa por código e ID, preservando a identidade da referência pendente. Renderizar causas como pendências do cadastro, não como HTTP 400.

Retornar custo por opção de rendimento e detalhamento de componentes será definido na consulta de custos. Não duplicar percentuais de lucro/indiretos em cada preparo neste POST: custo direto alimenta a precificação de produto. A política de percentuais específicos por receita segue a pendência existente no spec.

## Salvamento e consistência

Persistir o conjunto inteiro ou nada. Revalidar referências e dimensões na mesma operação que salva os vínculos. Coordenação transacional precisa impedir corrida com arquivamento de insumo e mudança de unidade da compra, conforme contratos aprovados. Preparos e cálculo devem usar leitura consistente; não confundir versão inicial da receita com versão de seus insumos.

Criação inválida não deve deixar uma receita vazia nem parte dos componentes como resíduo. Exceção de perda de resposta após commit exige consulta antes de retry; não prometer deduplicação de POST nem unicidade por nome, pois ainda não há chave de idempotência definida.

## Erros do cadastro

| Situação | HTTP / código |
| --- | --- |
| Nome vazio ou acima do limite | 400 / NOME_OBRIGATORIO ou NOME_MUITO_LONGO |
| Quantidade/fração não positiva ou fora da precisão | 400 / QUANTIDADE_INVALIDA ou FRACAO_INVALIDA |
| Unidade inexistente/inativa | 400 / UNIDADE_INVALIDA |
| Dimensão incompatível | 400 / UNIDADE_INCOMPATIVEL |
| Insumo/preparo inexistente | 400 / INSUMO_INVALIDO ou PREPARO_INVALIDO |
| Insumo/preparo arquivado | 409 / INSUMO_ARQUIVADO ou PREPARO_ARQUIVADO |
| Nome de rendimento repetido | 400 / RENDIMENTO_DUPLICADO |
| Ciclo de dependência | 409 / DEPENDENCIA_CIRCULAR |
| Array/item null, campo desconhecido ou texto acima do limite | 400 / LISTA_INVALIDA, ITEM_INVALIDO, CAMPO_NAO_PERMITIDO ou TEXTO_MUITO_LONGO |

Erros apontam campos JSON como ingredientes[0].quantidade, preparos[0].preparoId e rendimentos[1].nome. Usar VALIDACAO_FALHOU no topo dos 400 de domínio. Formato de JSON/tipo incorreto usa REQUISICAO_INVALIDA. Erros inesperados e indisponibilidade usam 500/503, conforme convenção aprovada.

## Exemplos de custo para validação futura

- Compra de R$ 40 por 1000 g; receita consome 100 g: custo consumido R$ 4.
- Mesmo insumo em duas linhas de 100 g e 50 g: custo total consumido R$ 6.
- Preparo de R$ 32 com fracaoLote 0,5: custo consumido R$ 16.
- Receita hipotética de lote R$ 60 com rendimentos Pequeno 30 e Normal 20: custo unitário R$ 2 ou R$ 3. Não são rendimentos reais aprovados para os brownies do usuário.

## Consulta da receita — aprovada

`GET /api/v1/receitas/{id}` retorna 200 com a representação completa definida no cadastro: cabeçalho, ingredientes/preparos com IDs estáveis, rendimentos cadastrados, datas, versao e custo direto atual do lote. Incluir opções de rendimento arquivadas com sua identificação, sem oferecê-las como novas seleções.

Receita incompleta ou custo pendente não gera erro HTTP: retornar 200 com custo indisponível e causas. Receita arquivada continua consultável por ID para preservar leitura de dependentes; as transições de arquivamento estão aprovadas na seção própria deste documento.

ID válido inexistente retorna 404 RECURSO_NAO_ENCONTRADO. ID fora do padrão de long positivo retorna 400 com ID_INVALIDO, seguindo o contrato de insumos. GET não altera composição, datas ou versão.

Listagem e filtros para selecionar preparos estão aprovados na seção própria deste documento; consulta individual não depende da paginação.

## Consulta de custos — aprovada

`GET /api/v1/receitas/{id}/custos`.

Retornar 200 com:

| Campo | Conteúdo |
| --- | --- |
| receitaId, versao | Identificação e versão atual da receita base |
| calculadoEm | Instante UTC da operação de cálculo |
| custoDiretoLote | Total direto, como string decimal de precisão de cálculo, ou null |
| situacaoCusto, pendencias | Mesmas regras do cadastro; não assumir custo zero quando faltar dado |
| ingredientes | Linhas com id, insumoId, nome do insumo, quantidade, unidadeCodigo, arquivadoEm do insumo e custoConsumido |
| preparos | Linhas com id, preparoId, nome, fracaoLote, arquivadoEm do preparo e custoConsumido |
| rendimentos | Opções com id, nome, quantidade, arquivadoEm e custoDiretoUnitario |

Custo de rendimento = custo direto do lote / quantidade daquele rendimento. Se o lote está indisponível, custoDiretoUnitario é null. Se não há opções, rendimentos é vazio, mas o custo do lote pode estar disponível.

Componentes conhecidos podem mostrar custoConsumido mesmo se o total da receita estiver indisponível; não apresentar sua soma parcial como custo completo. Custo de preparo usa recursivamente seu custo direto e sua fração. Não somar novamente os ingredientes internos do preparo ao total do pai, pois já estão incluídos no custoConsumido do preparo.

Consultar insumos/preparos por operações próprias permite navegar no detalhamento. Lista de compras consolidada com expansão de todos os níveis não faz parte deste primeiro contrato; a simulação apresenta a composição de cada receita sem prometer consolidação global.

Dados arquivados/inativos já vinculados continuam no cálculo. Arquivamento por si só não gera custo indisponível. Pendências de preço e composição continuam propagadas como no cadastro.

Não aplicar lucro ou indiretos ao custo direto desta consulta. Percentuais da precificação de produto continuam separados. Alterar preço de um insumo atualiza o próximo cálculo, preservando a versão da receita se sua composição não mudou.

## Simular quantidade — aprovada

`POST /api/v1/receitas/{id}/simulacoes`, `Content-Type: application/json`.

Operação de cálculo sem persistência, retorno HTTP 200. A chamada não cria registro de produção, compra, venda, nova receita ou histórico de simulação; não incrementa versão nem altera datas do cadastro.

Entrada para meia receita:

```json
{
  "versao": "1",
  "fatorLote": "0.5"
}
```

| Campo | Regra |
| --- | --- |
| versao | Obrigatória; string da versão da receita que a interface carregou |
| fatorLote | Obrigatório, positivo, dentro de DECIMAL(18,6); 0,5 = metade, 1 = lote base, 2 = dobro |

Versão esperada desatualizada retorna 409 CONFLITO_DE_EDICAO, permitindo recarregar a receita antes de simular. Campo ausente/inválido usa VERSAO_OBRIGATORIA, VERSAO_INVALIDA ou FATOR_LOTE_INVALIDO com HTTP 400. Rejeitar campos desconhecidos. Receita inexistente retorna 404. O corpo não recebe preços nem custos para sobrescrever os dados atuais.

Retornar:

| Campo | Conteúdo |
| --- | --- |
| receitaId, versao, fatorLote | Base usada e fator solicitado |
| calculadoEm | Instante UTC da consulta |
| ingredientes | Linhas com id, insumoId, unidadeCodigo, quantidadeBase, quantidadeSimulada e custoConsumido simulado |
| preparos | Linhas com id, preparoId, fracaoLoteBase, fracaoLoteSimulada e custoConsumido simulado |
| rendimentos | Opções com id, nome, quantidadeBase, quantidadeSimulada, arquivadoEm e custoDiretoUnitario |
| custoDiretoLoteBase | Custo do lote cadastrado ou null |
| custoDiretoLoteSimulado | Custo direto da quantidade simulada ou null |
| situacaoCusto, pendencias | Completude do cálculo; causas identificadas |

Fórmulas:

```text
quantidadeSimulada = quantidadeBase × fatorLote
fracaoLoteSimulada = fracaoLoteBase × fatorLote
custoDiretoLoteSimulado = custoDiretoLoteBase × fatorLote
quantidadeSimuladaRendimento = quantidadeBaseRendimento × fatorLote
```

Preparos seguem a mesma proporcionalidade: se o lote base utiliza 0,5 receita de recheio, simular fator 2 exige 1 receita de recheio. A expansão interna do preparo, quando consultada, deve usar esse fator acumulado; nunca alterar o cadastro do recheio.

O custo direto unitário de cada tamanho permanece equivalente ao custo unitário base quando todos os componentes escalam proporcionalmente, ressalvada a precisão interna do decimal. A simulação não assume descontos por volume, perdas, arredondamento de ovos ou necessidade de comprar embalagens inteiras.

Se o cálculo resultar em quantidade fracionária de produto final, mostrar a fração em vez de arredondar silenciosamente e sinalizar “rendimento fracionado”. Aviso e limite da primeira versão sem ajuste automático para unidades inteiras aprovados em 2026-10-31, conforme seção Produção fracionada de [spec.md](spec.md). Preservar quantidades matemáticas e permitir ajuste explícito do fator pelo usuário; sem arredondamento automático de ovos/embalagens. Não muda o fator aceito nem cria operação de produção.

Receita sem preço completo ainda pode ser simulada: quantidades e rendimentos informados são escalados, custos incompletos permanecem null, HTTP 200. Receita com composição vazia retorna as listas vazias/quantidades disponíveis e pendência COMPOSICAO_VAZIA, sem inventar custo zero.

## Precisão e leitura consistente

Não arredondar cada ingrediente ou preparo para centavos antes de somar. Campos simulados derivados podem exigir mais de 6 casas decimais; por não serem persistidos, não truncar para a escala de entrada. Usar BigDecimal e política numérica Java de contratos-api.md. Resultado fora do limite de cálculo retorna 400 RESULTADO_FORA_DO_LIMITE, não 500 nem resultado parcial apresentado como válido.

Preço/custo atualizado de dependências é lido em snapshot consistente por operação. versao identifica a edição da receita base, não a versão de todos os preços e preparos dependentes. Por isso, a mesma versao pode produzir custo diferente depois de alteração de insumo. calculadoEm é informação temporal, não token de versão global.

Não misturar resposta antiga de custos com resposta nova de simulação como se compartilhassem snapshot. A simulação devolve custoBase e custoSimulado calculados juntos. Aplicar a verificação da versão e leitura das dependências no mesmo snapshot; preservar integridade do grafo e evitar consultas repetidas para a mesma receita dentro da operação.

## Exemplos para validação futura

Exemplo hipotético: custo de lote R$ 60; ingrediente base 200 g; preparo utilizado 0,5 lote; rendimentos Pequeno 30 ou Normal 20.

| Fator | Ingrediente | Preparo | Pequenos OU normais | Custo simulado |
| --- | --- | --- | --- | --- |
| 0,5 | 100 g | 0,25 lote | 15 ou 10 | R$ 30 |
| 1 | 200 g | 0,5 lote | 30 ou 20 | R$ 60 |
| 2 | 400 g | 1 lote | 60 ou 40 | R$ 120 |

Custo unitário permanece R$ 2 pequeno e R$ 3 normal. Esses números não fixam os rendimentos reais do negócio e não descrevem composição completa do lote.

Verificações adicionais: fator zero/negativo inválido; versão antiga gera conflito; falta de preço mantém quantidades simuladas, mas custo indisponível; sem rendimento não há custo por unidade; nenhuma simulação altera registros, versão ou datas.

## Edição da receita — aprovada

`PATCH /api/v1/receitas/{id}`, `Content-Type: application/json`.

Documento parcial próprio, seguindo a convenção de edição de insumos: versão esperada obrigatória, operações atômicas, campos omitidos preservados. Não confundir edição da receita base com simular outra quantidade.

### Cabeçalho e presença dos campos

| Campo | Comportamento |
| --- | --- |
| versao | Obrigatória, string de long positivo; comparar com a versão atual da receita |
| nome | Opcional; se presente, obrigatório não vazio e até 150 caracteres |
| observacoes | Opcional; se presente, string até 10.000 caracteres ou null para limpar |
| ingredientes | Ausente preserva todas as linhas; array presente representa a lista final; vazio remove todos os ingredientes |
| preparos | Ausente preserva todas as linhas; array presente representa a lista final; vazio remove todos os preparos |
| rendimentos | Ausente preserva todas as opções; array presente define opções e arquivamento conforme regra abaixo |

null é inválido para nome e listas. Não transformar ausência de lista em array vazio na desserialização. Exigir pelo menos um campo editável além de versao; rejeitar propriedades desconhecidas e dados calculados. Datas, custo, ID da receita e versão nova são gerados pelo servidor.

Modificar ingredientes sem enviar rendimentos preserva os rendimentos cadastrados. O backend não infere o efeito culinário de uma mudança de fórmula: a interface permite revisar composição e rendimento juntos, na mesma edição. Escalar produção continua sendo simulação que não altera cadastro.

### IDs de ingredientes e preparos

Quando uma lista é enviada:

- Item com id identifica uma linha existente desta receita e mantém esse ID ao atualizar quantidade, unidade compatível ou fração. id de outra receita, desconhecido ou duplicado no mesmo array retorna 400 COMPONENTE_INVALIDO ou COMPONENTE_DUPLICADO.
- Item sem id cria linha nova com ID gerado pelo backend. Não enviar id=null como atalho: null é inválido.
- Linhas antigas omitidas da lista final são removidas da composição. Essa remoção interna não é exclusão definitiva do insumo nem da receita do preparo, e não implementa FUT-01.
- Cada item representa a linha completa: campos de referência e quantidade/unidade ou fração são obrigatórios mesmo em linha existente. A posição no array determina ordem final.
- Não trocar insumoId ou preparoId de uma linha existente: rejeitar com COMPONENTE_REFERENCIA_IMUTAVEL. Para substituir componente, remover a linha antiga e criar outra sem id. Isso distingue novo uso de vínculo preservado.
- Permitir mesmo insumo/preparo em mais de uma linha, com IDs distintos; não consolidar automaticamente por referência.
- Referência arquivada/inativa já vinculada pode permanecer e ter quantidade/fração ajustada. Nova linha ou nova unidade escolhida exige disponibilidade. Não usar essa exceção para permitir um novo vínculo arquivado.
- Revalidar dimensões das unidades e grafo de dependências após aplicar virtualmente todas as alterações, antes de salvar. Ciclo direto/indireto retorna 409 DEPENDENCIA_CIRCULAR.

### Identidade e arquivamento dos rendimentos

Produtos apontam para o ID da opção de rendimento. Preservar esse ID evita quebrar relações ao renomear ou corrigir uma opção.

Quando rendimentos é enviado:

- Item com id deve pertencer à receita e incluir nome, quantidade e arquivado booleano explícito; descricaoPorcao é opcional e null/vazio limpa a descrição. Campos numéricos usam as mesmas validações do cadastro.
- Item sem id cria uma opção; arquivado ausente equivale a false, e arquivado=true é rejeitado para uma opção nova. Opção nova não herda ID de uma opção de nome parecido.
- Opção existente omitida do array é arquivada, sem apagar seu ID. Lista vazia arquiva todas as opções atuais; opções já arquivadas e omitidas não são alteradas.
- Item existente com arquivado=true arquiva; false reativa ou mantém ativo. Não recriar a opção em uma reativação. Definir arquivadoEm pelo servidor em transição efetiva; preservar data se já estava arquivado.
- Nome deve ser único em todas as opções da receita, inclusive arquivadas, conforme chave única do modelo. Renomear/reativar uma opção existente em vez de criar outra duplicada.
- Arquivar deixa de oferecer o rendimento em novas composições. Produtos que já o utilizam mantêm o cálculo com quantidade atual dessa opção, sinalizando que ela foi arquivada.
- Alterar nome ou quantidade preserva o ID e afeta os custos dependentes. Se o novo tamanho representa uma alternativa distinta, criar outra opção, em vez de reaproveitar um ID com significado diferente.

Este arquivamento é de opções de rendimento. Arquivar/reativar a receita inteira é outra operação, aprovada adiante. Não criar endpoint de exclusão física de rendimento neste passo.

### Exemplo de edição

Dados fictícios. A versão atual é 3. Esta receita tem uma única linha de ingrediente id 1001 e duas opções de rendimento id 501/502. O comando abaixo mantém os IDs e atualiza composição/rendimentos na mesma transação:

```json
{
  "versao": "3",
  "ingredientes": [
    {
      "id": "1001",
      "insumoId": "42",
      "quantidade": "200",
      "unidadeCodigo": "g"
    }
  ],
  "rendimentos": [
    {
      "id": "501",
      "nome": "Pequeno",
      "descricaoPorcao": "Porção pequena de referência",
      "quantidade": "30",
      "arquivado": false
    },
    {
      "id": "502",
      "nome": "Normal",
      "descricaoPorcao": "Porção normal de referência",
      "quantidade": "20",
      "arquivado": false
    }
  ]
}
```

preparos e observacoes não enviados são preservados. Se houvesse outras linhas de ingredientes no banco, a lista completa enviada deveria incluí-las para mantê-las; omitir linha em uma lista presente significa removê-la, não preservá-la.

### Impacto nos produtos e nos preparos dependentes

- Mudar custo direto do lote altera os custos de dependentes diretos/indiretos na próxima consulta. Não sobrescrever preço praticado de produto.
- Produto que usa uma unidade do rendimento acompanha `custo direto atual / quantidade atual da opção escolhida`. Produto que usa fração fixa acompanha apenas a fração do lote; não substituir consumo por unidade automaticamente.
- Exemplo: lote custa R$ 60 e opção Pequeno rende 30, custo unitário R$ 2. Se a opção passa explicitamente a 24, com mesmo custo de lote, custo unitário passa a R$ 2,50. É edição do rendimento/porção, não simulação de produção e não rendimento real aprovado.
- Limpar a composição inteira é permitido como receita incompleta aprovada: custos dependentes ficam indisponíveis com causa identificada, preço praticado preservado. Não eliminar produtos dependentes ou assumir custo zero.
- A interface deve explicar o efeito de editar uma receita reutilizada. Não duplicar a receita automaticamente para evitar impactos: versionamento histórico de receitas não foi incluído no escopo atual.

### Resposta, concorrência e falhas

- Sucesso com alteração efetiva: 200 com representação completa da receita, todos os IDs, novo custo/pendências, atualizadoEm atual e versão incrementada uma vez, mesmo com várias linhas alteradas.
- Nenhuma mudança efetiva, com versão atual e comando válido: 200 com versão/datas preservadas. Reordenar linhas é mudança efetiva.
- ID inexistente: 404. Versão ausente/inválida: 400 VERSAO_OBRIGATORIA/VERSAO_INVALIDA. Versão antiga: 409 CONFLITO_DE_EDICAO, sem alteração.
- Proteger a edição do agregado inteiro: mudanças de ingredientes, preparos e rendimentos usam a mesma versão da receita, não versões independentes que permitam perda de alterações.
- Validar e gravar tudo numa transação. Não apagar e reinserir todas as linhas para simplificar o mapeamento; isso trocaria identidades e quebraria referências dos rendimentos.
- Atualizar o grafo e validar ciclos com a serialização prevista no modelo de dados. Não basta controle de versão isolado da receita para impedir um ciclo criado por duas receitas editadas concorrentemente.
- Coordenar novas associações com arquivamento e alterações de dimensão do insumo, como no cadastro. Custos retornados usam leitura consistente.
- Falha em qualquer componente ou rendimento rejeita o comando inteiro, inclusive renomeação do cabeçalho.

Erros apontam campos como ingredientes[0].id e rendimentos[1].nome. Além dos códigos do cadastro, usar COMPONENTE_INVALIDO, COMPONENTE_DUPLICADO, COMPONENTE_REFERENCIA_IMUTAVEL, RENDIMENTO_INVALIDO, RENDIMENTO_DUPLICADO e ESTADO_RENDIMENTO_INVALIDO, conforme a causa.

### Verificação futura

| Cenário | Resultado esperado |
| --- | --- |
| Editar somente nome | Composição e rendimentos preservados |
| Atualizar quantidade de ingrediente existente | Mesmo ID de linha e nova quantidade |
| Enviar ingredientes vazio | Remover ingredientes, sem apagar insumos |
| Enviar id de componente de outra receita | 400; nada salvo |
| Inserir preparo que fecha ciclo | 409; nada salvo |
| Renomear rendimento existente | Mesmo ID; produtos mantêm referência |
| Omitir rendimento de lista presente | Arquivar opção; preservar custo dos usos existentes |
| Reativar rendimento por ID | Restaurar opção sem criar outra identidade |
| Mudar porção Pequeno de 30 para 24 em lote R$ 60 | Custo por unidade muda de R$ 2 para R$ 2,50 |
| Limpar todos os componentes | Receita incompleta e custos dependentes indisponíveis |
| Conflito de versão | 409; preservar a alteração de outro cliente |

## Listagem de receitas — aprovada

`GET /api/v1/receitas`.

Usar a lista para localizar receitas e selecionar preparos reutilizáveis. Receita não tem tipo fixo MASSA/RECHEIO nesta versão; o uso como preparo é definido pelo vínculo.

### Filtros e paginação

| Parâmetro | Padrão | Regra |
| --- | --- | --- |
| busca | Sem filtro | Trecho do nome, trim, até 150 caracteres; vazio equivale a sem filtro |
| situacaoCusto | Sem filtro | DISPONIVEL ou INDISPONIVEL |
| composicaoVazia | Sem filtro | true ou false; verificar existência de ingrediente/preparo direto |
| status | ATIVOS | ATIVOS, ARQUIVADOS ou TODOS |
| pagina | 1 | Inteiro entre 1 e 2147483647 |
| tamanhoPagina | 20 | Inteiro de 1 a 100 |

Filtros combinam com AND. Busca sem distinguir maiúsculas/minúsculas ou acentos, tratando curingas como texto literal, conforme insumos. Ordem por nome e id crescentes. Mesmas validações de query, parâmetros repetidos/desconhecidos e erros de paginação de insumos; composicaoVazia inválido usa 400 COMPOSICAO_FILTRO_INVALIDO.

composicaoVazia=false significa ao menos um componente, não garante custo completo nem receita pronta. INDISPONIVEL encontra composições vazias, preços pendentes ou preparos incompletos. Custo conhecido zero é DISPONIVEL. Sem rendimento ativo não invalida custo de lote nem uso por fração.

### Exemplo de receitas a preencher

```http
GET /api/v1/receitas?composicaoVazia=true&pagina=1&tamanhoPagina=20
```

Resposta 200 ilustrativa:

```json
{
  "itens": [
    {
      "id": "101",
      "nome": "Recheio a preencher",
      "composicaoVazia": true,
      "situacaoCusto": "INDISPONIVEL",
      "custoDiretoLote": null,
      "rendimentosAtivos": [],
      "arquivadoEm": null,
      "versao": "1"
    }
  ],
  "pagina": 1,
  "tamanhoPagina": 20,
  "totalItens": "1",
  "temProximaPagina": false
}
```

rendimentosAtivos contém opções não arquivadas com id, nome e quantidade em string. Não define sozinho permissão de novo uso: a receita também deve estar ativa. Detalhe por ID continua retornando todas as opções. Pendências completas ficam no detalhe/custos.

totalItens é string de inteiro não negativo, contagem filtrada antes de paginar. Lista vazia ou página além do fim retorna 200, mantendo página pedida e total real. temProximaPagina usa o cálculo inteiro amplo definido para insumos.

Filtro de custo deve ser aplicado antes de paginar e contar, não apenas sobre a página obtida. Compartilhar resultados de cálculo dentro da operação para dependências comuns, sem cache persistente desatualizado. Itens, custos e total usam o mesmo snapshot por requisição; sem garantia de snapshot entre páginas.

### Seleção para novos usos

- Lista ativa pode incluir receita incompleta; seu uso como preparo é permitido com custo pendente, conforme cadastro aprovado.
- Tela de edição não oferece a própria receita como preparo. Isso não elimina ciclos indiretos nem substitui validação transacional do backend.
- Consumo por unidade em produto exige opção ativa de rendimento de receita ativa. Consumo por fração não exige rendimento.
- Receita arquivada já vinculada permanece visível na composição existente; não remover vínculo ao recarregar o seletor.

## Arquivamento e reativação da receita inteira — aprovados

| Método e rota | Objetivo |
| --- | --- |
| POST /api/v1/receitas/{id}/arquivar | Retirar receita das opções para novos usos |
| POST /api/v1/receitas/{id}/reativar | Disponibilizar a mesma receita novamente |

Corpo apenas com versão esperada obrigatória:

```json
{
  "versao": "4"
}
```

Retornar 200 com representação completa, arquivadoEm, versao e custos atuais. ID inexistente é 404; versão ausente/inválida é 400; versão antiga é 409 CONFLITO_DE_EDICAO, mesmo se o estado já tiver sido alcançado por outra operação. Estado já coincidente com versão atual retorna 200 sem alterar datas/versão. Campos extras são rejeitados, seguindo convenção de comandos de insumos.

### Arquivar

- Marcar arquivadoEm UTC, atualizar atualizadoEm e incrementar versão uma vez em transação.
- Preservar ingredientes, preparos, rendimentos, IDs e estados individuais. Não arquivar rendimentos, insumos, produtos ou receitas dependentes em cascata.
- Usos existentes continuam incluindo o custo direto atual da receita. Arquivamento não transforma custo conhecido em indisponível.
- Bloquear novo vínculo a receita arquivada como preparo ou componente de produto, inclusive por rendimento que continua ativo. Usar 409 PREPARO_ARQUIVADO em novo preparo e 409 RECEITA_ARQUIVADA em nova composição de produto, apontando o campo correspondente.
- Vínculo existente pode permanecer e ser ajustado; remover e recriar é nova associação.
- Permitir consulta, simulação e correção da receita arquivada, sem reativá-la implicitamente. Mudanças de custo continuam repercutindo nos usos existentes.
- Listagem padrão exclui; filtros ARQUIVADOS/TODOS e GET por ID permitem localizar.

### Reativar

- Limpar arquivadoEm, atualizar atualizadoEm e incrementar versão, preservando ID e composição.
- Não exigir custo completo nem composição preenchida: receita incompleta pode voltar ativa para ser preenchida.
- Preservar estado individual de cada rendimento: uma opção já arquivada continua arquivada; uma opção que era ativa continua ativa.
- Componentes arquivados/inativos já vinculados não impedem reativação: não são recriados pela transição. Mantê-los sinalizados. Essa regra difere de reativar insumo cujo próprio tipo/unidade foi desativado.
- Não copiar receita, não alterar preços praticados e não reativar dependências automaticamente.

### Integridade e verificações futuras

Edição e ciclo de vida usam a mesma versão da receita. Coordenar novas associações e arquivamento para impedir uso validado em estado antigo. Preservar FKs e não remover dados de negócio em cascata.

Exclusão definitiva de receitas solicitada para versão futura e registrada como FUT-02. Não haverá endpoint de exclusão física na primeira versão; elegibilidade, vínculos com produtos/preparos e tratamento dos rendimentos serão definidos antes dessa implementação futura. FUT-01 permanece a exclusão definitiva de insumos. Arquivamento reversível não substitui essas evoluções.

| Cenário | Resultado esperado |
| --- | --- |
| Lista sem parâmetros | Ativos, página 1, tamanho 20, ordem nome/id |
| Receita apenas com nome | composicaoVazia=true e custo indisponível |
| Receita com componentes e preço pendente | composicaoVazia=false e custo indisponível |
| Receita com custo conhecido zero | Disponível |
| Arquivar receita já utilizada | Custo e vínculos preservados; sem novas seleções |
| Novo uso de receita arquivada | 409; nenhuma gravação parcial |
| Arquivar com rendimento ativo | Não alterar estado individual do rendimento |
| Reativar receita incompleta | Permitido; custo permanece pendente |
| Reativar com opção arquivada | Opção continua arquivada |
| Comando com versão antiga | 409; não sobrescrever edição concorrente |

## Próximo passo

Contratos principais, premissas de domínio e modelo de dados aprovados. Próximo passo: fechar versões/persistência e preparar o primeiro incremento do backend de insumos; receitas serão implementadas em entrega própria. Formatos/contratos de importação/backup continuam pendentes. FUT-01 e FUT-02 permanecem fora da primeira versão. Implementação e OpenAPI ainda não iniciados.
