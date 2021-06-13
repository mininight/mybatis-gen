package io.light.frame.dal.mybatis.generator.sql.builder.appender;

import io.light.frame.dal.mybatis.generator.core.domain.mapper.MapperFunc;
import io.light.frame.dal.mybatis.generator.sql.builder.SqlBuilder;
import org.apache.commons.lang3.EnumUtils;
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
        if (EnumUtils.getEnum(MapperFunc.Type.class, element.getName().toLowerCase()) != null) {
            return false;
        }
        return canAccept(mapperFunc, element);
    }

    protected abstract boolean canAccept(MapperFunc mapperFunc, Element element);
}
