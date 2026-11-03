package br.com.pedacinhodeorcamento.api;

import java.util.List;

public class ApiException extends RuntimeException {
    public record ErroCampo(String campo,String codigo,String mensagem) { }
    public final int status;
    public final String codigo;
    public final List<ErroCampo> erros;
    public ApiException(int status,String codigo,List<ErroCampo> erros) {
        super(codigo); this.status=status; this.codigo=codigo; this.erros=List.copyOf(erros);
    }
    public static ApiException validacao(List<ErroCampo> erros) { return new ApiException(400,"VALIDACAO_FALHOU",erros); }
    public static ApiException formato(String campo) { return new ApiException(400,"REQUISICAO_INVALIDA",List.of(new ErroCampo(campo,"FORMATO_INVALIDO","Use o tipo JSON esperado para o campo."))); }
}
