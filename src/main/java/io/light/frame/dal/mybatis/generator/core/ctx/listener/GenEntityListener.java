package io.light.frame.dal.mybatis.generator.core.ctx.listener;

import io.light.frame.dal.mybatis.generator.core.ctx.GenContext;
import io.light.frame.dal.mybatis.generator.core.domain.clazz.Clazz;
import org.springframework.core.Ordered;

import java.io.File;

/**
 * Generate entity listener
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-12 12:36
 */
public interface GenEntityListener extends Ordered {

    void onReady(GenContext context, Clazz entityClazz);

    void afterGenerated(GenContext context, Clazz entityClazz, File entityFile);

}
