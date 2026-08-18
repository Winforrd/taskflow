package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskVO {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务描述
     */
    private String description;

    /**
     * TODO
     * IN_PROGRESS
     * IN_REVIEW
     * DONE
     * CANCELLED
     */
    private String status;

    /**
     * LOW
     * MEDIUM
     * HIGH
     * URGENT
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
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 截止日期
     */
    private LocalDate deadline;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;

    /**
     * 乐观锁版本
     */
    private Integer version;

    /**
     * 任务负责人
     */
    private List<TaskAssigneeVO> assignees;

    /**
     * 当前用户能够执行的下一状态
     *
     * 这个字段由后端根据：
     *
     * 状态机
     * +
     * 当前用户权限
     *
     * 动态计算。
     */
    private List<String> allowedNextStatuses;

    /**
     * 当前用户是否可以编辑任务基本信息
     */
    private Boolean canEdit;

    /**
     * 当前用户是否拥有负责人管理权限
     *
     * 团队 OWNER / ADMIN
     * 项目负责人
     * 项目 MANAGER
     *
     * MEMBER = false
     */
    private Boolean canManageAssignees;

    /**
     * 当前用户是否可以给其他成员分配任务
     *
     * MEMBER = false
     */
    private Boolean canAssignOthers;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}