package com.winforrd.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateProjectRequest {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称不能超过100个字符")
    private String name;

    @Size(max = 1000, message = "项目描述不能超过1000个字符")
    private String description;

    /**
     * 项目负责人
     */
    @NotNull(message = "项目负责人不能为空")
    private Long ownerId;

    private LocalDate startDate;

    private LocalDate deadline;
}