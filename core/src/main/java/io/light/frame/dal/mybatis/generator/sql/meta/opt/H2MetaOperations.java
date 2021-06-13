package io.light.frame.dal.mybatis.generator.sql.meta.opt;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import io.light.frame.dal.mybatis.generator.exceptions.MybatisGenException;
import io.light.frame.dal.mybatis.generator.sql.Dialect;
import io.light.frame.dal.mybatis.generator.sql.meta.entity.Table;
import io.light.frame.dal.mybatis.generator.sql.meta.entity.TableColumn;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * H2 meta operations
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-10 03:18
 */
public class H2MetaOperations extends BaseMetaOperations {

    public H2MetaOperations() {
        super(Dialect.H2);
    }

    @Override
    public void checkDatabase(String database) {
        Integer count = null;
        if (StringUtils.isNotBlank(database)) {
            String sql = "select count(*) from information_schema.schemata where `schema_name`='" + database + "'";
            count = lookupJdbcTemplate().queryForObject(sql, Collections.emptyMap(), Integer.class);
        }
        if (count == null || count < 1) {
            throw new MybatisGenException(String.format("Unknown database '%s'", database));
        }
    }

    @Override
    public String getDefaultDatabase() {
        return lookupJdbcTemplate().queryForObject("select database()", Collections.emptyMap(), String.class);
    }

    @Override
    public Table table(String tableSchema, String tableName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("tableSchema", tableSchema);
        params.put("tableName", tableName);
        //
        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        sql.append("*");
        sql.append(" from ").append("information_schema.tables");
        sql.append(" where ");
        if (StringUtils.isNotBlank(tableSchema)) {
            sql.append("table_catalog=:tableSchema");
            sql.append(" and ");
        }
        sql.append("`table_name`=:tableName");
        List<Table> tables = lookupJdbcTemplate().query(sql.toString(), params, newRowMapper(Table.class,
                this::prettyMapping));
        if (CollectionUtils.isEmpty(tables)) {
            return null;
        }
        Table table = tables.get(0);
        if (!table.isValid()) {
            return null;
        }
        table.setColumns(columns(table.getSchema(), table.getName()));
        return table;
    }

    @Override
    public List<TableColumn> columns(String tableSchema, String tableName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("tableSchema", tableSchema);
        params.put("tableName", tableName);
        //
        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        sql.append("*");
        sql.append(" from ").append("information_schema.columns");
        sql.append(" where ");
        if (StringUtils.isNotBlank(tableSchema)) {
            sql.append("table_catalog=:tableSchema");
            sql.append(" and ");
        }
        sql.append("`table_name`=:tableName");
        List<TableColumn> columns = lookupJdbcTemplate().query(sql.toString(), params, newRowMapper(TableColumn.class,
                this::prettyMapping));
        if (!CollectionUtils.isEmpty(columns)) {
            columns.removeIf(column -> !column.isValid());
        }
        if (CollectionUtils.isEmpty(columns)) {
            return null;
        }
        return columns;
    }

    @Override
    public List<Table> tableList(String tableSchema) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("tableSchema", tableSchema);
        //
        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        sql.append("*");
        sql.append(" from ").append("information_schema.tables");
        if (StringUtils.isNotBlank(tableSchema)) {
            sql.append(" where ");
            sql.append("table_catalog=:tableSchema");
        }
        List<Table> tables = lookupJdbcTemplate().query(sql.toString(), params, newRowMapper(Table.class,
                this::prettyMapping));
        if (!CollectionUtils.isEmpty(tables)) {
            tables.removeIf(table -> !table.isValid());
        }
        if (CollectionUtils.isEmpty(tables)) {
            return null;
        }
        tables.forEach(table -> {
            table.setColumns(columns(table.getSchema(), table.getName()));
        });
        return tables;
    }

    private void prettyMapping(Type type, JSONObject mapping) {
        editMapping(mapping, entry -> {
            String key = entry.getKey();
            if (type == Table.class) {
                if ("remarks".equalsIgnoreCase(key)) {
                    return "table_comment";
                }
                if ("table_catalog".equalsIgnoreCase(key)) {
                    return "table_schema";
                }
            }
            if (type == TableColumn.class) {
                if ("remarks".equalsIgnoreCase(key)) {
                    return "column_comment";
                }
            }
            return key;
        });
    }

}
