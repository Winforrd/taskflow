package com.winforrd.taskflow.controller;

import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.dto.UpdateTeamMemberRoleRequest;
import com.winforrd.taskflow.service.TeamMemberService;
import com.winforrd.taskflow.vo.TeamMemberVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/members")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    /**
     * 查询团队成员
     */
    @GetMapping
    public Result<List<TeamMemberVO>> getMembers(@PathVariable Long teamId) {

        return Result.success(teamMemberService.getTeamMembers(teamId));

    }

    /**
     * 修改成员角色
     */
    @PutMapping("/{userId}/role")
    public Result<Void> updateMemberRole(@PathVariable Long teamId, @PathVariable Long userId, @RequestBody @Valid UpdateTeamMemberRoleRequest request) {

        teamMemberService.updateMemberRole(teamId, userId, request);

        return Result.success();
    }

    /**
     * 移除成员
     */
    @DeleteMapping("/{userId}")
    public Result<Void> removeMember(@PathVariable Long teamId, @PathVariable Long userId) {

        teamMemberService.removeMember(teamId, userId);

        return Result.success();
    }

    /**
     * 主动退出团队
     */
    @PostMapping("/quit")
    public Result<Void> quitTeam(@PathVariable Long teamId) {

        teamMemberService.quitTeam(teamId);

        return Result.success();
    }
}