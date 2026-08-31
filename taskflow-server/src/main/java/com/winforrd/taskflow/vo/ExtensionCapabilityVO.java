package com.winforrd.taskflow.vo;


import lombok.Data;


/**
 * =========================================================
 * 扩展能力状态
 * =========================================================
 */
@Data
public class ExtensionCapabilityVO {


    /**
     * APPROVAL
     * CALENDAR
     * WORKTIME
     * SMART_ASSIGNMENT
     */
    private String code;


    /**
     * 显示名称。
     */
    private String name;


    /**
     * 功能说明。
     */
    private String description;


    /**
     * 当前是否启用。
     */
    private Boolean enabled;


}