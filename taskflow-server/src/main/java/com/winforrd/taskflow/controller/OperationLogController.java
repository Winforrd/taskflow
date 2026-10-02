package com.winforrd.taskflow.controller;


import com.winforrd.taskflow.common.result.Result;

import com.winforrd.taskflow.service.OperationLogService;

import com.winforrd.taskflow.vo.OperationLogVO;


import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;


import java.util.List;


@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;
    /**
     * 获取项目操作日志
     */
    @GetMapping("/{projectId}/operation-logs")
    public Result<List<OperationLogVO>> getProjectOperationLogs(@PathVariable Long projectId, @RequestParam(required = false) String module, @RequestParam(required = false) Boolean success, @RequestParam(required = false) String keyword) {

        return Result.success(operationLogService.getProjectLogs(projectId, module, success, keyword));
    }
}