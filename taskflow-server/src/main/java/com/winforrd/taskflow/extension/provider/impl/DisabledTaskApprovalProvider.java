package com.winforrd.taskflow.extension.provider.impl;


import com.winforrd.taskflow.common.exception.BusinessException;

import com.winforrd.taskflow.extension.ExtensionCode;

import com.winforrd.taskflow.extension.provider.TaskApprovalProvider;


import org.springframework.stereotype.Component;


/**
 * =========================================================
 * 默认审批流Provider
 * =========================================================
 *
 * 当前审批流尚未启用。
 */
@Component
public class DisabledTaskApprovalProvider implements TaskApprovalProvider {


    @Override
    public ExtensionCode getCode() {

        return ExtensionCode.APPROVAL;
    }


    @Override
    public String getDisplayName() {

        return "任务审批流";
    }


    @Override
    public String getDescription() {

        return "为任务提交、审批通过、审批拒绝等多节点审批能力预留扩展接口";
    }


    @Override
    public boolean isEnabled() {

        return false;
    }


    @Override
    public void submit(

            Long taskId,

            Long operatorId,

            String comment

    ) {

        throw disabled();
    }


    @Override
    public void approve(

            Long taskId,

            Long operatorId,

            String comment

    ) {

        throw disabled();
    }


    @Override
    public void reject(

            Long taskId,

            Long operatorId,

            String comment

    ) {

        throw disabled();
    }


    private BusinessException disabled() {

        return new BusinessException(
                "审批流扩展尚未启用"
        );
    }


}