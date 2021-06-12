/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.core.domain.mapper;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import io.light.frame.dal.mybatis.generator.core.domain.clazz.Clazz;
import io.light.frame.dal.mybatis.generator.exceptions.MybatisGenException;
import io.light.frame.dal.mybatis.generator.sql.JdbcType;
import io.light.frame.dal.mybatis.generator.sql.meta.entity.Table;
import io.light.frame.dal.mybatis.generator.sql.meta.entity.TableColumn;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Table mapper
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-17 09:26
 */
@Getter
@Setter
public class TableMapper {
    public static final Converter<String, String> FIELD_NAME_CAMEL_CONVERTER = CaseFormat.LOWER_UNDERSCORE.converterTo(
            CaseFormat.LOWER_CAMEL);
    private final Table table;
    private final List<Property> properties = new ArrayList<>();
    private final List<Property> primaryKeys = new ArrayList<>();
    private final List<Property> insertProperties = new ArrayList<>();
    private final List<Property> updateProperties = new ArrayList<>();
    private final List<MapperFunc> funcList = new ArrayList<>();
    private Clazz entityClazz;
    private Clazz daoClazz;

    public TableMapper(Table table) {
        table.setName(table.getName().toLowerCase());
        this.table = table;
        List<TableColumn> columns = table.getColumns();
        if (columns == null || columns.isEmpty()) {
            return;
        }
        columns.stream().map(c -> Property.of(table, c)).forEach(property -> {
            properties.add(property);
            insertProperties.add(property);
            updateProperties.add(property);
            if (property.isPrimaryKey()) {
                primaryKeys.add(property);
            }
            insertProperties.removeIf(Property::isInsertIgnore);
            updateProperties.removeIf(Property::isUpdateIgnore);
        });
    }

    @Getter
    @Setter
    public static class Property {
        private final TableColumn column;
        private final String columnName;
        private final String propertyName;
        private final String comment;
        private final Clazz javaType;
        private final JdbcType jdbcType;
        private final boolean primaryKey;
        private boolean insertIgnore;
        private boolean updateIgnore;
        private String tableAlias;
        private String columnAlias;

        private Property(Table table, TableColumn column) {
            column.setName(column.getName().toLowerCase());
            this.column = column;
            this.columnName = column.getName();
            this.comment = column.getComment();
            this.jdbcType = JdbcType.of(column);
            if (this.jdbcType == null) {
                throw new MybatisGenException(String.format("Unsupported jdbc type: %s, table: %s, column: %s",
                        column.getDataType(), table.getName(), column.getName()));
            }
            this.javaType = this.jdbcType.getJavaType();
            this.propertyName = FIELD_NAME_CAMEL_CONVERTER.convert(column.getName());
            this.primaryKey = "pri".equalsIgnoreCase(column.getKey());
        }

        public static Property of(Table table, TableColumn column) {
            Property property = new Property(table, column);
            property.setInsertIgnore(false);
            property.setUpdateIgnore(false);
            String extra = column.getExtra();
            if (extra != null) {
                extra = extra.toLowerCase();
                if (extra.contains("auto_increment")) {
                    property.setInsertIgnore(true);
                    property.setUpdateIgnore(true);
                } else if (extra.contains("default_generated")) {
                    property.setInsertIgnore(true);
                } else if (extra.contains("on update")) {
                    property.setUpdateIgnore(true);
                } // ...
            }
            return property;
        }
    }
}
