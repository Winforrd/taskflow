package com.winforrd.taskflow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    PARAM_ERROR(400, "请求参数错误"),

    PASSWORD_TOO_SHORT(400, "密码长度不能少于6位"),

    USER_NOT_FOUND(400, "用户不存在"),

    USERNAME_EXISTS(400, "用户名已经存在"),

    PASSWORD_ERROR(400, "用户名或密码错误"),

    USER_DISABLED(400, "账号已被禁用"),

    TASK_NOT_FOUND(404, "任务不存在"),

    EMAIL_EXISTS(400, "该邮箱已经被使用"),

    OLD_PASSWORD_ERROR(400, "原密码错误"),

    NEW_PASSWORD_SAME(400, "新密码不能与原密码相同"),

    TEAM_NOT_FOUND(404, "团队不存在"),

    TEAM_MEMBER_NOT_FOUND(404, "团队成员不存在"),

    NOT_TEAM_MEMBER(403, "你不是该团队成员"),

    TEAM_PERMISSION_DENIED(403, "没有该团队操作权限"),

    TEAM_OWNER_CANNOT_QUIT(403, "团队所有者不能直接退出团队"),

    TEAM_OWNER_CANNOT_REMOVE(403, "不能移除团队所有者"),

    TEAM_OWNER_ROLE_CANNOT_CHANGE(403, "不能直接修改团队所有者角色"),

    TEAM_MEMBER_ALREADY_EXISTS(400, "用户已经是该团队成员");

    private final Integer code;
    private final String message;
}