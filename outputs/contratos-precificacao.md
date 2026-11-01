# Contratos da API — precificação

Versão: 0.5  
Atualizado: 2026-10-31  
Estado: aprovado em 2026-10-30; sem implementação.

Referências: [spec.md](spec.md), [modelo de dados](modelo-de-dados.md), [convenções da API](contratos-api.md) e [produtos e variações](contratos-produtos.md).

## Objetivo e fronteira

Configurar percentuais globais e personalizações de produto/variação; consultar custos atuais, preço sugerido e resultado estimado no preço praticado. Contrato aprovado em 2026-10-30, após a aprovação da criação de variações. A aprovação não define frontend, ferramentas ou implementação integral.

Confirmados: indiretos e lucro configuráveis, conversão entre margem e acréscimo, preço praticado manual por variação e preservação desse preço quando custos/objetivos mudam. A aprovação desta etapa inclui hierarquia, formatos, limites, arredondamento e comportamento de cada indicador quando faltam dados.

Edição do preço praticado/composição, listagem e arquivamento de produtos terão contratos próprios. Não incluir impostos, taxas, frete, vendas ou histórico. FUT-01/FUT-02 permanecem fora da primeira versão.

## Herança e entradas

Resolver independentemente:

- Indiretos: primeiro percentual não null entre variação, produto e global.
- Lucro: primeiro par completo modo/percentual não null entre variação, produto e global. Nunca misturar modo de um nível com percentual de outro.

`null` em uma personalização significa herdar; `"0"` significa valor próprio de 0%. Informar a origem GLOBAL, PRODUTO ou VARIACAO de cada configuração efetiva. Ausência em todos os níveis produz valor e origem null; não inserir 30%/50% automaticamente.

Exemplo ilustrativo: global com indiretos 30% e margem 50%; produto com margem própria 40%; variação com indiretos próprios 0%. Resultado: indiretos 0% da variação e margem 40% do produto. Retirar a margem própria do produto faz o lucro voltar ao global, preservando os indiretos próprios da variação.

| Entrada | Validação aprovada |
| --- | --- |
| indiretosPercentual | Decimal não negativo, até 6 casas em DECIMAL(18,6) |
| regraLucro | Objeto com modo e percentual, ambos obrigatórios quando o objeto está preenchido |
| regraLucro.modo | MARGEM ou ACRESCIMO, exatamente nesses códigos |
| regraLucro.percentual | Decimal com até 6 casas em DECIMAL(18,6); MARGEM de 0 até 99.999999; ACRESCIMO não negativo |

O limite físico não negativo de DECIMAL(18,6) é 999999999999.999999. Indiretos e acréscimo podem superar 100%, respeitado esse limite. Guardar apenas o modo e percentual informados; derivar o equivalente, sem persistir duas fontes de lucro nem converter repetidamente valores arredondados.

IDs/versões e decimais seguem as strings e a gramática aprovadas. Percentuais persistidos retornam 6 casas; derivados retornam precisão de cálculo, sem notação científica. Campos desconhecidos, inclusive dentro de regraLucro, são rejeitados. Não aceitar custos, origens, preço sugerido ou margem equivalente como entradas.

## Rotas aprovadas

Todas sob `/api/v1`, corpo JSON nas gravações.

| Método / rota | Operação |
| --- | --- |
| GET /configuracao-precificacao | Consultar configuração global |
| PUT /configuracao-precificacao | Criar ou substituir configuração global completa |
| GET /produtos/{produtoId}/precificacao | Consultar personalizações e configurações efetivas do agrupador |
| PATCH /produtos/{produtoId}/precificacao | Alterar personalizações do agrupador |
| GET /produtos/{produtoId}/variacoes/{variacaoId}/precificacao | Consultar personalizações, configurações efetivas e cálculos da variação |
| PATCH /produtos/{produtoId}/variacoes/{variacaoId}/precificacao | Alterar personalizações da variação |

## Configuração global

GET retorna HTTP 200. Se nunca configurada, corpo com configurado false, versao null, atualizadoEm null, indiretosPercentual null e regraLucro null. Isso é um estado normal de configuração inicial.

PUT inicial, com valores ilustrativos:

```json
{
  "versao": null,
  "indiretosPercentual": "30",
  "regraLucro": {
    "modo": "MARGEM",
    "percentual": "50"
  }
}
```

As três propriedades são obrigatórias. Na criação, versao deve ser null explicitamente; no estado já configurado, deve ser a string da versão atual. Percentuais/regraLucro não podem ser null no global. Não há exclusão/reset global neste contrato.

Persistir a linha única com versão inicial "1". Responder HTTP 200 com configurado true, os valores salvos, atualizadoEm UTC e versao. Atualização efetiva incrementa versão uma vez. Valores numericamente iguais com versão atual retornam 200 sem mudar versão/data. Mudança de modo conta como alteração, mesmo com percentual economicamente equivalente.

Duas criações concorrentes não podem se sobrescrever: se já existe configuração e o comando informa versao null, responder 409 CONFLITO_DE_EDICAO. Versão positiva quando ainda não existe também é conflito. Omissão da propriedade versao é 400 VERSAO_OBRIGATORIA; formato inválido usa VERSAO_INVALIDA.

## Personalizações do produto e da variação

PATCH é parcial. Exemplo ilustrativo para retirar a regra própria de lucro e definir indiretos próprios 0%:

```json
{
  "versao": "2",
  "indiretosPercentual": "0",
  "regraLucro": null
}
```

- versao obrigatória: versão atual do produto no PATCH do produto; versão atual da variação no PATCH da variação. Não usar a versão do global como versão desses cadastros.
- Propriedade de configuração omitida preserva o valor próprio atual; null remove a personalização. Objeto regraLucro preenchido substitui o par inteiro.
- Exigir ao menos uma das duas propriedades de configuração; caso contrário, 400 ALTERACAO_VAZIA. precoPraticado não pertence a este PATCH.
- Validar todos os campos e persistir numa transação. Alteração efetiva incrementa somente a versão e atualizadoEm da entidade alterada. Produto não incrementa versões das variações dependentes; global não incrementa versões de produtos/variações.
- Versão antiga retorna 409 mesmo quando o valor solicitado já está salvo. Sem mudança efetiva e com versão atual: 200 sem alterar data/versão.

PATCH retorna 200 com produtoId, variacaoId quando aplicável, versao, atualizadoEm e os dois valores próprios salvos, inclusive null. O cliente consulta GET para obter cálculo e herança atualizados; a gravação não promete congelar preços dos insumos ou configurações superiores.

GET do produto retorna produtoId, versao, atualizadoEm, arquivadoEm, configuracaoPropria e configuracaoEfetiva. A própria contém indiretosPercentual/regraLucro nullable. A efetiva contém indiretos e lucro com seus valores/origens, resolvidos em produto → global. Não existe custo ou preço único do agrupador: não somar preços de suas variações.

## Consulta da variação

GET retorna HTTP 200 com dados de uma leitura consistente: composição, preços de insumos, rendimentos e configurações. Não grava totais nem altera datas/versões. Próxima consulta usa os valores atuais. Versões de produto/variação não representam uma versão de todos os custos.

| Campo | Conteúdo aprovado |
| --- | --- |
| produtoId, variacaoId, versaoProduto, versaoVariacao | Identificação e versões atuais |
| versaoConfiguracaoGlobal | Versão global consultada ou null |
| calculadoEm | Instante UTC do cálculo |
| atualizadoEm, arquivadoEm, produtoArquivadoEm | Datas da variação e estado do agrupador |
| configuracaoPropria | indiretosPercentual e regraLucro da variação, nullable |
| configuracaoEfetiva.indiretos | percentual e origem, ambos nullable |
| configuracaoEfetiva.lucro | modo, percentual, margemPercentual, acrescimoPercentual e origem; null quando não resolvido |
| receitas, insumos | Linhas da composição com IDs, referências, nomes, consumos originais, ordem, estados de arquivamento das referências e custoConsumido nullable |
| custoDiretoUnitario | Soma completa dos consumos por unidade ou null |
| situacaoCusto | DISPONIVEL ou INDISPONIVEL, referente apenas ao custo direto |
| custoIndiretoUnitario, custoTotalEstimadoUnitario | Valores calculados com precisão interna ou null |
| precoSugerido | Valor final em duas casas ou null |
| precoPraticado, precoPraticadoAtualizadoEm | Valor manual em duas casas e sua data, ou null |
| resultadoEstimadoUnitario, margemEfetivaPercentual | Indicadores calculados com precisão interna ou null |
| situacaoPrecificacao | DISPONIVEL se precoSugerido pôde ser calculado, senão INDISPONIVEL; não exige preço praticado |
| pendencias | Causas dos valores indisponíveis; array vazio se nenhuma |
| alertas | RESULTADO_NEGATIVO e/ou ABAIXO_DA_META quando verificáveis; senão array vazio |

Em configuracaoEfetiva.indiretos sem valor resolvido, percentual e origem são null. Quando o lucro existe, preservar seu modo/percentual autoritativo e calcular os dois indicadores equivalentes. Leituras de configurações próprias/efetivas não exigem custo disponível.

Pendências seguem codigo/mensagem e IDs das referências quando aplicáveis. Reutilizar INSUMO_SEM_PRECO e COMPOSICAO_VAZIA; esta última identifica receitaId para receita vazia ou variacaoId para composição vazia da variação. Acrescentar INDIRETOS_NAO_CONFIGURADOS, LUCRO_NAO_CONFIGURADO e PRECO_PRATICADO_NAO_DEFINIDO com variacaoId. Deduplicar por código e identidade da causa; a interface usa códigos, sem interpretar mensagem.

Ausência de preço praticado pode gerar pendência mesmo com situacaoPrecificacao DISPONIVEL: a situação indica disponibilidade do sugerido, e cada indicador depende dos seus próprios dados. Sem objetivo de lucro, custo total e margem efetiva ainda podem ser calculados.

## Cálculo e disponibilidade por indicador

Aplicar indiretos e lucro uma única vez, na variação final; preparos/receitas contribuem com custo direto. Indiretos incidem no custo direto completo, incluindo embalagem. Usar as fórmulas de [spec.md](spec.md), sem arredondar componentes a centavos antes da soma.

| Indicador | Dependências |
| --- | --- |
| Custo direto D | Todos os componentes com custos conhecidos e composição não vazia |
| Custo indireto | D e percentual efetivo de indiretos; D × i / 100 |
| Custo total C | D e indiretos; D + custo indireto |
| Preço sugerido | C e regra de lucro efetiva |
| Resultado estimado | C e preço praticado positivo V; V − C |
| Margem efetiva | C e V; 100 × (V − C) / V |
| RESULTADO_NEGATIVO | C e V disponíveis, com V < C |
| ABAIXO_DA_META | Margem efetiva e margem desejada equivalente disponíveis, com efetiva < desejada |

Com componente sem preço, não apresentar soma parcial como D nem calcular total/sugerido/margem. Custos conhecidos por linha permanecem visíveis. Arquivamento de referências existentes não as retira do cálculo nem é pendência de custo por si só.

Falta de indiretos impede C, sugerido e margem efetiva; não assumir 0%. Falta de lucro impede apenas sugerido/comparação com a meta, se os demais dados forem suficientes. Falta de V impede resultado e margem efetiva, preservando custo e sugerido disponíveis. Não emitir alertas positivos/negativos quando os valores necessários estão indisponíveis.

Com D conhecido igual a zero, C pode ser zero e o sugerido "0.00" quando houver configurações efetivas; V positivo permite margem efetiva de 100%. Isso não autoriza salvar precoPraticado zero. Resultado e margem efetiva negativos são válidos; não aplicar os limites do lucro desejado à margem efetiva.

Arredondamento aprovado: apenas precoSugerido final é arredondado a duas casas, com empate para cima (1.005 → "1.01"). Cálculos e comparações usam valores internos antes da formatação; percentuais exibidos podem ser arredondados pela interface, sem voltar a ser fonte de cálculo.

Limites aprovados: cálculos usam BigDecimal com a política numérica Java de contratos-api.md; ultrapassar o limite de cálculo deixa o indicador afetado e os dependentes null com CALCULO_FORA_DO_LIMITE, incluindo campo com o nome do indicador. Não usar saturação, infinito ou zero substituto. Se o sugerido arredondado ultrapassar 9999999999999999.99, usar precoSugerido null e PRECO_SUGERIDO_FORA_DO_LIMITE com campo precoSugerido, compatível com DECIMAL(18,2). Preservar indicadores independentes calculáveis. Essas pendências retornam 200; não invalidam retroativamente configuração válida ou custo conhecido.

## Exemplo ilustrativo completo

D = R$ 5, indiretos 30% e margem desejada 50%: indiretos R$ 1,50, C = R$ 6,50, acréscimo equivalente 100%, sugerido R$ 13,00. V = R$ 15,00: resultado estimado R$ 8,50 e margem efetiva aproximadamente 56,67%, sem alertas.

Após aumento de custo para D = R$ 6: C = R$ 7,80, sugerido R$ 15,60; V permanece R$ 15,00 e sua data não muda. Margem efetiva 48%, alerta ABAIXO_DA_META. São valores sintéticos, sem padrões percentuais aprovados ou promessa de lucro líquido.

## Erros, concorrência e limites de operação

| Situação | HTTP / código |
| --- | --- |
| Rota com ID inválido | 400 / ID_INVALIDO |
| Produto/variação inexistente ou variação de outro produto | 404 / RECURSO_NAO_ENCONTRADO |
| PATCH em produto arquivado, inclusive por rota de variação | 409 / PRODUTO_ARQUIVADO |
| PATCH de variação arquivada em produto ativo | 409 / VARIACAO_ARQUIVADA |
| Versão ausente/inválida/antiga | 400 / VERSAO_OBRIGATORIA ou VERSAO_INVALIDA; 409 / CONFLITO_DE_EDICAO para antiga |
| Indiretos negativos, escala/sintaxe/limite inválidos ou null no global | 400 / INDIRETOS_INVALIDOS |
| regraLucro incompleta ou null no global | 400 / REGRA_LUCRO_INVALIDA |
| Modo desconhecido / percentual inválido para o modo | 400 / MODO_LUCRO_INVALIDO ou PERCENTUAL_LUCRO_INVALIDO |
| Campo desconhecido ou somente leitura | 400 / CAMPO_NAO_PERMITIDO |
| PATCH sem propriedades de configuração | 400 / ALTERACAO_VAZIA |

GET permite consultar registros arquivados por ID. Arquivamento de produto/opções continua sujeito ao contrato futuro; estas regras delimitam somente a consulta e escrita da precificação nesses estados. Não bloquear consulta porque custo/global/preço praticado faltam.

Aplicar Problem Details aprovado: 400 de domínio com VALIDACAO_FALHOU no topo e erros por campo como regraLucro.percentual; JSON/tipo incompatível usa REQUISICAO_INVALIDA. Content-Type incorreto: 415; falha técnica: 500/503 conforme convenções. Propriedade indiretosPercentual omitida no PUT usa INDIRETOS_INVALIDOS; regraLucro omitida usa REGRA_LUCRO_INVALIDA. Não criar/gravar parcialmente em falha de validação.

Versão e alteração são atômicas. Após perda de resposta, consultar antes de repetir: comando com versão anterior retorna conflito se a gravação ocorreu. Mesmo PUT não deve repetir automaticamente com nova versão sem revisão dos valores atuais. Mudanças globais afetam dependentes por leitura, sem regravar composições/preços. Alterações locais válidas não exigem configuração global; não validar todos os dependentes para aceitar um percentual dentro dos limites.

Acesso permanece no escopo local já planejado; este contrato não define exposição pública, autenticação externa ou integrações. Erros técnicos usam traceId/logs conforme o padrão aprovado. Não há histórico, expiração ou serviço externo novo nesta etapa.

## Verificação futura

Os critérios PRC-01 a PRC-14 de spec.md foram aprovados em 2026-10-30 e permanecem pendentes de implementação. Além deles, verificar:

| Cenário | Resultado esperado |
| --- | --- |
| Global vazio e duas criações concorrentes | Uma configuração versão 1; outra requisição recebe 409 |
| PATCH omite lucro e altera só indiretos | Lucro próprio preservado |
| PATCH remove indiretos próprios | Herança retomada, lucro próprio preservado |
| Percentual 0% próprio | Não herdar do nível superior |
| Acréscimo 300% | Margem equivalente 75%, sem teto artificial de 100% no acréscimo |
| Indiretos ausentes | D disponível; C/sugerido/margem indisponíveis |
| Lucro ausente com C = 13 e V = 20 | Sugerido null, margem efetiva 35% |
| V ausente, C e lucro completos | Sugerido disponível; resultado/margem efetiva null |
| C conhecido zero e V positivo | Sugerido 0.00 com regra completa; margem efetiva 100% |
| Componente pendente ou custo zero conhecido | D null no primeiro caso; zero disponível no segundo |
| Consulta repetida após alteração de custo/global | Valores atuais sem mudar preço manual, sua data ou versões dos dependentes |
| Percentuais com mais de 6 casas / sugerido fora do limite | Entrada rejeitada sem truncar / cálculo pendente, respectivamente |
| Versão atual e mesma configuração própria | 200 sem incremento de versão/data |

## Próximo passo

Precificação e contratos de produto/variações aprovados, incluindo listagem e ciclo de vida. Revisar as premissas gerais ainda propostas em [spec.md](spec.md) antes de definir ambiente e plano de entregas.
