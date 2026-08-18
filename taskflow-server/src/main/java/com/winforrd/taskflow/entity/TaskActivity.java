package com.winforrd.taskflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_activity")
public class TaskActivity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * CREATED：任务被创建
     * UPDATED：任务基础信息修改（标题、描述、截止时间、优先级等）
     * STATUS_CHANGED：任务状态发生变化
     * ASSIGNEE_ADDED：新增任务负责人
     * ASSIGNEE_REMOVED：移除任务负责人
     */
    private String activityType;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}