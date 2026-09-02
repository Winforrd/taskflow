package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubTaskVO {

    private Long id;

    private Long taskId;

    private String title;

    private String description;

    private String status;

    private Integer requiredFlag;

    private Integer sortOrder;

    private Long creatorId;

    private String creatorName;

    private LocalDateTime completedTime;

    /**
     * 当前用户是否可以修改该子任务
     */
    private Boolean canEdit;

    /**
     * 当前用户是否可以删除该子任务
     */
    private Boolean canDelete;

    /**
     * 当前用户是否可以完成 / 取消完成该子任务
     */
    private Boolean canChangeStatus;
}
