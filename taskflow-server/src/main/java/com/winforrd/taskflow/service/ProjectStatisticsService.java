package com.winforrd.taskflow.service;


import com.winforrd.taskflow.vo.ProjectStatisticsVO;


public interface ProjectStatisticsService {


    /**
     * 项目统计。
     */
    ProjectStatisticsVO getStatistics(Long projectId);


}