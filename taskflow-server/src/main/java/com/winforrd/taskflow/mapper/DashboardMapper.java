package com.winforrd.taskflow.mapper;

import com.winforrd.taskflow.vo.DashboardActivityVO;
import com.winforrd.taskflow.vo.DashboardTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardMapper {

    /**
     * 当前用户负责的某状态任务数量
     */
    Long countAssignedTaskByStatus(@Param("userId") Long userId, @Param("status") String status);


    /**
     * 当前用户负责的全部任务数量
     */
    Long countAssignedTasks(@Param("userId") Long userId);

    /**
     * 当前用户负责的逾期任务数
     */
    Long countOverdueTasks(@Param("userId") Long userId);
    /**
     * 待当前用户验收
     */
    Long countReviewTasks(@Param("userId") Long userId);


    /**
     * 最近任务
     */
    List<DashboardTaskVO> selectRecentTasks(@Param("userId") Long userId);


    /**
     * 最近任务动态
     */
    List<DashboardActivityVO> selectRecentActivities(@Param("userId") Long userId);
}