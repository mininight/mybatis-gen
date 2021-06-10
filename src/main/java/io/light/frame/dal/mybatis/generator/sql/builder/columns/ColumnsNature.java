/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.sql.builder.columns;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * TODO
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-22 09:47
 */
@Getter
@Setter
public abstract class ColumnsNature {
    private String tableAlias;
    private String includeColumns;
    private String excludeColumns;
    @JSONField(name = "alias")
    private List<ColumnAlias> columnAliasList;
}
