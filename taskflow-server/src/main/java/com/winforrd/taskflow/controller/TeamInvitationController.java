package com.winforrd.taskflow.controller;

import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.dto.CreateTeamInvitationRequest;
import com.winforrd.taskflow.service.TeamInvitationService;
import com.winforrd.taskflow.vo.InvitationPreviewVO;
import com.winforrd.taskflow.vo.TeamInvitationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TeamInvitationController {

    private final TeamInvitationService teamInvitationService;
    /**
     * 创建邀请码
     *
     * OWNER / ADMIN
     */
    @PostMapping("/api/teams/{teamId}/invitations")
    public Result<TeamInvitationVO>
    createInvitation(@PathVariable Long teamId, @RequestBody @Valid CreateTeamInvitationRequest request) {
        return Result.success(teamInvitationService.createInvitation(teamId, request));
    }
    /**
     * 查询团队邀请码
     */
    @GetMapping("/api/teams/{teamId}/invitations")
    public Result<List<TeamInvitationVO>> getTeamInvitations(@PathVariable Long teamId) {
        return Result.success(teamInvitationService.getTeamInvitations(teamId));
    }
    /**
     * 查看邀请码
     */
    @GetMapping("/api/invitations/{code}")
    public Result<InvitationPreviewVO> previewInvitation(@PathVariable String code) {
        return Result.success(teamInvitationService.getInvitationPreview(code));
    }
    /**
     * 接受邀请
     */
    @PostMapping("/api/invitations/{code}/accept") public Result<Long> acceptInvitation(@PathVariable String code) {
        Long teamId = teamInvitationService.acceptInvitation(code);
        return Result.success(teamId);
    }
    /**
     * 禁用邀请码
     */
    @DeleteMapping("/api/teams/{teamId}/invitations/{invitationId}")
    public Result<Void> disableInvitation(@PathVariable Long teamId, @PathVariable Long invitationId) {
        teamInvitationService.disableInvitation(teamId, invitationId);
        return Result.success();
    }
}