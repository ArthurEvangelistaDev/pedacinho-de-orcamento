# Primeiro incremento Java: insumos

Especificação única: [outputs/spec.md](../../../outputs/spec.md). Contrato: [contratos-api.md](../../../outputs/contratos-api.md). Autorizado pelo usuário ao confirmar a stack Java e o início do incremento. Frontend, listagem, edição e ciclo de vida não pertencem a este incremento.

## Passos atômicos

1. Registrar a stack Java e adaptar as convenções técnicas. Arquivos: outputs/, README.md, .specs/STATE.md e este plano. Gate: validate_spec.py outputs/spec.md. Commit: docs(stack): adopt java spring boot backend.
2. Criar base executável Spring Boot, Maven Wrapper e migrações de unidades/tipos/insumos. Arquivos: backend/pom.xml, wrapper, aplicação/configuração/migração e scripts locais. Gate: build e teste de schema/seed no MySQL isolado. Commit: build(backend): bootstrap java mysql application.
3. Entregar POST /api/v1/insumos e GET /api/v1/insumos/{id}, com testes dos resultados e erros aprovados. Arquivos: backend/src e evidências deste incremento. Gate: testes unitários e HTTP/MySQL, verify completo. Commit: feat(insumos): create and retrieve supplies.

## Critérios desta entrega

INS-01, INS-05 a INS-07, INS-09 a INS-14 e CON-01/CON-02, mais as convenções aprovadas de transporte/erro da criação e consulta individual. Catálogos são persistidos por seed; rotas de catálogo serão uma entrega própria. Atualização de preços/dependentes e demais critérios seguem pendentes.

## Verificação

Testes de domínio: validação de nome/quantidades/preço/zero e cálculo unitário. Testes HTTP/MySQL: 201/Location, IDs/decimais em strings, preço ausente e zero, GET 200/404/ID inválido, referências e formatos inválidos, campos desconhecidos, falhas sem persistência parcial e dados preservados. Schema/seed: MySQL real em banco local de testes, sem SQLite/H2.

Passo 1 concluído no commit 4e565b1: validate_spec.py --strict retornou 0 erros/0 avisos; check_commit.py aprovou a mensagem. Política Java explícita e decisões anteriores preservadas como histórico.

Passo 2 concluído em 2026-11-01: Maven Wrapper 3.9.16 executou verify com Spring Boot 4.1.2/JDK 25.0.5/MySQL 8.4.13. Migração V1 aplicada; 1 teste de integração passou, 0 falhas/erros/ignorados. Jar executável gerado. Nenhum servidor global alterado; MySQL de teste encerrado pelo script.

| Critério do passo 2 | Evidência em backend/src/test/java/br/com/pedacinhodeorcamento/SchemaIT.java | Resultado esperado |
| --- | --- | --- |
| Banco isolado | linha 14, isEqualTo("pedacinho_insumos_test") | Somente base de teste |
| Tipos iniciais | linha 15, isEqualTo(2) | Ingrediente e embalagem |
| Unidades iniciais | linha 16, isEqualTo(5) | g/kg/ml/l/un |
| Conversão kg | linha 17, isEqualByComparingTo("1000") | Fator base 1000 |
| Tabelas MySQL | linha 18, isEqualTo(3) | Três tabelas em InnoDB |

Cada teste do passo 2 atende aos critérios acima; nenhum teste especulativo. Passo 2 salvo no commit 17a8d02. Passo 3 implementado no disco, com fechamento ainda pendente conforme pausa abaixo. Critérios funcionais permanecem pendentes até o gate e a revisão independente.

## Pausa de 2026-11-01

Usuário interrompeu o trabalho e solicitou salvar onde paramos. Não continuar a implementação até ele retomar.

### Código preservado

- POST /api/v1/insumos e GET /api/v1/insumos/{id}, em backend/src/main/java/br/com/pedacinhodeorcamento/insumos/.
- Validação explícita de tipos JSON e strings decimais, preço ausente, confirmação de preço zero, limites DECIMAL e referências de catálogo; BigDecimal com DECIMAL128.
- Entidades/repositórios JPA e cálculo de custo por unidade base. Referências lazy são acessadas por getters; os dois erros de GET inicialmente encontrados foram corrigidos e o gate posterior de 74 testes passou.
- Tratamento central de Problem Details em backend/src/main/java/br/com/pedacinhodeorcamento/api/, incluindo 400/404/415 e falhas técnicas 500/503.
- Testes NumerosTest, TechnicalErrorsTest e InsumosHttpIT. As classes MySQL fixam o banco isolado antes de Flyway/JPA e verificam a identidade da base antes de limpeza. Não apontar esses testes para dados reais.

### Evidência e limite da evidência

O gate completo anterior terminou com BUILD SUCCESS e 74 testes: 4 unitários/erro técnico e 70 de integração, sem falhas, erros ou ignorados. Depois foram acrescentados três casos de cobertura (unidade inexistente e nomes de comprimento 1/150), com total esperado 77.

Na execução redirecionada seguinte, os XML em backend/target/ mostram 2 NumerosTest, 2 TechnicalErrorsTest, 72 InsumosHttpIT e 1 SchemaIT, todos com 0 falhas/erros/ignorados. Seus horários são 2026-11-01 04:57:19 a 04:57:46. Contudo, o comando externo retornou erro de PowerShell NativeCommandError em um aviso do Mockito; work/toolchain/final-gate.log não contém fechamento BUILD SUCCESS. Esses relatórios não bastam para declarar o gate final concluído.

A correção de test-local.ps1 foi aplicada antes da interrupção: ErrorActionPreference Continue apenas durante chamadas nativas (setup MySQL, Maven, shutdown), preservando saída e o código real de término. A sintaxe foi analisada sem erros na pausa. A tentativa de execução após essa alteração foi interrompida; não há resultado confirmado para ela. Não suprimir avisos, ignorar testes ou trocar assertions para contornar o problema.

### Retomada

1. Conferir git status e as alterações atuais, sem descartar os arquivos não commitados. HEAD: 17a8d02, branch master. Primeiro commit: 4e565b1. Não há remoto.
2. Executar backend/scripts/test-local.ps1 com as ferramentas já baixadas. Para reproduzir a forma redirecionada: executar o script com saída para work/toolchain/retomada-gate.log, guardar LASTEXITCODE, ler o final do log e devolver esse exit code. Confirmar BUILD SUCCESS, 77 testes no total e 0 falhas/erros/ignorados. Se houver divergência, investigar o código/script; não relaxar testes.
3. Atualizar evidências por requisito e status da entrega; o escopo continua INS-01, INS-05 a INS-07, INS-09 a INS-14 e CON-01/CON-02, mais convenções comuns. Não declarar os 168 critérios do app implementados.
4. Fazer o commit atômico do módulo e sua rastreabilidade somente após gate válido. Escrita em .git precisou de aprovação fora do sandbox e safe.directory somente por comando para o caminho canônico; não configurar confiança global.
5. Executar a revisão independente prevista na skill tlc-spec-driven, com sensor em cópia isolada e relatório validation.md. Ainda não houve Verifier ou mutações nesta sessão.

Ferramentas portáteis: work/toolchain/jdk25, maven, maven-user, m2, mysql e python. Python serve somente aos scripts de validação da skill. work/ e target/ são ignorados pelo Git. Credenciais de teste em work/mysql-insumos-test não devem aparecer em logs, commits ou memória.

Na conferência da pausa não foram encontrados processos java/mysqld. O servidor local de teste não é serviço global; somente 127.0.0.1:33306, com dados descartáveis. Nenhuma instalação global de Java/MySQL foi alterada.

O MCP ai-memory não disponibilizou ferramentas de handoff nesta sessão. O registro de continuidade está neste arquivo e em .specs/STATE.md; a especificação única permanece outputs/spec.md.
