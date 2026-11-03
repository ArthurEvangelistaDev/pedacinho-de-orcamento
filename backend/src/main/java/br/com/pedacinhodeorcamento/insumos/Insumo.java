package br.com.pedacinhodeorcamento.insumos;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;

@Entity @Table(name="insumos")
class Insumo {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false,length=150) String nome;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="tipo_id") TipoInsumo tipo;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="unidade_compra_id") Unidade unidade;
    @Column(name="quantidade_compra",nullable=false,precision=18,scale=6) BigDecimal quantidade;
    @Column(name="preco_compra",precision=18,scale=2) BigDecimal preco;
    @Column(name="preco_atualizado_em") Instant precoAtualizadoEm;
    @Column(name="criado_em",nullable=false) Instant criadoEm;
    @Column(name="atualizado_em",nullable=false) Instant atualizadoEm;
    @Column(name="arquivado_em") Instant arquivadoEm;
    @Version @Column(nullable=false) long versao=1;
    protected Insumo() { }
    Insumo(InsumoInput input,TipoInsumo tipo,Unidade unidade) {
        this.nome=input.nome(); this.tipo=tipo; this.unidade=unidade;
        this.quantidade=input.quantidadeCompra(); this.preco=input.precoCompra();
        this.criadoEm=Instant.now().truncatedTo(ChronoUnit.MICROS); this.atualizadoEm=criadoEm;
        this.precoAtualizadoEm=preco==null?null:criadoEm;
    }
}
interface InsumoRepository extends JpaRepository<Insumo,Long> { }
