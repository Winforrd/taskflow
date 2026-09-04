package com.winforrd.taskflow.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;


import lombok.Data;


import java.time.LocalDateTime;


@Data
@TableName("tag")
public class Tag {


    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * TEAM / PROJECT
     */
    private String scopeType;


    /**
     * TEAM:
     * teamId
     *
     * PROJECT:
     * projectId
     */
    private Long scopeId;


    private String name;


    private String color;


    private Long creatorId;


    private LocalDateTime createdTime;


    private LocalDateTime updatedTime;


    @TableLogic
    private Integer deleted;


}