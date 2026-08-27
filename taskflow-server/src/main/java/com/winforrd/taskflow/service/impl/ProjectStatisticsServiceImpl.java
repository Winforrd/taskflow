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
public class ProjectStatisticsServiceImpl
        implements ProjectStatisticsService {


    private final ProjectStatisticsMapper
            projectStatisticsMapper;


    private final ProjectMemberService
            projectMemberService;


    /**
     * =====================================================
     * 项目统计
     * =====================================================
     */
    @Override
    public ProjectStatisticsVO getStatistics(
            Long projectId
    ) {


        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        /**
         * =================================================
         * 数据权限。
         *
         * 直接复用你现有项目成员权限逻辑。
         *
         * 不能因为这是统计接口，
         * 就允许用户通过修改projectId查看别的项目。
         * =================================================
         */
        projectMemberService.checkCanView(
                projectId,
                currentUserId
        );


        Long totalTaskCount =
                safeLong(
                        projectStatisticsMapper
                                .countAllTasks(
                                        projectId
                                )
                );


        Long validTaskCount =
                safeLong(
                        projectStatisticsMapper
                                .countValidTasks(
                                        projectId
                                )
                );


        Long doneTaskCount =
                safeLong(
                        projectStatisticsMapper
                                .countDoneTasks(
                                        projectId
                                )
                );


        Long overdueTaskCount =
                safeLong(
                        projectStatisticsMapper
                                .countOverdueTasks(
                                        projectId
                                )
                );


        ProjectStatisticsVO vo =
                new ProjectStatisticsVO();


        vo.setProjectId(
                projectId
        );


        vo.setTotalTaskCount(
                totalTaskCount
        );


        vo.setValidTaskCount(
                validTaskCount
        );


        vo.setDoneTaskCount(
                doneTaskCount
        );


        vo.setOverdueTaskCount(
                overdueTaskCount
        );


        /**
         * 项目完成率。
         *
         * DONE / 有效任务
         */
        vo.setCompletionRate(
                calculateCompletionRate(
                        doneTaskCount,
                        validTaskCount
                )
        );


        vo.setStatusDistribution(
                buildStatusDistribution(
                        projectId
                )
        );


        vo.setCompletionTrend(
                buildCompletionTrend(
                        projectId
                )
        );


        vo.setMemberWorkloads(
                projectStatisticsMapper
                        .selectMemberWorkloads(
                                projectId
                        )
        );


        return vo;
    }


    /**
     * =====================================================
     * 完成率
     * =====================================================
     */
    private BigDecimal calculateCompletionRate(
            Long doneCount,
            Long validCount
    ) {


        if (
                validCount == null
                        ||
                        validCount <= 0
        ) {

            return BigDecimal.ZERO
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );
        }


        return BigDecimal
                .valueOf(
                        doneCount
                )
                .multiply(
                        BigDecimal.valueOf(
                                100
                        )
                )
                .divide(
                        BigDecimal.valueOf(
                                validCount
                        ),
                        2,
                        RoundingMode.HALF_UP
                );
    }


    /**
     * =====================================================
     * 状态分布。
     *
     * 即使某个状态是0，
     * 前端也应该拿到完整五种状态。
     * =====================================================
     */
    private List<TaskStatusStatVO>
    buildStatusDistribution(
            Long projectId
    ) {


        List<TaskStatusStatVO> raw =
                projectStatisticsMapper
                        .selectStatusDistribution(
                                projectId
                        );


        Map<String, Long> countMap =
                new HashMap<>();


        if (raw != null) {

            for (
                    TaskStatusStatVO item :
                    raw
            ) {

                countMap.put(
                        item.getStatus(),
                        safeLong(
                                item.getCount()
                        )
                );
            }
        }


        List<TaskStatusStatVO> result =
                new ArrayList<>();


        result.add(
                createStatusStat(
                        "TODO",
                        countMap.get(
                                "TODO"
                        )
                )
        );


        result.add(
                createStatusStat(
                        "IN_PROGRESS",
                        countMap.get(
                                "IN_PROGRESS"
                        )
                )
        );


        result.add(
                createStatusStat(
                        "IN_REVIEW",
                        countMap.get(
                                "IN_REVIEW"
                        )
                )
        );


        result.add(
                createStatusStat(
                        "DONE",
                        countMap.get(
                                "DONE"
                        )
                )
        );


        result.add(
                createStatusStat(
                        "CANCELLED",
                        countMap.get(
                                "CANCELLED"
                        )
                )
        );


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
    private List<ProjectTrendPointVO>
    buildCompletionTrend(
            Long projectId
    ) {


        LocalDate today =
                LocalDate.now();


        LocalDate startDate =
                today.minusDays(
                        13
                );


        List<ProjectTrendPointVO> raw =
                projectStatisticsMapper
                        .selectCompletionTrend(
                                projectId,
                                startDate
                        );


        Map<LocalDate, Long> countMap =
                new HashMap<>();


        if (raw != null) {

            for (
                    ProjectTrendPointVO point :
                    raw
            ) {

                countMap.put(
                        point.getDate(),
                        safeLong(
                                point.getCompletedCount()
                        )
                );
            }
        }


        List<ProjectTrendPointVO> result =
                new ArrayList<>();


        for (
                int i = 0;
                i < 14;
                i++
        ) {


            LocalDate date =
                    startDate.plusDays(
                            i
                    );


            ProjectTrendPointVO point =
                    new ProjectTrendPointVO();


            point.setDate(
                    date
            );


            point.setCompletedCount(
                    countMap.getOrDefault(
                            date,
                            0L
                    )
            );


            result.add(
                    point
            );
        }


        return result;
    }


    private TaskStatusStatVO createStatusStat(
            String status,
            Long count
    ) {


        TaskStatusStatVO vo =
                new TaskStatusStatVO();


        vo.setStatus(
                status
        );


        vo.setCount(
                safeLong(
                        count
                )
        );


        return vo;
    }


    /**
     * 防止Mapper空值影响统计。
     */
    private Long safeLong(
            Long value
    ) {

        return value == null
                ? 0L
                : value;
    }


}