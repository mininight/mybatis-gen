/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.core.domain.clazz;

import io.light.frame.dal.mybatis.generator.util.GenToolKit;
import io.light.frame.dal.mybatis.generator.util.StatefulSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Class method definition
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-16 01:43
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class ClazzMethod {
    public static final Clazz NONE_RETURN_TYPE = Clazz.of(void.class);
    @EqualsAndHashCode.Include
    private final String name;
    private final StatefulSet<JavaKeyword> keywords = new StatefulSet<>();
    @EqualsAndHashCode.Include
    private final StatefulSet<Param> params = new StatefulSet<>();
    private final Clazz returnType;
    private final StatefulSet<Clazz> annotations = new StatefulSet<>();
    private final StatefulSet<Clazz> expectThrows = new StatefulSet<>();
    @EqualsAndHashCode.Include
    private Clazz target;
    private String comment;
    private String returnComment;
    private String throwsComment;
    private String codeBlock;
    private Map<String, Object> ext;

    public ClazzMethod(String name, Class<?> returnType, JavaKeyword... keywords) {
        this(name, Clazz.of(returnType), keywords);
    }

    public ClazzMethod(String name, Clazz returnType, JavaKeyword... keywords) {
        this.name = name;
        this.returnType = returnType == null ? NONE_RETURN_TYPE : returnType;
        GenToolKit.fillKeywords(this.keywords, keywords);
    }

    public ClazzMethod addParam(String paramName, Class<?> paramType) {
        addParam(paramName, Clazz.of(paramType));
        return this;
    }

    public ClazzMethod addParam(String paramName, Clazz paramType) {
        addParam(new Param(paramName, paramType));
        return this;
    }

    public ClazzMethod addParam(Param param) {
        if (param == null) {
            return this;
        }
        param.setMethod(this);
        this.params.add(param);
        return this;
    }

    public ClazzMethod addParams(Param... params) {
        Optional.ofNullable(params).ifPresent(arr -> Arrays.stream(arr)
                .filter(Objects::nonNull).forEach(this::addParam)
        );
        return this;
    }

    public ClazzMethod addAnnotations(Class<? extends Annotation>... annotations) {
        GenToolKit.fillAnnotations(this.annotations, annotations);
        return this;
    }

    public ClazzMethod addAnnotations(Clazz... annotations) {
        GenToolKit.fillAnnotations(this.annotations, annotations);
        return this;
    }

    public ClazzMethod addThrows(Class<? extends Throwable>... throwArr) {
        Optional.ofNullable(throwArr).ifPresent(arr -> Arrays.stream(arr)
                .filter(Objects::nonNull).forEach(c -> this.expectThrows.add(Clazz.of(c)))
        );
        return this;
    }

    public ClazzMethod addThrows(Clazz... throwArr) {
        Optional.ofNullable(throwArr).ifPresent(arr -> Arrays.stream(arr)
                .filter(Objects::nonNull).forEach(this.expectThrows::add)
        );
        return this;
    }

    public boolean getHasReturn() {
        return returnType != null && !NONE_RETURN_TYPE.getName().equals(returnType.getName());
    }

    public Clazz getReturnType() {
        return returnType == null ? NONE_RETURN_TYPE : returnType;
    }

    public String getReturnComment() {
        if (!getHasReturn()) {
            return null;
        }
        returnComment = returnComment == null ? "" : returnComment;
        if (returnType.isPrimitive()) {
            return "{@code " + returnType + "} " + returnComment;
        } else {
            if (returnType.getGenericTypes().isEmpty()) {
                return "{@link " + returnType + "}" + returnComment;
            } else {
                return "{@literal " + returnType + "}" + returnComment;
            }
        }
    }

    public boolean getHasThrows() {
        return !expectThrows.isEmpty();
    }

    public String getThrowsComment() {
        if (!getHasThrows()) {
            return null;
        }
        throwsComment = throwsComment == null ? "" : throwsComment;
        StringBuilder strBuilder = new StringBuilder();
        int i = 0;
        int max = expectThrows.size() - 1;
        for (Clazz t : expectThrows) {
            strBuilder.append(t);
            if (i < max) {
                strBuilder.append(",");
            }
            i++;
        }
        strBuilder.append(" ").append(throwsComment);
        return strBuilder.toString();
    }

    public String getParamsAndBlock() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("(");
        int i = 0;
        int max = params.size() - 1;
        for (Param param : params) {
            strBuilder.append(param);
            if (i < max) {
                strBuilder.append(", ");
            }
            i++;
        }
        strBuilder.append(")");
        if (!expectThrows.isEmpty()) {
            strBuilder.append(" throws ");
            i = 0;
            max = expectThrows.size() - 1;
            for (Clazz t : expectThrows) {
                strBuilder.append(t);
                if (i < max) {
                    strBuilder.append(", ");
                }
                i++;
            }
        }
        if ((target.isInterface() && !keywords.contains(JavaKeyword.DEFAULT)) || keywords.contains(JavaKeyword.ABSTRACT)) {
            strBuilder.append(";");
            return strBuilder.toString();
        }
        codeBlock = !StringUtils.hasLength(codeBlock) ? "{}" : codeBlock;
        strBuilder.append(codeBlock);
        return strBuilder.toString();
    }


    @Getter
    @Setter
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class Param {
        @EqualsAndHashCode.Include
        private final String name;
        private final Clazz type;
        private ClazzMethod method;
        private String comment;
        private StatefulSet<Clazz> annotations = new StatefulSet<>();

        public Param(String name, Clazz type) {
            this.name = name;
            Assert.isTrue(type != null, "Param type cannot be null");
            Assert.isTrue(!NONE_RETURN_TYPE.getName().equals(type.getName()),
                    "Illegal param type :" + type.getName());
            this.type = type;
        }

        public Param addAnnotations(Class<? extends Annotation>... annotations) {
            GenToolKit.fillAnnotations(this.annotations, annotations);
            return this;
        }

        public Param addAnnotations(Clazz... annotations) {
            GenToolKit.fillAnnotations(this.annotations, annotations);
            return this;
        }

        @Override
        public String toString() {
            StringBuilder strBuilder = new StringBuilder();
            for (Clazz ann : annotations) {
                String annName = ann.toString();
                if (!annName.startsWith("@")) {
                    annName = "@" + annName;
                }
                strBuilder.append(annName);
            }
            if (!annotations.isEmpty()) {
                strBuilder.append(" ");
            }
            strBuilder.append(type).append(" ").append(name);
            return strBuilder.toString();
        }
    }
}
