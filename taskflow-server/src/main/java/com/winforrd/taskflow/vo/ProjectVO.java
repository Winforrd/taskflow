package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectVO {

    private Long id;

    private Long teamId;

    private String projectCode;

    private String name;

    private String description;

    private Long ownerId;

    private String ownerName;

    private String status;

    private LocalDate startDate;

    private LocalDate deadline;

    /**
     * 当前用户在项目中的角色。
     *
     * 团队 OWNER / ADMIN
     * 如果没有加入项目可能为空。
     */
    private String myRole;

    private Integer memberCount;

    private LocalDateTime createdTime;
}