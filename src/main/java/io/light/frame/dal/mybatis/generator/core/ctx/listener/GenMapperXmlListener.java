package io.light.frame.dal.mybatis.generator.core.ctx.listener;

import io.light.frame.dal.mybatis.generator.core.ctx.GenContext;
import io.light.frame.dal.mybatis.generator.core.domain.mapper.MapperFunc;
import org.springframework.core.Ordered;

import java.io.File;

/**
 * Generate mapper xml listener
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-12 12:37
 */
public interface GenMapperXmlListener extends Ordered {

    void onFuncReady(GenContext context, MapperFunc mapperFunc, MapperFunc.ContentBuilder contentBuilder);

    void onReady(GenContext context);

    void afterGenerated(GenContext context, File mapperXml);
}
