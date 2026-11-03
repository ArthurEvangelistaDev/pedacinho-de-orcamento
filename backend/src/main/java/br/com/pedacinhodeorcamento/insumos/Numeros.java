package br.com.pedacinhodeorcamento.insumos;

import java.math.BigDecimal;
import java.math.MathContext;

final class Numeros {
    private Numeros() { }
    static String custoUnitario(BigDecimal preco,BigDecimal quantidade,BigDecimal fatorBase) {
        return preco.divide(quantidade.multiply(fatorBase),MathContext.DECIMAL128).stripTrailingZeros().toPlainString();
    }
}
