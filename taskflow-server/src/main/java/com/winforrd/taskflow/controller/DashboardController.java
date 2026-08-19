package com.winforrd.taskflow.controller;

import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.service.DashboardService;
import com.winforrd.taskflow.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;


    /**
     * 当前用户工作台
     *
     * GET /api/dashboard
     */
    @GetMapping
    public Result<DashboardVO> getDashboard() {

        return Result.success(dashboardService.getDashboard());
    }
}