package com.winforrd.taskflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sub_task")
public class SubTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父任务ID
     */
    private Long taskId;

    /**
     * 子任务标题
     */
    private String title;

    /**
     * 子任务描述
     */
    private String description;

    /**
     * TODO / IN_PROGRESS / DONE / CANCELLED
     */
    private String status;

    /**
     * 是否必需：0否 1是
     */
    private Integer requiredFlag;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建人
     */
    private Long creatorId;

    /**
     * 完成用户
     */
    private Long completedBy;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
