package com.winforrd.taskflow.extension;


/**
 * =========================================================
 * TaskFlow统一扩展能力接口
 * =========================================================
 *
 * 所有扩展模块都必须实现这个接口。
 *
 * 这样系统可以统一：
 *
 * 1. 识别扩展类型
 * 2. 判断是否启用
 * 3. 获取扩展名称
 * 4. 获取扩展说明
 *
 *
 * 当前扩展：
 *
 * APPROVAL
 * CALENDAR
 * WORKTIME
 * SMART_ASSIGNMENT
 */
public interface ExtensionProvider {


    /**
     * 扩展编码。
     */
    ExtensionCode getCode();


    /**
     * 显示名称。
     */
    String getDisplayName();


    /**
     * 扩展说明。
     */
    String getDescription();


    /**
     * 是否启用。
     *
     * 当前默认实现全部false。
     *
     * 后续真正实现功能时返回true。
     */
    boolean isEnabled();


}