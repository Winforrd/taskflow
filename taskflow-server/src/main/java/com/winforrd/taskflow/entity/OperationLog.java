package com.winforrd.taskflow.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import lombok.Data;


import java.time.LocalDateTime;


@Data
@TableName("operation_log")
public class OperationLog {


    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 操作者。
     */
    private Long userId;


    /**
     * 操作者显示名称。
     */
    private String username;


    /**
     * 关联团队。
     */
    private Long teamId;


    /**
     * 关联项目。
     */
    private Long projectId;


    /**
     * 关联任务。
     */
    private Long taskId;


    /**
     * 模块：
     *
     * TEAM
     * PROJECT
     * TASK
     * COMMENT
     * NOTIFICATION
     * USER
     * OTHER
     */
    private String module;


    /**
     * 操作描述。
     */
    private String operation;


    /**
     * GET / POST / PUT / DELETE...
     */
    private String httpMethod;


    /**
     * 请求URI。
     */
    private String requestUri;


    /**
     * 请求参数。
     */
    private String requestParams;


    /**
     * 客户端IP。
     */
    private String ip;


    /**
     * 浏览器信息。
     */
    private String userAgent;


    /**
     * 1：成功
     * 0：失败
     */
    private Integer success;


    /**
     * 失败原因。
     */
    private String errorMessage;


    /**
     * 执行耗时。
     */
    private Long costMs;


    private LocalDateTime createdTime;


}