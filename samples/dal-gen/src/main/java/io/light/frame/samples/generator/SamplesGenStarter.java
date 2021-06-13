package io.light.frame.samples.generator;

import io.light.frame.dal.mybatis.generator.EnableMybatisGen;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Samples generator starter
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-13 13:16
 */
@EnableMybatisGen
@SpringBootApplication
public class SamplesGenStarter {
    public static void main(String[] args) {
        SpringApplication.run(SamplesGenStarter.class, args);
    }
}
