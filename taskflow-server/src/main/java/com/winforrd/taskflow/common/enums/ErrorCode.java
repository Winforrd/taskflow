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
            403,
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
            401,
            "用户不存在"
    ),

    USERNAME_EXISTS(
            401,
            "用户名已经存在"
    ),

    PASSWORD_ERROR(
            402,
            "用户名或密码错误"
    ),

    USER_DISABLED(
            401,
            "账号已被禁用"
    ),

    TEAM_NOT_FOUND(
            402,
            "团队不存在"
    ),

    PROJECT_NOT_FOUND(
            403,
            "项目不存在"
    ),

    TASK_NOT_FOUND(
            404,
            "任务不存在"
    ),
    EMAIL_EXISTS(
            404,
            "该邮箱已经被使用"
    ),

    OLD_PASSWORD_ERROR(
            405,
            "原密码错误"
    ),

    NEW_PASSWORD_SAME(
            407,
            "新密码不能与原密码相同"
    );

    private final Integer code;

    private final String message;
}