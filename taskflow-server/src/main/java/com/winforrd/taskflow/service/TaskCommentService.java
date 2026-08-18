package com.winforrd.taskflow.service;

import com.winforrd.taskflow.dto.CreateTaskCommentRequest;
import com.winforrd.taskflow.vo.TaskCommentVO;

import java.util.List;

public interface TaskCommentService {

    /**
     * 查询任务评论
     */
    List<TaskCommentVO> getTaskComments(
            Long taskId
    );


    /**
     * 发表评论
     */
    Long createComment(Long taskId, CreateTaskCommentRequest request);


    /**
     * 删除评论
     */
    void deleteComment(
            Long commentId
    );


    /**
     * 当前用户是否允许评论
     */
    boolean canComment(
            Long taskId,
            Long userId
    );
}