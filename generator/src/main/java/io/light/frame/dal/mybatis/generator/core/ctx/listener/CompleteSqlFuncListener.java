package io.light.frame.dal.mybatis.generator.core.ctx.listener;

import io.light.frame.dal.mybatis.generator.core.ctx.GenContext;
import io.light.frame.dal.mybatis.generator.core.domain.mapper.MapperFunc;
import io.light.frame.dal.mybatis.generator.sql.builder.appender.columns.Columns;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Complete sql func listener
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-12 16:12
 */
@Component
public class CompleteSqlFuncListener implements GenMapperXmlListener {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void onFuncReady(MapperFunc mapperFunc, MapperFunc.ContentBuilder builder) {
        GenContext context = GenContext.current();
        MapperFunc.Type funcType = mapperFunc.getType();
        String curTableName = context.getTableMapper().getTable().getName();
        String sqlContent = builder.toString().toLowerCase();
        if (funcType == MapperFunc.Type.select) {
            if (!sqlContent.contains("from ") && !sqlContent.contains("from\n")) {
                builder.append("\n\t\tfrom ").append(curTableName);
                Object cols = context.getVars().get(GenContext.VAR_KEY_SQL_FUNC_COLUMNS);
                if (cols == null) {
                    return;
                }
                Columns columns = (Columns) cols;
                if (StringUtils.isNotBlank(columns.getTableAlias())) {
                    builder.append(" ").append(columns.getTableAlias());
                }
            }
        }
    }
}
