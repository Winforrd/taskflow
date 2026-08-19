package com.winforrd.taskflow.dto;

import com.winforrd.taskflow.common.enums.MyTaskType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class MyTaskQuery {

    /**
     * 默认查询：
     * 我负责的任务
     */
    private MyTaskType type = MyTaskType.ASSIGNED;
    /**
     * 当前页
     */
    @Min(value = 1, message = "页码不能小于1")
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Long pageSize = 10L;


    /**
     * 关键词
     *
     * 搜索任务标题
     */
    private String keyword;

    /**
     * 任务状态
     *
     * TODO
     * IN_PROGRESS
     * IN_REVIEW
     * DONE
     * CANCELLED
     */
    private String status;
}