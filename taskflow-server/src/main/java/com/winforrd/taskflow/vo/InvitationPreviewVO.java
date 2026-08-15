package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvitationPreviewVO {

    /**
     * 团队ID
     */
    private Long teamId;

    /**
     * 团队名称
     */
    private String teamName;

    /**
     * 团队简介
     */
    private String teamDescription;

    /**
     * 加入后的角色
     */
    private String defaultRole;

    /**
     * 邀请码过期时间
     */
    private LocalDateTime expiresAt;
}