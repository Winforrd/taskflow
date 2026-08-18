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

    TEAM_MEMBER_ALREADY_EXISTS(400, "用户已经是该团队成员"),

    INVITATION_NOT_FOUND(404, "邀请码不存在"),

    INVITATION_EXPIRED(400, "邀请码已过期"),

    INVITATION_DISABLED(400, "邀请码已失效"),

    INVITATION_USED_UP(400, "邀请码使用次数已达到上限"),

    INVITATION_ROLE_INVALID(400, "邀请码默认角色不合法"),

    INVITATION_CONSUME_FAILED(400, "邀请码已失效，请重新获取"),

    PROJECT_PERMISSION_DENIED(403, "没有该项目操作权限"),

    PROJECT_MEMBER_NOT_FOUND(404, "项目成员不存在"),

    PROJECT_NOT_FOUND(404, "项目不存在"),

    PROJECT_MEMBER_ALREADY_EXISTS(400, "用户已经是项目成员"),

    PROJECT_OWNER_CANNOT_REMOVE(400, "不能移除项目负责人"),

    PROJECT_OWNER_ROLE_CANNOT_CHANGE(400, "不能修改项目负责人的项目角色"),

    PROJECT_ARCHIVED(400, "项目已归档，不能继续修改"),

    PROJECT_MEMBER_NOT_TEAM_MEMBER(400, "项目成员必须是团队成员"),

    PROJECT_DATE_INVALID(400, "项目截止日期不能早于开始日期"),

    PROJECT_STATUS_INVALID(400, "项目状态不合法"),

    PROJECT_CODE_GENERATE_FAILED(500, "项目编号生成失败"),

    TASK_NOT_FOUND(404, "任务不存在"),

    TASK_PERMISSION_DENIED(403, "没有该任务操作权限"),

    TASK_STATUS_TRANSITION_INVALID(400, "当前任务状态不允许执行该操作"),

    TASK_ASSIGNEE_NOT_PROJECT_MEMBER(400, "任务负责人必须是项目成员"),

    TASK_ASSIGNEE_ALREADY_EXISTS(400, "该用户已经是任务负责人"),

    TASK_ASSIGNEE_NOT_FOUND(404, "任务负责人不存在"),

    TASK_DATE_INVALID(400, "任务截止日期不能早于开始日期"),

    TASK_PRIORITY_INVALID(400, "任务优先级不合法"),

    TASK_ARCHIVED_PROJECT(400, "项目已归档，不能操作任务"),

    TASK_LAST_ASSIGNEE_CANNOT_REMOVE(400, "进行中或待验收的任务至少需要保留一名负责人"),

    TASK_CONCURRENT_MODIFICATION(409, "任务已被其他用户修改，请刷新后重试");

    private final Integer code;
    private final String message;
}