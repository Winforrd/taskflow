package com.winforrd.taskflow.controller;

import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.dto.CreateSubTaskRequest;
import com.winforrd.taskflow.dto.UpdateSubTaskRequest;
import com.winforrd.taskflow.service.SubTaskService;
import com.winforrd.taskflow.vo.SubTaskVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubTaskController {

    private final SubTaskService subTaskService;

    /**
     * 查询任务子任务列表
     */
    @GetMapping("/tasks/{taskId}/subtasks")
    public Result<List<SubTaskVO>> list(@PathVariable Long taskId) {

        return Result.success(subTaskService.list(taskId));
    }


    /**
     * 创建子任务
     */
    @PostMapping("/tasks/{taskId}/subtasks")
    public Result<Void> create(@PathVariable Long taskId, @Valid @RequestBody CreateSubTaskRequest request) {

        subTaskService.create(taskId, request);

        return Result.success();
    }


    /**
     * 修改子任务
     */
    @PutMapping("/tasks/{taskId}/subtasks/{id}")
    public Result<Void> update(@PathVariable Long taskId, @PathVariable Long id, @Valid @RequestBody UpdateSubTaskRequest request) {

        subTaskService.update(taskId, id, request);

        return Result.success();
    }

    /**
     * 删除子任务
     */
    @DeleteMapping("/tasks/{taskId}/subtasks/{id}")
    public Result<Void> delete(@PathVariable Long taskId, @PathVariable Long id) {

        subTaskService.delete(taskId, id);

        return Result.success();
    }

    /**
     * 完成 / 取消完成子任务
     */
    @PostMapping("/tasks/{taskId}/subtasks/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long taskId, @PathVariable Long id) {

        subTaskService.changeStatus(taskId, id);

        return Result.success();
    }
}
