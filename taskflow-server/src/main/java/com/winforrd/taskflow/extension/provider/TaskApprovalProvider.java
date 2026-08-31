package com.winforrd.taskflow.extension.provider;


import com.winforrd.taskflow.extension.ExtensionProvider;


/**
 * =========================================================
 * 任务审批流扩展接口
 * =========================================================
 *
 * 当前阶段：
 *
 * 只定义扩展契约，
 * 不真正实现审批流程。
 *
 *
 * 后续可以实现：
 *
 * TaskApprovalProvider
 *        ↓
 * DatabaseTaskApprovalProvider
 *
 *
 * 支持：
 *
 * 提交审批
 * 审批通过
 * 审批拒绝
 */
public interface TaskApprovalProvider
        extends ExtensionProvider {


    /**
     * 提交任务审批。
     */
    void submit(

            Long taskId,

            Long operatorId,

            String comment

    );


    /**
     * 审批通过。
     */
    void approve(

            Long taskId,

            Long operatorId,

            String comment

    );


    /**
     * 审批拒绝。
     */
    void reject(

            Long taskId,

            Long operatorId,

            String comment

    );


}