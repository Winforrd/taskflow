package com.winforrd.taskflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属团队
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
     * 项目负责人
     */
    private Long ownerId;

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
     * 创建人
     */
    private Long createdBy;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}