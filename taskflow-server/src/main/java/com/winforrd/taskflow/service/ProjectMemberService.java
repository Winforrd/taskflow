package com.winforrd.taskflow.service;

import com.winforrd.taskflow.dto.AddProjectMemberRequest;
import com.winforrd.taskflow.dto.UpdateProjectMemberRoleRequest;
import com.winforrd.taskflow.entity.ProjectMember;
import com.winforrd.taskflow.vo.ProjectMemberVO;

import java.util.List;

public interface ProjectMemberService {

    /**
     * 查询用户在项目中的成员关系
     */
    ProjectMember getProjectMember(Long projectId, Long userId);
    /**
     * 校验项目查看权限
     *
     * 可以查看：
     *
     * 1. 团队 OWNER
     * 2. 团队 ADMIN
     * 3. 项目成员
     */
    void checkCanView(Long projectId, Long userId);
    /**
     * 校验项目基础信息编辑权限
     *
     * 可以编辑：
     *
     * 1. 团队 OWNER
     * 2. 团队 ADMIN
     * 3. 项目负责人
     * 4. 项目 MANAGER
     */
    void checkCanEditProject(Long projectId, Long userId);
    /**
     * 校验项目核心管理权限
     *
     * 可以：
     *
     * 1. 归档项目
     * 2. 添加项目成员
     * 3. 修改项目成员角色
     * 4. 移除项目成员
     *
     * 允许：
     *
     * 1. 团队 OWNER
     * 2. 团队 ADMIN
     * 3. 项目负责人
     *
     * 注意：
     * 项目 MANAGER 没有该权限
     */
    void checkCanManageProject(Long projectId, Long userId);
    /**
     * 查询项目成员
     */
    List<ProjectMemberVO> getProjectMembers(Long projectId);
    /**
     * 添加项目成员
     */
    void addProjectMember(Long projectId, AddProjectMemberRequest request);
    /**
     * 修改项目成员角色
     */
    void updateProjectMemberRole(Long projectId, Long userId, UpdateProjectMemberRoleRequest request);

    /**
     * 移除项目成员
     */
    void removeProjectMember(Long projectId, Long userId);
}