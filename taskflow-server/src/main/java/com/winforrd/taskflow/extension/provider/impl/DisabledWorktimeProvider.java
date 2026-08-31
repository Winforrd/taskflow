package com.winforrd.taskflow.extension.provider.impl;


import com.winforrd.taskflow.common.exception.BusinessException;

import com.winforrd.taskflow.extension.ExtensionCode;

import com.winforrd.taskflow.extension.provider.WorktimeProvider;


import org.springframework.stereotype.Component;


/**
 * =========================================================
 * 默认工时Provider
 * =========================================================
 */
@Component
public class DisabledWorktimeProvider
        implements WorktimeProvider {


    @Override
    public ExtensionCode getCode() {

        return ExtensionCode.WORKTIME;
    }


    @Override
    public String getDisplayName() {

        return "工时管理";
    }


    @Override
    public String getDescription() {

        return "为任务工时填报、成员工时统计和项目投入分析预留扩展接口";
    }


    @Override
    public boolean isEnabled() {

        return false;
    }


    @Override
    public Long record(

            Long taskId,

            Long userId,

            Integer minutes,

            String description

    ) {

        throw new BusinessException(
                "工时扩展尚未启用"
        );
    }


    @Override
    public Long getTaskTotalMinutes(
            Long taskId
    ) {

        throw new BusinessException(
                "工时扩展尚未启用"
        );
    }


}