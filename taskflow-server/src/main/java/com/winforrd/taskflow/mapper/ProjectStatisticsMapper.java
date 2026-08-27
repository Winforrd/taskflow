package com.winforrd.taskflow.mapper;


import com.winforrd.taskflow.vo.MemberWorkloadVO;

import com.winforrd.taskflow.vo.ProjectTrendPointVO;

import com.winforrd.taskflow.vo.TaskStatusStatVO;


import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;


import java.time.LocalDate;

import java.util.List;


@Mapper
public interface ProjectStatisticsMapper {


    /**
     * 项目全部任务。
     */
    Long countAllTasks(
            @Param("projectId")
            Long projectId
    );


    /**
     * 有效任务。
     *
     * 不统计CANCELLED。
     */
    Long countValidTasks(
            @Param("projectId")
            Long projectId
    );


    /**
     * 已完成。
     */
    Long countDoneTasks(
            @Param("projectId")
            Long projectId
    );


    /**
     * 逾期任务。
     */
    Long countOverdueTasks(
            @Param("projectId")
            Long projectId
    );


    /**
     * 状态分布。
     */
    List<TaskStatusStatVO>
    selectStatusDistribution(
            @Param("projectId")
            Long projectId
    );


    /**
     * 从某一天开始统计完成趋势。
     *
     * SQL只返回真实存在任务完成的数据。
     * 没有完成任务的日期由Service补0。
     */
    List<ProjectTrendPointVO>
    selectCompletionTrend(
            @Param("projectId")
            Long projectId,

            @Param("startDate")
            LocalDate startDate
    );


    /**
     * 成员工作量。
     */
    List<MemberWorkloadVO>
    selectMemberWorkloads(
            @Param("projectId")
            Long projectId
    );


}