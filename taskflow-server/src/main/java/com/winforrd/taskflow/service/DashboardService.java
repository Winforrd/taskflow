package com.winforrd.taskflow.service;

import com.winforrd.taskflow.vo.DashboardVO;

public interface DashboardService {

    /**
     * 获取当前用户工作台数据
     */
    DashboardVO getDashboard();
}