/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.sql.meta.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Table column metadata
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-15 07:16
 */
@Getter
@Setter
public class TableColumn extends Meta {
    @JSONField(name = "column_name")
    private String name;
    @JSONField(name = "data_type")
    private String dataType;
    @JSONField(name = "column_type")
    private String colType;
    @JSONField(name = "ordinal_position")
    private Integer position;
    @JSONField(name = "column_key")
    private String key;
    @JSONField(name = "extra")
    private String extra;
    @JSONField(name = "numeric_precision")
    private Integer numericPrecision;
    @JSONField(name = "column_comment")
    private String comment;

    @Override
    public boolean isValid() {
        return StringUtils.isNotBlank(name);
    }
}
