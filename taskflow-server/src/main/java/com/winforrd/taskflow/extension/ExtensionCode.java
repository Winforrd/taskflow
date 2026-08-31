package com.winforrd.taskflow.extension;


/**
 * =========================================================
 * TaskFlow扩展能力编码
 * =========================================================
 *
 * 当前阶段仅作为扩展能力标识。
 *
 * 后续增加新的扩展模块时，
 * 统一在这里注册。
 */
public enum ExtensionCode {


    /**
     * 审批流
     */
    APPROVAL,


    /**
     * 项目日历
     */
    CALENDAR,


    /**
     * 工时管理
     */
    WORKTIME,


    /**
     * 智能任务分派
     */
    SMART_ASSIGNMENT


}