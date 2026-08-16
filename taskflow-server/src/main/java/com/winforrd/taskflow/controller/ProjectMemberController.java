package com.winforrd.taskflow.controller;

import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.dto.AddProjectMemberRequest;
import com.winforrd.taskflow.dto.UpdateProjectMemberRoleRequest;
import com.winforrd.taskflow.service.ProjectMemberService;
import com.winforrd.taskflow.vo.ProjectMemberVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    /**
     * 项目成员
     */
    @GetMapping
    public Result<List<ProjectMemberVO>> getProjectMembers(@PathVariable Long projectId) {
        return Result.success(projectMemberService.getProjectMembers(projectId));
    }

    /**
     * 添加项目成员
     */
    @PostMapping
    public Result<Void> addProjectMember(@PathVariable Long projectId, @RequestBody @Valid AddProjectMemberRequest request) {
        projectMemberService.addProjectMember(projectId, request);
        return Result.success();
    }

    /**
     * 修改项目角色
     */
    @PutMapping("/{userId}/role")
    public Result<Void> updateProjectMemberRole(@PathVariable Long projectId, @PathVariable Long userId, @RequestBody @Valid UpdateProjectMemberRoleRequest request) {
        projectMemberService.updateProjectMemberRole(projectId, userId, request);
        return Result.success();
    }

    /**
     * 移除项目成员
     */
    @DeleteMapping("/{userId}")
    public Result<Void> removeProjectMember(@PathVariable Long projectId, @PathVariable Long userId) {
        projectMemberService.removeProjectMember(projectId, userId);
        return Result.success();
    }
}