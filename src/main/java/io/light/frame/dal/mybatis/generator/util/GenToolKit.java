/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.annotation.JSONField;
import io.light.frame.dal.mybatis.generator.cfg.MybatisGenProperties;
import io.light.frame.dal.mybatis.generator.domain.clazz.*;
import io.light.frame.dal.mybatis.generator.domain.mapper.TableMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tool kit
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-16 09:30
 */
@Slf4j
public class GenToolKit implements ApplicationListener<ApplicationPreparedEvent> {

    public static final DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Method PRIMITIVE_CLASS_FETCH_METHOD;

    private static final Map<String, Class<?>> CLASS_ALIAS = new HashMap<>();

    private static final ConcurrentHashMap<Class<?>, Map<String, Class<?>>> BEAN_ATTR_PATH_CLASS_MAP;

    private static ApplicationContext ctx;

    static {
        BEAN_ATTR_PATH_CLASS_MAP = new ConcurrentHashMap<>(1 << 21);
        try {
            PRIMITIVE_CLASS_FETCH_METHOD = Class.class.getDeclaredMethod("getPrimitiveClass", String.class);
            PRIMITIVE_CLASS_FETCH_METHOD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        addClassAlias("Collection", List.class);
        addClassAlias("List", List.class);
        addClassAlias("Set", HashSet.class);
        addClassAlias("Map", HashMap.class);
        addClassAlias("Void", void.class);
        addClassAlias("java.lang.Void", void.class);
    }

    public static ApplicationContext beanFactory() {
        return ctx;
    }

    private static void addClassAlias(String alias, Class<?> c) {
        CLASS_ALIAS.put(alias, c);
        if (!alias.contains(".")) {
            CLASS_ALIAS.put(alias.toLowerCase(), c);
        }
    }

    public static Class<?> findClass(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        Class<?> clazz = CLASS_ALIAS.get(name);
        if (clazz != null) {
            return clazz;
        }
        try {
            clazz = Class.forName(name);
        } catch (Exception e) {
            // skip
        }
        if (clazz == null) {
            try {
                clazz = ((Class<?>) PRIMITIVE_CLASS_FETCH_METHOD.invoke(null, name));
            } catch (Exception e) {
                // skip
            }
        }
        return clazz;
    }

    public static void fillGenerics(Clazz clazz, Clazz... generics) {
        if (generics != null && generics.length == 1) {
            Clazz g = generics[0];
            if (g != null) {
                clazz.getGenericTypes().add(g);
            }
            return;
        }
        Optional.ofNullable(generics).ifPresent(arr -> Arrays.stream(arr)
                .filter(Objects::nonNull)
                .forEach(g -> {
                    clazz.getGenericTypes().add(g);
                })
        );
    }

    public static void fillGenerics(Clazz clazz, Class<?>... generics) {
        if (generics != null && generics.length == 1) {
            Class<?> g = generics[0];
            if (g != null) {
                clazz.getGenericTypes().add(Clazz.of(g));
            }
            return;
        }
        Optional.ofNullable(generics).ifPresent(arr -> Arrays.stream(arr)
                .filter(Objects::nonNull)
                .map(Clazz::of)
                .forEach(g -> {
                    clazz.getGenericTypes().add(g);
                })
        );
    }

    public static void fillFields(Clazz clazz, ClazzField... fields) {
        if (fields != null && fields.length == 1) {
            ClazzField f = fields[0];
            if (f != null) {
                f.setTarget(clazz);
                clazz.getFields().add(f);
            }
            return;
        }
        Optional.ofNullable(fields).ifPresent(arr -> Arrays.stream(arr)
                .filter(Objects::nonNull)
                .forEach(f -> {
                    f.setTarget(clazz);
                    clazz.getFields().add(f);
                })
        );
    }

    public static void fillMethods(Clazz clazz, ClazzMethod... methods) {
        if (methods != null && methods.length == 1) {
            ClazzMethod m = methods[0];
            if (m != null) {
                m.setTarget(clazz);
                clazz.getMethods().add(m);
            }
            return;
        }
        Optional.ofNullable(methods).ifPresent(arr -> Arrays.stream(arr)
                .filter(Objects::nonNull)
                .forEach(m -> {
                    m.setTarget(clazz);
                    clazz.getMethods().add(m);
                })
        );
    }

    public static void fillKeywords(Collection<JavaKeyword> keywordSet, JavaKeyword... keywords) {
        if (keywords != null && keywords.length == 1) {
            JavaKeyword k = keywords[0];
            if (k != null) {
                keywordSet.add(k);
            }
            return;
        }
        Optional.ofNullable(keywords).ifPresent(arr -> Arrays.stream(arr)
                .filter(Objects::nonNull)
                .forEach(keywordSet::add)
        );
    }

    public static void fillAnnotations(Collection<Clazz> annSet, Clazz... annArr) {
        if (annArr != null && annArr.length == 1) {
            Clazz ann = annArr[0];
            if (ann != null && ann.getMode() == ClazzMode.ANNOTATION) {
                annSet.add(ann);
            }
            return;
        }
        Optional.ofNullable(annArr).ifPresent(arr -> Arrays.stream(arr)
                .filter(Objects::nonNull)
                .filter(ann -> ann.getMode() == ClazzMode.ANNOTATION)
                .forEach(annSet::add)
        );
    }

    public static void fillAnnotations(Collection<Clazz> annSet, Class<? extends Annotation>... annArr) {
        if (annArr != null && annArr.length == 1) {
            Class<? extends Annotation> ann = annArr[0];
            if (ann != null) {
                annSet.add(Clazz.of(ann));
            }
            return;
        }
        Optional.ofNullable(annArr).ifPresent(arr -> Arrays.stream(arr)
                .filter(Objects::nonNull)
                .map(Clazz::of)
                .forEach(annSet::add)
        );
    }

    /**
     * Walks a file tree.
     *
     * @param start   the starting file
     * @param visitor the file visitor to invoke for each file
     * @return the starting file
     * @throws SecurityException If the security manager denies access to the starting file.
     * @throws IOException       if an I/O error is thrown by a visitor method
     * @see Files#walkFileTree(Path, FileVisitor)
     */
    public static Path walkFileTree(Path start, FileVisitorFunction visitor) throws IOException {
        return Files.walkFileTree(start, visitor);
    }

    public static File resolveProjectDir(String moduleName) {
        File userDir = new File(System.getProperty("user.dir"));
        if (StringUtils.isBlank(moduleName)) {
            return userDir;
        }
        String module = moduleName.trim();
        if (".".equals(module) || "./".equals(module) || "/".equals(module)) {
            return userDir;
        }
        Path path = userDir.toPath().resolveSibling(module);
        File projectDir = path.toFile();
        if (projectDir.exists()) {
            return projectDir;
        }
        throw new IllegalArgumentException(String.format("Unable resolve module: %s", moduleName));
    }

    public static File resolvePackageDir(File baseDir, String buildPath, String pkg) {
        String resolvePath = buildPath;
        if (!resolvePath.endsWith("/")) {
            resolvePath = buildPath + "/";
        }
        resolvePath = resolvePath + pkg.replaceAll("\\.", "/");
        return baseDir.toPath().resolve(resolvePath).toFile();
    }

    public static Document readXml(File xmlFile) throws Exception {
        try (InputStream in = new FileInputStream(xmlFile)) {
            SAXReader reader = new SAXReader();
            return reader.read(in);
        }
    }

    public static <T> T elementAttrsAsObject(Element element, Class<T> objClass) {
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(objClass);
        if (descriptors.length == 0) {
            return BeanUtils.instantiateClass(objClass);
        } else {
            JSONObject json = elementAttrsAsJson(element);
            if (json.isEmpty()) {
                return BeanUtils.instantiateClass(objClass);
            }
            Map<String, Class<?>> classMap = fetchBeanAttrPathTypeMap(objClass);
            classMap.forEach((key, type) -> {
                Object jsonValue = JSONPath.eval(json, key);
                if (jsonValue instanceof List && !(type.isArray() || Collection.class.isAssignableFrom(type))) {
                    List<?> items = (List<?>) jsonValue;
                    Object attrValue = items.get(0);
                    JSONPath.set(json, key, attrValue);
                }
            });
            return json.toJavaObject(objClass);
        }
    }

    private static JSONObject elementAttrsAsJson(Element element) {
        JSONObject json = new JSONObject();
        if (element.attributes() != null) {
            element.attributes().forEach(attribute -> {
                String attrName = attribute.getName();
                if (StringUtils.isNotBlank(attrName)) {
                    json.put(attrName, attribute.getValue());
                }
            });
        }
        if (element.elements() != null) {
            Iterator<Element> iterator = element.elementIterator();
            while (iterator.hasNext()) {
                Element child = iterator.next();
                String attrName = child.getName();
                JSONArray subAttrList = (JSONArray) json.computeIfAbsent(attrName,
                        k -> new JSONArray());
                subAttrList.add(elementAttrsAsJson(child));
                json.put(attrName, subAttrList);
            }
        }
        return json;
    }

    private static Map<String, Class<?>> fetchBeanAttrPathTypeMap(Class<?> beanClass) {
        Map<String, Class<?>> classMap = BEAN_ATTR_PATH_CLASS_MAP.get(beanClass);
        if (classMap != null) {
            return classMap;
        } else {
            classMap = new HashMap<>();
        }
        Map<Class<?>, AtomicInteger> levelLimit = new HashMap<>();
        fillUpBeanAttrPathTypeMap("$", beanClass, classMap, levelLimit);
        BEAN_ATTR_PATH_CLASS_MAP.putIfAbsent(beanClass, classMap);
        return BEAN_ATTR_PATH_CLASS_MAP.get(beanClass);
    }

    private static void fillUpBeanAttrPathTypeMap(String basePath, Class<?> beanClass, Map<String, Class<?>> result,
                                                  Map<Class<?>, AtomicInteger> levelLimit) {
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(beanClass);
        if (descriptors.length == 0) {
            return;
        }
        AtomicInteger level = levelLimit.computeIfAbsent(beanClass, k -> new AtomicInteger(0));
        if (level.incrementAndGet() > 5) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (PropertyDescriptor descriptor : descriptors) {
            builder.setLength(0);
            String propName = descriptor.getName();
            Field field = ReflectionUtils.findField(beanClass, propName);
            if (field == null) {
                continue;
            }
            JSONField jsonField = field.getAnnotation(JSONField.class);
            if (jsonField != null) {
                propName = StringUtils.isNotBlank(jsonField.name()) ? jsonField.name() : propName;
            }
            builder.append(basePath).append(".").append(propName);
            String curPath = builder.toString();
            Class<?> propType = descriptor.getPropertyType();
            result.put(curPath, propType);
            if (propType == Object.class || BeanUtils.isSimpleValueType(propType)) {
                continue;
            }
            if (propType.isArray() || Map.class.isAssignableFrom(propType) || Collection.class.isAssignableFrom(propType)) {
                continue;
            }
            fillUpBeanAttrPathTypeMap(curPath, propType, result, levelLimit);
        }
    }

    public static Map<String, String> elementAttributes(Element element) {
        Map<String, String> attrs = new LinkedHashMap<>();
        if (element == null || element.attributes() == null || element.attributes().isEmpty()) {
            return attrs;
        }
        element.attributes().forEach(attribute -> {
            String attrName = attribute.getName();
            if (StringUtils.isNotBlank(attrName)) {
                attrs.put(attrName, attribute.getValue());
            }
        });
        return attrs;
    }

    @SuppressWarnings("unchecked")
    public static void createJavaFile(File outDir, MybatisGenProperties.GeneratorCfg config, Clazz clazz,
                                      Clazz... classes) throws Exception {
        if (clazz == null && (classes == null || classes.length == 0)) {
            return;
        }
        VelocityContext context = new VelocityContext();
        context.put("author", System.getProperty("user.name"));
        context.put("company", System.getProperty("user.name"));
        BeanMap.create(config).forEach((k, v) -> {
            if (v == null || !(k instanceof String)) {
                return;
            }
            context.put((String) k, v);
        });
        if (clazz != null) {
            createJavaFile(outDir, context, clazz);
        }
        if (classes == null || classes.length == 0) {
            return;
        }
        for (Clazz c : classes) {
            createJavaFile(outDir, context, c);
        }
    }

    public static void createJavaFile(File outDir, VelocityContext context, Clazz clazz) throws Exception {
        Assert.notNull(outDir, "Java files out dir need to specify");
        outDir.mkdirs();
        String fileName = clazz.getSimpleName();
        if (fileName.contains("<")) {
            fileName = fileName.substring(0, fileName.indexOf("<"));
        }
        File javaFile = outDir.toPath().resolve(fileName + ".java").toFile();
        try (FileWriter writer = new FileWriter(javaFile)) {
            context.put("today", LocalDateTime.now());
            context.put("todayStr", LocalDateTime.now().format(DEFAULT_DATE_FORMAT));
            context.put("clazz", clazz);
            VelocityEngineHelper.mergeTemplate("clazz.vm", "UTF-8", context, writer);
        }
    }

    @SuppressWarnings("unchecked")
    public static void createMapperXml(File outDir, MybatisGenProperties.GeneratorCfg config, TableMapper mapper,
                                       TableMapper... mappers) throws Exception {
        if (mapper == null && (mappers == null || mappers.length == 0)) {
            return;
        }
        VelocityContext context = new VelocityContext();
        context.put("author", System.getProperty("user.name"));
        context.put("company", System.getProperty("user.name"));
        BeanMap.create(config).forEach((k, v) -> {
            if (v == null || !(k instanceof String)) {
                return;
            }
            context.put((String) k, v);
        });
        if (mapper != null) {
            createMapperXml(outDir, context, mapper);
        }
        if (mappers == null || mappers.length == 0) {
            return;
        }
        for (TableMapper m : mappers) {
            createMapperXml(outDir, context, m);
        }
    }

    public static void createMapperXml(File outDir, VelocityContext context, TableMapper mapper) throws Exception {
        Assert.notNull(outDir, "Mapper xml files out dir need to specify");
        outDir.mkdirs();
        File outFile = outDir.toPath().resolve(mapper.getDaoClazz().getSimpleName() + ".xml").toFile();
        try (FileWriter writer = new FileWriter(outFile)) {
            context.put("today", LocalDateTime.now());
            context.put("todayStr", LocalDateTime.now().format(DEFAULT_DATE_FORMAT));
            context.put("mapper", mapper);
            VelocityEngineHelper.mergeTemplate("mapper.vm", "UTF-8", context, writer);
        }
    }

    public static Document newSampleDesignXml(MybatisGenProperties.GeneratorCfg config, TableMapper mapper)
            throws Exception {
        File userDir = new File(System.getProperty("user.dir"));
        Path designPath = userDir.toPath().resolve(config.getDesignDir());
        File outFile = designPath.resolve(mapper.getTable().getName() + ".xml").toFile();
        VelocityContext context = new VelocityContext();
        context.put("mapper", mapper);
        try (FileWriter writer = new FileWriter(outFile)) {
            VelocityEngineHelper.mergeTemplate("design_sample.vm", "UTF-8", context, writer);
        }
        return readXml(outFile);
    }

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        GenToolKit.ctx = event.getApplicationContext();
    }
}
