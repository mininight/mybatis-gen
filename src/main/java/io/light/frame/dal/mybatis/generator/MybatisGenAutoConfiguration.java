/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator;

import io.light.frame.dal.mybatis.generator.core.MybatisGenerator;
import io.light.frame.dal.mybatis.generator.core.cfg.MybatisGenProperties;
import io.light.frame.dal.mybatis.generator.util.VelocityEngineHelper;
import org.apache.velocity.spring.VelocityEngineFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Mybatis generator auto configuration
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-21 07:42
 */
@ComponentScan
@Configuration
@EnableConfigurationProperties(MybatisGenProperties.class)
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class MybatisGenAutoConfiguration {

    private final MybatisGenProperties properties;

    public MybatisGenAutoConfiguration(MybatisGenProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public VelocityEngineHelper velocityEngineHelper() {
        PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
        VelocityEngineFactoryBean velocityEngine = new VelocityEngineFactoryBean();
        velocityEngine.setConfigLocation(resourceLoader.getResource(
                "classpath:vm/vm.properties"));
        velocityEngine.setResourceLoader(resourceLoader);
        velocityEngine.setResourceLoaderPath("classpath:/vm");
        return new VelocityEngineHelper(velocityEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisGenerator mybatisGenerator() {
        return new MybatisGenerator(properties);
    }
}
