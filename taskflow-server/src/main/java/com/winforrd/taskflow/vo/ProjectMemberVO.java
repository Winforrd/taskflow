package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectMemberVO {

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private String roleType;

    private Boolean projectOwner;

    private LocalDateTime joinedTime;
}