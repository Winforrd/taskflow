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
    ASSIGNEE_REMOVED,

    /**
     * 子任务创建
     */
    CREATE_SUB_TASK,

    /**
     * 子任务完成
     */
    COMPLETE_SUB_TASK,

    /**
     * 子任务更新
     */
    UPDATE_SUB_TASK,

    /**
     * 子任务删除
     */
    DELETE_SUB_TASK
}