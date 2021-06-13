package io.light.frame.dal.mybatis.generator.util;

import io.light.frame.dal.mybatis.generator.sql.Dialect;
import lombok.Getter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * Dialect jdbc template
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-09 17:33
 */
@Getter
public class DialectJdbcTemplate extends NamedParameterJdbcTemplate {

    private final Dialect dialect;

    public DialectJdbcTemplate(Dialect dialect, DataSource dataSource) {
        super(dataSource);
        this.dialect = Objects.requireNonNull(dialect, "Missing datasource dialect");
    }
}
