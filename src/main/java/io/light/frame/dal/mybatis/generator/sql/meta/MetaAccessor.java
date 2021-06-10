/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.sql.meta;

import io.light.frame.dal.mybatis.generator.sql.Dialect;
import io.light.frame.dal.mybatis.generator.sql.meta.opt.MetaOperations;
import io.light.frame.dal.mybatis.generator.util.DialectJdbcTemplate;
import org.springframework.beans.factory.InitializingBean;

/**
 * Meta accessor
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-15 06:57
 */
public interface MetaAccessor extends InitializingBean {

    /**
     * Current sql dialect
     *
     * @return {@link Dialect}
     */
    default Dialect dialect() {
        return lookupJdbcTemplate().getDialect();
    }

    /**
     * Lookup jdbc template
     *
     * @return {@link DialectJdbcTemplate}
     */
    DialectJdbcTemplate lookupJdbcTemplate();

    /**
     * Do meta operations
     *
     * @param pipe meta operations pipeline
     */
    default void touch(Pipe<MetaOperations> pipe) {
        touch(null, pipe);
    }

    /**
     * Do meta operations
     *
     * @param datasourceId datasource bean id
     * @param pipe         meta operations pipeline
     */
    void touch(String datasourceId, Pipe<MetaOperations> pipe);

    interface Pipe<T> {
        void accept(T t) throws Exception;
    }
}