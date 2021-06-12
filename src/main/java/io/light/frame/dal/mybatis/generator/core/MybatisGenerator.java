package io.light.frame.dal.mybatis.generator.core;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.light.frame.dal.mybatis.generator.core.cfg.MybatisGenProperties;
import io.light.frame.dal.mybatis.generator.core.ctx.GenContext;
import io.light.frame.dal.mybatis.generator.core.ctx.listener.GenDaoListener;
import io.light.frame.dal.mybatis.generator.core.ctx.listener.GenEntityListener;
import io.light.frame.dal.mybatis.generator.core.ctx.listener.GenMapperXmlListener;
import io.light.frame.dal.mybatis.generator.core.domain.DesignXml;
import io.light.frame.dal.mybatis.generator.core.domain.clazz.Clazz;
import io.light.frame.dal.mybatis.generator.core.domain.clazz.ClazzField;
import io.light.frame.dal.mybatis.generator.core.domain.clazz.ClazzMode;
import io.light.frame.dal.mybatis.generator.core.domain.clazz.JavaKeyword;
import io.light.frame.dal.mybatis.generator.core.domain.mapper.MapperFunc;
import io.light.frame.dal.mybatis.generator.core.domain.mapper.TableMapper;
import io.light.frame.dal.mybatis.generator.exceptions.MybatisGenException;
import io.light.frame.dal.mybatis.generator.sql.Dialect;
import io.light.frame.dal.mybatis.generator.sql.builder.SqlBuilder;
import io.light.frame.dal.mybatis.generator.sql.meta.MetaAccessor;
import io.light.frame.dal.mybatis.generator.sql.meta.entity.Table;
import io.light.frame.dal.mybatis.generator.sql.meta.opt.MetaOperations;
import io.light.frame.dal.mybatis.generator.util.GenToolKit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mybatis generator
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-23 11:27
 */
@Slf4j
public class MybatisGenerator implements ApplicationListener<ContextRefreshedEvent> {

    public static final Converter<String, String> TABLE_NAME_CAMEL_CONVERTER = CaseFormat.LOWER_UNDERSCORE
            .converterTo(CaseFormat.UPPER_CAMEL);

    private final MybatisGenProperties mybatisGenProperties;

    private final String curModuleName;

    private final Set<String> asCharactersNodes = Sets.newHashSet("columns");

    /**
     * TODO reload able
     */
    private final Map<String, Map<String, DesignXml>> designXmlPool = new ConcurrentHashMap<>();

    @Autowired
    private MetaAccessor metaAccessor;

    @Autowired
    private List<SqlBuilder> sqlBuilders;

    @Autowired(required = false)
    private List<GenEntityListener> entityListeners;

    @Autowired(required = false)
    private List<GenDaoListener> daoListeners;

    @Autowired(required = false)
    private List<GenMapperXmlListener> mapperXmlListeners;

    public MybatisGenerator(MybatisGenProperties mybatisGenProperties) {
        this.mybatisGenProperties = mybatisGenProperties;
        String curProjectDir = System.getProperty("user.dir");
        curProjectDir = curProjectDir.replaceAll("\\\\", "/");
        if (curProjectDir.endsWith("/")) {
            curProjectDir = curProjectDir.substring(0, curProjectDir.length() - 1);
        }
        this.curModuleName = curProjectDir.substring(curProjectDir.lastIndexOf("/") + 1);
    }

    public void process(String tableName) throws Exception {
        process(curModuleName, tableName);
    }

    public void process(String module, String tableName) throws Exception {
        process(module, null, tableName);
    }

    public void process(String module, String scheme, String tableName) throws Exception {
        if (StringUtils.isBlank(module)) {
            module = curModuleName;
        }
        MybatisGenProperties.GeneratorCfg config = mybatisGenProperties.getGenerators().get(module);
        Assert.notNull(config, String.format("There is no configuration for the module '%s'", module));
        if (scheme == null) {
            scheme = config.getDefaultScheme();
        }
        Assert.hasText(scheme, String.format("The module '%s' does not specify a database or scheme", module));
        String schemeName = scheme;
        String moduleName = module;
        metaAccessor.touch(config.getDatasourceBeanId(), metaOperations -> {
            metaOperations.checkDatabase(schemeName);
            // step1: Resolve project dir
            File projectDir = GenToolKit.resolveProjectDir(moduleName);
            // step2: Get table metadata
            Table table = metaOperations.table(schemeName, tableName);
            Assert.notNull(table, String.format("Table '%s' not found, dialect: %s, datasource:'%s'", tableName,
                    config.getDialect() == null ? Dialect.DEFAULT : config.getDialect(), config.getDatasourceBeanId()));
            // step3: Create table mapper
            TableMapper tableMapper = new TableMapper(table);
            // step4: Read design xml and prepare context
            DesignXml designXml = designXmlPool.computeIfAbsent(moduleName, k -> new ConcurrentHashMap<>())
                    .get(schemeName + "@" + tableName);
            if (designXml == null) {
                designXml = GenToolKit.newSampleDesignXml(config, tableMapper);
            }
            Document designDoc = designXml.getDoc();
            GenContext context = new GenContext(config, moduleName, projectDir, designXml, tableMapper);
            if (log.isInfoEnabled()) {
                log.info("Processing generation design => {}", designXml.getFile().getAbsolutePath());
                log.info("Using datasource bean '{}'", config.getDatasourceBeanId());
                log.info("Detail => Scheme: {}, Table: {}, Module: {}, Project: {}", schemeName, tableName,
                        moduleName, projectDir.getAbsolutePath());
            }
            Element mapperElement = designDoc.getRootElement();
            // step5: Prepare entity class
            Clazz entityClazz = prepareEntityClazz(config.getEntityPackage(), context);
            // step6: Prepare dao class
            Clazz daoClazz = prepareDaoClazz(config.getDaoPackage(), context, mapperElement);
            // step7: Gen entity + dao
            File entityOutDir = GenToolKit.resolvePackageDir(projectDir, config.getJavaBuildPath(),
                    entityClazz.getPkg());
            File daoOutDir = GenToolKit.resolvePackageDir(projectDir, config.getJavaBuildPath(),
                    daoClazz.getPkg());
            File entityFile = GenToolKit.createJavaFile(entityOutDir, config, entityClazz);
            if (!CollectionUtils.isEmpty(entityListeners)) {
                entityListeners.forEach(l -> l.afterGenerated(context, entityClazz, entityFile));
            }
            File daoFile = GenToolKit.createJavaFile(daoOutDir, config, daoClazz);
            if (!CollectionUtils.isEmpty(daoListeners)) {
                daoListeners.forEach(l -> l.afterGenerated(context, daoClazz, daoFile));
            }
            // step8: Gen mapper xml
            File xmlOutDir = GenToolKit.resolvePackageDir(projectDir, config.getResourceBuildPath(),
                    config.getMapperXmlPackage());
            File mapperXml = GenToolKit.createMapperXml(xmlOutDir, config, tableMapper);
            if (!CollectionUtils.isEmpty(mapperXmlListeners)) {
                mapperXmlListeners.forEach(l -> l.afterGenerated(context, mapperXml));
            }
            if (log.isInfoEnabled()) {
                log.info("Generated SUCCESS");
                log.info("Entity file: {}", entityFile.getAbsolutePath());
                log.info("Dao file: {}", daoFile.getAbsolutePath());
                log.info("Mapper xml: {}", mapperXml.getAbsolutePath());
            }
        });
    }

    protected Clazz prepareEntityClazz(String pkg, GenContext context) {
        TableMapper tableMapper = context.getTableMapper();
        Table table = tableMapper.getTable();
        String className = TABLE_NAME_CAMEL_CONVERTER.convert(table.getName());
        String classComment = table.getComment();
        if (classComment != null) {
            classComment = classComment.replaceAll("[\\t\\n\\r]", ";");
            if (classComment.endsWith("\u8868")) {
                classComment = classComment.substring(0, classComment.indexOf("\u8868"));
            }
        }
        Clazz entityClazz = new Clazz(pkg, className, ClazzMode.CLASS, JavaKeyword.PUBLIC);
        entityClazz.setComment(classComment);
        entityClazz.getInterfaces().add(Clazz.of(Serializable.class));
        entityClazz.addAnnotations(Getter.class, Setter.class);
        tableMapper.getProperties().forEach(property -> {
            String comment = property.getColumn().getComment();
            if (comment != null) {
                comment = comment.replaceAll("[\\t\\n\\r]", ";");
            }
            ClazzField field = new ClazzField(property.getPropertyName(), property.getJavaType(), JavaKeyword.PRIVATE);
            field.setComment(comment);
            entityClazz.addFields(field);
        });
        tableMapper.setEntityClazz(entityClazz);
        if (!CollectionUtils.isEmpty(entityListeners)) {
            entityListeners.forEach(listener -> listener.onReady(context, entityClazz));
        }
        entityClazz.complete();
        return entityClazz;
    }

    protected Clazz prepareDaoClazz(String pkg, GenContext context, Element mapperElement) {
        TableMapper tableMapper = context.getTableMapper();
        // fill mapper`s sql functions
        fillMapperFunctions(context, mapperElement);
        // do prepare
        Clazz entityClazz = tableMapper.getEntityClazz();
        String daoName = entityClazz.getSimpleName() + "Dao";
        String classComment = entityClazz.getComment();
        if (classComment != null) {
            classComment += " Dao";
            classComment = classComment.replaceAll("[\\t\\n\\r]", ";");
        }
        Clazz daoClazz = new Clazz(pkg, daoName, ClazzMode.INTERFACE, JavaKeyword.PUBLIC);
        daoClazz.setComment(classComment);
        daoClazz.addAnnotations(Clazz.of("org.apache.ibatis.annotations.Mapper", ClazzMode.ANNOTATION));
        tableMapper.getFuncList().stream().map(MapperFunc::asMethod).forEach(daoClazz::addMethods);
        tableMapper.setDaoClazz(daoClazz);
        if (!CollectionUtils.isEmpty(daoListeners)) {
            daoListeners.forEach(listener -> listener.onReady(context, entityClazz));
        }
        daoClazz.complete();
        return daoClazz;
    }

    protected void fillMapperFunctions(GenContext context, Element mapperElement) {
        TableMapper mapper = context.getTableMapper();
        Clazz entityClazz = mapper.getEntityClazz();
        Iterator<Node> iter = mapperElement.nodeIterator();
        List<String> comments = new ArrayList<>();
        while (iter.hasNext()) {
            Node curNode = iter.next();
            if (!(curNode instanceof Element)) {
                if (curNode.getNodeType() == Node.COMMENT_NODE && curNode.getText() != null) {
                    String comment = curNode.getText().trim();
                    if (StringUtils.isNotBlank(comment) && !comment.startsWith("!!")) {
                        String[] cmnArr = comment.split("\n");
                        for (String cmn : cmnArr) {
                            if (cmn == null) {
                                continue;
                            }
                            cmn = cmn.trim();
                            if (StringUtils.isBlank(cmn)) {
                                continue;
                            }
                            comments.add(cmn);
                        }
                    }
                }
                continue;
            }
            Element funcElement = (Element) curNode;
            MapperFunc.Type funcType = MapperFunc.Type.of(funcElement);
            if (funcType != null) {
                MapperFunc mapperFunc = new MapperFunc(entityClazz, funcType, funcElement);
                if (!comments.isEmpty()) {
                    mapperFunc.setComment(String.join("\n     * ", comments));
                }
                mapperFunc.buildContent((appender, xmlElement) -> {
                    sqlBuilders.stream().filter(b -> b.accept(mapperFunc, xmlElement)).forEach(sqlBuilder -> {
                        if (!appender.isCompleted()) {
                            sqlBuilder.build(appender, xmlElement, mapper, mapperFunc);
                        } else {
                            if (!CollectionUtils.isEmpty(mapperXmlListeners)) {
                                mapperXmlListeners.forEach(listener ->
                                        listener.onFuncReady(context, mapperFunc, appender)
                                );
                            }
                        }
                    });
                }, asCharactersNodes);
                mapper.getFuncList().add(mapperFunc);
            }
            comments.clear();
        }
        if (!CollectionUtils.isEmpty(mapperXmlListeners)) {
            mapperXmlListeners.forEach(listener ->
                    listener.onReady(context)
            );
        }
    }

    protected void init() throws Exception {
        Assert.notNull(mybatisGenProperties, "Generator configuration missing");
        if (mybatisGenProperties.getGenerators() == null) {
            mybatisGenProperties.setGenerators(Maps.newHashMap());
        }
        if (mybatisGenProperties.getGenerator() != null) {
            Assert.isTrue(mybatisGenProperties.getGenerators().get(curModuleName) == null,
                    "The current module has been configured");
            mybatisGenProperties.getGenerators().put(curModuleName, mybatisGenProperties.getGenerator());
        }
        Assert.notEmpty(mybatisGenProperties.getGenerators(), "Generator configuration missing");
        for (Map.Entry<String, MybatisGenProperties.GeneratorCfg> entry : mybatisGenProperties.getGenerators()
                .entrySet()) {
            String moduleName = entry.getKey();
            MybatisGenProperties.GeneratorCfg config = entry.getValue();
            metaAccessor.touch(config.getDatasourceBeanId(), metaOperations ->
                    loadDesignDocuments(moduleName, config, metaOperations)
            );
        }
    }

    protected void loadDesignDocuments(String moduleName, MybatisGenProperties.GeneratorCfg config,
                                       MetaOperations metaOpt) throws Exception {
        File projectDir = GenToolKit.resolveProjectDir(moduleName);
        String defScheme = config.getDefaultScheme();
        if (StringUtils.isBlank(defScheme)) {
            defScheme = metaOpt.getDefaultDatabase();
            Assert.hasText(defScheme, String.format("The module '%s' does not specify a database or scheme",
                    moduleName));
            config.setDefaultScheme(defScheme);
        }
        String defaultScheme = defScheme;
        String designDirPath = config.getDesignDir();
        if (designDirPath == null) {
            designDirPath = "";
        }
        Path designPath = projectDir.toPath().resolve(designDirPath);
        designPath.toFile().mkdirs();
        GenToolKit.walkFileTree(designPath, (path, attrs) -> {
            File xmlFile = path.toFile();
            String fileName = xmlFile.getName();
            if (!fileName.endsWith(".xml")) {
                return FileVisitResult.CONTINUE;
            }
            Document designDoc;
            try {
                designDoc = GenToolKit.readXml(xmlFile);
            } catch (Exception e) {
                log.warn("Failed to read xml :{}", xmlFile.getAbsolutePath(), e);
                return FileVisitResult.CONTINUE;
            }
            Element meta = designDoc.getRootElement();
            String schemeName = meta.attributeValue("scheme");
            if (StringUtils.isBlank(schemeName)) {
                schemeName = defaultScheme;
            }
            metaOpt.checkDatabase(schemeName);
            String tableName = meta.attributeValue("table");
            Assert.hasText(tableName, String.format("Attribute 'table' must be set, xml: %s", xmlFile.getAbsolutePath()));
            Table table = metaOpt.table(schemeName, tableName);
            Assert.notNull(table, String.format("Table '%s' not found, xml: %s", tableName, xmlFile.getAbsolutePath()));
            if (designXmlPool.computeIfAbsent(moduleName, k -> new ConcurrentHashMap<>())
                    .putIfAbsent(schemeName + "@" + tableName, new DesignXml(xmlFile, designDoc)) != null) {
                throw new IOException(String.format("Duplicate mapper design for table '%s'", tableName));
            }
            return FileVisitResult.CONTINUE;
        });
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            init();
            if (!CollectionUtils.isEmpty(entityListeners)) {
                AnnotationAwareOrderComparator.sort(entityListeners);
            }
            if (!CollectionUtils.isEmpty(daoListeners)) {
                AnnotationAwareOrderComparator.sort(daoListeners);
            }
            if (!CollectionUtils.isEmpty(mapperXmlListeners)) {
                AnnotationAwareOrderComparator.sort(mapperXmlListeners);
            }
        } catch (Exception e) {
            throw new MybatisGenException("Init failed", e);
        }
    }
}