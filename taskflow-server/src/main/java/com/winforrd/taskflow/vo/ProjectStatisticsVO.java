package com.winforrd.taskflow.vo;


import lombok.Data;


import java.math.BigDecimal;

import java.util.List;


@Data
public class ProjectStatisticsVO {


    /**
     * 项目ID
     */
    private Long projectId;


    /**
     * 全部任务数。
     *
     * 包含CANCELLED。
     */
    private Long totalTaskCount;


    /**
     * 有效任务数。
     *
     * 不包含CANCELLED。
     */
    private Long validTaskCount;


    /**
     * 已完成任务数。
     */
    private Long doneTaskCount;


    /**
     * 当前逾期数量。
     */
    private Long overdueTaskCount;


    /**
     * 项目完成率。
     *
     * 0 ~ 100
     */
    private BigDecimal completionRate;


    /**
     * 五种状态分布。
     */
    private List<TaskStatusStatVO> statusDistribution;


    /**
     * 最近14天完成趋势。
     */
    private List<ProjectTrendPointVO> completionTrend;


    /**
     * 项目成员工作量。
     */
    private List<MemberWorkloadVO> memberWorkloads;


}