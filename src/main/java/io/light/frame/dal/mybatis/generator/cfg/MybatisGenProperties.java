package io.light.frame.dal.mybatis.generator.cfg;

import io.light.frame.dal.mybatis.generator.sql.Dialect;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * Mybatis generator env
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-23 10:21
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "light.mybatis")
public class MybatisGenProperties {

    /**
     * Generator for current module
     */
    private GeneratorCfg generator;

    /**
     * Generators[module name,configuration]
     */
    private Map<String, GeneratorCfg> generators;

    @Getter
    @Setter
    public static class GeneratorCfg {

        /**
         * Datasource bean id
         */
        @Nullable
        private String datasourceBeanId;

        /**
         * Dialect
         */
        private Dialect dialect = Dialect.MYSQL;

        /**
         * Default scheme
         */
        @Nullable
        private String defaultScheme;

        /**
         * Author name
         */
        private String author;

        /**
         * Company name
         */
        private String company;

        /**
         * Design dir
         */
        private String designDir;

        /**
         * Java files build path
         */
        private String javaBuildPath = "src/main/java";

        /**
         * Resource files build path
         */
        private String resourceBuildPath = "src/resource/java";

        /**
         * Entity class package
         */
        private String entityPackage = "gen.entity";

        /**
         * Dao interface package
         */
        private String daoPackage = "gen.dao";

        /**
         * Mybatis mapper Xml package
         */
        private String mapperXmlPackage = "mapper";
    }

}
