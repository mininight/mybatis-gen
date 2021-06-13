/*
 * Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */
package io.light.frame.samples.generator.dal.autogen.dao;

import org.apache.ibatis.annotations.Mapper;
import io.light.frame.samples.generator.dal.autogen.entity.CmsContent;
import java.util.List;

/**
 * 内容主体 Dao
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-14 01:06:31
 */
@Mapper
public interface CmsContentDao {

    /**
     * Insert record
     *
     * @param record 
     * @return {@code int} 
     */
    int insert(CmsContent record);

    /**
     * Select all
     *
     * @return {@literal List<CmsContent>}
     */
    List<CmsContent> selectAll();
}