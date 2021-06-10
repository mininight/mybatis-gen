package io.light.frame.dal.mybatis.generator.sql.meta;

import com.google.common.collect.Maps;
import io.light.frame.dal.mybatis.generator.cfg.MybatisGenProperties;
import io.light.frame.dal.mybatis.generator.exceptions.MetaAccessException;
import io.light.frame.dal.mybatis.generator.exceptions.MybatisGenException;
import io.light.frame.dal.mybatis.generator.sql.Dialect;
import io.light.frame.dal.mybatis.generator.sql.meta.opt.MetaOperations;
import io.light.frame.dal.mybatis.generator.util.DialectJdbcTemplate;
import io.light.frame.dal.mybatis.generator.util.GenToolKit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Meta accessor
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-24 05:49
 */
@Repository
public class MetaAccessorImpl implements MetaAccessor {

    private static final ThreadLocal<DialectJdbcTemplate> JDBC_TEMPLATE_HOLDER = new ThreadLocal<>();

    private final Map<String, DialectJdbcTemplate> jdbcTemplates = new HashMap<>();

    private final MybatisGenProperties mybatisGenProperties;

    private final Map<Dialect, MetaOperations> metaOperations;

    private String defaultDsBeanId;

    public MetaAccessorImpl(MybatisGenProperties mybatisGenProperties, List<MetaOperations> metaOperations) {
        this.mybatisGenProperties = mybatisGenProperties;
        this.metaOperations = metaOperations.stream().collect(Collectors.toMap(MetaOperations::getDialect,
                Function.identity(), (o, n) -> (o.getOrder() <= n.getOrder() ? o : n), HashMap::new));
    }

    @Override
    public DialectJdbcTemplate lookupJdbcTemplate() {
        DialectJdbcTemplate jdbcTemplate = JDBC_TEMPLATE_HOLDER.get();
        if (jdbcTemplate == null) {
            throw new MetaAccessException("Missing defined JDBC template");
        }
        return jdbcTemplate;
    }

    @Override
    public void touch(String datasourceId, Pipe<MetaOperations> consumer) {
        try {
            if (JDBC_TEMPLATE_HOLDER.get() == null) {
                if (StringUtils.isBlank(datasourceId)) {
                    datasourceId = defaultDsBeanId;
                }
                JDBC_TEMPLATE_HOLDER.set(jdbcTemplates.get(datasourceId));
            }
            Dialect dialect = lookupJdbcTemplate().getDialect();
            MetaOperations operation = metaOperations.get(dialect);
            if (operation == null) {
                throw new MetaAccessException(String.format("Unsupported sql dialect '%s'", dialect));
            }
            consumer.accept(operation);
        } catch (MybatisGenException mge) {
            throw mge;
        } catch (Exception e) {
            throw new MetaAccessException("Metadata operation error", e);
        } finally {
            JDBC_TEMPLATE_HOLDER.remove();
        }
    }

    private DataSource lookupDefaultDataSource() {
        DataSource defaultDs = null;
        try {
            defaultDs = GenToolKit.beanFactory().getBean(DataSource.class);
        } catch (Exception e) {
            //skip
        }
        if (defaultDs == null) {
            try {
                defaultDs = GenToolKit.beanFactory().getBean("dataSource", DataSource.class);
            } catch (Exception e) {
                //skip
            }
        }
        return defaultDs;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, DataSource> dataSourceMap = GenToolKit.beanFactory().getBeansOfType(DataSource.class);
        if (CollectionUtils.isEmpty(dataSourceMap)) {
            return;
        }
        DataSource defaultDs = lookupDefaultDataSource();
        defaultDsBeanId = dataSourceMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() == defaultDs)
                .map(Map.Entry::getKey)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
        if (mybatisGenProperties.getGenerators() == null) {
            mybatisGenProperties.setGenerators(Maps.newHashMap());
        }
        List<MybatisGenProperties.GeneratorCfg> generators = new ArrayList<>(mybatisGenProperties
                .getGenerators().values());
        if (mybatisGenProperties.getGenerator() != null) {
            generators.add(mybatisGenProperties.getGenerator());
        }
        Map<String, Dialect> dialectMap = new HashMap<>(dataSourceMap.size());
        for (MybatisGenProperties.GeneratorCfg cfg : generators) {
            String dsBeanId = cfg.getDatasourceBeanId();
            if (StringUtils.isBlank(dsBeanId)) {
                dsBeanId = defaultDsBeanId;
            }
            if (StringUtils.isBlank(dsBeanId)) {
                throw new MetaAccessException("Default datasource bean not found");
            }
            Dialect dialect = cfg.getDialect();
            if (dialect == null) {
                dialect = Dialect.DEFAULT;
            }
            Dialect existDialect = dialectMap.putIfAbsent(dsBeanId, dialect);
            if (existDialect != null && existDialect != dialect) {
                throw new MetaAccessException(String.format("Inconsistent dialect settings for datasource bean: '%s'",
                        dsBeanId));
            }
        }
        if (defaultDs != null) {
            jdbcTemplates.put(defaultDsBeanId, new DialectJdbcTemplate(dialectMap.get(defaultDsBeanId),
                    defaultDs));
        }
        dataSourceMap.entrySet().stream().filter(entry -> Objects.nonNull(entry.getValue()))
                .filter(entry -> entry.getValue() != defaultDs)
                .forEach(entry -> {
                    jdbcTemplates.put(entry.getKey(), new DialectJdbcTemplate(dialectMap.get(entry.getKey()),
                            entry.getValue()));
                });
    }
}
