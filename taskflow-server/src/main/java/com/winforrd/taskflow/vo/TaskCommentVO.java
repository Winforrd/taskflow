package com.winforrd.taskflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskCommentVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 是否允许当前用户删除
     */
    private Boolean canDelete;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}