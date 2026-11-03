package br.com.pedacinhodeorcamento.insumos;

import java.math.BigDecimal;
import java.util.Optional;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.*;

@Entity @Table(name="tipos_insumo")
class TipoInsumo {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false,length=30) String codigo;
    @Column(nullable=false,length=100) String nome;
    @Column(nullable=false) boolean ativo;
    protected TipoInsumo() { }
    public String getCodigo() { return codigo; }
}
@Entity @Table(name="unidades")
class Unidade {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false,length=10) String codigo;
    @Column(nullable=false,length=60) String nome;
    @Column(nullable=false,length=10) String dimensao;
    @Column(name="fator_base",nullable=false,precision=18,scale=6) BigDecimal fatorBase;
    @Column(nullable=false) boolean ativo;
    protected Unidade() { }
    public String getCodigo() { return codigo; }
    public BigDecimal getFatorBase() { return fatorBase; }
    public String baseCodigo() {
        return switch(dimensao) { case "MASSA" -> "g"; case "VOLUME" -> "ml"; case "UNIDADE" -> "un"; default -> throw new IllegalStateException("Dimensão inválida no catálogo"); };
    }
}
interface TipoRepository extends JpaRepository<TipoInsumo,Long> {
    @Lock(LockModeType.PESSIMISTIC_READ) Optional<TipoInsumo> findByCodigo(String codigo);
}
interface UnidadeRepository extends JpaRepository<Unidade,Long> {
    @Lock(LockModeType.PESSIMISTIC_READ) Optional<Unidade> findByCodigo(String codigo);
}
