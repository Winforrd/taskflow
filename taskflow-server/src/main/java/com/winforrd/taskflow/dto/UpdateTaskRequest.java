package com.winforrd.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTaskRequest {

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题不能超过200个字符")
    private String title;

    private String description;

    private String priority;

    private LocalDate startDate;

    private LocalDate deadline;

    /**
     * 乐观锁版本号
     */
    @NotNull(message = "任务版本号不能为空")
    private Integer version;
}