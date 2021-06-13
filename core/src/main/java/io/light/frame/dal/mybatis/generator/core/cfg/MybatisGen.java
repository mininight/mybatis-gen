package io.light.frame.dal.mybatis.generator.core.cfg;

import io.light.frame.dal.mybatis.generator.exceptions.InitializationException;
import io.light.frame.dal.mybatis.generator.sql.Dialect;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * Mybatis generator configuration
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-13 14:25
 */
@Getter
@Setter
public class MybatisGen {
    private static final String DEFAULT_JAVA_BUILD_PATH = "src/main/java";
    private static final String DEFAULT_RESOURCE_BUILD_PATH = "src/main/resources";
    private static final String DEFAULT_MAPPER_XML_LOCATION = "mapper/autogen";
    public static final String DEFAULT_DESIGN_HOME = "mybatis_design";

    /**
     * Author name
     */
    private String author;

    /**
     * Company name
     */
    private String company;

    /**
     * Datasource bean id
     */
    @Nullable
    private String datasourceBeanId;

    /**
     * Dialect
     */
    private Dialect dialect = Dialect.DEFAULT;

    /**
     * Default scheme
     */
    @Nullable
    private String defaultScheme;

    /**
     * Design dir
     */
    private String designDir;

    /**
     * Java files build path
     */
    private String javaBuildPath = DEFAULT_JAVA_BUILD_PATH;

    /**
     * Resource files build path
     */
    private String resourceBuildPath = DEFAULT_RESOURCE_BUILD_PATH;

    /**
     * Entity class package
     */
    private String entityPackage;

    /**
     * Dao interface package
     */
    private String daoPackage;

    /**
     * Mybatis mapper xml location
     */
    private String mapperXmlLocation = DEFAULT_MAPPER_XML_LOCATION;

    /**
     * Self check
     */
    public void selfCheck() {
        if (StringUtils.isBlank(designDir)) {
            throw new InitializationException("The design xml files dir needs to be specified");
        }
        if (StringUtils.isBlank(javaBuildPath)) {
            throw new InitializationException("The java build path needs to be specified");
        }
        if (StringUtils.isBlank(resourceBuildPath)) {
            throw new InitializationException("The resource build path needs to be specified");
        }
        if (StringUtils.isBlank(entityPackage)) {
            throw new InitializationException("The entity class package needs to be specified");
        }
        if (StringUtils.isBlank(daoPackage)) {
            throw new InitializationException("The dao interface package needs to be specified");
        }
        if (StringUtils.isBlank(mapperXmlLocation)) {
            throw new InitializationException("The mapper xml location needs to be specified");
        }
    }


    /**
     * Self check
     *
     * @param merger merger
     */
    public void selfCheck(MybatisGen merger) {
        if (this.dialect == null) {
            this.dialect = merger.getDialect();
        }
        if (StringUtils.isBlank(datasourceBeanId)) {
            this.datasourceBeanId = merger.getDatasourceBeanId();
        }
        if (StringUtils.isBlank(author)) {
            this.author = merger.getAuthor();
        }
        if (StringUtils.isBlank(company)) {
            this.company = merger.getCompany();
        }
        if (StringUtils.isBlank(javaBuildPath)) {
            this.javaBuildPath = merger.getJavaBuildPath();
        }
        if (StringUtils.isBlank(resourceBuildPath)) {
            this.resourceBuildPath = merger.getResourceBuildPath();
        }
        if (StringUtils.isBlank(entityPackage)) {
            this.entityPackage = merger.getEntityPackage();
        }
        if (StringUtils.isBlank(daoPackage)) {
            this.daoPackage = merger.getDaoPackage();
        }
        if (StringUtils.isBlank(mapperXmlLocation)) {
            this.mapperXmlLocation = merger.getMapperXmlLocation();
        }
        selfCheck();
    }
}
