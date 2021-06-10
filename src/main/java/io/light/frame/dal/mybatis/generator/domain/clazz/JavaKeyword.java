/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.domain.clazz;

/**
 * Java keywords
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-16 05:26
 */
public enum JavaKeyword {

    /**
     * private
     */
    PRIVATE,

    /**
     * protected
     */
    PROTECTED,

    /**
     * public
     */
    PUBLIC,

    /**
     * synchronized
     */
    SYNCHRONIZED,

    /**
     * static
     */
    STATIC,

    /**
     * final
     */
    FINAL,

    /**
     * transient
     */
    TRANSIENT,

    /**
     * abstract
     */
    ABSTRACT,

    /**
     * default
     */
    DEFAULT,

    ;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
