package com.winforrd.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSubTaskRequest {

    @NotBlank(message = "子任务标题不能为空")
    @Size(max = 200, message = "子任务标题不能超过200个字符")
    private String title;

    @Size(max = 1000, message = "子任务描述不能超过1000个字符")
    private String description;

    private Integer requiredFlag;

    private Integer sortOrder;
}
