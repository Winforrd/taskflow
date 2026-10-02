package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.winforrd.taskflow.common.enums.ErrorCode;
import com.winforrd.taskflow.common.enums.TeamRole;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.dto.CreateTeamInvitationRequest;
import com.winforrd.taskflow.entity.Team;
import com.winforrd.taskflow.entity.TeamInvitation;
import com.winforrd.taskflow.entity.TeamMember;
import com.winforrd.taskflow.mapper.TeamInvitationMapper;
import com.winforrd.taskflow.mapper.TeamMapper;
import com.winforrd.taskflow.mapper.TeamMemberMapper;
import com.winforrd.taskflow.service.TeamInvitationService;
import com.winforrd.taskflow.service.TeamMemberService;
import com.winforrd.taskflow.vo.InvitationPreviewVO;
import com.winforrd.taskflow.vo.TeamInvitationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamInvitationServiceImpl implements TeamInvitationService {

    private final TeamInvitationMapper teamInvitationMapper;

    private final TeamMapper teamMapper;

    private final TeamMemberMapper teamMemberMapper;

    private final TeamMemberService teamMemberService;


    /**
     * 创建邀请码
     */
    @Override
    @Transactional
    public TeamInvitationVO createInvitation(Long teamId, CreateTeamInvitationRequest request) {
        // 当前用户
        Long currentUserId = StpUtil.getLoginIdAsLong();
        /**
         * 1. 查询团队
         */
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        /**
         * 2. OWNER / ADMIN
         * 才能创建邀请码
         */
        teamMemberService.checkAdmin(teamId, currentUserId);
        /**
         * 3. 校验默认角色
         *
         * 邀请码第一版只允许：
         *
         * MEMBER
         * READ_ONLY
         */
        String defaultRole = request.getDefaultRole();
        // 校验默认角色
        boolean validRole = TeamRole.MEMBER.name().equals(defaultRole) || TeamRole.READ_ONLY.name().equals(defaultRole);
        // 如果默认角色既不是 MEMBER 也不是 READ_ONLY，则抛出异常
        if (!validRole) {
            throw new BusinessException(ErrorCode.INVITATION_ROLE_INVALID);
        }
        /**
         * 4. 参数默认值
         */
        Integer expiresHours = request.getExpiresHours();
        if (expiresHours == null) {
            expiresHours = 24;
        }
        // 邀请码使用次数
        Integer maxUses = request.getMaxUses();
        if (maxUses == null) {
            maxUses = 1;
        }
        /**
         * 5. 生成唯一邀请码
         */
        String invitationCode = generateInvitationCode();
        /**
         * 6. 创建邀请
         */
        TeamInvitation invitation = new TeamInvitation();
        invitation.setTeamId(teamId);
        invitation.setInvitationCode(invitationCode);
        invitation.setCreatorId(currentUserId);
        invitation.setDefaultRole(defaultRole);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(expiresHours));
        invitation.setMaxUses(maxUses);
        invitation.setUsedCount(0);
        invitation.setStatus(1);
        teamInvitationMapper.insert(invitation);
        /**
         * 7. 返回 VO
         */
        return convertToVO(invitation, team);
    }


    /**
     * 查询当前团队邀请码
     */
    @Override
    public List<TeamInvitationVO> getTeamInvitations(Long teamId) {
        // 当前用户
        Long currentUserId = StpUtil.getLoginIdAsLong();
        /**
         * OWNER / ADMIN
         */
        teamMemberService.checkAdmin(teamId, currentUserId);
        // 查询团队
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        // 查询邀请码列表
        List<TeamInvitation> invitationList = teamInvitationMapper.selectList(new LambdaQueryWrapper<TeamInvitation>()
                        .eq(TeamInvitation::getTeamId, teamId).orderByDesc(TeamInvitation::getCreatedTime));
        // 转换为 VO 列表
        List<TeamInvitationVO> result = new ArrayList<>();
        for (TeamInvitation invitation : invitationList) {
            result.add(convertToVO(invitation, team));
        }
        return result;
    }

    /**
     * 邀请码预览
     */
    @Override
    public InvitationPreviewVO
    getInvitationPreview(String invitationCode) {
        TeamInvitation invitation = getInvitationByCode(invitationCode);
        /**
         * 校验邀请码
         */
        validateInvitation(invitation);
        // 查询团队
        Team team = teamMapper.selectById(invitation.getTeamId());
        if (team == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        // 转换为 VO
        InvitationPreviewVO vo = new InvitationPreviewVO();
        vo.setTeamId(team.getId());
        vo.setTeamName(team.getName());
        vo.setTeamDescription(team.getDescription());
        vo.setDefaultRole(invitation.getDefaultRole());
        vo.setExpiresAt(invitation.getExpiresAt());
        return vo;
    }


    /**
     * 接受邀请
     */
    @Override
    @Transactional
    public Long acceptInvitation(String invitationCode) {
        // 当前用户
        Long currentUserId = StpUtil.getLoginIdAsLong();
        /**
         * 1. 查询邀请码
         */
        TeamInvitation invitation = getInvitationByCode(invitationCode);
        /**
         * 2. 基础校验
         */
        validateInvitation(invitation);
        /**
         * 3. 查询团队
         */
        Team team = teamMapper.selectById(invitation.getTeamId());
        if (team == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        /**
         * 4. 判断是否已经加入
         */
        TeamMember existingMember = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, team.getId())
                        .eq(TeamMember::getUserId, currentUserId)
                        .eq(TeamMember::getMemberStatus, 1));
        // 如果用户已经加入该团队，则抛出异常
        if (existingMember != null) {
            throw new BusinessException(ErrorCode.TEAM_MEMBER_ALREADY_EXISTS);
        }
        /**
         * 5. 原子消耗一次邀请码
         * 防止：
         * maxUses = 1
         * 两个用户同时接受
         * 最后都成功的问题
         */
        int affectedRows = teamInvitationMapper.update(null, new LambdaUpdateWrapper<TeamInvitation>()
                        .eq(TeamInvitation::getId, invitation.getId())
                        .eq(TeamInvitation::getStatus, 1)
                        .gt(TeamInvitation::getExpiresAt, LocalDateTime.now())
                        .lt(TeamInvitation::getUsedCount, invitation.getMaxUses())
                        .setSql("used_count = used_count + 1"));
        if (affectedRows == 0) {
            throw new BusinessException(ErrorCode.INVITATION_CONSUME_FAILED);
        }
        /**
         * 6. 创建成员关系
         */
        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(team.getId());
        teamMember.setUserId(currentUserId);
        teamMember.setRoleType(invitation.getDefaultRole());
        teamMember.setMemberStatus(1);
        teamMember.setJoinedTime(LocalDateTime.now());
        try {
            teamMemberMapper.insert(teamMember);
        } catch (
                DuplicateKeyException e
        ) {
            /**
             * team_member 有
             * UNIQUE(team_id, user_id)
             * 数据库最后兜底
             */
            throw new BusinessException(ErrorCode.TEAM_MEMBER_ALREADY_EXISTS);
        }
        /**
         * @Transactional
         * 如果这里成员创建失败，
         * used_count + 1 也会一起回滚。
         */

        return team.getId();
    }
    /**
     * 禁用邀请码
     */
    @Override
    @Transactional
    public void disableInvitation(Long teamId, Long invitationId) {
        // 当前用户
        Long currentUserId = StpUtil.getLoginIdAsLong();
        /**
         * OWNER / ADMIN
         */
        teamMemberService.checkAdmin(teamId, currentUserId);
        // 查询邀请码
        TeamInvitation invitation = teamInvitationMapper.selectById(invitationId);
        if (invitation == null || !teamId.equals(invitation.getTeamId())) {
            throw new BusinessException(ErrorCode.INVITATION_NOT_FOUND);
        }
        // 禁用邀请码
        TeamInvitation update = new TeamInvitation();
        update.setId(invitationId);
        update.setStatus(0);
        teamInvitationMapper.updateById(update);
    }
    /**
     * 根据邀请码查询
     */
    private TeamInvitation getInvitationByCode(String invitationCode) {
        // 邀请码
        if (invitationCode == null || invitationCode.isBlank()) {
            throw new BusinessException(ErrorCode.INVITATION_NOT_FOUND);
        }
        // 查询邀请码
        TeamInvitation invitation = teamInvitationMapper.selectOne(new LambdaQueryWrapper<TeamInvitation>()
                        .eq(TeamInvitation::getInvitationCode, invitationCode.trim().toUpperCase()));
        if (invitation == null) {
            throw new BusinessException(ErrorCode.INVITATION_NOT_FOUND);
        }
        return invitation;
    }

    /**
     * 校验邀请码状态
     */
    private void validateInvitation(TeamInvitation invitation) {
        /**
         * 已被管理员禁用
         */
        if (invitation.getStatus() == null || invitation.getStatus() != 1) {
            throw new BusinessException(ErrorCode.INVITATION_DISABLED);
        }
        /**
         * 已过期
         */
        if (invitation.getExpiresAt() == null || LocalDateTime.now().isAfter(invitation.getExpiresAt())) {
            throw new BusinessException(ErrorCode.INVITATION_EXPIRED);
        }
        /**
         * 使用次数已满
         */
        if (invitation.getUsedCount() >= invitation.getMaxUses()) {
            throw new BusinessException(ErrorCode.INVITATION_USED_UP);
        }
    }


    /**
     * 生成邀请码
     */
    private String generateInvitationCode() {
        // 随机生成邀请码
        for (int i = 0; i < 5; i++) {
            String code = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
            Long count = teamInvitationMapper.selectCount(new LambdaQueryWrapper<TeamInvitation>()
                                    .eq(TeamInvitation::getInvitationCode, code));
            if (count == 0) {
                return code;
            }
        }
        /**
         * 理论上极难发生
         */
        throw new BusinessException(500, "邀请码生成失败，请稍后重试");
    }
    /**
     * Entity 转 VO
     */
    private TeamInvitationVO convertToVO(TeamInvitation invitation, Team team) {
        TeamInvitationVO vo = new TeamInvitationVO();
        vo.setId(invitation.getId());
        vo.setTeamId(invitation.getTeamId());
        vo.setTeamName(team.getName());
        vo.setInvitationCode(invitation.getInvitationCode());
        vo.setDefaultRole(invitation.getDefaultRole());
        vo.setExpiresAt(invitation.getExpiresAt());
        vo.setMaxUses(invitation.getMaxUses());
        vo.setUsedCount(invitation.getUsedCount());
        vo.setStatus(invitation.getStatus());
        vo.setCreatedTime(invitation.getCreatedTime());
        return vo;
    }
}