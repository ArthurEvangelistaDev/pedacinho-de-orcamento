package br.com.pedacinhodeorcamento.insumos;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import br.com.pedacinhodeorcamento.api.ApiException;
import br.com.pedacinhodeorcamento.api.ApiException.ErroCampo;

record InsumoInput(String nome,String tipoCodigo,String unidadeCompraCodigo,BigDecimal quantidadeCompra,BigDecimal precoCompra) {
    private static final Set<String> CAMPOS=Set.of("nome","tipoCodigo","unidadeCompraCodigo","quantidadeCompra","precoCompra","confirmarPrecoZero");
    private static final Pattern DECIMAL=Pattern.compile("-?(0|[1-9][0-9]*)(\\.[0-9]+)?");
    static InsumoInput parse(Map<String,Object> body) {
        if(body==null) throw ApiException.formato("$");
        var errors=new ArrayList<ErroCampo>();
        for(String field:body.keySet()) if(!CAMPOS.contains(field)) errors.add(new ErroCampo(field,"CAMPO_NAO_PERMITIDO","Campo não permitido neste comando."));
        String nome=string(body,"nome"); if(nome!=null) nome=nome.strip();
        String tipo=string(body,"tipoCodigo"), unidade=string(body,"unidadeCompraCodigo");
        if(nome==null || nome.isBlank()) errors.add(new ErroCampo("nome","NOME_OBRIGATORIO","Informe o nome do insumo."));
        else if(nome.length()>150) errors.add(new ErroCampo("nome","NOME_MUITO_LONGO","Use até 150 caracteres."));
        if(tipo==null || tipo.isBlank()) errors.add(new ErroCampo("tipoCodigo","TIPO_OBRIGATORIO","Informe o código do tipo."));
        if(unidade==null || unidade.isBlank()) errors.add(new ErroCampo("unidadeCompraCodigo","UNIDADE_OBRIGATORIA","Informe o código da unidade."));
        BigDecimal quantidade=decimal(body,"quantidadeCompra",6,new BigDecimal("999999999999.999999"),false,"QUANTIDADE_INVALIDA",errors);
        BigDecimal preco=decimal(body,"precoCompra",2,new BigDecimal("9999999999999999.99"),true,"PRECO_INVALIDO",errors);
        boolean confirmed=false;
        if(body.containsKey("confirmarPrecoZero")) {
            if(!(body.get("confirmarPrecoZero") instanceof Boolean value)) throw ApiException.formato("confirmarPrecoZero");
            confirmed=value;
        }
        if(preco!=null && preco.signum()==0 && !confirmed) errors.add(new ErroCampo("precoCompra","PRECO_ZERO_REQUER_CONFIRMACAO","Confirme que o insumo não teve custo."));
        if(!errors.isEmpty()) throw ApiException.validacao(errors);
        return new InsumoInput(nome,tipo,unidade,quantidade,preco);
    }
    private static String string(Map<String,Object> body,String field) {
        Object value=body.get(field);
        if(value==null) return null;
        if(!(value instanceof String text)) throw ApiException.formato(field);
        return text;
    }
    private static BigDecimal decimal(Map<String,Object> body,String field,int scale,BigDecimal max,boolean optional,String code,List<ErroCampo> errors) {
        String text=string(body,field);
        if(optional && text==null) return null;
        BigDecimal value=null;
        if(text!=null && text.length()<=40 && DECIMAL.matcher(text).matches()) value=new BigDecimal(text);
        if(value==null || value.scale()>scale || value.signum()<0 || (!optional && value.signum()==0) || value.compareTo(max)>0) {
            errors.add(new ErroCampo(field,code,"Informe um valor dentro do formato, escala e limites permitidos."));
            return null;
        }
        return value;
    }
}
