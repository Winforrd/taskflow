package com.winforrd.taskflow.vo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardVO {

    /**
     * 我的待处理任务数
     */
    private Long todoCount;

    /**
     * 我的进行中任务数
     */
    private Long inProgressCount;

    /**
     * 待我验收任务数
     */
    private Long reviewCount;

    /**
     * 我的逾期任务数
     */
    private Long overdueCount;

    /**
     * 我的已完成任务数
     */
    private Long doneCount;

    /**
     * 我的任务总数
     *
     * 用于计算完成率
     */
    private Long assignedTotalCount;

    /**
     * 完成率
     *
     * 0 ~ 100
     */
    private Integer completionRate;

    /**
     * 最近任务
     */
    private List<DashboardTaskVO> recentTasks;

    /**
     * 最近动态
     */
    private List<DashboardActivityVO> recentActivities;
}