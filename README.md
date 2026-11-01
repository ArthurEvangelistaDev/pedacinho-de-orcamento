# Pedacinho de Orçamento

Aplicação de gestão de receitas e precificação de confeitaria. Primeiro incremento do backend em desenvolvimento.

Pasta canônica: `C:/Users/arthu/OneDrive/Documentos/Projetos/pedacinhoDeOrcamento`.

- Especificação e decisões: [outputs/spec.md](outputs/spec.md).
- Modelo de dados: [outputs/modelo-de-dados.md](outputs/modelo-de-dados.md).
- Contratos principais aprovados: [insumos e convenções](outputs/contratos-api.md), [receitas](outputs/contratos-receitas.md), [produtos e variações](outputs/contratos-produtos.md) e [precificação](outputs/contratos-precificacao.md).
- Retomada e próximo passo: [.specs/STATE.md](.specs/STATE.md).

Stack aprovada: Java 25, Spring Boot 4.1, Maven Wrapper, Spring Data JPA/Hibernate e MySQL. Backend em [backend/](backend/). Frontend será tratado em entrega posterior.

Uso inicial aprovado: interface em português, valores em BRL, dados no MySQL como fonte principal e acesso pelo navegador somente no próprio computador, sem login. A planilha fica reservada para futura importação validada, sem sincronização; formato e execução dessa importação continuam pendentes.

Ordem aprovada: validar o fluxo com cadastros manuais e dados de teste, disponibilizar backup/restauração local completo antes da adoção com dados reais e importar a planilha depois, com prévia/validação. Backup manual em destino escolhido pelo usuário; formatos e procedimentos ainda serão definidos.

O nome da pasta não altera as coordenadas internas da memória: `default/controle-receitas-precificacao`, preservadas em `.ai-memory.toml`.

A pasta anterior em `C:/Users/arthu/Documents/Codex/2026-09-11/eu-x20` é um backup da transferência realizada em 2026-10-30. As próximas edições devem ser feitas nesta nova pasta.
