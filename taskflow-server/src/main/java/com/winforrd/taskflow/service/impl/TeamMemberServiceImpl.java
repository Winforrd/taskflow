package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.winforrd.taskflow.common.enums.ErrorCode;
import com.winforrd.taskflow.common.enums.TeamRole;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.dto.UpdateTeamMemberRoleRequest;
import com.winforrd.taskflow.entity.SysUser;
import com.winforrd.taskflow.entity.TeamMember;
import com.winforrd.taskflow.mapper.SysUserMapper;
import com.winforrd.taskflow.mapper.TeamMapper;
import com.winforrd.taskflow.mapper.TeamMemberMapper;
import com.winforrd.taskflow.service.TeamMemberService;
import com.winforrd.taskflow.vo.TeamMemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamMemberServiceImpl
        implements TeamMemberService {

    private final TeamMemberMapper teamMemberMapper;

    private final TeamMapper teamMapper;

    private final SysUserMapper sysUserMapper;

    /**
     * 获取成员关系
     */
    @Override
    public TeamMember getTeamMember(Long teamId, Long userId) {

        return teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, teamId)
                        .eq(TeamMember::getUserId, userId)
                        .eq(TeamMember::getMemberStatus, 1));
    }

    /**
     * 是否为团队成员
     */
    @Override
    public void checkMember(Long teamId, Long userId) {

        TeamMember member = getTeamMember(teamId, userId);

        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
        }
    }

    /**
     * OWNER 或 ADMIN
     */
    @Override
    public void checkAdmin(Long teamId, Long userId) {

        // 获取团队成员
        TeamMember member = getTeamMember(teamId, userId);

        // 校验成员是否存在
        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
        }

        // 校验成员角色
        String role = member.getRoleType();
        if (!TeamRole.OWNER.name().equals(role) && !TeamRole.ADMIN.name().equals(role)) {

            throw new BusinessException(ErrorCode.TEAM_PERMISSION_DENIED);
        }
    }

    /**
     * 只有 OWNER
     */
    @Override
    public void checkOwner(Long teamId, Long userId) {

        // 获取团队成员
        TeamMember member = getTeamMember(teamId, userId);

        // 校验成员是否存在
        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
        }
        // 校验成员角色
        // 校验成员角色是否为 OWNER
        if (!TeamRole.OWNER.name().equals(member.getRoleType())) {
            throw new BusinessException(ErrorCode.TEAM_PERMISSION_DENIED);
        }
    }

    /**
     * 查询团队成员
     */
    @Override
    public List<TeamMemberVO> getTeamMembers(Long teamId) {

        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 校验当前用户是否为团队成员
        checkMember(teamId, currentUserId);
        // 查询团队成员列表
        List<TeamMember> memberList = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getTeamId, teamId).eq(TeamMember::getMemberStatus, 1));
        // 构建团队成员VO列表
        List<TeamMemberVO> result = new ArrayList<>();
        // 遍历团队成员列表
        for (TeamMember member : memberList) { // 构建团队成员VO
            SysUser user = sysUserMapper.selectById(member.getUserId());
            if (user == null) {
                continue;
            }
            // 构建团队成员VO
            TeamMemberVO vo = new TeamMemberVO();
            vo.setUserId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setRoleType(member.getRoleType());
            vo.setMemberStatus(member.getMemberStatus());
            vo.setJoinedTime(member.getJoinedTime());
            result.add(vo);
        }
        return result;
    }

    /**
     * 修改成员角色
     */
    @Override
    @Transactional
    public void updateMemberRole(Long teamId, Long userId, UpdateTeamMemberRoleRequest request) {

        Long currentUserId = StpUtil.getLoginIdAsLong();

        /**
         * 只有 OWNER  或 ADMIN 可以修改角色
         */
        checkOwner(teamId, currentUserId);

        TeamMember targetMember = getTeamMember(teamId, userId);

        if (targetMember == null) {
            throw new BusinessException(ErrorCode.TEAM_MEMBER_NOT_FOUND);
        }

        /**
         * 不能修改 OWNER
         */
        if (TeamRole.OWNER.name().equals(targetMember.getRoleType())) {

            throw new BusinessException(ErrorCode.TEAM_OWNER_ROLE_CANNOT_CHANGE);
        }

        String newRole = request.getRoleType();

        /**
         * OWNER 不能通过这个接口赋予
         */
        if (TeamRole.OWNER.name().equals(newRole)) {

            throw new BusinessException(ErrorCode.TEAM_OWNER_ROLE_CANNOT_CHANGE);
        }

        /**
         * 校验角色是否合法
         */
        boolean validRole = TeamRole.ADMIN.name().equals(newRole) || TeamRole.MEMBER.name().equals(newRole) || TeamRole.READ_ONLY.name().equals(newRole);

        if (!validRole) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        TeamMember updateMember = new TeamMember();

        updateMember.setId(targetMember.getId());

        updateMember.setRoleType(newRole);

        teamMemberMapper.updateById(updateMember);
    }

    /**
     * 移除成员
     */
    @Override
    @Transactional
    public void removeMember(Long teamId, Long userId) {

        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 校验当前用户是否为团队管理员
        checkAdmin(teamId, currentUserId);

        // 获取目标成员
        TeamMember targetMember = getTeamMember(teamId, userId);

        // 校验目标成员是否存在
        if (targetMember == null) {
            throw new BusinessException(ErrorCode.TEAM_MEMBER_NOT_FOUND);
        }

        // 校验目标成员是否为 OWNER
        if (TeamRole.OWNER.name().equals(targetMember.getRoleType())) {

            throw new BusinessException(ErrorCode.TEAM_OWNER_CANNOT_REMOVE);
        }

        // 校验目标成员是否为 ADMIN
//        if(TeamRole.ADMIN.name().equals(targetMember.getRoleType())){
//            throw new BusinessException(ErrorCode.CANNOT_REMOVE_OTHER_ADMIN);
//        }

        /**
         * 第一版直接删除关系记录
         */
        teamMemberMapper.deleteById(targetMember.getId());
    }

    /**
     * 主动退出团队
     */
    @Override
    @Transactional
    public void quitTeam(Long teamId) {

        Long currentUserId = StpUtil.getLoginIdAsLong();

        TeamMember currentMember = getTeamMember(teamId, currentUserId);

        if (currentMember == null) {
            throw new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
        }

        if (TeamRole.OWNER.name().equals(currentMember.getRoleType())) {

            throw new BusinessException(ErrorCode.TEAM_OWNER_CANNOT_QUIT);
        }

        teamMemberMapper.deleteById(currentMember.getId());
    }
}