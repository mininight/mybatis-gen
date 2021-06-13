package io.light.frame.dal.mybatis.generator.core.ctx.listener;

import io.light.frame.dal.mybatis.generator.core.domain.clazz.Clazz;
import org.springframework.core.Ordered;

import java.io.File;

/**
 * Generate dao listener
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-12 12:37
 */
public interface GenDaoListener extends Ordered {

    default void onReady(Clazz daoClazz){}

    default void afterGenerated(Clazz daoClazz, File daoFile){}

    @Override
    default int getOrder() {
        return 0;
    }
}
