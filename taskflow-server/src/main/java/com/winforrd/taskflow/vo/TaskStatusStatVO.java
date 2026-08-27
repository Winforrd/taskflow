package com.winforrd.taskflow.vo;


import lombok.Data;


@Data
public class TaskStatusStatVO {


    /**
     * 状态：
     *
     * TODO
     * IN_PROGRESS
     * IN_REVIEW
     * DONE
     * CANCELLED
     */
    private String status;


    /**
     * 数量
     */
    private Long count;


}