/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.domain.clazz;

import io.light.frame.dal.mybatis.generator.util.GenToolKit;
import io.light.frame.dal.mybatis.generator.util.StatefulSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Class definition
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-15 08:35
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Clazz {
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("^[A-Za-z.]+$");
    private final String pkg;
    @EqualsAndHashCode.Include
    private final String name;
    private final String simpleName;
    private final ClazzMode mode;
    private final StatefulSet<Clazz> interfaces = new StatefulSet<>();
    private final StatefulSet<JavaKeyword> keywords = new StatefulSet<>();
    private final StatefulSet<Clazz> genericTypes = new StatefulSet<>();
    private final StatefulSet<ClazzField> fields = new StatefulSet<>();
    private final StatefulSet<ClazzMethod> methods = new StatefulSet<>();
    private final StatefulSet<Clazz> annotations = new StatefulSet<>();
    private final StatefulSet<Clazz> declaredClasses = new StatefulSet<>();
    private final StatefulSet<Clazz> imports = new StatefulSet<>();
    private final boolean primitive;
    private String module;
    private Clazz superClazz;
    private String comment;

    public Clazz(String pkg, String simpleName, ClazzMode mode, JavaKeyword... keywords) {
        this(StringUtils.isNotBlank(pkg) ? pkg + "." + simpleName : simpleName, mode, (Clazz[]) null);
        GenToolKit.fillKeywords(this.keywords, keywords);
    }

    private Clazz(Class<?> actualClass) {
        this(actualClass, (Clazz[]) null);
    }

    private Clazz(Class<?> actualClass, Class<?>... genericTypes) {
        this(actualClass, (Clazz[]) null);
        addGenerics(genericTypes);
    }

    private Clazz(Class<?> actualClass, Clazz... genericTypes) {
        this.name = actualClass.getName();
        this.pkg = this.name.contains(".") ? this.name.substring(0, this.name.lastIndexOf(".")) : null;
        this.simpleName = actualClass.getSimpleName();
        this.primitive = actualClass.isPrimitive();
        this.mode = ClazzMode.of(actualClass);
        addGenerics(genericTypes);
    }

    private Clazz(String fullName, ClazzMode mode) {
        this(fullName, mode, (Clazz[]) null);
    }

    private Clazz(String fullName, ClazzMode mode, JavaKeyword... keywords) {
        this(fullName, mode, (Clazz[]) null);
        GenToolKit.fillKeywords(this.keywords, keywords);
    }

    private Clazz(String fullName, ClazzMode mode, Class<?>... genericTypes) {
        this(fullName, mode, (Clazz[]) null);
        addGenerics(genericTypes);
    }

    private Clazz(String fullName, ClazzMode mode, Clazz... genericTypes) {
        Assert.hasText(fullName, "Clazz name cannot be empty");
        fullName = fullName == null ? "" : fullName.trim();
        if (fullName.startsWith("@")) {
            mode = ClazzMode.ANNOTATION;
        }
        if (mode != ClazzMode.ANNOTATION) {
            Assert.isTrue(CLASS_NAME_PATTERN.matcher(fullName).matches(), "Illegal clazz name");
        } else {
            if (fullName.startsWith("@")) {
                fullName = fullName.substring(1);
            }
        }
        if (mode == ClazzMode.GENERIC_LABEL) {
            Assert.isTrue(fullName.length() == 1, "Illegal generic label: " + fullName);
            char c = fullName.charAt(0);
            Assert.isTrue(c == '?' || (c >= 'A' && c <= 'Z'), "Illegal generic label: " + fullName);
        }
        Class<?> actualClass = null;
        if (mode.isNormalClazz()) {
            try {
                actualClass = GenToolKit.findClass(fullName);
            } catch (Exception e) {
                // skip
            }
        }
        this.name = actualClass != null ? actualClass.getName() : fullName;
        Assert.hasText(this.name, "Illegal clazz name: " + fullName);
        if (actualClass != null) {
            this.primitive = actualClass.isPrimitive();
            this.mode = ClazzMode.of(actualClass);
        } else {
            this.primitive = false;
            this.mode = mode;
        }
        this.pkg = this.name.contains(".") ? this.name.substring(0, this.name.lastIndexOf(".")) : null;
        this.simpleName = this.name.contains(".") ? this.name.substring(this.name.lastIndexOf(".") + 1) : this.name;
        addGenerics(genericTypes);
    }

    public static Clazz of(Class<?> actualClass) {
        return new Clazz(actualClass);
    }

    public static Clazz of(Class<?> actualClass, Clazz... genericTypes) {
        return new Clazz(actualClass, genericTypes);
    }

    public static Clazz of(Class<?> actualClass, Class<?>... genericTypes) {
        return new Clazz(actualClass, genericTypes);
    }

    public static Clazz of(String fullName) {
        return new Clazz(fullName, ClazzMode.UNKNOWN);
    }

    public static Clazz of(String fullName, ClazzMode mode) {
        return new Clazz(fullName, mode);
    }

    public static Clazz of(String fullName, ClazzMode mode, Clazz... genericTypes) {
        return new Clazz(fullName, mode);
    }

    public static Clazz of(String fullName, ClazzMode mode, Class<?>... genericTypes) {
        return new Clazz(fullName, mode);
    }

    public Clazz addGenerics(Class<?>... generics) {
        GenToolKit.fillGenerics(this, generics);
        return this;
    }

    public Clazz addGenerics(Clazz... generics) {
        GenToolKit.fillGenerics(this, generics);
        return this;
    }

    public Clazz addFields(ClazzField... fields) {
        GenToolKit.fillFields(this, fields);
        return this;
    }

    public Clazz addMethods(ClazzMethod... methods) {
        GenToolKit.fillMethods(this, methods);
        return this;
    }

    public Clazz addAnnotations(Class<? extends Annotation>... annotations) {
        GenToolKit.fillAnnotations(this.annotations, annotations);
        return this;
    }

    public Clazz addAnnotations(Clazz... annotations) {
        GenToolKit.fillAnnotations(this.annotations, annotations);
        return this;
    }

    private void addImport(ClazzField field) {
        addImport(field.getType());
        field.getAnnotations().forEach(this::addImport);
    }

    private void addImport(ClazzMethod method) {
        method.getParams().forEach(this::addImport);
        addImport(method.getReturnType());
        method.getAnnotations().forEach(this::addImport);
    }

    private void addImport(ClazzMethod.Param methodParam) {
        addImport(methodParam.getType());
        methodParam.getAnnotations().forEach(this::addImport);
    }

    public Clazz addImport(Class<?> clazz) {
        addImport(Clazz.of(clazz));
        return this;
    }

    public Clazz addImport(Clazz clazz) {
        if (clazz == null || clazz.isPrimitive()) {
            return this;
        }
        if (!clazz.getGenericTypes().isEmpty()) {
            clazz.getGenericTypes().forEach(this::addImport);
        }
        if (!clazz.getDeclaredClasses().isEmpty()) {
            clazz.getDeclaredClasses().forEach(this::addImport);
        }
        String className = clazz.getName();
        if (clazz.getMode() == ClazzMode.GENERIC_LABEL || className.startsWith("java.lang.")
                || Objects.equals(pkg, clazz.getPkg()) || className.startsWith("@")
                || className.contains("(")) {
            return this;
        }
        Class<?> arrItemType = GenToolKit.findClass(clazz.getName());
        if (arrItemType != null && arrItemType.isPrimitive()) {
            return this;
        }
        imports.add(clazz);
        return this;
    }

    public boolean complete() {
        if (imports.isCompleted()) {
            return false;
        }
        if (superClazz != null) {
            addImport(superClazz);
        }
        if (!interfaces.isEmpty()) {
            interfaces.forEach(this::addImport);
        }
        annotations.forEach(this::addImport);
        fields.forEach(field -> {
            if (field == null) {
                return;
            }
            Assert.notNull(field.getName(), String.format("Class '%s.%s' missing field name", pkg, name));
            Assert.isTrue(field.getType() != null, String.format("Field '%s.%s#%s' missing java type",
                    pkg, name, field.getName()));
            addImport(field);
        });
        methods.forEach(method -> {
            if (method == null) {
                return;
            }
            Assert.notNull(method.getName(), String.format("Class '%s.%s' missing method name", pkg, name));
            addImport(method);
        });
        StatefulSet.markCompleted(this);
        return true;
    }

    public boolean isInterface() {
        return mode == ClazzMode.INTERFACE;
    }

    public boolean isArray() {
        return mode == ClazzMode.ARRAY;
    }

    public String getGenericName() {
        if (mode == ClazzMode.ANNOTATION || (!isArray() && genericTypes.isEmpty())) {
            return simpleName;
        }
        StringBuilder builder = new StringBuilder(simpleName);
        if (!genericTypes.isEmpty()) {
            builder.append("<");
            int i = 0;
            int max = genericTypes.size() - 1;
            for (Clazz t : genericTypes) {
                builder.append(t);
                if (t.getMode() == ClazzMode.GENERIC_LABEL && t.getSuperClazz() != null) {
                    builder.append(" extends ").append(t.getSuperClazz());
                }
                if (i < max) {
                    builder.append(", ");
                }
                i++;
            }
            builder.append(">");
        }
        if (mode == ClazzMode.ARRAY) {
            builder.append("[]");
        }
        return builder.toString();
    }

    public void setModule(String module) {
        if (mode.isNormalClazz()) {
            this.module = module;
        }
    }

    public void setComment(String comment) {
        if (mode.isNormalClazz()) {
            this.comment = comment;
        }
    }

    public Clazz copy() {
        Clazz c = new Clazz(pkg, simpleName, mode, keywords.toArray(new JavaKeyword[0]));
        c.setModule(getModule());
        c.setComment(getComment());
        c.setSuperClazz(this.superClazz);
        this.interfaces.forEach(itf -> c.getInterfaces().add(itf));
        this.fields.forEach(c::addFields);
        this.methods.forEach(c::addMethods);
        this.annotations.forEach(c::addAnnotations);
        this.genericTypes.forEach(c::addGenerics);
        this.imports.forEach(c::addImport);
        return c;
    }

    public Clazz copyAsArray() {
        Clazz c = Clazz.of(name, ClazzMode.ARRAY);
        this.genericTypes.forEach(c::addGenerics);
        return c;
    }

    @Override
    public String toString() {
        return getGenericName();
    }

}
