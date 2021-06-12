package io.light.frame.dal.mybatis.generator.sql.builder.auto;

import io.light.frame.dal.mybatis.generator.domain.mapper.MapperFunc;
import io.light.frame.dal.mybatis.generator.sql.builder.SqlBuilder;
import org.dom4j.Element;

/**
 * Auto sql builder
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-12 11:39
 */
public abstract class AutoSqlBuilder implements SqlBuilder {

    @Override
    public final boolean accept(MapperFunc mapperFunc, Element element) {
        if (!mapperFunc.isAutoGen()) {
            return false;
        }
        return canAccept(mapperFunc, element);
    }

    protected abstract boolean canAccept(MapperFunc mapperFunc, Element element);
}
