package com.winforrd.taskflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_assignee")
public class TaskAssignee {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 被指派人ID
     */
    private Long userId;

    /**
     * 指派人ID
     */
    private Long assignedBy;

    /**
     * 指派时间
     */
    private LocalDateTime assignedTime;
}