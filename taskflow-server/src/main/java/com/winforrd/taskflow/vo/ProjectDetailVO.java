package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectDetailVO {

    /**
     * 项目ID
     */
    private Long id;

    /**
     * 团队ID
     */
    private Long teamId;

    /**
     * 项目编号
     */
    private String projectCode;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 项目负责人ID
     */
    private Long ownerId;

    /**
     * 项目负责人名称
     */
    private String ownerName;

    /**
     * 项目状态
     */
    private String status;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 截止日期
     */
    private LocalDate deadline;

    /**
     * 当前用户项目角色
     */
    private String myProjectRole;

    /**
     * 当前用户团队角色
     */
    private String myTeamRole;

    /**
     * 项目成员数量
     */
    private Integer memberCount;

    /**
     * 是否可以编辑项目基础信息
     *
     * 团队 OWNER / ADMIN
     * 项目负责人
     * MANAGER
     */
    private Boolean canEdit;

    /**
     * 是否拥有项目核心管理权限
     *
     * 团队 OWNER / ADMIN
     * 项目负责人
     *
     * MANAGER 不具备
     */
    private Boolean canManageProject;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}