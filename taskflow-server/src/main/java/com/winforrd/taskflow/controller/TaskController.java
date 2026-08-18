package com.winforrd.taskflow.controller;

import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.dto.ChangeTaskStatusRequest;
import com.winforrd.taskflow.dto.CreateTaskRequest;
import com.winforrd.taskflow.dto.UpdateTaskRequest;
import com.winforrd.taskflow.service.TaskService;
import com.winforrd.taskflow.vo.TaskActivityVO;
import com.winforrd.taskflow.vo.TaskVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;


    /**
     * 创建任务
     */
    @PostMapping("/api/projects/{projectId}/tasks")
    public Result<Long> createTask(@PathVariable Long projectId, @RequestBody @Valid CreateTaskRequest request) {
        return Result.success(taskService.createTask(projectId, request));
    }

    /**
     * 项目任务
     */
    @GetMapping("/api/projects/{projectId}/tasks")
    public Result<List<TaskVO>> getProjectTasks(@PathVariable Long projectId, @RequestParam(required = false) String status, @RequestParam(required = false) String keyword) {
        return Result.success(taskService.getProjectTasks(projectId, status, keyword));
    }

    /**
     * 任务详情
     */
    @GetMapping("/api/tasks/{taskId}")
    public Result<TaskVO> getTaskDetail(@PathVariable Long taskId) {
        return Result.success(taskService.getTaskDetail(taskId));
    }

    /**
     * 修改任务
     */
    @PutMapping("/api/tasks/{taskId}")
    public Result<Void> updateTask(@PathVariable Long taskId, @RequestBody @Valid UpdateTaskRequest request) {
        taskService.updateTask(taskId, request);
        return Result.success();
    }

    /**
     * 状态流转
     */
    @PostMapping("/api/tasks/{taskId}/status")
    public Result<Void> changeStatus(@PathVariable Long taskId, @RequestBody @Valid ChangeTaskStatusRequest request) {
        taskService.changeTaskStatus(taskId, request);
        return Result.success();
    }

    /**
     * 添加负责人
     */
    @PostMapping("/api/tasks/{taskId}/assignees/{userId}")
    public Result<Void> addAssignee(@PathVariable Long taskId, @PathVariable Long userId) {
        taskService.addAssignee(taskId, userId);
        return Result.success();
    }

    /**
     * 移除负责人
     */
    @DeleteMapping("/api/tasks/{taskId}/assignees/{userId}")
    public Result<Void> removeAssignee(@PathVariable Long taskId, @PathVariable Long userId) {
        taskService.removeAssignee(taskId, userId);return Result.success();
    }

    /**
     * 任务动态
     */
    @GetMapping("/api/tasks/{taskId}/activities")
    public Result<List<TaskActivityVO>> getTaskActivities(@PathVariable Long taskId) {
        return Result.success(taskService.getTaskActivities(taskId));
    }
}