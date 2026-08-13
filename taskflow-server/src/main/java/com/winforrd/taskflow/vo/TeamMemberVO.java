package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamMemberVO {

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private String roleType;

    private Integer memberStatus;

    private LocalDateTime joinedTime;
}