package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamInvitationVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 团队ID
     */
    private Long teamId;

    /**
     * 团队名称
     */
    private String teamName;

    /**
     * 邀请码
     */
    private String invitationCode;

    /**
     * 默认角色
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
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}