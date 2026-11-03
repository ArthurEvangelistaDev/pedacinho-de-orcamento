package br.com.pedacinhodeorcamento;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties="spring.datasource.url=jdbc:mysql://127.0.0.1:33306/pedacinho_insumos_test?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true")
class SchemaIT {
    @Autowired JdbcTemplate jdbc;

    @Test void schemaUsesMysqlAndApprovedCatalogs() {
        assertThat(jdbc.queryForObject("SELECT DATABASE()", String.class)).isEqualTo("pedacinho_insumos_test");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tipos_insumo", Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM unidades", Integer.class)).isEqualTo(5);
        assertThat(jdbc.queryForObject("SELECT fator_base FROM unidades WHERE codigo='kg'", java.math.BigDecimal.class)).isEqualByComparingTo("1000");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('insumos','tipos_insumo','unidades') AND ENGINE='InnoDB'", Integer.class)).isEqualTo(3);
    }
}
