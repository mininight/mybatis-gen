/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.core.domain.clazz;

import io.light.frame.dal.mybatis.generator.util.GenToolKit;
import io.light.frame.dal.mybatis.generator.util.StatefulSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;

/**
 * Class field definition
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-16 01:43
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class ClazzField {
    @EqualsAndHashCode.Include
    private final String name;
    private final Clazz type;
    private final StatefulSet<JavaKeyword> keywords = new StatefulSet<>();
    private final StatefulSet<Clazz> annotations = new StatefulSet<>();
    @EqualsAndHashCode.Include
    private Clazz target;
    private String comment;

    public ClazzField(String name, Clazz type, JavaKeyword... keywords) {
        this.name = name;
        this.type = type;
        GenToolKit.fillKeywords(this.keywords, keywords);
    }

    public ClazzField addAnnotations(Class<? extends Annotation>... annotations) {
        GenToolKit.fillAnnotations(this.annotations, annotations);
        return this;
    }

    public ClazzField addAnnotations(Clazz... annotations) {
        GenToolKit.fillAnnotations(this.annotations, annotations);
        return this;
    }
}