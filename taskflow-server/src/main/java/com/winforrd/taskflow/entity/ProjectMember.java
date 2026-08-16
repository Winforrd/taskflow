package com.winforrd.taskflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_member")
public class ProjectMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long userId;

    /**
     * MANAGER
     * MEMBER
     * READ_ONLY
     */
    private String roleType;

    private LocalDateTime joinedTime;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}