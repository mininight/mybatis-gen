/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.sql.builder;

import io.light.frame.dal.mybatis.generator.core.domain.mapper.MapperFunc;
import io.light.frame.dal.mybatis.generator.core.domain.mapper.TableMapper;
import org.dom4j.Element;

/**
 * Sql builder
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-22 03:46
 */
public interface SqlBuilder {

    default boolean accept(MapperFunc mapperFunc, Element element) {
        return false;
    }

    void build(MapperFunc.ContentBuilder builder, Element element, TableMapper mapper, MapperFunc mapperFunc);
}
