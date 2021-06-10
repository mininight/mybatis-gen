/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.sql.builder;

import io.light.frame.dal.mybatis.generator.domain.mapper.TableMapper;
import io.light.frame.dal.mybatis.generator.domain.mapper.MapperFunc;
import org.dom4j.Element;

/**
 * TODO
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-22 03:46
 */
public interface SqlBuilder {

    default boolean accept(Element element) {
        return false;
    }

    void build(StringBuilder builder, Element element, TableMapper mapper, MapperFunc mapperFunc);
}
