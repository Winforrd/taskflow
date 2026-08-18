package com.winforrd.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题不能超过200个字符")
    private String title;

    private String description;

    /**
     * LOW / MEDIUM / HIGH / URGENT
     */
    private String priority;

    private LocalDate startDate;

    private LocalDate deadline;

    /**
     * 负责人
     */
    private List<Long> assigneeIds;
}