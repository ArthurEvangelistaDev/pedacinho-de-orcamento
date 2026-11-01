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

Cada teste do passo 2 atende aos critérios acima; nenhum teste especulativo. Passo 3 pendente. Critérios funcionais permanecem pendentes até seus testes e a revisão independente.
