package com.winforrd.taskflow.extension.provider.impl;


import com.winforrd.taskflow.common.exception.BusinessException;

import com.winforrd.taskflow.extension.ExtensionCode;

import com.winforrd.taskflow.extension.provider.ProjectCalendarProvider;


import org.springframework.stereotype.Component;


import java.time.LocalDate;

import java.util.List;


/**
 * =========================================================
 * 默认项目日历Provider
 * =========================================================
 */
@Component
public class DisabledProjectCalendarProvider
        implements ProjectCalendarProvider {


    @Override
    public ExtensionCode getCode() {return ExtensionCode.CALENDAR;}

    @Override
    public String getDisplayName() {
        return "项目日历";
    }

    @Override
    public String getDescription() {

        return "基于任务开始日期与截止时间，为月历、周历和后续甘特图能力预留接口";
    }


    @Override
    public boolean isEnabled() {

        return false;
    }


    @Override
    public List<Long> listTaskIds(Long projectId, LocalDate startDate, LocalDate endDate) {

        throw new BusinessException("项目日历扩展尚未启用");
    }


}