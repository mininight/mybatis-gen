package io.light.frame.dal.mybatis.generator;

import io.light.frame.dal.mybatis.generator.core.MybatisGenerator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.*;

/**
 * Enable {@link MybatisGenAutoConfiguration}
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-10 12:57
 * @see MybatisGenerator
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableAutoConfiguration
@ImportAutoConfiguration(MybatisGenAutoConfiguration.class)
public @interface EnableMybatisGen {

}
