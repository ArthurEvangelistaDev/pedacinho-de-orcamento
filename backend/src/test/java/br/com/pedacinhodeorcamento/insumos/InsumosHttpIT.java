package br.com.pedacinhodeorcamento.insumos;

import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties="spring.datasource.url=jdbc:mysql://127.0.0.1:33306/pedacinho_insumos_test?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true")
class InsumosHttpIT {
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    final JsonMapper json=JsonMapper.builder().build();
    final HttpClient client=HttpClient.newHttpClient();
    record Result(int status, JsonNode body, String contentType, String location) { }

    @BeforeEach void requireIsolatedDatabase() {
        assertThat(jdbc.queryForObject("SELECT DATABASE()",String.class)).isEqualTo("pedacinho_insumos_test");
        cleanup();
    }
    @AfterEach void cleanup() {
        if(!"pedacinho_insumos_test".equals(jdbc.queryForObject("SELECT DATABASE()",String.class))) throw new IllegalStateException("Refusing cleanup outside the isolated test database");
        jdbc.update("DELETE FROM insumos");
        jdbc.update("DELETE FROM tipos_insumo WHERE codigo='DECORACAO'");
        jdbc.update("UPDATE tipos_insumo SET ativo=TRUE");
        jdbc.update("UPDATE unidades SET ativo=TRUE");
    }
    Map<String,Object> valid() {
        var input=new LinkedHashMap<String,Object>();
        input.put("nome","Chocolate blend"); input.put("tipoCodigo","INGREDIENTE");
        input.put("unidadeCompraCodigo","g"); input.put("quantidadeCompra","1000"); input.put("precoCompra","40");
        return input;
    }
    Result send(String path,String method,String body,String contentType) throws Exception {
        var request=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path));
        if (contentType!=null) request.header("Content-Type",contentType);
        request.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body));
        var response=client.send(request.build(),HttpResponse.BodyHandlers.ofString());
        return new Result(response.statusCode(),json.readTree(response.body()),response.headers().firstValue("Content-Type").orElse(""),response.headers().firstValue("Location").orElse(""));
    }
    Result post(Map<String,Object> input) throws Exception { return send("/api/v1/insumos","POST",json.writeValueAsString(input),"application/json"); }
    Result get(String id) throws Exception { return send("/api/v1/insumos/"+id,"GET",null,null); }
    void problem(Result result,int status,String code,String field,String fieldCode) {
        assertThat(result.status()).isEqualTo(status);
        assertThat(result.contentType()).startsWith("application/problem+json");
        assertThat(result.body().get("type").asText()).isEqualTo("about:blank");
        assertThat(result.body().get("status").asInt()).isEqualTo(status);
        assertThat(result.body().get("codigo").asText()).isEqualTo(code);
        assertThat(result.body().get("traceId").asText()).isNotBlank();
        assertThat(result.body().get("instance").asText()).startsWith("/api/v1/insumos");
        if (field!=null) {
            boolean found=false;
            for(var error:result.body().get("erros")) {
                if(error.get("campo").asText().equals(field) && error.get("codigo").asText().equals(fieldCode)) found=true;
            }
            assertThat(found).as("error %s/%s",field,fieldCode).isTrue();
        } else assertThat(result.body().get("erros").size()).isZero();
    }

    @Test void createAndReadPreserveApprovedRepresentationAndPersistData() throws Exception {
        var input=valid(); input.put("nome","  Açúcar cristal  ");
        var created=post(input); var body=created.body();
        assertThat(created.status()).isEqualTo(201);
        assertThat(created.contentType()).startsWith("application/json");
        assertThat(body.get("id").isTextual()).isTrue();
        assertThat(body.get("id").asText()).matches("[1-9][0-9]*");
        assertThat(created.location()).isEqualTo("/api/v1/insumos/"+body.get("id").asText());
        assertThat(body.get("nome").asText()).isEqualTo("Açúcar cristal");
        assertThat(body.get("tipoCodigo").asText()).isEqualTo("INGREDIENTE");
        assertThat(body.get("unidadeCompraCodigo").asText()).isEqualTo("g");
        assertThat(body.get("quantidadeCompra").asText()).isEqualTo("1000.000000");
        assertThat(body.get("quantidadeCompra").isTextual()).isTrue();
        assertThat(body.get("precoCompra").asText()).isEqualTo("40.00");
        assertThat(body.get("precoCompra").isTextual()).isTrue();
        assertThat(body.get("custoUnitario").isTextual()).isTrue();
        assertThat(body.get("custoUnitario").asText()).isEqualTo("0.04");
        assertThat(body.get("unidadeCustoCodigo").asText()).isEqualTo("g");
        assertThat(body.get("situacaoCusto").asText()).isEqualTo("DISPONIVEL");
        assertThat(body.get("pendencias").size()).isZero();
        assertThat(body.get("versao").isTextual()).isTrue();
        assertThat(body.get("versao").asText()).isEqualTo("1");
        assertThat(Instant.parse(body.get("criadoEm").asText())).isEqualTo(Instant.parse(body.get("atualizadoEm").asText()));
        assertThat(body.get("precoAtualizadoEm").asText()).isEqualTo(body.get("criadoEm").asText());
        long id=Long.parseLong(body.get("id").asText());
        var stored=jdbc.queryForMap("SELECT nome,quantidade_compra,preco_compra,versao FROM insumos WHERE id=?",id);
        assertThat(stored.get("nome")).isEqualTo("Açúcar cristal");
        assertThat((java.math.BigDecimal)stored.get("quantidade_compra")).isEqualByComparingTo("1000");
        assertThat((java.math.BigDecimal)stored.get("preco_compra")).isEqualByComparingTo("40");
        assertThat(((Number)stored.get("versao")).longValue()).isEqualTo(1L);
        var before=jdbc.queryForMap("SELECT * FROM insumos WHERE id=?",id);
        var read=get(Long.toString(id));
        assertThat(read.status()).isEqualTo(200);
        for(String field:List.of("id","nome","tipoCodigo","unidadeCompraCodigo","quantidadeCompra","precoCompra","precoAtualizadoEm","custoUnitario","unidadeCustoCodigo","situacaoCusto","pendencias","criadoEm","atualizadoEm","versao")) assertThat(read.body().get(field)).as(field).isEqualTo(body.get(field));
        assertThat(read.body().get("arquivadoEm").isNull()).isTrue();
        assertThat(jdbc.queryForMap("SELECT * FROM insumos WHERE id=?",id)).isEqualTo(before);
    }

    @ParameterizedTest @ValueSource(booleans={false,true})
    void absentOrNullPriceIsUnknownEvenWhenZeroConfirmationIsTrue(boolean omit) throws Exception {
        var input=valid(); if(omit) input.remove("precoCompra"); else input.put("precoCompra",null);
        input.put("confirmarPrecoZero",true);
        var result=post(input);
        assertThat(result.status()).isEqualTo(201);
        assertThat(result.body().get("precoCompra").isNull()).isTrue();
        assertThat(result.body().get("precoAtualizadoEm").isNull()).isTrue();
        assertThat(result.body().get("custoUnitario").isNull()).isTrue();
        assertThat(result.body().get("situacaoCusto").asText()).isEqualTo("INDISPONIVEL");
        assertThat(result.body().get("pendencias").get(0).get("codigo").asText()).isEqualTo("INSUMO_SEM_PRECO");
        assertThat(result.body().get("pendencias").get(0).get("insumoId").asText()).isEqualTo(result.body().get("id").asText());
        assertThat(get(result.body().get("id").asText()).body().get("custoUnitario").isNull()).isTrue();
    }

    @ParameterizedTest @ValueSource(strings={"0","0.0","0.00"})
    void confirmedZeroIsKnownCost(String zero) throws Exception {
        var input=valid(); input.put("precoCompra",zero); input.put("confirmarPrecoZero",true);
        var result=post(input);
        assertThat(result.status()).isEqualTo(201);
        assertThat(result.body().get("precoCompra").asText()).isEqualTo("0.00");
        assertThat(result.body().get("custoUnitario").asText()).isEqualTo("0");
        assertThat(result.body().get("precoAtualizadoEm").isTextual()).isTrue();
        assertThat(result.body().get("situacaoCusto").asText()).isEqualTo("DISPONIVEL");
        assertThat(result.body().get("pendencias").size()).isZero();
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
            Arguments.of("nome",null,"NOME_OBRIGATORIO"),Arguments.of("nome","  ","NOME_OBRIGATORIO"),Arguments.of("nome","x".repeat(151),"NOME_MUITO_LONGO"),
            Arguments.of("tipoCodigo",null,"TIPO_OBRIGATORIO"),Arguments.of("tipoCodigo","","TIPO_OBRIGATORIO"),Arguments.of("tipoCodigo","INEXISTENTE","TIPO_INVALIDO"),Arguments.of("tipoCodigo","ingrediente","TIPO_INVALIDO"),
            Arguments.of("unidadeCompraCodigo",null,"UNIDADE_OBRIGATORIA"),Arguments.of("unidadeCompraCodigo","","UNIDADE_OBRIGATORIA"),Arguments.of("unidadeCompraCodigo","KG","UNIDADE_INVALIDA"),Arguments.of("unidadeCompraCodigo","INEXISTENTE","UNIDADE_INVALIDA"),
            Arguments.of("quantidadeCompra",null,"QUANTIDADE_INVALIDA"),Arguments.of("quantidadeCompra","0","QUANTIDADE_INVALIDA"),Arguments.of("quantidadeCompra","-1","QUANTIDADE_INVALIDA"),Arguments.of("quantidadeCompra","1.0000001","QUANTIDADE_INVALIDA"),Arguments.of("quantidadeCompra","1000000000000","QUANTIDADE_INVALIDA"),Arguments.of("quantidadeCompra","1e3","QUANTIDADE_INVALIDA"),Arguments.of("quantidadeCompra","01","QUANTIDADE_INVALIDA"),Arguments.of("quantidadeCompra","1,5","QUANTIDADE_INVALIDA"),Arguments.of("quantidadeCompra"," 1","QUANTIDADE_INVALIDA"),
            Arguments.of("precoCompra","-1","PRECO_INVALIDO"),Arguments.of("precoCompra","1.001","PRECO_INVALIDO"),Arguments.of("precoCompra","10000000000000000","PRECO_INVALIDO"),Arguments.of("precoCompra","1e3","PRECO_INVALIDO"),Arguments.of("precoCompra","","PRECO_INVALIDO"),
            Arguments.of("precoCompra","0","PRECO_ZERO_REQUER_CONFIRMACAO"),Arguments.of("precoCompra","0.00","PRECO_ZERO_REQUER_CONFIRMACAO"),
            Arguments.of("id","123","CAMPO_NAO_PERMITIDO"),Arguments.of("versao","1","CAMPO_NAO_PERMITIDO"),Arguments.of("custoUnitario","1","CAMPO_NAO_PERMITIDO"));
    }
    @ParameterizedTest @MethodSource("invalidFields")
    void invalidDomainFieldsDoNotPersist(String field,Object value,String code) throws Exception {
        var input=valid(); input.put(field,value);
        problem(post(input),400,"VALIDACAO_FALHOU",field,code);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM insumos",Integer.class)).isZero();
    }

    static Stream<Arguments> wrongTypes() {
        return Stream.of(Arguments.of("nome",10),Arguments.of("quantidadeCompra",1000),Arguments.of("precoCompra",40),Arguments.of("confirmarPrecoZero","true"),Arguments.of("confirmarPrecoZero",null));
    }
    @ParameterizedTest @MethodSource("wrongTypes")
    void wrongJsonTypesAreRequestErrors(String field,Object value) throws Exception {
        var input=valid(); input.put(field,value);
        problem(post(input),400,"REQUISICAO_INVALIDA",field,"FORMATO_INVALIDO");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM insumos",Integer.class)).isZero();
    }

    @ParameterizedTest @ValueSource(strings={"{","[]","null","\"texto\""})
    void malformedOrNonObjectJsonIsRejected(String body) throws Exception {
        problem(send("/api/v1/insumos","POST",body,"application/json"),400,"REQUISICAO_INVALIDA","$","FORMATO_INVALIDO");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM insumos",Integer.class)).isZero();
    }

    @Test void returnsAllDetectableDomainErrors() throws Exception {
        var input=valid(); input.put("nome"," "); input.put("quantidadeCompra","0"); input.put("precoCompra","0");
        var result=post(input);
        problem(result,400,"VALIDACAO_FALHOU","nome","NOME_OBRIGATORIO");
        problem(result,400,"VALIDACAO_FALHOU","quantidadeCompra","QUANTIDADE_INVALIDA");
        problem(result,400,"VALIDACAO_FALHOU","precoCompra","PRECO_ZERO_REQUER_CONFIRMACAO");
        assertThat(result.body().get("erros").size()).isEqualTo(3);
    }

    @Test void unsupportedContentTypeIs415() throws Exception {
        problem(send("/api/v1/insumos","POST","text","text/plain"),415,"TIPO_CONTEUDO_NAO_SUPORTADO",null,null);
    }

    @ParameterizedTest @ValueSource(strings={"0","-1","01","1.0","abc","9223372036854775808"})
    void invalidIdIs400InsteadOf404(String id) throws Exception { problem(get(id),400,"VALIDACAO_FALHOU","id","ID_INVALIDO"); }
    @Test void missingIdIs404() throws Exception { problem(get("9223372036854775807"),404,"RECURSO_NAO_ENCONTRADO",null,null); }

    @Test void referencesMustBeAvailableAndErrorsDoNotLeaveRows() throws Exception {
        jdbc.update("UPDATE tipos_insumo SET ativo=FALSE WHERE codigo='INGREDIENTE'");
        problem(post(valid()),400,"VALIDACAO_FALHOU","tipoCodigo","TIPO_INVALIDO");
        jdbc.update("UPDATE tipos_insumo SET ativo=TRUE");
        jdbc.update("UPDATE unidades SET ativo=FALSE WHERE codigo='g'");
        problem(post(valid()),400,"VALIDACAO_FALHOU","unidadeCompraCodigo","UNIDADE_INVALIDA");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM insumos",Integer.class)).isZero();
    }

    @ParameterizedTest @CsvSource({"nome,NOME_OBRIGATORIO","tipoCodigo,TIPO_OBRIGATORIO","unidadeCompraCodigo,UNIDADE_OBRIGATORIA","quantidadeCompra,QUANTIDADE_INVALIDA"})
    void requiredFieldsCannotBeOmitted(String field,String code) throws Exception {
        var input=valid(); input.remove(field);
        problem(post(input),400,"VALIDACAO_FALHOU",field,code);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM insumos",Integer.class)).isZero();
    }

    @ParameterizedTest @ValueSource(strings={"0","0.0","0.00"})
    void explicitlyFalseDoesNotConfirmZero(String zero) throws Exception {
        var input=valid(); input.put("precoCompra",zero); input.put("confirmarPrecoZero",false);
        problem(post(input),400,"VALIDACAO_FALHOU","precoCompra","PRECO_ZERO_REQUER_CONFIRMACAO");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM insumos",Integer.class)).isZero();
    }

    @Test void zeroConfirmationDoesNotAllowNegativePrice() throws Exception {
        var input=valid(); input.put("precoCompra","-1"); input.put("confirmarPrecoZero",true);
        problem(post(input),400,"VALIDACAO_FALHOU","precoCompra","PRECO_INVALIDO");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM insumos",Integer.class)).isZero();
    }

    @ParameterizedTest @CsvSource({"0.000001,0.01","999999999999.999999,9999999999999999.99"})
    void acceptsInclusiveDecimalBoundariesWithoutTruncating(String quantity,String price) throws Exception {
        var input=valid(); input.put("quantidadeCompra",quantity); input.put("precoCompra",price);
        var result=post(input);
        assertThat(result.status()).isEqualTo(201);
        assertThat(result.body().get("quantidadeCompra").asText()).isEqualTo(quantity);
        assertThat(result.body().get("precoCompra").asText()).isEqualTo(price);
        if(quantity.equals("0.000001")) assertThat(result.body().get("custoUnitario").asText()).isEqualTo("10000");
    }

    @ParameterizedTest @CsvSource({"l,1.5,25.50,ml,0.017","un,10,5,un,0.5"})
    void volumeAndUnitCostsHaveCorrectBaseCode(String unit,String quantity,String price,String base,String cost) throws Exception {
        var input=valid(); input.put("unidadeCompraCodigo",unit); input.put("quantidadeCompra",quantity); input.put("precoCompra",price);
        var result=post(input);
        assertThat(result.status()).isEqualTo(201);
        assertThat(result.body().get("unidadeCustoCodigo").asText()).isEqualTo(base);
        assertThat(result.body().get("custoUnitario").asText()).isEqualTo(cost);
    }

    @Test void catalogTypeIsNotHardcodedAndDuplicateNamesAreAllowed() throws Exception {
        jdbc.update("INSERT INTO tipos_insumo(codigo,nome,ativo) VALUES('DECORACAO','Decoração',TRUE)");
        var input=valid(); input.put("tipoCodigo","DECORACAO");
        var first=post(input); var second=post(input);
        assertThat(first.status()).isEqualTo(201); assertThat(second.status()).isEqualTo(201);
        assertThat(first.body().get("tipoCodigo").asText()).isEqualTo("DECORACAO");
        assertThat(first.body().get("id").asText()).isNotEqualTo(second.body().get("id").asText());
    }

    @Test void kilogramCostUsesBaseUnit() throws Exception {
        var input=valid(); input.put("unidadeCompraCodigo","kg"); input.put("quantidadeCompra","1");
        var result=post(input);
        assertThat(result.status()).isEqualTo(201);
        assertThat(result.body().get("unidadeCustoCodigo").asText()).isEqualTo("g");
        assertThat(result.body().get("custoUnitario").asText()).isEqualTo("0.04");
    }

    @ParameterizedTest @ValueSource(ints={1,150})
    void acceptsInclusiveNameLengthLimits(int length) throws Exception {
        var input=valid(); input.put("nome","x".repeat(length));
        var result=post(input);
        assertThat(result.status()).isEqualTo(201);
        assertThat(result.body().get("nome").asText()).isEqualTo("x".repeat(length));
    }

    @Test void archivedSupplyAndInactiveExistingReferenceRemainReadableWithoutMutation() throws Exception {
        var created=post(valid()); String originalId=created.body().get("id").asText();
        String largeId="9007199254740993";
        jdbc.update("UPDATE insumos SET id=?,arquivado_em='2026-10-20 15:00:00' WHERE id=?",largeId,originalId);
        jdbc.update("UPDATE unidades SET ativo=FALSE WHERE codigo='g'");
        var before=jdbc.queryForMap("SELECT * FROM insumos WHERE id=?",largeId);
        var result=get(largeId);
        assertThat(result.status()).isEqualTo(200);
        assertThat(result.body().get("id").asText()).isEqualTo(largeId);
        assertThat(result.body().get("arquivadoEm").asText()).isEqualTo("2026-10-20T15:00:00Z");
        assertThat(result.body().get("custoUnitario").asText()).isEqualTo("0.04");
        assertThat(jdbc.queryForMap("SELECT * FROM insumos WHERE id=?",largeId)).isEqualTo(before);
    }
}
