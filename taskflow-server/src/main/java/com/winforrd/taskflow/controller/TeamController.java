package com.winforrd.taskflow.controller;

import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.dto.CreateTeamRequest;
import com.winforrd.taskflow.dto.UpdateTeamRequest;
import com.winforrd.taskflow.service.TeamService;
import com.winforrd.taskflow.vo.TeamDetailVO;
import com.winforrd.taskflow.vo.TeamVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    /**
     * 创建团队
     */
    @PostMapping
    public Result<Long> createTeam(@RequestBody @Valid CreateTeamRequest request) {
        Long teamId = teamService.createTeam(request);
        return Result.success(teamId);
    }

    /**
     * 我的团队
     */
    @GetMapping("/my")
    public Result<List<TeamVO>> getMyTeams() {
        return Result.success(teamService.getMyTeams());
    }

    /**
     * 团队详情
     */
    @GetMapping("/{teamId}")
    public Result<TeamDetailVO> getTeamDetail(@PathVariable Long teamId) {
        return Result.success(teamService.getTeamDetail(teamId));
    }

    /**
     * 修改团队
     */
    @PutMapping("/{teamId}")
    public Result<Void> updateTeam(@PathVariable Long teamId, @RequestBody @Valid UpdateTeamRequest request) {
        teamService.updateTeam(teamId, request);
        return Result.success();
    }
}