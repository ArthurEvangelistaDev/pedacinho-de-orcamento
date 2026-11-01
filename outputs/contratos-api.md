# Contratos da API — definição incremental

Versão: 0.11  
Atualizado: 2026-10-31  
Estado: contratos aprovados; cadastro/consulta individual de insumos em implementação Java.

Referências: [especificação](spec.md) e [modelo de dados](modelo-de-dados.md).

## Primeira operação: cadastrar insumo

Proposta de rota: `POST /api/v1/insumos`.

Objetivo: cadastrar ingrediente ou embalagem com os dados de compra de referência, permitindo que o preço seja preenchido posteriormente.

### Entrada proposta

```json
{
  "nome": "Chocolate blend",
  "tipoCodigo": "INGREDIENTE",
  "unidadeCompraCodigo": "g",
  "quantidadeCompra": "1000",
  "precoCompra": null
}
```

| Campo | Proposta de validação |
| --- | --- |
| nome | Obrigatório, remover espaços das extremidades; 1 a 150 caracteres |
| tipoCodigo | Obrigatório; código existente e disponível em tipos_insumo. Iniciais: INGREDIENTE e EMBALAGEM; não limitar o contrato a enum fixa |
| unidadeCompraCodigo | Obrigatório; código existente e disponível em unidades. Iniciais: g, kg, ml, l e un |
| quantidadeCompra | Obrigatório, maior que zero, até 6 casas decimais e dentro de DECIMAL(18,6); representação JSON proposta como string decimal |
| precoCompra | Opcional; omissão ou null na criação significa preço desconhecido. Se informado, não negativo, até 2 casas decimais e dentro de DECIMAL(18,2); representação JSON proposta como string decimal |
| confirmarPrecoZero | Booleano opcional, padrão false; precisa ser true quando precoCompra = 0. Mecanismo confirmado |

Confirmados: nome obrigatório, quantidade positiva, tabelas de tipos/unidades com envio por código e zero aceito com confirmarPrecoZero = true. A equivalência entre omissão e null vale para esta criação; não define futuras edições parciais.

O cliente consulta os catálogos, mostra nomes e envia apenas códigos. Propostas: `GET /api/v1/tipos-insumo` e `GET /api/v1/unidades`, detalhadas na seção de catálogos deste documento. O cálculo e a validação autoritativos usam os valores do banco no backend. Códigos são sensíveis a maiúsculas/minúsculas na API; exemplos mostram a forma canônica, independentemente da collation do banco.

Se um código deixou de estar disponível entre a consulta e o salvamento, retornar validação no campo e não persistir associação inválida. Não criar tipo/unidade automaticamente a partir de texto recebido. Não aceitar nome ou objeto completo em lugar do código.

Não aceitar custo calculado, identificador, datas ou versão enviados como campos de criação. A política geral para propriedades JSON desconhecidas será fechada com as convenções comuns da API.

### Saída de sucesso proposta

`201 Created`, cabeçalho `Location: /api/v1/insumos/42`.

Exemplo ilustrativo; identificador e datas não representam um cadastro real:

```json
{
  "id": "42",
  "nome": "Chocolate blend",
  "tipoCodigo": "INGREDIENTE",
  "unidadeCompraCodigo": "g",
  "quantidadeCompra": "1000.000000",
  "precoCompra": null,
  "precoAtualizadoEm": null,
  "custoUnitario": null,
  "unidadeCustoCodigo": "g",
  "situacaoCusto": "INDISPONIVEL",
  "pendencias": [
    {
      "codigo": "INSUMO_SEM_PRECO",
      "insumoId": "42",
      "mensagem": "Informe o preço de compra do Chocolate blend."
    }
  ],
  "criadoEm": "2026-10-19T15:00:00Z",
  "atualizadoEm": "2026-10-19T15:00:00Z",
  "versao": "1"
}
```

`unidadeCustoCodigo` usa a unidade base da dimensão: g, ml ou un. Quando o preço for informado posteriormente como R$ 40 por 1000 g, custoUnitario passa a 0,04 e a data do preço será preenchida. A precisão de apresentação desse custo não deve ser limitada a centavos.

### Preço zero: confirmação aprovada

Preço normal é maior que zero. Para permitir um insumo gratuito sem salvar zero por engano:

1. Usuário informa zero; interface mostra: “O preço informado é R$ 0,00. Confirma que este insumo não teve custo?”
2. Antes da confirmação, não salvar. Se o usuário confirmar, enviar precoCompra = 0 e confirmarPrecoZero = true.
3. A API exige essa confirmação mesmo quando chamada diretamente. Preço zero sem confirmação retorna 400 com código PRECO_ZERO_REQUER_CONFIRMACAO no campo precoCompra, sem criar insumo.
4. Com confirmação e dados válidos, salvar zero, registrar data do preço, custo unitário zero e situacaoCusto DISPONIVEL. Não gerar INSUMO_SEM_PRECO.

Exemplo de comando após confirmação:

```json
{
  "nome": "Caixa recebida sem custo",
  "tipoCodigo": "EMBALAGEM",
  "unidadeCompraCodigo": "un",
  "quantidadeCompra": "10",
  "precoCompra": "0.00",
  "confirmarPrecoZero": true
}
```

O campo não é armazenado: confirma a intenção deste comando, não cria aprovação permanente para preços futuros. Se preço é positivo ou ausente, o booleano não dispensa outras validações nem transforma ausência em zero. A interface deve reiniciar a confirmação quando o usuário altera o preço.

Preço negativo é sempre rejeitado, mesmo com confirmarPrecoZero = true. Valor ausente é permitido sem confirmação e não pode ser substituído por zero padrão. Estas regras descrevem criação; edições serão detalhadas no contrato próprio.

### Comportamento confirmado quando falta preço

- Salvar o cadastro normalmente; ausência de preço não é falha de validação.
- Insumo: mostrar custo indisponível, com valor calculado null.
- Receita e produto dependentes, inclusive por preparos intermediários: mostrar total indisponível e identificar a pendência.
- Preservar preço praticado e objetivo de lucro, mas não apresentar margem efetiva ou preço sugerido como se o custo estivesse completo.
- Mostrar custos conhecidos por componente é permitido; não apresentar sua soma parcial como custo total.
- Ao preencher o preço, a próxima consulta recalcula os dependentes com os dados atuais; outras pendências ainda podem impedir o cálculo.

IDs de custo/situação e formato de pendências acima são propostas de transporte para representar essa regra.

### Falhas de cadastro — padrão aprovado

Entrada inválida, como quantidade zero, produz `400 Bad Request`, apontando o campo e o motivo no formato abaixo. Falha de banco não poderá retornar 201. Padrão aprovado em 2026-10-19; contratos de operações futuras continuam sujeitos a definição própria.

| Campo | Código de validação | Condição |
| --- | --- | --- |
| nome | NOME_OBRIGATORIO | Ausente, null ou apenas espaços |
| nome | NOME_MUITO_LONGO | Mais de 150 caracteres após remover espaços das extremidades |
| tipoCodigo | TIPO_OBRIGATORIO / TIPO_INVALIDO | Ausente/vazio ou código inexistente/indisponível |
| unidadeCompraCodigo | UNIDADE_OBRIGATORIA / UNIDADE_INVALIDA | Ausente/vazio ou código inexistente/indisponível |
| quantidadeCompra | QUANTIDADE_INVALIDA | Ausente/null, zero, negativa ou fora da precisão/escala |
| precoCompra | PRECO_INVALIDO | Negativo ou fora da precisão/escala |
| precoCompra | PRECO_ZERO_REQUER_CONFIRMACAO | Zero com confirmarPrecoZero ausente ou false |

Preço zero confirmado não contorna nome, quantidade ou códigos inválidos. Sem criação parcial de registros após falha de validação.

### Padrão de resposta de erro — aprovado

Usar Problem Details (RFC 9457), com `Content-Type: application/problem+json`, status HTTP real e membro status iguais. No backend Java/Spring, mapear erros de binding e validação para este contrato por tratamento centralizado. Os campos codigo, erros e traceId são extensões próprias; não expor respostas automáticas do framework com outro formato.

Referências: [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html) e [Problem Details no Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html).

Exemplo: nome vazio, quantidade zero e preço zero sem confirmação:

```json
{
  "type": "about:blank",
  "title": "Requisição inválida",
  "status": 400,
  "detail": "Revise os campos indicados.",
  "instance": "/api/v1/insumos",
  "codigo": "VALIDACAO_FALHOU",
  "erros": [
    {
      "campo": "nome",
      "codigo": "NOME_OBRIGATORIO",
      "mensagem": "Informe o nome do insumo."
    },
    {
      "campo": "quantidadeCompra",
      "codigo": "QUANTIDADE_INVALIDA",
      "mensagem": "Informe uma quantidade maior que zero."
    },
    {
      "campo": "precoCompra",
      "codigo": "PRECO_ZERO_REQUER_CONFIRMACAO",
      "mensagem": "Confirme que este insumo não teve custo para salvar o preço zero."
    }
  ],
  "traceId": "exemplo-rastreio-001"
}
```

Convenções do padrão aprovado:

- `codigo` no topo identifica a categoria do problema; cada item de `erros` identifica uma falha específica.
- `campo` usa o nome JSON da entrada, não o nome de coluna do banco. Falha no corpo inteiro usa `$`; erros sem campo específico podem retornar lista vazia.
- Retornar todas as falhas de campo detectáveis numa requisição bem formada, sem depender da ordem da lista. Não exigir consulta ao banco para verificar códigos quando o JSON não pôde ser interpretado.
- Interface destaca os campos, exibe mensagens e trata PRECO_ZERO_REQUER_CONFIRMACAO para oferecer confirmação. Depois da confirmação, reenvia os dados; todas as outras validações continuam ativas.
- Código de erro é identificador estável. Cliente não interpreta nem compara texto das mensagens.
- JSON inválido, tipo incorreto (ex.: quantidade como objeto) ou confirmarPrecoZero null/em formato diferente de booleano produzem 400 com codigo REQUISICAO_INVALIDA; item específico pode usar FORMATO_INVALIDO. Não expor mensagens técnicas do serializador.
- Referência de tipo/unidade inválida neste POST é 400, não 404 do insumo. Rejeitar o comando sem criar cadastro.
- Nome igual ao de outro insumo não é conflito por si só: nomes não foram definidos como únicos.
- Exceções inesperadas produzem mensagem genérica; detalhes técnicos ficam nos logs associados ao traceId. Não incluir senha, SQL ou stack trace na resposta pública.

| HTTP | Código geral | Quando usar |
| --- | --- | --- |
| 400 | VALIDACAO_FALHOU | JSON interpretável, mas um ou mais campos violam as regras |
| 400 | REQUISICAO_INVALIDA | JSON malformado ou tipos incompatíveis |
| 415 | TIPO_CONTEUDO_NAO_SUPORTADO | Corpo enviado em Content-Type não aceito pelo endpoint |
| 500 | ERRO_INTERNO | Falha inesperada no servidor |
| 503 | SERVICO_INDISPONIVEL | Dependência essencial indisponível, quando a falha transitória for identificada como tal |

Banco sem conexão não é erro de preenchimento. Se a indisponibilidade for reconhecida, usar 503; erros inesperados de persistência continuam 500. Não prometer que nenhuma gravação ocorreu após timeout ou conexão interrompida: consultar antes de repetir criação quando o resultado for incerto. Idempotência de criação ainda será definida; não fazer retry automático de POST.

Para operações futuras: 404/RECURSO_NAO_ENCONTRADO ao buscar ID inexistente e 409/CONFLITO_DE_EDICAO para versão desatualizada na estratégia de versão pelo corpo. Não usar 409 no POST apenas porque o nome já existe. Se a edição adotar If-Match, revisar a opção de 412 antes de fechar o contrato.

Ausência de preço não é resposta de erro: cadastro válido continua retornando 201 com pendência de custo. Preço zero confirmado é custo conhecido e não gera pendência.

### Verificação futura

| Cenário | Resultado esperado |
| --- | --- |
| Cadastro válido com precoCompra null | Insumo criado, data do preço null, custo indisponível |
| Cadastro válido sem propriedade precoCompra | Mesmo comportamento de preço ausente |
| Quantidade de compra igual a zero | Cadastro rejeitado |
| Quantidade negativa, null ou omitida | Cadastro rejeitado |
| Nome ausente, vazio ou só espaços | Cadastro rejeitado |
| Tipo ou unidade inexistente/indisponível | Cadastro rejeitado com indicação do campo |
| Código de tipo disponível no banco | Aceito pela referência, sem depender de enum fixa na entrada |
| Preço negativo, mesmo confirmado | Cadastro rejeitado |
| Preço zero sem confirmação | PRECO_ZERO_REQUER_CONFIRMACAO, sem criação |
| Preço zero confirmado e demais dados válidos | Custo zero disponível, data do preço preenchida |
| Receita utiliza insumo sem preço | Total da receita indisponível com causa identificada |
| Produto utiliza essa receita | Custo total, preço sugerido e margem efetiva indisponíveis; preço praticado preservado |
| Preço preenchido posteriormente, sem outras pendências | Cálculos disponíveis na consulta seguinte |
| Múltiplos campos inválidos em JSON bem formado | 400 com erros por campo; sem salvar parcialmente |
| JSON malformado ou confirmarPrecoZero em tipo incorreto | 400 REQUISICAO_INVALIDA |
| Tipo/unidade inexistente no cadastro | 400 VALIDACAO_FALHOU com erro no campo de código |
| Indisponibilidade de banco identificada | 503 SERVICO_INDISPONIVEL; sem apresentar como erro de campo |

## Representação de valores no JSON — aprovada

Representação aprovada pelo usuário em 2026-10-19. Os exemplos de entrada/saída foram alinhados a esta convenção; não existem clientes ou endpoints implementados a migrar.

### Convenção de transporte

| Informação | JSON | Backend / banco |
| --- | --- | --- |
| IDs e versão | String de inteiro positivo, ex.: "42" e "1" | long / BIGINT |
| Preços e custos | String decimal, ex.: "40.00" | BigDecimal / DECIMAL |
| Quantidades, rendimentos, fatores e percentuais | String decimal, ex.: "1000" e "0.5" | BigDecimal / DECIMAL |
| Valores desconhecidos permitidos | null real | Valor nullable; não string "null" |
| Confirmações | true ou false reais | bool |
| Status HTTP e futuros limites de paginação | Inteiros JSON pequenos | int com limites explícitos |
| Datas | String ISO 8601 com UTC, ex.: "2026-10-19T15:00:00Z" | Datas UTC |

Razão: o alcance de BIGINT excede o inteiro seguro de Number em JavaScript; o tipo Number também não é um decimal financeiro exato. Strings mantêm os dígitos recebidos sem obrigar o frontend a converter valores para ponto flutuante. O custo é conversão explícita no backend e maior disciplina de formatação no cliente. Não há mudança para colunas de texto no banco.

Referências: [limite de inteiros JavaScript](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MAX_SAFE_INTEGER) e [BigDecimal](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html). Validar explicitamente a gramática, a escala e os limites antes de converter e persistir.

### Formato

- Decimais no transporte usam ponto, sem separador de milhar, símbolo de moeda, espaços, sinal + ou notação científica. Entrada de sintaxe válida pode ter sinal negativo, mas é rejeitada pela regra de domínio nos campos que não o permitem.
- Padrão de sintaxe decimal: `^-?(0|[1-9][0-9]*)(\.[0-9]+)?$`. Aceitar "40", "40.0" ou "40.00" para preço; normalizar a resposta monetária a duas casas. Não aceitar "40,00", "R$ 40,00", "1e3" nem string vazia.
- Quantidade persistida aceita até 6 casas e resposta usa 6; preço de compra/praticado aceita até 2 e resposta usa 2. Validar a escala antes de persistir, sem truncamento implícito.
- Custos unitários calculados não ficam limitados a duas casas: usar BigDecimal e serializar por toPlainString, sem notação exponencial. Divisões usam MathContext.DECIMAL128 (34 dígitos significativos, HALF_EVEN). Essa representação não promete exatidão matemática de frações periódicas.
- IDs e versões usam `^[1-9][0-9]*$` e valor até 9223372036854775807. API não deve ler ID/versão por double. Valores de rota também são validados como long positivo.
- Strings são obrigatórias nesses campos JSON. Número literal em campo definido como string produzirá 400 REQUISICAO_INVALIDA; sintaxe/escala/limite inválido de string decimal usa 400 VALIDACAO_FALHOU com código do campo. Não configurar um conversor permissivo global que aceite silenciosamente os dois formatos.
- No cálculo de preço zero, "0", "0.0" e "0.00" representam o mesmo valor numérico e exigem confirmação. null ou propriedade omitida não representam zero.
- Interface mostra os valores em formato local, como R$ 40,00, e converte a entrada para o formato do contrato antes de enviar. Não usar Number/parseFloat como etapa intermediária obrigatória para valores monetários; formatação exata e eventual biblioteca decimal do frontend serão escolhidas na implementação.
- O backend é a fonte autoritativa do cálculo de custos e preços. Caso a interface ofereça prévia de conversão margem/acréscimo, a confirmação final usa o cálculo retornado pela API.

### Política numérica Java

Criar BigDecimal a partir de strings, sem passagem por double/float. Comparar valores numericamente por compareTo; escala lexical diferente não representa mudança de preço. Rejeitar escala/limites de entradas antes da persistência. Preço sugerido final usa escala 2 com HALF_UP; não arredondar componentes a centavos.

BigDecimal não possui o overflow fixo do decimal anterior. Para preservar a fronteira do contrato, cálculos derivados mantêm limite absoluto 79228162514264337593543950335; ultrapassá-lo usa os códigos de cálculo fora do limite da operação. Campos persistidos continuam nos limites DECIMAL(18,2)/(18,6). A adoção de 34 dígitos nas divisões é decisão técnica de adaptação; resultados devem cumprir os exemplos e arredondamentos aprovados.

Exemplo com preço conhecido:

```json
{
  "nome": "Chocolate blend",
  "tipoCodigo": "INGREDIENTE",
  "unidadeCompraCodigo": "g",
  "quantidadeCompra": "1000",
  "precoCompra": "40.00"
}
```

## Consulta de insumo — aprovada

`GET /api/v1/insumos/{id}`.

- Retornar 200 com os mesmos campos do cadastro, acrescentando `arquivadoEm` (data ou null).
- Não exigir que o insumo tenha preço para consultá-lo: retornar 200 com custo indisponível e pendência identificada, como no cadastro.
- ID inexistente: 404 com codigo RECURSO_NAO_ENCONTRADO, erros vazio e o padrão Problem Details aprovado.
- ID fora da gramática/faixa definida: 400 VALIDACAO_FALHOU com erro ID_INVALIDO no campo id; não depender apenas de restrição da rota que transforme formato inválido em 404.
- Insumo arquivado continua acessível pelo ID, com arquivadoEm preenchido. Isso preserva consulta de componentes existentes, conforme arquivamento aprovado.
- Retornar preço e datas atuais, versão como string e valores decimais como strings. GET não altera versão ou data do cadastro/preço.

## Listagem e busca de insumos — aprovada

`GET /api/v1/insumos`.

Objetivo: listar os insumos para consulta, edição e localização de pendências de preço.

### Parâmetros de consulta

| Parâmetro | Padrão | Comportamento |
| --- | --- | --- |
| busca | Sem filtro | Texto contido no nome, após trim; vazio equivale a sem filtro; até 150 caracteres |
| tipoCodigo | Sem filtro | Código exato de um tipo cadastrado |
| situacaoCusto | Sem filtro | DISPONIVEL ou INDISPONIVEL; zero confirmado é DISPONIVEL |
| status | ATIVOS | ATIVOS, ARQUIVADOS ou TODOS |
| pagina | 1 | Inteiro de 1 até 2147483647 |
| tamanhoPagina | 20 | Inteiro de 1 a 100 |

Filtros são combinados com AND. Na query string, pagina e tamanhoPagina são textos de inteiro decimal, retornados como números JSON limitados a int; não aceitar notação exponencial, valores fracionários, parâmetros repetidos ou parâmetros desconhecidos. Códigos vazios são inválidos; somente busca vazia equivale à ausência.

Ordenação inicial fixa por nome crescente e id crescente como desempate. Não incluir ordenação configurável neste primeiro contrato. Ordenar id numericamente no banco, apesar da representação string no JSON.

Busca sem distinção de maiúsculas/minúsculas e acentos, com comparação/collation a implementar e testar no ambiente escolhido. Tratar %, _ e caracteres de escape como texto literal da busca, não como curingas SQL. Identidade dos códigos continua sensível a maiúsculas/minúsculas, independente da comparação de nomes.

Filtro de tipo aceita código cadastrado mesmo se o tipo estiver inativo, para localizar dados antigos. Tipo inexistente é 400 com TIPO_INVALIDO; não retornar lista vazia que esconda erro de código. Referências inativas não são novas associações, por isso não seguem a mesma regra do POST.

### Exemplo

```http
GET /api/v1/insumos?busca=chocolate&tipoCodigo=INGREDIENTE&situacaoCusto=INDISPONIVEL&pagina=1&tamanhoPagina=20
```

Resposta 200 ilustrativa:

```json
{
  "itens": [
    {
      "id": "42",
      "nome": "Chocolate blend",
      "tipoCodigo": "INGREDIENTE",
      "unidadeCompraCodigo": "g",
      "quantidadeCompra": "1000.000000",
      "precoCompra": null,
      "precoAtualizadoEm": null,
      "custoUnitario": null,
      "unidadeCustoCodigo": "g",
      "situacaoCusto": "INDISPONIVEL",
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

`totalItens` é contagem de todos os registros filtrados, antes da paginação. Representar como string de inteiro não negativo, pois é uma contagem potencialmente longa; "0" é válido, diferente da regra dos IDs positivos. Não confundir totalItens com o tamanho do array. `temProximaPagina = pagina × tamanhoPagina < totalItens`, calculado com inteiro amplo, sem overflow de int.

A lista é resumida; não retorna a lista completa de pendências, mas situacaoCusto indica o preço ausente. Detalhe por ID fornece pendencias, criadoEm e atualizadoEm. Opcionalmente a interface resolve os rótulos de tipo e unidade pelo catálogo; não converter código em nome por regra fixa no frontend.

Nenhum resultado: 200 com itens vazio, totalItens "0" e temProximaPagina false. Página além do fim: 200 com itens vazio, mantendo totalItens real dos filtros. Não retornar 404 nesses casos e não trocar silenciosamente a página solicitada.

Página/tamanho inválidos produzem 400 com PAGINA_INVALIDA ou TAMANHO_PAGINA_INVALIDO. Busca longa, status/situação/códigos inválidos usam erros no campo com BUSCA_INVALIDA, STATUS_INVALIDO, SITUACAO_CUSTO_INVALIDA ou TIPO_INVALIDO. Query repetida ou desconhecida: PARAMETRO_INVALIDO. Valores que não podem ser interpretados usam REQUISICAO_INVALIDA/FORMATO_INVALIDO conforme o padrão aprovado.

Não incluir arquivados por padrão é convenção de listagem, não filtro global de persistência: componentes arquivados existentes continuam participando de custos. Criar/arquivar um cadastro entre páginas pode deslocar itens na paginação por offset; o contrato não garante snapshot entre requisições. Dentro de cada resposta, itens e total devem usar o mesmo snapshot de leitura. GET não provoca gravação.

### Verificação futura das consultas

| Cenário | Resultado esperado |
| --- | --- |
| ID válido existente | 200 com cadastro atual |
| ID válido inexistente | 404 RECURSO_NAO_ENCONTRADO |
| ID zero, negativo, não numérico ou fora de long | 400 com erro de identificação |
| Cadastro sem preço | 200 com custo indisponível |
| Lista sem parâmetros | Página 1, tamanho 20, somente ativos, nome/id crescentes |
| Insumos com mesmo nome | Ordenação por id como desempate |
| Insumo com preço zero confirmado | Incluído em DISPONIVEL, excluído de INDISPONIVEL |
| Busca sem resultados | 200 e lista vazia |
| Página além do fim | 200 e lista vazia com total real |
| tamanhoPagina 101 | 400 com TAMANHO_PAGINA_INVALIDO |
| status TODOS | Incluir ativos e arquivados respeitando demais filtros |
| Campo preço alterado antes da consulta seguinte | Dados atuais, sem histórico implícito |

## Edição de insumo e atualização de preço — aprovada

`PATCH /api/v1/insumos/{id}`, `Content-Type: application/json`.

Usar documento parcial próprio, definido nesta seção; não é JSON Patch nem JSON Merge Patch. PATCH permite modificações parciais e exige aplicação atômica das mudanças. Referência: [RFC 5789](https://www.rfc-editor.org/rfc/rfc5789.html). Rota, formato e comportamento aprovados pelo usuário; sem implementação.

Uma operação pode editar nome/tipo e/ou referência de compra na mesma transação. Não criar endpoints concorrentes que também gravem os mesmos campos de preço nesta etapa.

### Entrada

| Campo | Quando obrigatório | Regra |
| --- | --- | --- |
| versao | Sempre | String de long positivo recebida no GET; versão esperada do cadastro |
| nome | Opcional | Ausente preserva; null/vazio é inválido; mesmas regras da criação |
| tipoCodigo | Opcional | Ausente preserva; null/vazio é inválido; novo tipo deve estar disponível |
| unidadeCompraCodigo | Junto com quantidadeCompra e precoCompra | Alteração de qualquer um exige os três campos |
| quantidadeCompra | Junto com unidadeCompraCodigo e precoCompra | Maior que zero; mesmas regras da criação |
| precoCompra | Junto com unidadeCompraCodigo e quantidadeCompra | String não negativa ou null explícito para remover preço |
| confirmarPrecoZero | Se precoCompra é zero | true explícito; mesma confirmação da criação |

Campos não enviados não são preenchidos por default. Não desserializar ausência de precoCompra para null e apagar preço involuntariamente. Identificar presença das propriedades antes de executar a alteração.

Se qualquer campo do trio de compra for enviado, exigir todos, mesmo se apenas o preço mudou. Isso descreve explicitamente quanto foi comprado por aquele valor. A versão esperada protege o trio contra reenvio de dados antigos.

Exemplo de edição apenas do nome:

```json
{
  "versao": "3",
  "nome": "Chocolate blend para cobertura"
}
```

Exemplo de atualização do preço mantendo a compra de referência de 1.000 g:

```json
{
  "versao": "3",
  "unidadeCompraCodigo": "g",
  "quantidadeCompra": "1000",
  "precoCompra": "45.00"
}
```

Os exemplos são alternativos a partir da mesma versão 3, não duas requisições sequenciais válidas. Após uma mudança efetiva, o cliente deve usar a versão devolvida.

Para remover o preço, enviar o trio completo com precoCompra null. Para definir zero, enviar o trio com precoCompra "0.00" e confirmarPrecoZero true. Editar somente nome de insumo cujo preço já é zero não exige nova confirmação, pois o preço não foi enviado. Enviar explicitamente preço zero exige confirmação mesmo se já era zero.

### Concorrência e retorno

- ID inexistente: 404 RECURSO_NAO_ENCONTRADO; PATCH não cria insumo novo.
- versao ausente: 400 com VERSAO_OBRIGATORIA; valor fora da gramática/faixa: 400 com VERSAO_INVALIDA.
- Versão esperada diferente da atual: 409 CONFLITO_DE_EDICAO, sem alteração. Interface mantém a edição local e oferece recarregar/comparar; não reenvia automaticamente usando uma versão nova.
- Validar a versão e aplicar a atualização atomicamente no banco. Verificar em memória e depois fazer UPDATE sem comparação da versão é insuficiente.
- Mudança efetiva: 200 com representação completa do insumo, mesmas convenções de GET, versão incrementada e atualizadoEm atual.
- Valores iguais aos já salvos, com versão correta e validação válida: 200 com representação atual, sem incrementar versão nem alterar datas.
- Exigir pelo menos um campo editável além de versao/confirmarPrecoZero; caso contrário 400 com NENHUMA_ALTERACAO_INFORMADA.
- Rejeitar campos desconhecidos ou somente de leitura nesse PATCH com CAMPO_NAO_PERMITIDO. Não aceitar criadoEm, atualizadoEm, custoUnitario, arquivadoEm nem id no corpo como edição.
- Se uma regra falhar, não salvar parcialmente nome, tipo ou compra.
- Após erro técnico com resultado incerto, consultar o cadastro antes de repetir. Não interpretar uma resposta 409 posterior como prova de que a primeira operação falhou.

Exemplo de conflito no padrão aprovado:

```json
{
  "type": "about:blank",
  "title": "Conflito de edição",
  "status": 409,
  "detail": "Este insumo foi alterado desde a sua consulta. Recarregue os dados antes de salvar.",
  "instance": "/api/v1/insumos/42",
  "codigo": "CONFLITO_DE_EDICAO",
  "erros": [
    {
      "campo": "versao",
      "codigo": "VERSAO_DESATUALIZADA",
      "mensagem": "A versão enviada não corresponde à versão atual."
    }
  ],
  "traceId": "exemplo-rastreio-002"
}
```

409 é a escolha deste contrato com versão esperada no corpo. Não implementar If-Match/ETag como requisito paralelo; se esse mecanismo for adotado no futuro, revisar a semântica de 412.

### Datas e impacto do custo

| Situação | precoAtualizadoEm | Outros efeitos |
| --- | --- | --- |
| Apenas nome/tipo muda | Preservada | atualizadoEm e versão mudam |
| Preço, quantidade ou unidade da compra muda, com preço conhecido | Instante da alteração em UTC | Recalcular custos nas próximas consultas |
| Preço conhecido passa a null | null | Custos dependentes indisponíveis; preço praticado preservado |
| Preço ausente passa a conhecido, inclusive zero confirmado | Instante da alteração em UTC | Custos disponíveis se não houver outras pendências |
| Nenhum dado efetivamente muda | Preservada | Preservar versão e atualizadoEm |

`precoAtualizadoEm` representa a atualização da referência de custo: quantidade e unidade também importam, mesmo se precoCompra não mudou. Se o preço já é null e apenas quantidade/unidade muda, a data do preço continua null. Trocar 1 kg por 1.000 g altera a referência registrada mesmo que o custo matemático permaneça igual; se o preço é conhecido, atualizar a data.

O preço praticado dos produtos nunca é alterado por essa edição. Receitas e produtos devem usar o novo custo na consulta seguinte, inclusive por preparos intermediários; não é garantia de atualização instantânea de telas já abertas.

### Integridade de unidades e catálogos

- Alterar g para kg é permitido quando as quantidades de compra forem informadas corretamente; não converter automaticamente a quantidade enviada como se ela ainda estivesse na unidade antiga.
- Alterar dimensão (ex.: g para ml) quando há usos existentes incompatíveis é rejeitado com 409 UNIDADE_INCOMPATIVEL_COM_USOS, apontando unidadeCompraCodigo. Não converter peso em volume sem regra específica.
- Sem usos incompatíveis, a mudança de dimensão é permitida; custo passa a usar a nova unidade base. Validar dependências e editar na mesma transação, protegendo contra novos usos concorrentes incompatíveis.
- Código inativo já vinculado pode permanecer inalterado numa edição; nova associação exige código disponível. Nunca impedir atualização de preço apenas porque o tipo ou a unidade atual foi desativado.
- Insumo arquivado pode ter nome, tipo e preço corrigidos pelos mesmos critérios; reativação não é efeito implícito deste PATCH e será operação própria.

### Falhas específicas e cenários de verificação

Demais falhas usam os códigos já definidos na criação. Trio incompleto: 400 VALIDACAO_FALHOU com REFERENCIA_COMPRA_INCOMPLETA para cada campo obrigatório ausente do trio.

| Cenário | Resultado esperado |
| --- | --- |
| Versão 3 atual, nome muda | 200; versão 4, data do preço preservada |
| Versão 3 atual, preço 40 → 45 por 1000 g | 200; custo por g 0,045, versão 4, data do preço atualizada |
| Dois clientes leram versão 3; primeiro salva e segundo tenta salvar | Segundo recebe 409; não sobrescreve primeiro |
| Só precoCompra enviado | 400; exigir quantidade e unidade da referência |
| Nome alterado sem enviar preço | Preço e data do preço preservados |
| Trio completo e precoCompra null | Remover preço; custos dependentes indisponíveis |
| Editar preço para zero sem confirmação | 400 PRECO_ZERO_REQUER_CONFIRMACAO; nada salvo |
| Editar só nome de insumo com preço zero | Não exigir confirmação de zero |
| Preço permanece 40 e compra muda de 1000 g para 500 g | Custo unitário muda de 0,04 para 0,08; atualizar data do preço |
| Dimensão nova incompatível com componente existente | 409; preservar todos os campos |
| Valores idênticos, versão correta | 200; sem incrementar versão/datas |
| Código atual inativo, edição apenas do preço | Permitida |

## Arquivamento e reativação de insumos — aprovados

### Objetivo

Retirar insumos de uso futuro sem quebrar composições existentes. Aprovado arquivamento reversível para todos os insumos, usados ou não, na primeira versão. Não criar histórico de preços nem cópias de receitas como efeito do arquivamento.

Exclusão definitiva solicitada para versão futura, registrada como FUT-01 na especificação. Não há endpoint de exclusão real na primeira versão; sua elegibilidade e tratamento de dependências serão definidos quando essa evolução for planejada. A ausência atual do endpoint não representa abandono da funcionalidade futura.

### Operações aprovadas

| Método e rota | Efeito |
| --- | --- |
| POST /api/v1/insumos/{id}/arquivar | Preencher arquivadoEm e retirar da seleção para novas associações |
| POST /api/v1/insumos/{id}/reativar | Remover arquivadoEm e permitir novamente novas associações |

Ambas recebem somente a versão esperada, como no exemplo:

```json
{
  "versao": "4"
}
```

As datas são geradas pelo backend; não aceitar arquivadoEm ou outros campos nesse corpo. Retornar 200 com a representação completa do insumo após a operação, incluindo arquivadoEm e versão. Mesmas validações de ID, versão e campos desconhecidos da edição; ID inexistente retorna 404.

### Regras de arquivamento

- Ativo com versão atual: arquivar, registrar instante UTC, atualizar atualizadoEm e incrementar versão uma vez.
- Arquivado com versão atual: responder 200 com o estado atual, sem alterar datas ou versão.
- Versão desatualizada: 409 CONFLITO_DE_EDICAO mesmo quando o insumo já está no estado solicitado. Repetir exatamente um comando de versão antiga não garante novo sucesso; consultar o cadastro em caso de resultado incerto.
- Preservar preço, data do preço, quantidade/unidade de compra, composições e preço praticado dos produtos.
- Receita/produto que já usa o insumo continua incluindo seu custo atual. Arquivar insumo de custo conhecido não torna o custo indisponível; insumo sem preço continua pendente.
- Impedir inclusão do insumo arquivado em novos componentes de receita/produto. Se a seleção ficou antiga e o insumo foi arquivado antes de salvar a associação, retornar 409 INSUMO_ARQUIVADO no campo insumoId.
- Vínculo existente pode permanecer e ter sua quantidade ajustada sem exigir reativar o insumo. Não interpretar o envio de uma composição existente completa como criação de todos os vínculos. Remover e adicionar novamente um vínculo será novo uso, exigindo insumo ativo.
- Não impedir editar preço de insumo arquivado; os dependentes existentes precisam continuar podendo receber custo atualizado.
- Listagem padrão continua apenas ativos. `status=ARQUIVADOS` e `status=TODOS` permitem localizar o item; GET por ID permanece disponível.
- Na interface, identificar “Insumo arquivado” nos componentes existentes sem substituir a situação de custo. Estado do cadastro e disponibilidade de custo são informações diferentes.

### Regras de reativação

- Arquivado com versão atual e tipo/unidade disponíveis: reativar, atualizar atualizadoEm, incrementar versão e manter preço/data do preço intactos.
- Ativo com versão atual: responder 200 sem mudança; não refazer validações de ativação quando nenhuma transição ocorre.
- Preço ausente não impede reativar: volta ativo, mas com custo indisponível. Zero conhecido não exige nova confirmação, pois reativar não define nem altera preço.
- Se o insumo está arquivado e seu tipo ou unidade de compra foi desativado no catálogo, rejeitar a reativação com 409 REFERENCIA_INATIVA, indicando tipoCodigo ou unidadeCompraCodigo. Permitir corrigir as referências pelo PATCH aprovado e depois reativar.
- Não reativar automaticamente tipo/unidade nem criar cópia do insumo. Reativação usa o mesmo ID.

### Integridade e concorrência

Aplicar transição e versão atomicamente. Gravação de nova associação e arquivamento devem ser coordenados no banco para que um não valide “ativo” antes de o outro arquivar e depois grave com estado antigo. O desenho de transação/bloqueio será detalhado na implementação. A proteção de versão do insumo isolada não protege automaticamente a gravação de composição de outra entidade.

### Verificação futura

| Cenário | Resultado esperado |
| --- | --- |
| Arquivar insumo ativo com versão atual | Mesmo ID, arquivadoEm preenchido, versão incrementada |
| Receita já utiliza o insumo | Relação e custo preservados; indicar arquivamento |
| Nova composição tenta selecionar insumo arquivado | 409 INSUMO_ARQUIVADO; não gravar parcialmente |
| Atualização de preço de insumo arquivado | Permitida conforme PATCH, repercutindo nos custos existentes |
| Reativar com catálogos disponíveis | Mesmo ID, arquivadoEm null, preço/data do preço preservados |
| Reativar sem preço | Permitido; custo permanece indisponível |
| Reativar com referência inativa | 409 REFERENCIA_INATIVA; manter arquivado |
| Solicitar estado já existente, com versão atual | 200 sem mudança de datas/versão |
| Qualquer comando com versão antiga | 409; não sobrescrever edição concorrente |

## Catálogos de tipos e unidades — aprovados

### Objetivo e limites

Alimentar os seletores com valores do banco, exibindo nome e enviando código no cadastro/edição. Tabelas, referência por código e contratos de consulta aprovados pelo usuário em 2026-10-19.

Catálogo inicial pequeno carregado por seed/migração, com consultas somente de leitura; sem criar telas ou endpoints de manutenção neste passo. Isso não impede novos tipos no banco, mas não autoriza conversões arbitrárias nem torna regras especiais de um novo tipo automaticamente implementadas.

### Rotas

| Rota GET | Finalidade |
| --- | --- |
| /api/v1/tipos-insumo | Listar tipos para seleção |
| /api/v1/unidades | Listar unidades para seleção |
| /api/v1/tipos-insumo/{codigo} | Consultar tipo específico, inclusive inativo |
| /api/v1/unidades/{codigo} | Consultar unidade específica, inclusive inativa |

Consultas por código não expõem nem exigem o ID interno do catálogo. Códigos e nomes são diferentes: por exemplo, INGREDIENTE / Ingrediente. Não gerar o código a partir do nome na interface.

Listagens aceitam `status=ATIVOS|INATIVOS|TODOS`, padrão ATIVOS. Não confundir INATIVOS de catálogo (booleano ativo) com ARQUIVADOS de insumo (arquivadoEm). Listagem de unidades aceita ainda `dimensao=MASSA|VOLUME|UNIDADE`, omitido para todas. Filtros combinam com AND.

Sem paginação nesta primeira versão porque são catálogos de referência pequenos; retornar lista completa dos filtros em envelope itens. Ordenar por nome crescente e codigo como desempate. Query desconhecida, repetida ou enum inválida retorna 400 com PARAMETRO_INVALIDO, STATUS_INVALIDO ou DIMENSAO_INVALIDA. Não aceitar pagina/tamanhoPagina nesses catálogos.

### Tipos iniciais e exemplo

`GET /api/v1/tipos-insumo` retorna 200:

```json
{
  "itens": [
    {
      "codigo": "EMBALAGEM",
      "nome": "Embalagem",
      "ativo": true
    },
    {
      "codigo": "INGREDIENTE",
      "nome": "Ingrediente",
      "ativo": true
    }
  ]
}
```

Novos tipos disponíveis cadastrados no banco são retornados sem alterar uma enum fixa no contrato de insumos. Consumo/comportamento específico de novos tipos requer desenho próprio se diferir de um insumo comum.

### Unidades iniciais

| Código | Nome | Dimensão | Unidade base | Fator para a base |
| --- | --- | --- | --- | --- |
| g | Grama | MASSA | g | 1 |
| kg | Quilograma | MASSA | g | 1000 |
| ml | Mililitro | VOLUME | ml | 1 |
| l | Litro | VOLUME | ml | 1000 |
| un | Unidade | UNIDADE | un | 1 |

O backend retorna fatorBase como string decimal. Exemplo de `GET /api/v1/unidades?dimensao=MASSA`, HTTP 200:

```json
{
  "itens": [
    {
      "codigo": "g",
      "nome": "Grama",
      "dimensao": "MASSA",
      "unidadeBaseCodigo": "g",
      "fatorBase": "1.000000",
      "ativo": true
    },
    {
      "codigo": "kg",
      "nome": "Quilograma",
      "dimensao": "MASSA",
      "unidadeBaseCodigo": "g",
      "fatorBase": "1000.000000",
      "ativo": true
    }
  ]
}
```

`unidadeBaseCodigo` é derivado da dimensão; o banco já guarda fator_base. Calcular `quantidadeBase = quantidade × fatorBase` apenas dentro da mesma dimensão. kg→g e l→ml são conversões permitidas; g→ml não é permitido automaticamente. Fator/dimensão das unidades existentes não devem ser alterados em seed futura para mudar retroativamente custos.

### Consulta individual e referências inativas

Consulta individual retorna 200 com o objeto correspondente, sem envelope itens, mesmo se ativo=false. Código inexistente ou com capitalização diferente do código canônico: 404 RECURSO_NAO_ENCONTRADO. Código vazio, não representável pela rota ou fora do tamanho do campo usa o tratamento de rota/validação definido; excesso de tamanho num código de rota válido retorna 400 CODIGO_INVALIDO. GET não altera nenhum cadastro.

A interface pode consultar diretamente os códigos atuais de um insumo para mostrar referências inativas. Nos seletores de nova associação, oferecer somente opções ativas. Em edição, uma opção inativa já vinculada deve continuar visível como inativa e poder permanecer sem mudança, conforme PATCH aprovado; não substituí-la pela primeira opção ativa.

Quando um código é desativado após carregar o seletor, o salvamento revalida no banco. Rejeitar nova associação, sem reutilizar cache do cliente como autoridade. Nenhum item inativo deixa de ser considerado no cálculo de relações existentes.

Catálogo filtrado sem resultados retorna 200 com itens vazio. Falha ao consultar catálogo retorna erro técnico no padrão aprovado; interface apresenta falha e permite tentar novamente, sem inventar valores padrão ou substituir seleção existente.

### Verificação futura

| Cenário | Resultado esperado |
| --- | --- |
| Catálogo sem parâmetros | Somente itens ativos |
| Consulta de código inativo existente | 200 com ativo=false |
| Lista com status TODOS | Inclui ativos e inativos |
| Unidades filtradas por MASSA | Somente unidades de massa |
| Código novo de tipo disponível no banco | Aparece no catálogo sem alterar enum da entrada |
| Catálogo sem correspondência | 200 com itens vazio |
| Tipo/unidade desativado depois da seleção | Nova associação rejeitada ao salvar |
| Edição com referência atual inativa | Mostrar referência e permitir preservá-la |
| Fator de kg para g | 1000; fatorBase decimal em string |

## Próxima definição

Contratos iniciais de insumos e catálogos aprovados; FUT-01 preservado. Próxima definição em [contratos-receitas.md](contratos-receitas.md): cadastro da receita base com composição e rendimentos. Essa proposta ainda será revisada. A definição dos contratos não significa implementação pronta; OpenAPI e testes executáveis ainda serão elaborados.
