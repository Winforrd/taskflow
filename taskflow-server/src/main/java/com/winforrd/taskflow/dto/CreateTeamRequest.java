package com.winforrd.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTeamRequest {

    @NotBlank(message = "团队名称不能为空")
    @Size(max = 100, message = "团队名称不能超过100个字符")
    private String name; // 团队名称

    @Size(max = 500, message = "团队简介不能超过500个字符")
    private String description; // 团队简介
}
