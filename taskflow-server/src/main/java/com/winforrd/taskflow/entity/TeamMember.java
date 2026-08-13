package com.winforrd.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("team_member")
public class TeamMember {

    // 主键
    @TableId(type = IdType.AUTO)
    private Long id;

    // 团队ID
    private Long teamId;

    // 用户ID
    private Long userId;

    // 成员角色类型
    private String roleType;

    // 成员状态
    private Integer memberStatus;

    // 加入时间
    private LocalDateTime joinedTime;

    // 创建时间
    private LocalDateTime createdTime;

    // 更新时间
    private LocalDateTime updatedTime;
}