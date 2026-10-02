package com.winforrd.taskflow.controller;


import com.winforrd.taskflow.common.result.Result;

import com.winforrd.taskflow.service.ProjectStatisticsService;

import com.winforrd.taskflow.vo.ProjectStatisticsVO;


import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectStatisticsController {


    private final ProjectStatisticsService projectStatisticsService;


    /**
     * =====================================================
     * 项目统计
     * =====================================================
     *
     * GET
     *
     * /api/projects/{projectId}/statistics
     */
    @GetMapping("/{projectId}/statistics")
    public Result<ProjectStatisticsVO> getStatistics(@PathVariable Long projectId) {

        return Result.success(projectStatisticsService.getStatistics(projectId));
    }


}