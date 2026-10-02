package com.winforrd.taskflow.extension.provider;


import com.winforrd.taskflow.extension.ExtensionProvider;


/**
 * =========================================================
 * 工时扩展接口
 * =========================================================
 *
 * 后续用于：
 *
 * 用户填写工时
 * 任务工时统计
 * 成员投入统计
 * 项目工时统计
 */
public interface WorktimeProvider
        extends ExtensionProvider {


    /**
     * 记录一次工时。
     *
     * @return 将来可以返回work_log主键ID
     */
    Long record(Long taskId, Long userId, Integer minutes, String description);


    /**
     * 查询任务累计工时。
     */
    Long getTaskTotalMinutes(Long taskId);


}