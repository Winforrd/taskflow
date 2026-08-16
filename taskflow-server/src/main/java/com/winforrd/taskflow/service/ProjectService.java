package com.winforrd.taskflow.service;

import com.winforrd.taskflow.dto.CreateProjectRequest;
import com.winforrd.taskflow.dto.UpdateProjectRequest;
import com.winforrd.taskflow.vo.ProjectDetailVO;
import com.winforrd.taskflow.vo.ProjectVO;

import java.util.List;

public interface ProjectService {

    /**
     * 创建项目
     */
    Long createProject(Long teamId, CreateProjectRequest request);

    /**
     * 获取团队项目
     */
    List<ProjectVO> getTeamProjects(Long teamId);

    /**
     * 获取项目详情
     */
    ProjectDetailVO getProjectDetail(Long projectId);

    /**
     * 更新项目
     */
    void updateProject(Long projectId, UpdateProjectRequest request);

    /**
     * 归档项目
     */
    void archiveProject(Long projectId);
}