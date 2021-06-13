/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.sql.meta.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Table metadata
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-15 07:17
 */
@Getter
@Setter
public class Table extends Meta {
    @JSONField(name = "table_name")
    private String name;
    @JSONField(name = "table_schema")
    private String schema;
    @JSONField(name = "table_comment")
    private String comment;
    private List<TableColumn> columns;

    @Override
    public boolean isValid() {
        return StringUtils.isNotBlank(name);
    }
}
