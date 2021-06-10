/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.sql.builder;

import io.light.frame.dal.mybatis.generator.domain.mapper.TableMapper;
import io.light.frame.dal.mybatis.generator.domain.mapper.MapperFunc;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TODO
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-22 10:20
 */
@Component
public class InsertBuilder implements SqlBuilder {

    private ColumnsBuilder columnsBuilder;

    public InsertBuilder(ColumnsBuilder columnsBuilder) {
        this.columnsBuilder = columnsBuilder;
    }

    @Override
    public boolean accept(Element element) {
        return "insert".equalsIgnoreCase(element.getName());
    }

    @Override
    public void build(StringBuilder builder, Element element, TableMapper mapper, MapperFunc mapperFunc) {
        if (mapperFunc.isAutoGen()) {
            List<TableMapper.Property> properties = mapper.getInsertProperties();
            columnsBuilder.appendInsertBlock(builder, mapper.getTable().getName(), properties);
        }
    }
}
