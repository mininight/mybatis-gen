/*
 * Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */
package module.level.two.autogen.entity;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * 内容主体
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-14 01:06:31
 */
@Getter
@Setter
public class CmsContent implements Serializable {

    /**
     * 全局唯一标识
     */
    private Integer id;

    /**
     * 栏目ID
     */
    private Integer channelId;

    /**
     * 撰写管理员ID
     */
    private Integer userId;

    /**
     * 组织ID
     */
    private Integer orgId;

    /**
     * 发布管理员
     */
    private Integer publishUserId;

    /**
     * 模型ID
     */
    private Integer modelId;

    /**
     * 站点ID
     */
    private Integer siteId;

    /**
     * 内容标题
     */
    private String title;

    /**
     * 内容标题是否加粗
     */
    private Integer titleIsBold;

    /**
     * 内容标题的颜色
     */
    private String titleColor;

    /**
     * 简短标题
     */
    private String shortTitle;

    /**
     * 发布时间
     */
    private Date releaseTime;

    /**
     * 下线时间
     */
    private Date offlineTime;

    /**
     * 内容密级
     */
    private Integer contentSecretId;

    /**
     * 内容状态(1:草稿;  2-初稿   3:流转中;   4:已审核;   5:已发布;  6:退回;  7:下线  8-归档 9 暂存 10 驳回 )
     */
    private Integer status;

    /**
     * 创建方式（1:直接创建    2:投稿  3:站群推送   4:站群采集   5:复制  6:链接型引用 7:镜像型引用）8 外部采集
     */
    private Integer createType;

    /**
     * 是否编辑（0-否   1-是）
     */
    private Integer isEdit;

    /**
     * 排序值
     */
    private Integer sortNum;

    /**
     * 排序值权重(排序值相同情况下，权重越大，排序越前)
     */
    private Integer sortWeight;

    /**
     * 浏览量
     */
    private Integer views;

    /**
     * 评论量
     */
    private Integer comments;

    /**
     * 点赞数
     */
    private Integer ups;

    /**
     * 点踩数
     */
    private Integer downs;

    /**
     * 下载量
     */
    private Integer downloads;

    /**
     * 浏览设置（1-允许游客访问   2-登录后访问）
     */
    private Integer viewControl;

    /**
     * 评论设置(1允许游客评论 2登录后评论  3不允许评论)
     */
    private Integer commentControl;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 置顶开始时间
     */
    private Date topStartTime;

    /**
     * 置顶结束时间
     */
    private Date topEndTime;

    /**
     * 是否发布至pc（0-否  1-是）
     */
    private Integer isReleasePc;

    /**
     * 是否发布至wap（0-否  1-是）
     */
    private Integer isReleaseWap;

    /**
     * 是否发布至app（0-否  1-是）
     */
    private Integer isReleaseApp;

    /**
     * 是否发布至小程序（0-否  1-是）
     */
    private Integer isReleaseMiniprogram;

    /**
     * 是否加入回收站（0-否  1-是）
     */
    private Integer isRecycle;

    /**
     * 复制来源内容id
     */
    private Integer copySourceContentId;

    /**
     * 是否已生成静态化页面
     */
    private Integer hasStatic;

    /**
     * 内容审核标识
     */
    private String checkMark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 创建者
     */
    private String createUser;

    /**
     * 修改者
     */
    private String updateUser;

    /**
     * 删除标识（0为正常1为删除）
     */
    private Integer deletedFlag;

    /**
     * 内容快照：记录修改当前内容的模型的所有字段
     */
    private String modelFieldSet;

    /**
     * 内容列表位置(1 内容列表 2智能审核 3回收站 4已归档)
     */
    private Integer contentPos;

    /**
     * 引用内容的原内容ID
     */
    private Integer oriContentId;

    /**
     * 浏览人数
     */
    private Integer peopleViews;
}