package br.com.pedacinhodeorcamento.insumos;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class NumerosTest {
    @Test void recurringCostUsesApprovedJavaPrecisionWithoutRoundingToCents() {
        assertThat(Numeros.custoUnitario(new BigDecimal("1"),new BigDecimal("3"),BigDecimal.ONE))
            .isEqualTo("0.3333333333333333333333333333333333");
    }
    @Test void literConsumptionConvertsToMilliliters() {
        assertThat(Numeros.custoUnitario(new BigDecimal("25.50"),new BigDecimal("1.5"),new BigDecimal("1000")))
            .isEqualTo("0.017");
    }
}
