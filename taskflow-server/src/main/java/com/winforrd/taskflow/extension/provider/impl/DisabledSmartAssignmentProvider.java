package com.winforrd.taskflow.extension.provider.impl;


import com.winforrd.taskflow.common.exception.BusinessException;

import com.winforrd.taskflow.extension.ExtensionCode;

import com.winforrd.taskflow.extension.provider.SmartAssignmentProvider;


import org.springframework.stereotype.Component;


import java.util.List;


/**
 * =========================================================
 * 默认智能分派Provider
 * =========================================================
 */
@Component
public class DisabledSmartAssignmentProvider
        implements SmartAssignmentProvider {


    @Override
    public ExtensionCode getCode() {

        return ExtensionCode.SMART_ASSIGNMENT;
    }


    @Override
    public String getDisplayName() {

        return "智能任务分派";
    }


    @Override
    public String getDescription() {

        return "为基于成员工作量、历史任务和技能特征的负责人推荐算法预留接口";
    }


    @Override
    public boolean isEnabled() {

        return false;
    }


    @Override
    public List<Long> recommend(

            Long projectId,

            Long taskId,

            Integer limit

    ) {

        throw new BusinessException(
                "智能任务分派扩展尚未启用"
        );
    }


}