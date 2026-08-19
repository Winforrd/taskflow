package com.winforrd.taskflow.common.enums;

/**
 * 我的任务分类
 */
public enum MyTaskType {

    /**
     * 我负责的
     */
    ASSIGNED,

    /**
     * 我创建的
     */
    CREATED,

    /**
     * 待我验收
     */
    REVIEW,

    /**
     * 我负责且已经逾期
     */
    OVERDUE
}