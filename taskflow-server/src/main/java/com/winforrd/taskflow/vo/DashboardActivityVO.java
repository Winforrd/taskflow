package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DashboardActivityVO {

    /**
     * 动态ID
     */
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务标题
     */
    private String taskTitle;

    /**
     * 操作人
     */
    private String operatorName;

    /**
     * 动态类型
     */
    private String activityType;

    /**
     * 内容
     */
    private String content;

    /**
     * 时间
     */
    private LocalDateTime createdTime;
}