package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {

    private Long id;

    private String type;

    private String title;

    private String content;

    private String relatedType;

    private Long relatedId;

    private Boolean read;

    private LocalDateTime readTime;

    private LocalDateTime createdTime;
}