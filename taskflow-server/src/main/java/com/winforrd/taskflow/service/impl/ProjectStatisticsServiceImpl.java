package com.winforrd.taskflow.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import com.winforrd.taskflow.mapper.ProjectStatisticsMapper;
import com.winforrd.taskflow.service.ProjectMemberService;
import com.winforrd.taskflow.service.ProjectStatisticsService;
import com.winforrd.taskflow.vo.ProjectStatisticsVO;
import com.winforrd.taskflow.vo.ProjectTrendPointVO;
import com.winforrd.taskflow.vo.TaskStatusStatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ProjectStatisticsServiceImpl implements ProjectStatisticsService {

    private final ProjectStatisticsMapper projectStatisticsMapper;

    private final ProjectMemberService projectMemberService;

    /**
     * =====================================================
     * 项目统计
     * =====================================================
     */
    @Override
    public ProjectStatisticsVO getStatistics(Long projectId) {
        // 获取当前登录用户
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 校验当前用户是否有权限查看该项目
        projectMemberService.checkCanView(projectId, currentUserId);
        // 获取项目统计信息
        Long totalTaskCount = safeLong(projectStatisticsMapper.countAllTasks(projectId));
        // 获取有效任务数
        Long validTaskCount = safeLong(projectStatisticsMapper.countValidTasks(projectId));
        // 获取完成任务数
        Long doneTaskCount = safeLong(projectStatisticsMapper.countDoneTasks(projectId));
        // 获取逾期任务数
        Long overdueTaskCount = safeLong(projectStatisticsMapper.countOverdueTasks(projectId));
        // 创建项目统计VO对象
        ProjectStatisticsVO vo = new ProjectStatisticsVO();
        vo.setProjectId(projectId);
        vo.setTotalTaskCount(totalTaskCount);
        vo.setValidTaskCount(validTaskCount);
        vo.setDoneTaskCount(doneTaskCount);
        vo.setOverdueTaskCount(overdueTaskCount);
        // 设置项目完成率
        vo.setCompletionRate(calculateCompletionRate(doneTaskCount, validTaskCount));
        vo.setStatusDistribution(buildStatusDistribution(projectId));
        vo.setCompletionTrend(buildCompletionTrend(projectId));
        vo.setMemberWorkloads(projectStatisticsMapper.selectMemberWorkloads(projectId));
        return vo;
    }

    /**
     * =====================================================
     * 完成率
     * =====================================================
     */
    private BigDecimal calculateCompletionRate(Long doneCount, Long validCount) {
        // 避免除以零
        if (validCount == null || validCount <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(doneCount).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(validCount), 2, RoundingMode.HALF_UP);
    }

    /**
     * =====================================================
     * 状态分布。
     * 即使某个状态是0，
     * 前端也应该拿到完整五种状态。
     * =====================================================
     */
    private List<TaskStatusStatVO>
    buildStatusDistribution(Long projectId) {
        // 获取项目状态分布
        List<TaskStatusStatVO> raw = projectStatisticsMapper.selectStatusDistribution(projectId);
        // 创建一个Map来存储状态和对应的数量
        Map<String, Long> countMap = new HashMap<>();
        // 遍历原始数据并填充Map
        if (raw != null) {
            for (TaskStatusStatVO item : raw) {
                countMap.put(item.getStatus(), safeLong(item.getCount()));}
        }
        // 创建结果列表
        List<TaskStatusStatVO> result = new ArrayList<>();
        result.add(createStatusStat("TODO", countMap.get("TODO")));
        result.add(createStatusStat("IN_PROGRESS", countMap.get("IN_PROGRESS")));
        result.add(createStatusStat("IN_REVIEW", countMap.get("IN_REVIEW")));
        result.add(createStatusStat("DONE", countMap.get("DONE")));
        result.add(createStatusStat("CANCELLED", countMap.get("CANCELLED")));
        return result;
    }


    /**
     * =====================================================
     * 最近14天完成趋势。
     *
     * SQL没有数据的日期，
     * 在这里自动补0。
     * =====================================================
     */
    private List<ProjectTrendPointVO> buildCompletionTrend(Long projectId) {
        // 获取今天和14天前的日期
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(13);
        // 获取最近14天的完成趋势数据
        List<ProjectTrendPointVO> raw = projectStatisticsMapper.selectCompletionTrend(projectId, startDate);
        // 创建一个Map来存储日期和对应的完成数量
        Map<LocalDate, Long> countMap = new HashMap<>();
        if (raw != null) {
            for (ProjectTrendPointVO point : raw) {
                countMap.put(point.getDate(), safeLong(point.getCompletedCount()));}
        }
        // 创建结果列表
        List<ProjectTrendPointVO> result = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            LocalDate date = startDate.plusDays(i);
            ProjectTrendPointVO point = new ProjectTrendPointVO();
            point.setDate(date);
            point.setCompletedCount(countMap.getOrDefault(date, 0L));
            result.add(point);
        }
        return result;
    }

    private TaskStatusStatVO createStatusStat(String status, Long count) {
        // 创建任务状态统计VO对象
        TaskStatusStatVO vo = new TaskStatusStatVO();
        vo.setStatus(status);
        vo.setCount(safeLong(count));
        return vo;
    }

    /**
     * 防止Mapper空值影响统计。
     */
    private Long safeLong(Long value) {
        return value == null ? 0L : value;
    }

}