package com.winforrd.taskflow.service;

import com.winforrd.taskflow.dto.CreateTeamInvitationRequest;
import com.winforrd.taskflow.vo.InvitationPreviewVO;
import com.winforrd.taskflow.vo.TeamInvitationVO;

import java.util.List;

public interface TeamInvitationService {

    /**
     * 创建邀请码
     */
    TeamInvitationVO createInvitation(Long teamId, CreateTeamInvitationRequest request);

    /**
     * 查询团队的邀请码
     */
    List<TeamInvitationVO> getTeamInvitations(Long teamId);

    /**
     * 查看邀请码信息
     */
    InvitationPreviewVO getInvitationPreview(String invitationCode);

    /**
     * 接受邀请
     *
     * 返回加入的团队ID
     */
    Long acceptInvitation(String invitationCode);

    /**
     * 禁用邀请码
     */
    void disableInvitation(Long teamId, Long invitationId);
}