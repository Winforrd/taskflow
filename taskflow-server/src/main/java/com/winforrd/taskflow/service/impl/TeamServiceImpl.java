package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.winforrd.taskflow.common.enums.ErrorCode;
import com.winforrd.taskflow.common.enums.TeamRole;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.dto.CreateTeamRequest;
import com.winforrd.taskflow.dto.UpdateTeamRequest;
import com.winforrd.taskflow.entity.Team;
import com.winforrd.taskflow.entity.TeamMember;
import com.winforrd.taskflow.mapper.TeamMapper;
import com.winforrd.taskflow.mapper.TeamMemberMapper;
import com.winforrd.taskflow.service.TeamMemberService;
import com.winforrd.taskflow.service.TeamService;
import com.winforrd.taskflow.vo.TeamDetailVO;
import com.winforrd.taskflow.vo.TeamVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamMapper teamMapper;

    private final TeamMemberMapper teamMemberMapper;

    private final TeamMemberService teamMemberService;

    /**
     * 创建团队
     */
    @Override
    @Transactional
    public Long createTeam(CreateTeamRequest request) {

        // 当前用户
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 创建团队
        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setOwnerId(currentUserId);
        team.setCreatedBy(currentUserId);
        team.setStatus(1);
        teamMapper.insert(team);

        /**
         * 创建者自动成为 OWNER
         */
        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(team.getId());
        teamMember.setUserId(currentUserId);
        teamMember.setRoleType(TeamRole.OWNER.name());
        teamMember.setMemberStatus(1);
        teamMember.setJoinedTime(LocalDateTime.now());
        teamMemberMapper.insert(teamMember);
        return team.getId();
    }

    /**
     * 查询当前用户加入的团队
     */
    @Override
    public List<TeamVO> getMyTeams() {

        // 当前用户
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 查询当前用户加入的团队
        List<TeamMember> memberList = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getUserId, currentUserId).eq(TeamMember::getMemberStatus, 1));
        // 构建结果列表
        List<TeamVO> result = new ArrayList<>();
        // 遍历成员列表，构建团队VO
        for (TeamMember member : memberList) {
            // 查询团队信息
            Team team = teamMapper.selectById(member.getTeamId());
            // 如果团队不存在，则跳过
            if (team == null) {
                continue;
            }
            // 构建团队VO
            TeamVO vo = new TeamVO();
            vo.setId(team.getId());
            vo.setName(team.getName());
            vo.setDescription(team.getDescription());
            vo.setAvatar(team.getAvatar());
            vo.setOwnerId(team.getOwnerId());
            vo.setMyRole(member.getRoleType());
            // 查询团队成员数
            Long memberCount = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getTeamId, team.getId()).eq(TeamMember::getMemberStatus, 1));
            vo.setMemberCount(memberCount.intValue());
            // 设置创建时间
            vo.setCreatedTime(team.getCreatedTime());
            result.add(vo);
        }
        return result;
    }

    /**
     * 团队详情
     */
    @Override
    public TeamDetailVO getTeamDetail(Long teamId) {

        // 当前用户
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 查询团队信息
        Team team = teamMapper.selectById(teamId);
        // 如果团队不存在，则抛出异常
        if (team == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        // 查询当前用户在团队中的角色
        TeamMember member = teamMemberService.getTeamMember(teamId, currentUserId);
        // 如果当前用户不是团队成员，则抛出异常
        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
        }
        // 查询团队成员数
        Long memberCount = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getTeamId, teamId).eq(TeamMember::getMemberStatus, 1));
        // 构建团队详情VO
        TeamDetailVO vo = new TeamDetailVO();
        // 设置团队ID
        vo.setId(team.getId());
        // 设置团队名称
        vo.setName(team.getName());
        // 设置团队描述
        vo.setDescription(team.getDescription());
        // 设置团队头像
        vo.setAvatar(team.getAvatar());
        // 设置团队所有者ID
        vo.setOwnerId(team.getOwnerId());
        // 设置团队状态
        vo.setStatus(team.getStatus());
        // 设置当前用户在团队中的角色
        vo.setMyRole(member.getRoleType());
        // 设置团队成员数
        vo.setMemberCount(memberCount.intValue());
        // 设置团队创建时间
        vo.setCreatedTime(team.getCreatedTime());
        return vo;
    }

    /**
     * 修改团队
     */
    @Override
    @Transactional
    public void updateTeam(Long teamId, UpdateTeamRequest request) {

        // 当前用户
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 查询团队信息
        Team team = teamMapper.selectById(teamId);
        // 如果团队不存在，则抛出异常
        if (team == null) {
            // 如果团队不存在，则抛出异常
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        /**
         * OWNER 或 ADMIN 可以修改
         */
        teamMemberService.checkAdmin(teamId,currentUserId);
        Team updateTeam = new Team();
        updateTeam.setId(teamId);
        updateTeam.setName(request.getName());
        updateTeam.setDescription(request.getDescription());
        teamMapper.updateById(updateTeam);
    }
}