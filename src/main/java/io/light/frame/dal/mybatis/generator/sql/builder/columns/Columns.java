/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.sql.builder.columns;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * TODO
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-20 13:54
 */
@Getter
@Setter
public class Columns extends ColumnsNature {
    private Scope scope;
    @JSONField(name = "append")
    private List<Append> appends;


    public enum Scope {
        /**
         * all
         */
        all,
        /**
         * auto
         */
        auto
    }

    @Getter
    @Setter
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class Append extends ColumnsNature {
        @EqualsAndHashCode.Include
        private String table;
    }
}
