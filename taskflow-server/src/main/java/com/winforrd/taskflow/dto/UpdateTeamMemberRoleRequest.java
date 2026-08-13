package com.winforrd.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTeamMemberRoleRequest {

    @NotBlank(message = "团队角色不能为空")
    private String roleType;
}