package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MyTaskVO {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 负责人名称
     *
     * 多负责人：
     * 王五、赵六
     */
    private String assigneeNames;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 截止日期
     */
    private LocalDate deadline;

    /**
     * 是否已经逾期
     */
    private Boolean overdue;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}