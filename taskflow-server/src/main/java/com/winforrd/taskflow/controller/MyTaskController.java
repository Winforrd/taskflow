package com.winforrd.taskflow.controller;

import com.winforrd.taskflow.common.result.PageResult;
import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.dto.MyTaskQuery;
import com.winforrd.taskflow.service.MyTaskService;
import com.winforrd.taskflow.vo.MyTaskVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class MyTaskController {

    private final MyTaskService myTaskService;


    /**
     * 我的任务
     *
     * GET /api/tasks/my
     */
    @GetMapping("/my")
    public Result<PageResult<MyTaskVO>> getMyTasks(@Valid MyTaskQuery query) {

        return Result.success(myTaskService.getMyTasks(query));
    }
}