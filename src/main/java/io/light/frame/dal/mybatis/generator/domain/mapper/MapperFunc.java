/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.domain.mapper;

import io.light.frame.dal.mybatis.generator.domain.clazz.Clazz;
import io.light.frame.dal.mybatis.generator.domain.clazz.ClazzMethod;
import io.light.frame.dal.mybatis.generator.domain.clazz.ClazzMode;
import io.light.frame.dal.mybatis.generator.util.GenToolKit;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Mapper func
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-20 13:51
 */
@Getter
@Setter
public class MapperFunc {
    public static final String ENTITY_CLAZZ_NAME_ALIAS = "object";
    private final String id;
    private final Clazz entityClazz;
    private final Type type;
    private final Element element;
    private final Map<String, String> attrs;
    private final List<ClazzMethod.Param> params = new ArrayList<>();
    private boolean autoGen;
    private String parameterTypeName;
    private Clazz parameterClazz;
    private String resultTypeName;
    private Clazz resultClazz;
    private boolean returnMany;
    private String comment;
    private String content;

    public MapperFunc(Clazz entityClazz, Type type, Element element) {
        this.entityClazz = entityClazz;
        this.type = type;
        this.element = element;
        // apply attributes
        this.attrs = GenToolKit.elementAttributes(element);
        this.id = this.attrs.get("id");
        parameterTypeName = attrs.get("parameterType");
        resultTypeName = attrs.get("resultType");
        attrs.remove("id");
        attrs.remove("parameterType");
        attrs.remove("resultType");
        try {
            this.returnMany = Boolean.parseBoolean(attrs.get("multi"));
        } catch (Exception e) {
            this.returnMany = false;
        } finally {
            attrs.remove("multi");
        }
        try {
            this.autoGen = Boolean.parseBoolean(attrs.get("autoGen"));
        } catch (Exception e) {
            this.autoGen = false;
        } finally {
            attrs.remove("autoGen");
        }
        // apply params
        applyParams(element);
        // build content
    }

    private void applyParams(Element element) {
        // apply parameter definition by the attribute 'parameterType'
        if (StringUtils.isNotBlank(parameterTypeName)) {
            if (ENTITY_CLAZZ_NAME_ALIAS.equalsIgnoreCase(parameterTypeName) || entityClazz.getName()
                    .equals(parameterTypeName)) {
                this.parameterClazz = entityClazz;
            } else {
                this.parameterClazz = Clazz.of(parameterTypeName);
            }
            String paramName = type == Type.insert || type == Type.update ? "record" : "query";
            ClazzMethod.Param param = new ClazzMethod.Param(paramName, this.parameterClazz);
            params.add(param);
            return;
        }
        // apply parameter definition by the element '<parameters>...</parameters>'
        List<ClazzMethod.Param> paramList = new ArrayList<>();
        List<Node> paramMapNodeList = element.selectNodes("/parameters");
        Element paramMapNode = null;
        if (paramMapNodeList != null && !paramMapNodeList.isEmpty()) {
            paramMapNode = (Element) paramMapNodeList.get(0);
        }
        if (paramMapNode != null) {
            List<Node> paramNodes = paramMapNode.selectNodes("/param");
            if (paramNodes != null && !paramNodes.isEmpty()) {
                for (Node paramNode : paramNodes) {
                    Map<String, String> paramInfo = GenToolKit.elementAttributes((Element) paramNode);
                    String paramName = paramInfo.get("name");
                    String paramType = paramInfo.get("type");
                    if (StringUtils.isBlank(paramName) || StringUtils.isBlank(paramType)) {
                        continue;
                    }
                    if (ENTITY_CLAZZ_NAME_ALIAS.equalsIgnoreCase(paramType)) {
                        paramType = entityClazz.getName();
                    }
                    boolean list;
                    try {
                        list = Boolean.parseBoolean(paramInfo.get("multi"));
                    } catch (Exception e) {
                        list = false;
                    }
                    Clazz paramClazz = entityClazz.getName().equals(paramType) ? entityClazz : Clazz.of(paramType);
                    ClazzMethod.Param param = new ClazzMethod.Param(paramName, list ?
                            Clazz.of(List.class, paramClazz) : paramClazz);
                    paramList.add(param);
                }
            }
        }
        if (!paramList.isEmpty()) {
            params.addAll(paramList);
            if (params.size() == 1) {
                parameterClazz = params.get(0).getType();
            } else {
                params.forEach(param -> {
                    Clazz paramAnnClazz = Clazz.of("@Param(\"" + param.getName() + "\")",
                            ClazzMode.ANNOTATION);
                    paramAnnClazz.getDeclaredClasses().add(Clazz.of("org.apache.ibatis.annotations.Param",
                            ClazzMode.ANNOTATION));
                    param.addAnnotations(paramAnnClazz);
                });
            }
        }
        if ((type == Type.insert || type == Type.update) && StringUtils.isBlank(parameterTypeName)
                && params.isEmpty()) {
            parameterClazz = entityClazz;
            ClazzMethod.Param param = new ClazzMethod.Param("record", parameterClazz);
            params.add(param);
        }
    }

    public void buildContent(BiConsumer<ContentBuilder, Element> appender, Set<String> asCharactersNodes) {
        ContentBuilder builder;
        if (autoGen) {
            builder = new ContentBuilder();
            appender.accept(builder, element);
            content = builder.toString().trim();
            return;
        }
        builder = new ContentBuilder();
        appender.accept(builder, element);
        builder.markPrepared();
        List<Node> contentNodes = element.content();
        Iterator<Node> iter = contentNodes.iterator();
        while (iter.hasNext()) {
            Node node = iter.next();
            String nodeName = node.getName();
            if ("parameters".equalsIgnoreCase(nodeName)) {
                continue;
            }
            if (asCharactersNodes != null && asCharactersNodes.contains(nodeName)) {
                appender.accept(builder, (Element) node);
            } else {
                new StringBuilder().append(node.asXML());
            }
        }
        builder.markCompleted();
        appender.accept(builder, element);
        content = builder.toString().trim();
        while (content.contains("\n\n")) {
            content = content.replaceAll("\\n\\n", "");
        }
    }

    public ClazzMethod asMethod() {
        Clazz returnClazz = null;
        if (StringUtils.isBlank(resultTypeName)) {
            if (type != Type.select) {
                returnClazz = Clazz.of(int.class);
            }
        } else {
            if (ENTITY_CLAZZ_NAME_ALIAS.equalsIgnoreCase(resultTypeName)
                    || entityClazz.getName().equals(resultTypeName)) {
                returnClazz = entityClazz;
            } else {
                returnClazz = Clazz.of(resultTypeName);
            }
            if (returnMany) {
                returnClazz = Clazz.of(List.class, returnClazz);
            }
        }
        if (returnClazz != null && ClazzMethod.NONE_RETURN_TYPE.getName().equals(returnClazz.getName())) {
            returnClazz = null;
        }
        this.resultClazz = returnClazz;
        if (type == Type.select) {
            Assert.notNull(this.resultClazz, String.format("<%s> function missing return type, id:'%s', path:[%s]",
                    type.name(), id, element.getUniquePath()));
        }
        ClazzMethod method = new ClazzMethod(id, this.resultClazz);
        for (ClazzMethod.Param param : params) {
            method.addParam(param);
        }
        method.setComment(comment);
        return method;
    }

    /**
     * Func type
     */
    public enum Type {

        /**
         * insert
         */
        insert,
        /**
         * select
         */
        select,
        /**
         * update
         */
        update,
        /**
         * delete
         */
        delete;

        public static Type of(Element element) {
            String elementName = element.getName();
            if (StringUtils.isBlank(elementName)) {
                return null;
            }
            for (Type type : values()) {
                if (type.name().equalsIgnoreCase(elementName)) {
                    return type;
                }
            }
            return null;
        }
    }

    @Getter
    public static class ContentBuilder {
        private final StringBuilder appender = new StringBuilder();
        private volatile boolean prepared;
        private volatile boolean completed;

        private void markPrepared() {
            this.prepared = true;
        }

        private void markCompleted() {
            this.completed = true;
        }

        public ContentBuilder append(Object obj) {
            appender.append(obj);
            return this;
        }

        public ContentBuilder append(String str) {
            appender.append(str);
            return this;
        }

        public int indexOf(String str) {
            return indexOf(str, 0);
        }

        public int indexOf(String str, int fromIndex) {
            return appender.indexOf(str, fromIndex);
        }

        @Override
        public String toString() {
            return appender.toString();
        }
    }

}
