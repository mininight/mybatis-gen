package io.light.frame.dal.mybatis.generator.sql.meta.opt;

import io.light.frame.dal.mybatis.generator.sql.Dialect;
import io.light.frame.dal.mybatis.generator.sql.meta.entity.Table;
import io.light.frame.dal.mybatis.generator.sql.meta.entity.TableColumn;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * Meta operations
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-09 13:09
 */
public interface MetaOperations extends Ordered {

    Dialect getDialect();

    void checkDatabase(String database);

    String getDefaultDatabase();

    Table table(String tableSchema, String tableName);

    List<TableColumn> columns(String tableSchema, String tableName);

    List<Table> tableList(String tableSchema);

    @Override
    default int getOrder() {
        return 0;
    }
}
