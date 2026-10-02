package com.winforrd.taskflow.vo;


import lombok.Data;


import java.time.LocalDateTime;


@Data
public class OperationLogVO {

    /**
     * ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 团队ID
     */
    private Long teamId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 模块
     */
    private String module;

    /**
     * 操作
     */
    private String operation;

    /**
     * HTTP方法
     */
    private String httpMethod;

    /**
     * 请求URI
     */
    private String requestUri;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * IP地址
     */
    private String ip;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 耗时（毫秒）
     */
    private Long costMs;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;


}