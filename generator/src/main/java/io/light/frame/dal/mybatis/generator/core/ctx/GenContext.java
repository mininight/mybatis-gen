package io.light.frame.dal.mybatis.generator.core.ctx;

import io.light.frame.dal.mybatis.generator.core.cfg.MybatisGen;
import io.light.frame.dal.mybatis.generator.core.domain.DesignXml;
import io.light.frame.dal.mybatis.generator.core.domain.mapper.TableMapper;
import io.light.frame.dal.mybatis.generator.exceptions.MybatisGenException;
import io.light.frame.dal.mybatis.generator.util.GenToolKit;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Mybatis generator context
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-12 14:55
 */
@Getter
@Setter
public class GenContext {
    private static final ThreadLocal<GenContext> CTX = new ThreadLocal<>();
    public static final String VAR_KEY_SQL_FUNC_COLUMNS = "VAR_KEY_SQL_FUNC_COLUMNS";
    private final MybatisGen config;
    private final String module;
    private final File projectDir;
    private final TableMapper tableMapper;
    private final DesignXml designXml;
    private final Map<String, Object> vars = new HashMap<>();

    private GenContext(String module, MybatisGen config, TableMapper tableMapper, DesignXml designXml) {
        this.module = module;
        this.projectDir = GenToolKit.resolveProjectDir(module);
        this.config = config;
        this.tableMapper = tableMapper;
        this.designXml = designXml;
    }

    public static GenContext create(String module, MybatisGen config, TableMapper tableMapper, DesignXml designXml) {
        if (CTX.get() != null) {
            throw new MybatisGenException("The current thread generator context already exists");
        }
        CTX.set(new GenContext(module, config, tableMapper, designXml));
        return CTX.get();
    }

    public static GenContext current() {
        return current(true);
    }

    public static GenContext current(boolean check) {
        if (check && CTX.get() == null) {
            throw new MybatisGenException("The current thread generator context has not initialized");
        }
        return CTX.get();
    }

    public static void destroy() {
        GenContext context = current(false);
        if (context != null) {
            context.clean();
        }
        CTX.remove();
    }

    private void clean() {
        vars.clear();
    }
}
