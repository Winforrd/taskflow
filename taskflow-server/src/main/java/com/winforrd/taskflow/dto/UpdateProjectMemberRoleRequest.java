package com.winforrd.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProjectMemberRoleRequest {

    @NotBlank(message = "项目角色不能为空")
    private String roleType;
}