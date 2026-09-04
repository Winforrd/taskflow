package com.winforrd.taskflow.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import lombok.Data;


import java.time.LocalDateTime;


@Data
@TableName("task_tag")
public class TaskTag {


    @TableId(type = IdType.AUTO)
    private Long id;


    private Long taskId;


    private Long tagId;


    private Long createdBy;


    private LocalDateTime createdTime;


}