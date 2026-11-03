package br.com.pedacinhodeorcamento.api;

import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.HttpMediaTypeNotSupportedException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String,Object>> domain(ApiException error,HttpServletRequest request) {
        return problem(error.status,error.codigo,error.erros,request,null);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String,Object>> malformed(HttpMessageNotReadableException error,HttpServletRequest request) {
        return domain(ApiException.formato("$"),request);
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<Map<String,Object>> media(HttpMediaTypeNotSupportedException error,HttpServletRequest request) {
        return problem(415,"TIPO_CONTEUDO_NAO_SUPORTADO",List.of(),request,null);
    }
    @ExceptionHandler({DataAccessResourceFailureException.class,CannotCreateTransactionException.class})
    ResponseEntity<Map<String,Object>> unavailable(Exception error,HttpServletRequest request) {
        return problem(503,"SERVICO_INDISPONIVEL",List.of(),request,error);
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String,Object>> unexpected(Exception error,HttpServletRequest request) {
        if(error instanceof ErrorResponse frameworkError) {
            int status=frameworkError.getStatusCode().value();
            String code=switch(status) { case 404 -> "RECURSO_NAO_ENCONTRADO"; case 405 -> "METODO_NAO_PERMITIDO"; default -> "REQUISICAO_INVALIDA"; };
            return problem(status,code,List.of(),request,null);
        }
        return problem(500,"ERRO_INTERNO",List.of(),request,error);
    }
    private ResponseEntity<Map<String,Object>> problem(int status,String code,List<ApiException.ErroCampo> errors,HttpServletRequest request,Exception failure) {
        String traceId=UUID.randomUUID().toString();
        if(failure!=null) LoggerFactory.getLogger(ApiExceptionHandler.class).error("Falha da API, traceId={}",traceId,failure);
        var body=new LinkedHashMap<String,Object>();
        body.put("type","about:blank");
        body.put("title",status>=500?"Serviço indisponível":"Requisição inválida");
        body.put("status",status);
        body.put("detail",status>=500?"Não foi possível concluir a operação.":"Revise a requisição e os campos indicados.");
        body.put("instance",request.getRequestURI()); body.put("codigo",code); body.put("erros",errors); body.put("traceId",traceId);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body);
    }
}
