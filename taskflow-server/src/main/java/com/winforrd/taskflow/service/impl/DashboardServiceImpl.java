package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.winforrd.taskflow.common.enums.TaskStatus;
import com.winforrd.taskflow.mapper.DashboardMapper;
import com.winforrd.taskflow.service.DashboardService;
import com.winforrd.taskflow.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl
        implements DashboardService {

    private final DashboardMapper dashboardMapper;


    /**
     * =====================================================
     * 当前用户工作台
     * =====================================================
     */
    @Override
    public DashboardVO getDashboard() {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        /**
         * 待处理
         */
        Long todoCount =
                dashboardMapper
                        .countAssignedTaskByStatus(

                                currentUserId,

                                TaskStatus.TODO.name()
                        );


        /**
         * 进行中
         */
        Long inProgressCount =
                dashboardMapper
                        .countAssignedTaskByStatus(

                                currentUserId,

                                TaskStatus.IN_PROGRESS.name()
                        );


        /**
         * 已完成
         */
        Long doneCount =
                dashboardMapper
                        .countAssignedTaskByStatus(

                                currentUserId,

                                TaskStatus.DONE.name()
                        );


        /**
         * 待我验收
         */
        Long reviewCount =
                dashboardMapper
                        .countReviewTasks(
                                currentUserId
                        );


        /**
         * 逾期
         */
        Long overdueCount =
                dashboardMapper
                        .countOverdueTasks(
                                currentUserId
                        );


        /**
         * 当前用户负责的所有非取消任务
         */
        Long assignedTotalCount =
                dashboardMapper
                        .countAssignedTasks(
                                currentUserId
                        );


        /**
         * 完成率
         */
        int completionRate = 0;


        if (
                assignedTotalCount != null
                        &&
                        assignedTotalCount > 0
        ) {

            completionRate =
                    (int) Math.round(

                            doneCount
                                    * 100.0
                                    /
                                    assignedTotalCount

                    );
        }


        DashboardVO vo =
                new DashboardVO();


        vo.setTodoCount(
                safeLong(todoCount)
        );


        vo.setInProgressCount(
                safeLong(inProgressCount)
        );


        vo.setReviewCount(
                safeLong(reviewCount)
        );


        vo.setOverdueCount(
                safeLong(overdueCount)
        );


        vo.setDoneCount(
                safeLong(doneCount)
        );


        vo.setAssignedTotalCount(
                safeLong(assignedTotalCount)
        );


        vo.setCompletionRate(
                completionRate
        );


        vo.setRecentTasks(

                dashboardMapper
                        .selectRecentTasks(
                                currentUserId
                        )

        );


        vo.setRecentActivities(

                dashboardMapper
                        .selectRecentActivities(
                                currentUserId
                        )

        );


        return vo;
    }


    /**
     * 防止 Mapper 意外返回 null
     */
    private Long safeLong(
            Long value
    ) {

        return value == null
                ? 0L
                : value;
    }
}