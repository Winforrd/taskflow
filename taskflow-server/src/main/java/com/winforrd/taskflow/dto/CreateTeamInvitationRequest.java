package com.winforrd.taskflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTeamInvitationRequest {

    /**
     * 默认加入角色
     *
     * 第一版只允许：
     * MEMBER
     * READ_ONLY
     */
    @NotBlank(message = "默认角色不能为空")
    private String defaultRole;

    /**
     * 有效小时数
     *
     * 1小时 ~ 168小时
     * 最长7天
     */
    @Min(value = 1, message = "邀请码有效时间不能少于1小时")
    @Max(value = 168, message = "邀请码有效时间不能超过168小时")
    private Integer expiresHours;

    /**
     * 最大使用次数
     */
    @Min(value = 1, message = "邀请码至少允许使用1次")
    @Max(value = 100, message = "邀请码最大使用次数不能超过100次")
    private Integer maxUses;
}