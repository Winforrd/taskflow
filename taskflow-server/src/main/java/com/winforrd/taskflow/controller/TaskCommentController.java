package com.winforrd.taskflow.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.dto.CreateTaskCommentRequest;
import com.winforrd.taskflow.service.TaskCommentService;
import com.winforrd.taskflow.vo.TaskCommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService taskCommentService;


    /**
     * 查询任务评论
     */
    @GetMapping("/api/tasks/{taskId}/comments")
    public Result<List<TaskCommentVO>> getTaskComments(@PathVariable Long taskId) {
        return Result.success(taskCommentService.getTaskComments(taskId));
    }
    /**
     * 发布评论
     */
    @PostMapping("/api/tasks/{taskId}/comments")
    public Result<Long> createComment(@PathVariable Long taskId, @RequestBody @Valid CreateTaskCommentRequest request) {
        return Result.success(taskCommentService.createComment(taskId, request));
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/api/task-comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        taskCommentService.deleteComment(commentId);
        return Result.success();
    }

    /**
     * 查询当前用户是否有评论权限
     */
    @GetMapping("/api/tasks/{taskId}/comment-permission")
    public Result<Boolean> canComment(@PathVariable Long taskId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        return Result.success(taskCommentService.canComment(taskId, currentUserId)
        );
    }
}