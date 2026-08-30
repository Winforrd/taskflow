package com.winforrd.taskflow.vo;


import lombok.Data;


import java.time.LocalDateTime;


@Data
public class OperationLogVO {


    private Long id;


    private Long userId;


    private String username;


    private Long teamId;


    private Long projectId;


    private Long taskId;


    private String module;


    private String operation;


    private String httpMethod;


    private String requestUri;


    private String requestParams;


    private String ip;


    private String userAgent;


    private Boolean success;


    private String errorMessage;


    private Long costMs;


    private LocalDateTime createdTime;


}