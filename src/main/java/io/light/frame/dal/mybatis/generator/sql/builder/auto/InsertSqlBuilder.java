/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.sql.builder.auto;

import io.light.frame.dal.mybatis.generator.domain.mapper.MapperFunc;
import io.light.frame.dal.mybatis.generator.domain.mapper.TableMapper;
import io.light.frame.dal.mybatis.generator.sql.builder.appender.columns.ColumnsSqlAppender;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Insert sql builder
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-22 10:20
 */
@Component
public class InsertSqlBuilder extends AutoSqlBuilder {

    private ColumnsSqlAppender columnsSqlAppender;

    public InsertSqlBuilder(ColumnsSqlAppender columnsSqlAppender) {
        this.columnsSqlAppender = columnsSqlAppender;
    }

    @Override
    public boolean canAccept(MapperFunc mapperFunc, Element element) {
        return "insert".equalsIgnoreCase(element.getName());
    }

    @Override
    public void build(MapperFunc.ContentBuilder builder, Element element, TableMapper mapper, MapperFunc mapperFunc) {
        List<TableMapper.Property> properties = mapper.getInsertProperties();
        columnsSqlAppender.appendInsertBlock(builder, mapper.getTable().getName(), properties);
    }
}
