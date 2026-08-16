package com.winforrd.taskflow.controller;

import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.dto.CreateProjectRequest;
import com.winforrd.taskflow.dto.UpdateProjectRequest;
import com.winforrd.taskflow.service.ProjectService;
import com.winforrd.taskflow.vo.ProjectDetailVO;
import com.winforrd.taskflow.vo.ProjectVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 创建项目
     */
    @PostMapping("/api/teams/{teamId}/projects")
    public Result<Long> createProject(@PathVariable Long teamId, @RequestBody @Valid CreateProjectRequest request) {
        return Result.success(projectService.createProject(teamId, request));
    }

    /**
     * 团队项目列表
     */
    @GetMapping("/api/teams/{teamId}/projects")
    public Result<List<ProjectVO>> getTeamProjects(@PathVariable Long teamId) {
        return Result.success(projectService.getTeamProjects(teamId));
    }

    /**
     * 项目详情
     */
    @GetMapping("/api/projects/{projectId}")
    public Result<ProjectDetailVO> getProjectDetail(@PathVariable Long projectId) {
        return Result.success(projectService.getProjectDetail(projectId));
    }

    /**
     * 编辑项目
     */
    @PutMapping("/api/projects/{projectId}")
    public Result<Void> updateProject(@PathVariable Long projectId, @RequestBody @Valid UpdateProjectRequest request) {
        projectService.updateProject(projectId, request);
        return Result.success();
    }

    /**
     * 归档项目
     */
    @PostMapping("/api/projects/{projectId}/archive")
    public Result<Void> archiveProject(@PathVariable Long projectId) {
        projectService.archiveProject(projectId);
        return Result.success();
    }
}