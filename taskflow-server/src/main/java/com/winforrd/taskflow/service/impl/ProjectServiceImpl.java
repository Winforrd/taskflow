package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.winforrd.taskflow.common.enums.ErrorCode;
import com.winforrd.taskflow.common.enums.ProjectRole;
import com.winforrd.taskflow.common.enums.ProjectStatus;
import com.winforrd.taskflow.common.enums.TeamRole;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.dto.CreateProjectRequest;
import com.winforrd.taskflow.dto.UpdateProjectRequest;
import com.winforrd.taskflow.entity.Project;
import com.winforrd.taskflow.entity.ProjectMember;
import com.winforrd.taskflow.entity.SysUser;
import com.winforrd.taskflow.entity.Team;
import com.winforrd.taskflow.entity.TeamMember;
import com.winforrd.taskflow.mapper.ProjectMapper;
import com.winforrd.taskflow.mapper.ProjectMemberMapper;
import com.winforrd.taskflow.mapper.SysUserMapper;
import com.winforrd.taskflow.mapper.TeamMapper;
import com.winforrd.taskflow.service.ProjectMemberService;
import com.winforrd.taskflow.service.ProjectService;
import com.winforrd.taskflow.service.TeamMemberService;
import com.winforrd.taskflow.vo.ProjectDetailVO;
import com.winforrd.taskflow.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    private final ProjectMemberMapper projectMemberMapper;

    private final TeamMapper teamMapper;

    private final SysUserMapper sysUserMapper;

    private final TeamMemberService teamMemberService;

    private final ProjectMemberService projectMemberService;
    /**
     * 创建项目
     */
    @Override
    @Transactional
    public Long createProject(Long teamId, CreateProjectRequest request) {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取团队信息
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        /**
         * 团队 OWNER / ADMIN
         * 才能创建项目
         */
        teamMemberService.checkAdmin(teamId, currentUserId);
        /**
         * 项目负责人必须是团队成员
         */
        TeamMember ownerTeamMember = teamMemberService.getTeamMember(teamId, request.getOwnerId());
        if (ownerTeamMember == null) {
            throw new BusinessException(ErrorCode.PROJECT_MEMBER_NOT_TEAM_MEMBER);
        }
        // 校验日期
        checkDate(request.getStartDate(), request.getDeadline());
        // 创建项目
        Project project = new Project();
        project.setTeamId(teamId);
        project.setProjectCode(generateProjectCode(teamId));
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwnerId(request.getOwnerId());
        project.setStatus(ProjectStatus.NOT_STARTED.name());
        project.setStartDate(request.getStartDate());
        project.setDeadline(request.getDeadline());
        project.setCreatedBy(currentUserId);
        projectMapper.insert(project);
        /**
         * 项目负责人自动加入项目
         * project_member 中：
         * role_type = MANAGER
         * 但是！
         * 他是否是负责人，
         * 仍然必须通过：
         * project.owner_id
         * 判断。
         */
        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProjectId(project.getId());
        ownerMember.setUserId(request.getOwnerId());
        ownerMember.setRoleType(ProjectRole.MANAGER.name());
        ownerMember.setJoinedTime(LocalDateTime.now());
        projectMemberMapper.insert(ownerMember);
        return project.getId();
    }
    /**
     * 团队项目列表
     */
    @Override
    public List<ProjectVO> getTeamProjects(Long teamId) {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取团队信息
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        // 获取当前用户在团队中的信息
        TeamMember currentTeamMember = teamMemberService.getTeamMember(teamId, currentUserId);
        if (currentTeamMember == null) {
            throw new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
        }
        // 获取团队项目列表
        List<Project> projects;
        boolean teamAdmin = TeamRole.OWNER.name().equals(currentTeamMember.getRoleType()) || TeamRole.ADMIN.name().equals(currentTeamMember.getRoleType());
        /**
         * 团队 OWNER / ADMIN
         *
         * 查看全部项目
         */
        if (teamAdmin) {
            projects = projectMapper.selectList(new LambdaQueryWrapper<Project>().eq(Project::getTeamId, teamId).orderByDesc(Project::getCreatedTime));
        }
        else {
            /**
             * 普通团队成员
             *
             * 只查看自己参与的项目
             */
            List<ProjectMember> memberships = projectMemberMapper.selectList(new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getUserId, currentUserId));
            List<Long> projectIds = memberships.stream().map(ProjectMember::getProjectId).toList();
            if (projectIds.isEmpty()) {
                return new ArrayList<>();
            }
            projects = projectMapper.selectList(new LambdaQueryWrapper<Project>().eq(Project::getTeamId, teamId).in(Project::getId, projectIds).orderByDesc(Project::getCreatedTime));
        }
        // 转换项目列表
        List<ProjectVO> result = new ArrayList<>();
        for (Project project : projects) {
            result.add(convertProjectVO(project, currentUserId));
        }
        return result;
    }
    /**
     * 项目详情
     */
    @Override
    public ProjectDetailVO getProjectDetail(Long projectId) {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取项目信息
        Project project = getProjectOrThrow(projectId);
        // 检查项目可见性
        projectMemberService.checkCanView(projectId, currentUserId);
        // 获取团队成员信息
        TeamMember teamMember = teamMemberService.getTeamMember(project.getTeamId(), currentUserId);
        // 获取项目成员信息
        ProjectMember projectMember = projectMemberService.getProjectMember(projectId, currentUserId);
        // 获取项目负责人信息
        SysUser owner = sysUserMapper.selectById(project.getOwnerId());
        // 获取项目成员数量
        Long memberCount = projectMemberMapper.selectCount(new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, projectId));
        /**
         * 是否可以编辑
         */
        boolean canEdit = calculateCanEdit(project, teamMember, projectMember, currentUserId);
        /**
         * 是否拥有核心管理权限
         */
        boolean canManageProject = calculateCanManageProject(project, teamMember, currentUserId);
        // 转换项目详情
        ProjectDetailVO vo = new ProjectDetailVO();
        vo.setId(project.getId());
        vo.setTeamId(project.getTeamId());
        vo.setProjectCode(project.getProjectCode());
        vo.setName(project.getName());
        vo.setDescription(project.getDescription());
        vo.setOwnerId(project.getOwnerId());
        vo.setOwnerName(owner == null ? null : owner.getNickname());
        vo.setStatus(project.getStatus());
        vo.setStartDate(project.getStartDate());
        vo.setDeadline(project.getDeadline());
        vo.setMyProjectRole(projectMember == null ? null : projectMember.getRoleType());
        vo.setMyTeamRole(teamMember == null ? null : teamMember.getRoleType());
        vo.setMemberCount(memberCount.intValue());
        vo.setCanEdit(canEdit);
        vo.setCanManageProject(canManageProject);
        vo.setCreatedTime(project.getCreatedTime());
        return vo;
    }

    /**
     * 编辑项目
     * MANAGER 可以
     */
    @Override
    @Transactional
    public void updateProject(Long projectId, UpdateProjectRequest request) {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取项目信息
        Project project = getProjectOrThrow(projectId);
        // 检查项目归档状态
        checkNotArchived(project);
        /**
         * 编辑权限
         * MANAGER 可以通过
         */
        projectMemberService.checkCanEditProject(projectId, currentUserId);
        // 校验日期
        checkDate(request.getStartDate(), request.getDeadline());
        // 校验项目状态
        validateProjectStatus(request.getStatus());
        // 准备更新数据
        Project update =new Project();
        update.setId(projectId);
        update.setName(request.getName());
        update.setDescription(request.getDescription());
        update.setStartDate(request.getStartDate());
        update.setDeadline(request.getDeadline());
        update.setStatus(request.getStatus());
        projectMapper.updateById(update);
    }
    /**
     * 归档项目
     *
     * MANAGER 不可以
     */
    @Override
    @Transactional
    public void archiveProject(Long projectId) {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取项目信息
        Project project = getProjectOrThrow(projectId);
        if (ProjectStatus.ARCHIVED.name().equals(project.getStatus())) {
            return;
        }
        /**
         * 核心管理权限
         * MANAGER 无法通过
         */
        projectMemberService.checkCanManageProject(projectId, currentUserId);
        // 归档项目
        Project update = new Project();
        update.setId(projectId);
        update.setStatus(ProjectStatus.ARCHIVED.name());
        projectMapper.updateById(update);
    }


    /**
     * 获取项目
     */
    private Project getProjectOrThrow(Long projectId) {
        // 获取项目信息
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        return project;
    }
    /**
     * 项目列表转换
     */
    private ProjectVO convertProjectVO(Project project, Long currentUserId) {
        // 转换项目列表
        ProjectVO vo = new ProjectVO();
        SysUser owner = sysUserMapper.selectById(project.getOwnerId());
        ProjectMember currentMember = projectMemberService.getProjectMember(project.getId(), currentUserId);
        Long memberCount = projectMemberMapper.selectCount(new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, project.getId()));
        vo.setId(project.getId());
        vo.setTeamId(project.getTeamId());
        vo.setProjectCode(project.getProjectCode());
        vo.setName(project.getName());
        vo.setDescription(project.getDescription());
        vo.setOwnerId(project.getOwnerId());
        vo.setOwnerName(owner == null ? null : owner.getNickname());
        vo.setStatus(project.getStatus());
        vo.setStartDate(project.getStartDate());
        vo.setDeadline(project.getDeadline());
        vo.setMyRole(currentMember == null ? null : currentMember.getRoleType());
        vo.setMemberCount(memberCount.intValue());
        vo.setCreatedTime(project.getCreatedTime());
        return vo;
    }
    /**
     * 生成项目编号
     */
    private String generateProjectCode(Long teamId) {
        // 生成项目编号
        for (int i = 0; i < 5; i++) {
            String code = "PRJ-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            Long count = projectMapper.selectCount(new LambdaQueryWrapper<Project>().eq(Project::getTeamId, teamId).eq(Project::getProjectCode, code));
            if (count == 0) {
                return code;
            }
        }
        throw new BusinessException(ErrorCode.PROJECT_CODE_GENERATE_FAILED);
    }
    /**
     * 日期校验
     */
    private void checkDate(LocalDate startDate, LocalDate deadline) {
        // 日期校验
        if (startDate != null && deadline != null && deadline.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.PROJECT_DATE_INVALID);
        }
    }
    /**
     * 项目状态校验
     */
    private void validateProjectStatus(String status) {
        if (ProjectStatus.NOT_STARTED.name().equals(status) || ProjectStatus.IN_PROGRESS.name().equals(status) || ProjectStatus.COMPLETED.name().equals(status)) {
            return;
        }
        throw new BusinessException(ErrorCode.PROJECT_STATUS_INVALID);
    }
    /**
     * 项目是否归档
     */
    private void checkNotArchived(Project project) {
        if (ProjectStatus.ARCHIVED.name().equals(project.getStatus())) {
            throw new BusinessException(ErrorCode.PROJECT_ARCHIVED);
        }
    }
    /**
     * 计算是否允许编辑项目
     *
     * 团队 OWNER
     * 团队 ADMIN
     * 项目负责人
     * MANAGER
     */
    private boolean calculateCanEdit(Project project, TeamMember teamMember, ProjectMember projectMember, Long userId) {
        if (teamMember != null && (TeamRole.OWNER.name().equals(teamMember.getRoleType()) || TeamRole.ADMIN.name().equals(teamMember.getRoleType()))) {
            return true;
        }
        if (project.getOwnerId().equals(userId)) {
            return true;
        }
        return projectMember != null && ProjectRole.MANAGER.name().equals(projectMember.getRoleType());
    }
    /**
     * 计算核心管理权限
     *
     * 团队 OWNER
     * 团队 ADMIN
     * 项目负责人
     *
     * MANAGER 不具备
     */
    private boolean calculateCanManageProject(Project project, TeamMember teamMember,Long userId) {
        if (project.getOwnerId().equals(userId)) {return true;
        }
        return teamMember != null && (TeamRole.OWNER.name().equals(teamMember.getRoleType()) || TeamRole.ADMIN.name().equals(teamMember.getRoleType()));
    }
}