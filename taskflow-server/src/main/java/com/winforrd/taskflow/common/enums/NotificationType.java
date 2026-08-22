package com.winforrd.taskflow.common.enums;

public enum NotificationType {

    /**
     * 被分配任务
     */
    TASK_ASSIGNED,

    /**
     * 被移除负责人
     */
    TASK_UNASSIGNED,

    /**
     * 提交验收
     */
    TASK_SUBMITTED,

    /**
     * 验收通过
     */
    TASK_APPROVED,

    /**
     * 验收驳回
     */
    TASK_REJECTED,

    /**
     * 任务取消
     */
    TASK_CANCELLED,

    /**
     * 任务评论
     */
    TASK_COMMENT
}