package com.winforrd.taskflow.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import lombok.Data;


import java.time.LocalDateTime;



@Data
@TableName("reminder_record")
public class ReminderRecord {



    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 接收用户
     */
    private Long userId;

    /**
     * 提醒类型
     *
     * DEADLINE_24H
     * DEADLINE_2H
     * DEADLINE_OVERDUE
     */
    private String reminderType;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;



}