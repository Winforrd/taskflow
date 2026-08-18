package com.winforrd.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目
     */
    private Long projectId;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * TODO  待办
     * IN_PROGRESS  进行中
     * IN_REVIEW    审核中
     * DONE         完成
     * CANCELLED    取消
     */
    private String status;

    /**
     * LOW          低
     * MEDIUM       中
     * HIGH         高
     * URGENT
     */
    private String priority;

    /**
     * 创建人
     */
    private Long creatorId;

    private LocalDate startDate;

    private LocalDate deadline;

    private LocalDateTime completedTime;

    /**
     * 乐观锁
     */
    @Version
    private Integer version;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}