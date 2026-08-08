package com.winforrd.taskflow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    PARAM_ERROR(
            400,
            "请求参数错误"
    ),

    PASSWORD_TOO_SHORT(
            40003,
            "密码长度不能少于6位"
    ),

    UNAUTHORIZED(
            401,
            "未登录或登录已过期"
    ),

    FORBIDDEN(
            403,
            "没有操作权限"
    ),

    USER_NOT_FOUND(
            40401,
            "用户不存在"
    ),

    USERNAME_EXISTS(
            40001,
            "用户名已经存在"
    ),

    PASSWORD_ERROR(
            40002,
            "用户名或密码错误"
    ),

    USER_DISABLED(
            40301,
            "账号已被禁用"
    ),

    TEAM_NOT_FOUND(
            40402,
            "团队不存在"
    ),

    PROJECT_NOT_FOUND(
            40403,
            "项目不存在"
    ),

    TASK_NOT_FOUND(
            40404,
            "任务不存在"
    );

    private final Integer code;

    private final String message;
}