package com.winforrd.taskflow.vo;


import lombok.Data;


import java.time.LocalDateTime;


@Data
public class TagVO {


    private Long id;


    /**
     * TEAM / PROJECT
     */
    private String scopeType;


    private Long scopeId;


    private String name;


    private String color;


    private Long creatorId;


    private LocalDateTime createdTime;


}