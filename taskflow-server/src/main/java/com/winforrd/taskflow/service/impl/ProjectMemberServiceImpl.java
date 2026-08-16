package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.winforrd.taskflow.common.enums.ErrorCode;
import com.winforrd.taskflow.common.enums.ProjectRole;
import com.winforrd.taskflow.common.enums.ProjectStatus;
import com.winforrd.taskflow.common.enums.TeamRole;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.dto.AddProjectMemberRequest;
import com.winforrd.taskflow.dto.UpdateProjectMemberRoleRequest;
import com.winforrd.taskflow.entity.Project;
import com.winforrd.taskflow.entity.ProjectMember;
import com.winforrd.taskflow.entity.SysUser;
import com.winforrd.taskflow.entity.TeamMember;
import com.winforrd.taskflow.mapper.ProjectMapper;
import com.winforrd.taskflow.mapper.ProjectMemberMapper;
import com.winforrd.taskflow.mapper.SysUserMapper;
import com.winforrd.taskflow.service.ProjectMemberService;
import com.winforrd.taskflow.service.TeamMemberService;
import com.winforrd.taskflow.vo.ProjectMemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMapper projectMapper;

    private final ProjectMemberMapper projectMemberMapper;

    private final SysUserMapper sysUserMapper;

    private final TeamMemberService teamMemberService;
    /**
     * 查询项目成员关系
     */
    @Override
    public ProjectMember getProjectMember(Long projectId, Long userId) {
        return projectMemberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, projectId).eq(ProjectMember::getUserId, userId));
    }
    /**
     * 查看项目权限
     *
     * 团队 OWNER / ADMIN：
     * 可以查看团队内全部项目。
     *
     * 普通团队成员：
     * 必须已经加入项目。
     */
    @Override
    public void checkCanView(Long projectId, Long userId) {
        Project project = getProjectOrThrow(projectId);
        TeamMember teamMember = teamMemberService.getTeamMember(project.getTeamId(), userId);
        /**
         * 首先必须属于团队
         */
        if (teamMember == null) {
            throw new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
        }
        /**
         * 团队 OWNER / ADMIN
         *
         * 直接拥有查看权限
         */
        if (TeamRole.OWNER.name().equals(teamMember.getRoleType()) || TeamRole.ADMIN.name().equals(teamMember.getRoleType())) {
            return;
        }
        /**
         * 普通团队成员
         * 必须属于项目
         */
        ProjectMember projectMember = getProjectMember(projectId, userId);
        if (projectMember == null) {
            throw new BusinessException(ErrorCode.PROJECT_PERMISSION_DENIED);
        }
    }
    /**
     * 项目基础信息编辑权限
     *
     * 可以编辑：
     *
     * 团队 OWNER
     * 团队 ADMIN
     * 项目负责人
     * 项目 MANAGER
     */
    @Override
    public void checkCanEditProject(Long projectId, Long userId) {
        Project project = getProjectOrThrow(projectId);
        TeamMember teamMember = teamMemberService.getTeamMember(project.getTeamId(), userId);
        if (teamMember == null) {
            throw new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
        }
        /**
         * 团队 OWNER / ADMIN
         */
        if (TeamRole.OWNER.name().equals(teamMember.getRoleType()) || TeamRole.ADMIN.name().equals(teamMember.getRoleType())) {
            return;
        }
        /**
         * 项目负责人
         */
        if (project.getOwnerId().equals(userId)) {
            return;
        }
        /**
         * 项目 MANAGER
         */
        ProjectMember projectMember = getProjectMember(projectId, userId);
        if (projectMember != null && ProjectRole.MANAGER.name().equals(projectMember.getRoleType())) {
            return;
        }
        throw new BusinessException(ErrorCode.PROJECT_PERMISSION_DENIED);
    }
    /**
     * 项目核心管理权限
     *
     * 可以：
     *
     * 归档项目
     * 管理成员
     *
     * 只有：
     *
     * 团队 OWNER
     * 团队 ADMIN
     * 项目负责人
     *
     * MANAGER 不允许
     */
    @Override
    public void checkCanManageProject(Long projectId, Long userId) {
        Project project = getProjectOrThrow(projectId);
        TeamMember teamMember = teamMemberService.getTeamMember(project.getTeamId(), userId);
        if (teamMember == null) {
            throw new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
        }
        /**
         * 团队 OWNER / ADMIN
         */
        if (TeamRole.OWNER.name().equals(teamMember.getRoleType()) || TeamRole.ADMIN.name().equals(teamMember.getRoleType())) {
            return;
        }
        /**
         * 项目负责人
         */
        if (project.getOwnerId().equals(userId)) {
            return;
        }
        /**
         * 注意：
         *
         * 这里绝对没有
         * ProjectRole.MANAGER
         *
         * 这是负责人和管理员
         * 权限区别的关键。
         */
        throw new BusinessException(ErrorCode.PROJECT_PERMISSION_DENIED);
    }
    /**
     * 查询项目成员列表
     */
    @Override
    public List<ProjectMemberVO> getProjectMembers(Long projectId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        checkCanView(projectId, currentUserId);
        Project project = getProjectOrThrow(projectId);
        List<ProjectMember> members = projectMemberMapper.selectList(new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, projectId).orderByAsc(ProjectMember::getJoinedTime));
        List<ProjectMemberVO> result = new ArrayList<>();
        for (ProjectMember member : members) {
            SysUser user = sysUserMapper.selectById(member.getUserId());
            if (user == null) {
                continue;
            }
            ProjectMemberVO vo = new ProjectMemberVO();
            vo.setUserId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setRoleType(member.getRoleType());
            /**
             * 单独判断是不是项目负责人
             *
             * 不能只根据 MANAGER 判断
             */
            vo.setProjectOwner(project.getOwnerId().equals(user.getId()));
            vo.setJoinedTime(member.getJoinedTime());
            result.add(vo);
        }
        return result;
    }
    /**
     * 添加项目成员
     *
     * MANAGER 不允许
     */
    @Override
    @Transactional
    public void addProjectMember(Long projectId, AddProjectMemberRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Project project = getProjectOrThrow(projectId);
        checkNotArchived(project);
        /**
         * 核心管理权限
         */
        checkCanManageProject(projectId, currentUserId);
        /**
         * 被添加者必须是团队成员
         */
        TeamMember teamMember = teamMemberService.getTeamMember(project.getTeamId(), request.getUserId());

        if (teamMember == null) {
            throw new BusinessException(ErrorCode.PROJECT_MEMBER_NOT_TEAM_MEMBER);
        }
        /**
         * 校验项目角色
         */
        validateProjectRole(request.getRoleType());
        /**
         * 是否已经加入项目
         */
        ProjectMember exists = getProjectMember(projectId, request.getUserId());
        if (exists != null) {
            throw new BusinessException(ErrorCode.PROJECT_MEMBER_ALREADY_EXISTS);
        }
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(request.getUserId());
        member.setRoleType(request.getRoleType());
        member.setJoinedTime(LocalDateTime.now());
        try {
            projectMemberMapper.insert(member);
        }
        catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.PROJECT_MEMBER_ALREADY_EXISTS);
        }
    }
    /**
     * 修改项目成员角色
     *
     * MANAGER 不允许
     */
    @Override
    @Transactional
    public void updateProjectMemberRole(Long projectId, Long userId, UpdateProjectMemberRoleRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Project project = getProjectOrThrow(projectId);
        checkNotArchived(project);
        checkCanManageProject(projectId, currentUserId);
        ProjectMember member = getProjectMember(projectId, userId);
        if (member == null) {
            throw new BusinessException(ErrorCode.PROJECT_MEMBER_NOT_FOUND);
        }
        /**
         * 项目负责人不能在这里修改角色
         */
        if (project.getOwnerId().equals(userId)) {
            throw new BusinessException(ErrorCode.PROJECT_OWNER_ROLE_CANNOT_CHANGE);
        }
        validateProjectRole(request.getRoleType());
        ProjectMember update = new ProjectMember();
        update.setId(member.getId());
        update.setRoleType(request.getRoleType());
        projectMemberMapper.updateById(update);
    }
    /**
     * 移除项目成员
     *
     * MANAGER 不允许
     */
    @Override
    @Transactional
    public void removeProjectMember(Long projectId, Long userId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Project project = getProjectOrThrow(projectId);
        checkNotArchived(project);
        checkCanManageProject(projectId, currentUserId);
        /**
         * 项目负责人不能移除
         */
        if (project.getOwnerId().equals(userId)) {
            throw new BusinessException(ErrorCode.PROJECT_OWNER_CANNOT_REMOVE);
        }
        ProjectMember member = getProjectMember(projectId, userId);
        if (member == null) {
            throw new BusinessException(ErrorCode.PROJECT_MEMBER_NOT_FOUND);
        }
        projectMemberMapper.deleteById(member.getId());
    }


    /**
     * 获取项目
     */
    private Project getProjectOrThrow(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        return project;
    }
    /**
     * 校验项目角色
     */
    private void validateProjectRole(String role) {
        boolean valid = ProjectRole.MANAGER.name().equals(role) || ProjectRole.MEMBER.name().equals(role) || ProjectRole.READ_ONLY.name().equals(role);
        if (!valid) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }
    /**
     * 已归档项目禁止修改
     */
    private void checkNotArchived(Project project) {
        if (ProjectStatus.ARCHIVED.name().equals(project.getStatus())) {
            throw new BusinessException(ErrorCode.PROJECT_ARCHIVED);
        }
    }
}