package io.light.frame.dal.mybatis.generator.sql.builder.appender;

import io.light.frame.dal.mybatis.generator.domain.mapper.MapperFunc;
import io.light.frame.dal.mybatis.generator.sql.builder.SqlBuilder;
import org.dom4j.Element;

/**
 * Sql appender
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-12 11:38
 */
public abstract class SqlAppender implements SqlBuilder {

    @Override
    public final boolean accept(MapperFunc mapperFunc, Element element) {
        if (mapperFunc.isAutoGen()) {
            return false;
        }
        return canAccept(mapperFunc, element);
    }

    protected abstract boolean canAccept(MapperFunc mapperFunc, Element element);
}
