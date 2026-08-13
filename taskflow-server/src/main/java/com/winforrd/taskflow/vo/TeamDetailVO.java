package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamDetailVO {

    private Long id;

    private String name;

    private String description;

    private String avatar;

    private Long ownerId;

    private String myRole;

    private Integer memberCount;

    private Integer status;

    private LocalDateTime createdTime;
}