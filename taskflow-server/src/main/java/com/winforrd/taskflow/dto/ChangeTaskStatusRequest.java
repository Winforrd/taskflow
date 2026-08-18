package com.winforrd.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeTaskStatusRequest {

    /**
     * 目标状态
     */
    @NotBlank(message = "目标状态不能为空")
    private String targetStatus;

    /**
     * 乐观锁
     */
    @NotNull(message = "任务版本号不能为空")
    private Integer version;
}