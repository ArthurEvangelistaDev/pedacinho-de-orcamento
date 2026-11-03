package br.com.pedacinhodeorcamento.insumos;

import java.util.*;
import br.com.pedacinhodeorcamento.api.ApiException;
import br.com.pedacinhodeorcamento.api.ApiException.ErroCampo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class InsumoService {
    private final InsumoRepository insumos;
    private final TipoRepository tipos;
    private final UnidadeRepository unidades;
    InsumoService(InsumoRepository insumos,TipoRepository tipos,UnidadeRepository unidades) { this.insumos=insumos; this.tipos=tipos; this.unidades=unidades; }

    @Transactional
    public Map<String,Object> criar(Map<String,Object> body) {
        InsumoInput input=InsumoInput.parse(body);
        var errors=new ArrayList<ErroCampo>();
        var tipo=tipos.findByCodigo(input.tipoCodigo()).filter(item->item.ativo && item.codigo.equals(input.tipoCodigo())).orElse(null);
        var unidade=unidades.findByCodigo(input.unidadeCompraCodigo()).filter(item->item.ativo && item.codigo.equals(input.unidadeCompraCodigo())).orElse(null);
        if(tipo==null) errors.add(new ErroCampo("tipoCodigo","TIPO_INVALIDO","Tipo inexistente ou indisponível."));
        if(unidade==null) errors.add(new ErroCampo("unidadeCompraCodigo","UNIDADE_INVALIDA","Unidade inexistente ou indisponível."));
        if(!errors.isEmpty()) throw ApiException.validacao(errors);
        return representar(insumos.saveAndFlush(new Insumo(input,tipo,unidade)),false);
    }
    @Transactional(readOnly=true)
    public Map<String,Object> buscar(long id) {
        return representar(insumos.findById(id).orElseThrow(()->new ApiException(404,"RECURSO_NAO_ENCONTRADO",List.of())),true);
    }
    private Map<String,Object> representar(Insumo item,boolean includeArchive) {
        var body=new LinkedHashMap<String,Object>();
        String id=Long.toString(item.id);
        body.put("id",id); body.put("nome",item.nome); body.put("tipoCodigo",item.tipo.getCodigo()); body.put("unidadeCompraCodigo",item.unidade.getCodigo());
        body.put("quantidadeCompra",item.quantidade.setScale(6).toPlainString());
        body.put("precoCompra",item.preco==null?null:item.preco.setScale(2).toPlainString());
        body.put("precoAtualizadoEm",item.precoAtualizadoEm==null?null:item.precoAtualizadoEm.toString());
        body.put("custoUnitario",item.preco==null?null:Numeros.custoUnitario(item.preco,item.quantidade,item.unidade.getFatorBase()));
        body.put("unidadeCustoCodigo",item.unidade.baseCodigo());
        body.put("situacaoCusto",item.preco==null?"INDISPONIVEL":"DISPONIVEL");
        body.put("pendencias",item.preco==null?List.of(Map.of("codigo","INSUMO_SEM_PRECO","insumoId",id,"mensagem","Informe o preço de compra do "+item.nome+".")):List.of());
        body.put("criadoEm",item.criadoEm.toString()); body.put("atualizadoEm",item.atualizadoEm.toString()); body.put("versao",Long.toString(item.versao));
        if(includeArchive) body.put("arquivadoEm",item.arquivadoEm==null?null:item.arquivadoEm.toString());
        return body;
    }
}
