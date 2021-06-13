/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.sql.builder.appender.columns;

import io.light.frame.dal.mybatis.generator.core.ctx.GenContext;
import io.light.frame.dal.mybatis.generator.core.domain.clazz.Clazz;
import io.light.frame.dal.mybatis.generator.core.domain.mapper.MapperFunc;
import io.light.frame.dal.mybatis.generator.core.domain.mapper.TableMapper;
import io.light.frame.dal.mybatis.generator.exceptions.MybatisGenException;
import io.light.frame.dal.mybatis.generator.sql.builder.appender.SqlAppender;
import io.light.frame.dal.mybatis.generator.sql.meta.MetaAccessor;
import io.light.frame.dal.mybatis.generator.util.GenToolKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Columns appender
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-22 11:03
 */
@Slf4j
@Component
public class ColumnsSqlAppender extends SqlAppender {

    private final MetaAccessor metaAccessor;

    public ColumnsSqlAppender(MetaAccessor metaAccessor) {
        this.metaAccessor = metaAccessor;
    }

    @Override
    public boolean canAccept(MapperFunc mapperFunc, Element element) {
        boolean canAccept = !mapperFunc.isAutoGen() && "columns".equalsIgnoreCase(element.getName());
        if (!canAccept) {
            return false;
        }
        MapperFunc.Type funcType = mapperFunc.getType();
        if (funcType == MapperFunc.Type.delete) {
            throw new MybatisGenException("Unsupported mapper function:" + funcType);
        }
        List<Element> elements = element.getParent().elements("columns");
        if (elements.size() > 1) {
            throw new MybatisGenException("Only one '<columns>' can be used in function: " + funcType);
        }
        return true;
    }

    @Override
    public void build(MapperFunc.ContentBuilder builder, Element element, TableMapper mapper, MapperFunc mapperFunc) {
        Columns columns = GenToolKit.elementAttrsAsObject(element, Columns.class);
        GenContext.current().getVars().put(GenContext.VAR_KEY_SQL_FUNC_COLUMNS, columns);
        List<Columns.Append> appends = columns.getAppends();
        Columns.Scope scope = columns.getScope();
        Clazz entityClazz = mapper.getEntityClazz();
        String thisTableName = mapper.getTable().getName();
        MapperFunc.Type funcType = mapperFunc.getType();
        List<TableMapper.Property> properties = fetchMapperProperties(scope, funcType, mapper, columns);
        if (properties == null || properties.isEmpty()) {
            return;
        }
        if (funcType == MapperFunc.Type.insert || funcType == MapperFunc.Type.update) {
            if (mapperFunc.getParameterClazz() == null || !entityClazz.getName().equals(mapperFunc.getParameterClazz().getName())) {
                return;
            }
        }
        // for insert
        if (funcType == MapperFunc.Type.insert) {
            appendInsertBlock(builder, thisTableName, properties);
            return;
        }
        // for update
        if (funcType == MapperFunc.Type.update) {
            completeSqlPrefix(builder, funcType);
            if (builder.indexOf(thisTableName) < 0) {
                builder.append(" ");
                builder.append(thisTableName);
                builder.append("\n\t\t");
            }
            builder.append("<set>");
            for (TableMapper.Property property : properties) {
                builder.append("\n\t\t\t<if test=\"");
                builder.append(property.getPropertyName()).append(" != null");
                builder.append("\">");
                builder.append("\n\t\t\t\t").append(property.getColumnName());
                builder.append(" = ");
                builder.append("#{").append(property.getPropertyName()).append("},");
                builder.append("\n\t\t\t</if>");
            }
            builder.append("\n\t\t</set>");
            return;
        }
        // for select
        if (funcType == MapperFunc.Type.select) {
            completeSqlPrefix(builder, funcType);
            if (appends != null && !appends.isEmpty()) {
                appends.forEach(appender -> {
                    try {
                        metaAccessor.touch(metaOperation -> {
                            TableMapper tm = new TableMapper(metaOperation.table(mapper.getTable().getSchema(),
                                    appender.getTable()));
                            List<TableMapper.Property> propList = fetchMapperProperties(scope, funcType, tm, appender);
                            if (propList == null || propList.isEmpty()) {
                                return;
                            }
                            properties.addAll(propList);
                        });
                    } catch (Exception e) {
                        GenToolKit.handleException(log, String.format("Touch metadata failed, schema:'%S', table:'%s'",
                                mapper.getTable().getSchema(), appender.getTable()), e);
                    }
                });
            }
            int i = 0;
            int max = properties.size() - 1;
            for (TableMapper.Property property : properties) {
                String tableAlias = property.getTableAlias();
                if (tableAlias != null) {
                    builder.append(tableAlias).append(".");
                }
                builder.append(property.getColumnName());
                if (tableAlias != null) {
                    String colAlias = property.getPropertyName();
                    if (StringUtils.isNotBlank(property.getColumnAlias())) {
                        colAlias = property.getColumnAlias();
                    }
                    builder.append(" as ").append(colAlias);
                }
                if (i < max) {
                    builder.append(", ");
                }
                i++;
                if (i % 3 == 0 && i <= max) {
                    builder.append("\n\t\t\t");
                }
            }
        }
    }

    public void appendInsertBlock(MapperFunc.ContentBuilder builder, String tableName, List<TableMapper.Property> properties) {
        completeSqlPrefix(builder, MapperFunc.Type.insert);
        if (builder.indexOf(tableName) < 0) {
            builder.append(" into ");
            builder.append(tableName);
        }
        builder.append(" (\n\t\t\t");
        AtomicInteger i = new AtomicInteger(0);
        int max = properties.size() - 1;
        properties.forEach(property -> {
            builder.append(property.getColumnName());
            if (i.get() < max) {
                builder.append(", ");
            }
            i.incrementAndGet();
            if (i.get() % 4 == 0 && i.get() <= max) {
                builder.append("\n\t\t\t");
            }
        });
        builder.append("\n\t\t) values (\n");
        i.set(0);
        builder.append("\t\t\t");
        properties.forEach(property -> {
            builder.append("#{").append(property.getPropertyName()).append("}");
            if (i.get() < max) {
                builder.append(", ");
            }
            i.incrementAndGet();
            if (i.get() % 4 == 0 && i.get() <= max) {
                builder.append("\n\t\t\t");
            }
        });
        builder.append("\n\t\t)");
    }

    private List<TableMapper.Property> fetchMapperProperties(Columns.Scope scope, MapperFunc.Type funcType,
                                                             TableMapper mapper, ColumnsNature nature) {
        List<TableMapper.Property> mapperProperties;
        if (scope == null || scope == Columns.Scope.auto) {
            switch (funcType) {
                case insert:
                    mapperProperties = mapper.getInsertProperties();
                    break;
                case update:
                    mapperProperties = mapper.getUpdateProperties();
                    break;
                case select:
                    mapperProperties = mapper.getProperties();
                    break;
                default:
                    mapperProperties = null;
                    break;
            }
        } else {
            mapperProperties = mapper.getProperties();
        }
        if (mapperProperties == null || mapperProperties.isEmpty()) {
            return null;
        }
        List<TableMapper.Property> properties = new ArrayList<>(mapperProperties);
        String includeCols = StringUtils.isBlank(nature.getIncludeColumns()) ? null : nature.getIncludeColumns().trim();
        String excludeCols = StringUtils.isBlank(nature.getExcludeColumns()) ? null : nature.getExcludeColumns().trim();
        if (includeCols != null || excludeCols != null) {
            properties.removeIf(property -> {
                if (includeCols != null) {
                    return !ArrayUtils.contains(includeCols.split(","), property
                            .getColumnName());
                }
                return ArrayUtils.contains(excludeCols.split(","), property
                        .getColumnName());
            });
        }
        Map<String, ColumnAlias> columnAliasMap = nature.getColumnAliasList() == null
                || nature.getColumnAliasList().isEmpty() ? null : nature.getColumnAliasList().stream()
                .collect(Collectors.toMap(ColumnAlias::getColumn, Function.identity(),
                        (o, n) -> o, LinkedHashMap::new));
        properties.forEach(property -> {
            String tableAlias = StringUtils.isBlank(nature.getTableAlias()) ? null : nature.getTableAlias().trim();
            property.setTableAlias(tableAlias);
            if (columnAliasMap == null || columnAliasMap.isEmpty()) {
                return;
            }
            ColumnAlias columnAlias = columnAliasMap.get(property.getColumnName());
            if (columnAlias != null) {
                property.setColumnAlias(columnAlias.getAs());
            }
        });
        return properties;
    }

    private void completeSqlPrefix(MapperFunc.ContentBuilder builder, MapperFunc.Type funcType) {
        if (builder.indexOf(funcType.name()) < 0) {
            builder.append(" ");
            builder.append(funcType.name());
            if (funcType == MapperFunc.Type.select) {
                builder.append("\n\t\t\t");
            }
        }
    }
}
