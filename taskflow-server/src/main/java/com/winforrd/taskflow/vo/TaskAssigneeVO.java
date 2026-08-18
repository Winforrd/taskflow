package com.winforrd.taskflow.vo;

import lombok.Data;

@Data
public class TaskAssigneeVO {

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;
}