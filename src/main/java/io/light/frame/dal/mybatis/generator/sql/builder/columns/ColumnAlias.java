/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.sql.builder.columns;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * TODO
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-22 09:54
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ColumnAlias {
    @EqualsAndHashCode.Include
    private String column;
    private String as;
}
