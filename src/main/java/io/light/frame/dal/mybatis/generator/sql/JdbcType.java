/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.sql;

import io.light.frame.dal.mybatis.generator.domain.clazz.Clazz;
import io.light.frame.dal.mybatis.generator.domain.clazz.ClazzMode;
import io.light.frame.dal.mybatis.generator.sql.meta.entity.TableColumn;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.function.Function;

/**
 * TODO
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-15 08:53
 */
public enum JdbcType {

    /**
     * CHAR
     */
    CHAR(String.class),

    /**
     * VARCHAR
     */
    VARCHAR(String.class, col ->
            "TEXT".equalsIgnoreCase(col.getDataType())
                    || "TINYTEXT".equalsIgnoreCase(col.getDataType())
                    || "MEDIUMTEXT".equalsIgnoreCase(col.getDataType())
                    || "LONGTEXT".equalsIgnoreCase(col.getDataType())
                    || "JSON".equalsIgnoreCase(col.getDataType())
    ),

    /**
     * CLOB
     */
    CLOB(String.class),

    /**
     * INTEGER
     */
    INTEGER(Integer.class, col ->
            "INT".equalsIgnoreCase(col.getDataType())
                    || "TINYINT".equalsIgnoreCase(col.getDataType())
                    || "SMALLINT".equalsIgnoreCase(col.getDataType())
    ),

    /**
     * BIGINT
     */
    BIGINT(Long.class),

    /**
     * DECIMAL
     */
    DECIMAL(BigDecimal.class, col -> "NUMERIC".equalsIgnoreCase(col.getDataType())),

    /**
     * BIT
     */
    BIT(Boolean.class),

    /**
     * TIMESTAMP
     */
    TIMESTAMP(Date.class, col ->
            "DATE".equalsIgnoreCase(col.getDataType())
                    || "DATETIME".equalsIgnoreCase(col.getDataType())
    ),

    /**
     * BINARY
     */
    BINARY(Clazz.of(byte.class.getName(), ClazzMode.ARRAY)),

    /**
     * VARBINARY
     */
    VARBINARY(Clazz.of(byte.class.getName(), ClazzMode.ARRAY)),

    /**
     * BLOB
     */
    BLOB(Clazz.of(byte.class.getName(), ClazzMode.ARRAY), col ->
            "BLOB".equalsIgnoreCase(col.getDataType())
                    || "TINYBLOB".equalsIgnoreCase(col.getDataType())
                    || "MEDIUMBLOB".equalsIgnoreCase(col.getDataType())
                    || "LONGBLOB".equalsIgnoreCase(col.getDataType())
    );

    @Getter
    private final Clazz javaType;

    @Getter
    private final Function<TableColumn, Boolean> acceptFunc;

    JdbcType(Class<?> javaType) {
        this(javaType, null);
    }

    JdbcType(Class<?> javaType, Function<TableColumn, Boolean> acceptFunc) {
        this(Clazz.of(javaType), acceptFunc);
    }

    JdbcType(Clazz javaType) {
        this(javaType, null);
    }

    JdbcType(Clazz javaType, Function<TableColumn, Boolean> acceptFunc) {
        this.javaType = javaType;
        this.acceptFunc = acceptFunc == null ? n -> false : acceptFunc;
    }

    public static JdbcType of(TableColumn column) {
        if (column == null || StringUtils.isBlank(column.getDataType()) || StringUtils.isBlank(column.getColType())) {
            return null;
        }
        String dataType = column.getDataType();
        boolean isCode = StringUtils.isNumeric(dataType);
        if (dataType.startsWith("-") && StringUtils.isNumeric(dataType.substring(1))) {
            isCode = true;
        }
        if (isCode) {
            int code = Integer.parseInt(dataType);
            org.apache.ibatis.type.JdbcType type = org.apache.ibatis.type.JdbcType.forCode(code);
            if (type != null) {
                dataType = type.name();
                column.setDataType(dataType);
            }
        }
        JdbcType[] types = values();
        for (JdbcType jdbcType : types) {
            if (jdbcType.name().equalsIgnoreCase(dataType) || jdbcType.getAcceptFunc().apply(column)) {
                return jdbcType;
            }
        }
        return null;
    }
}
