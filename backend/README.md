# Backend Java

Java 25, Spring Boot 4.1.2, Spring Data JPA/Hibernate, Flyway e MySQL. Maven Wrapper fixa a distribuição do build. A API escuta somente em 127.0.0.1.

## Executar

Configurar JDK 25 e um banco MySQL 8.4 vazio destinado ao aplicativo. Informar APP_DB_URL, APP_DB_USER e APP_DB_PASSWORD no ambiente do processo. Não usar credenciais em arquivos versionados.

```powershell
$env:JAVA_HOME = 'C:/caminho/para/jdk-25'
$env:APP_DB_URL = 'jdbc:mysql://127.0.0.1:3306/pedacinho?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true'
$env:APP_DB_USER = 'usuario_do_aplicativo'
# Defina APP_DB_PASSWORD no ambiente, sem gravar a senha neste documento.
./mvnw.cmd spring-boot:run
```

Flyway cria somente catálogos e insumos neste incremento. Hibernate valida o schema; não usa create-drop/update na base do aplicativo. Antes da adoção com dados reais, a entrega de backup/restauração ainda precisa ser concluída.

## Verificação

`./mvnw.cmd test` executa testes unitários. Com banco de teste configurado nas variáveis APP_DB_*, `./mvnw.cmd -Pmysql-integration verify` executa os testes HTTP/SQL e empacota o backend. A base de integração deve se chamar pedacinho_insumos_test; ela contém somente dados descartáveis de testes.

Nesta máquina, ferramentas portáteis aprovadas ficam em work/toolchain. A partir da raiz do projeto:

```powershell
./backend/scripts/test-local.ps1
```

O script inicia um MySQL isolado em 127.0.0.1:33306, guarda credenciais de teste em work/ (ignorado pelo Git), executa verify e encerra somente o processo que iniciou. Recusa porta já ocupada. Não toca em um servidor MySQL preexistente nem altera instalações/variáveis globais.
