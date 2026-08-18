package com.winforrd.taskflow.common.enums;

public enum TaskActivityType {

    /**
     * 创建任务
     */
    CREATED,

    /**
     * 编辑任务
     */
    UPDATED,

    /**
     * 状态改变
     */
    STATUS_CHANGED,

    /**
     * 添加负责人
     */
    ASSIGNEE_ADDED,

    /**
     * 移除负责人
     */
    ASSIGNEE_REMOVED
}