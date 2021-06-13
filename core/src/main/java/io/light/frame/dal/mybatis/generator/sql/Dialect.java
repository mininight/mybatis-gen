package io.light.frame.dal.mybatis.generator.sql;

/**
 * Sql dialect
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-09 17:30
 */
public enum Dialect {

    /**
     * MYSQL
     */
    MYSQL,

    /**
     * H2
     */
    H2,
    ;

    public static final Dialect DEFAULT = MYSQL;
}
