package io.light.frame.dal.mybatis.generator.core.ctx;

import io.light.frame.dal.mybatis.generator.core.cfg.MybatisGenProperties;
import io.light.frame.dal.mybatis.generator.core.domain.DesignXml;
import io.light.frame.dal.mybatis.generator.core.domain.mapper.TableMapper;
import lombok.Getter;
import org.dom4j.Document;

import java.io.File;

/**
 * Mybatis generator context
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-12 14:55
 */
@Getter
public class GenContext {
    private final MybatisGenProperties.GeneratorCfg config;
    private final String module;
    private final File projectDir;
    private final DesignXml designXml;
    private final TableMapper tableMapper;

    public GenContext(MybatisGenProperties.GeneratorCfg config, String module, File projectDir, DesignXml designXml,
                      TableMapper tableMapper) {
        this.config = config;
        this.module = module;
        this.projectDir = projectDir;
        this.designXml = designXml;
        this.tableMapper = tableMapper;
    }
}
