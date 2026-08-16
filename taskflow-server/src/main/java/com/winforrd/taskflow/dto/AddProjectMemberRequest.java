package com.winforrd.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddProjectMemberRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "项目角色不能为空")
    private String roleType;
}