package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DashboardTaskVO {

    private Long id;

    private Long projectId;

    private String projectName;

    private String title;

    private String status;

    private String priority;

    private LocalDate deadline;

    /**
     * 是否逾期
     */
    private Boolean overdue;
}