package br.com.pedacinhodeorcamento.insumos;

import java.util.Map;
import br.com.pedacinhodeorcamento.api.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

class TechnicalErrorsTest {
    @Test void unavailableDatabaseUses503WithoutExposingDetails() throws Exception {
        verifyFailure(new DataAccessResourceFailureException("SQL internal password=do-not-expose"),503,"SERVICO_INDISPONIVEL");
    }
    @Test void unexpectedFailureUses500WithoutExposingDetails() throws Exception {
        verifyFailure(new IllegalStateException("SQL internal password=do-not-expose"),500,"ERRO_INTERNO");
    }
    void verifyFailure(RuntimeException failure,int status,String code) throws Exception {
        var service=new InsumoService(null,null,null) {
            @Override public Map<String,Object> buscar(long id) { throw failure; }
        };
        var mvc=MockMvcBuilders.standaloneSetup(new InsumoController(service)).setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(get("/api/v1/insumos/1"))
            .andExpect(status().is(status))
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.status").value(status))
            .andExpect(jsonPath("$.codigo").value(code))
            .andExpect(jsonPath("$.erros").isEmpty())
            .andExpect(jsonPath("$.traceId").isNotEmpty())
            .andExpect(content().string(not(containsString("do-not-expose"))))
            .andExpect(content().string(not(containsString("SQL internal"))));
    }
}
