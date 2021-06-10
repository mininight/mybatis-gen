/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.domain.clazz;

import lombok.Getter;

/**
 * Class mode
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-16 01:35
 */
public enum ClazzMode {

    /**
     * class
     */
    CLASS("class"),

    /**
     * interface
     */
    INTERFACE("interface"),

    /**
     * enum
     */
    ENUM("enum"),

    /**
     * annotation
     */
    ANNOTATION("@interface"),

    /**
     * array
     */
    ARRAY,

    /**
     * generic label
     */
    GENERIC_LABEL,

    /**
     * UNKNOWN
     */
    UNKNOWN,
    ;

    @Getter
    private final String key;

    ClazzMode() {
        this(null);
    }

    ClazzMode(String key) {
        this.key = key;
    }

    public static ClazzMode of(Class<?> clazz) {
        if (clazz.isAnnotation()) {
            return ClazzMode.ANNOTATION;
        }
        if (clazz.isEnum()) {
            return ClazzMode.ENUM;
        }
        if (clazz.isInterface()) {
            return ClazzMode.INTERFACE;
        }
        return ClazzMode.CLASS;
    }

    public boolean isNormalClazz() {
        return this != ARRAY && this != GENERIC_LABEL;
    }

    @Override
    public String toString() {
        return key;
    }
}
