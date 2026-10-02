package com.winforrd.taskflow.extension.provider;


import com.winforrd.taskflow.extension.ExtensionProvider;


import java.time.LocalDate;

import java.util.List;


/**
 * =========================================================
 * 项目日历扩展接口
 * =========================================================
 *
 * 当前Task已经具备：
 *
 * startDate
 * deadline
 *
 * 后续可以通过这个Provider把任务映射成：
 *
 * 项目日历
 * 周视图
 * 月视图
 * 甘特图
 *
 * 而不修改Task核心领域。
 */
public interface ProjectCalendarProvider
        extends ExtensionProvider {


    /**
     * 查询一个时间范围内需要展示的任务ID。
     *
     * 后续真正实现时，
     * 可以进一步升级成CalendarEventVO。
     */
    List<Long> listTaskIds(Long projectId, LocalDate startDate, LocalDate endDate);


}