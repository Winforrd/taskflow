package com.winforrd.taskflow.service;

import com.winforrd.taskflow.dto.UpdateTeamMemberRoleRequest;
import com.winforrd.taskflow.entity.TeamMember;
import com.winforrd.taskflow.vo.TeamMemberVO;

import java.util.List;

public interface TeamMemberService {

    // 根据团队ID和用户ID获取团队成员
    TeamMember getTeamMember(Long teamId, Long userId);

    // 校验用户是否是团队成员
    void checkMember(Long teamId, Long userId);

    // 校验用户是否是团队管理员
    void checkAdmin(Long teamId, Long userId);

    // 校验用户是否是团队所有者
    void checkOwner(Long teamId, Long userId);

    // 查询团队成员
    List<TeamMemberVO> getTeamMembers(Long teamId);

    // 修改团队成员角色
    void updateMemberRole(Long teamId, Long userId, UpdateTeamMemberRoleRequest request);

    // 移除团队成员
    void removeMember(Long teamId, Long userId);

    // 主动退出团队
    void quitTeam(Long teamId);

    // 获取团队管理员
    List<TeamMember> getTeamAdmins(Long teamId);
}