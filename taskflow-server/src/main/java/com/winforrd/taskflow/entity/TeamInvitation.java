package com.winforrd.taskflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("team_invitation")
public class TeamInvitation {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 团队ID
     */
    private Long teamId;

    /**
     * 邀请码
     */
    private String invitationCode;

    /**
     * 创建人
     */
    private Long creatorId;

    /**
     * 加入后的默认角色
     *
     * MEMBER
     * READ_ONLY
     */
    private String defaultRole;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 最大使用次数
     */
    private Integer maxUses;

    /**
     * 已使用次数
     */
    private Integer usedCount;

    /**
     * 状态
     *
     * 1 有效
     * 0 禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}