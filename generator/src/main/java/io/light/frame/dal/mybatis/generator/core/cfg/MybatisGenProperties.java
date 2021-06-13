package io.light.frame.dal.mybatis.generator.core.cfg;

import com.google.common.collect.Maps;
import io.light.frame.dal.mybatis.generator.exceptions.InitializationException;
import io.light.frame.dal.mybatis.generator.util.GenToolKit;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

/**
 * Mybatis generator spring env
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-23 10:21
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "mybatis.generator")
public class MybatisGenProperties extends MybatisGen implements InitializingBean {

    /**
     * Module relative path
     */
    private String module;

    /**
     * Configuration for other modules`s generation [module relative path,{@link MybatisGen}]
     */
    private Map<String, MybatisGen> otherModules;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (otherModules == null) {
            otherModules = Maps.newHashMap();
        }
        setModule(GenToolKit.determineModule(module, this));
        if (StringUtils.isBlank(getDesignDir())) {
            setDesignDir(DEFAULT_DESIGN_HOME + "/" + module);
        }
        selfCheck();
        if (otherModules.isEmpty()) {
            return;
        }
        Map<String, String> filter = new HashMap<>(otherModules.size());
        filter.put(module, getDesignDir());
        Iterator<Map.Entry<String, MybatisGen>> iterator = otherModules.entrySet().iterator();
        Map<String, MybatisGen> optimizeCfgMap = new HashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, MybatisGen> kv = iterator.next();
            String originalModule = kv.getKey();
            String module = GenToolKit.determineModule(originalModule, this);
            MybatisGen cfg = kv.getValue();
            if (StringUtils.isBlank(cfg.getDesignDir())) {
                cfg.setDesignDir(DEFAULT_DESIGN_HOME + "/" + module);
            }
            String designDir = cfg.getDesignDir();
            if (filter.containsKey(module)) {
                throw new InitializationException(String.format("Duplicate configuration for module '%s'", module));
            }
            if (filter.containsValue(designDir)) {
                throw new InitializationException("Different modules cannot share the same design directory");
            }
            cfg.selfCheck(this);
            filter.put(module, designDir);
            if (!Objects.equals(originalModule, module)) {
                optimizeCfgMap.put(module, cfg);
                iterator.remove();
            }
        }
        filter.clear();
        if (!optimizeCfgMap.isEmpty()) {
            otherModules.putAll(optimizeCfgMap);
        }
        otherModules = Collections.unmodifiableMap(otherModules);
    }
}
