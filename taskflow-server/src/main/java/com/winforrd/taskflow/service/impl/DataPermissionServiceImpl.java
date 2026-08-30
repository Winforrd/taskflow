package com.winforrd.taskflow.service.impl;


import cn.dev33.satoken.stp.StpUtil;


import com.winforrd.taskflow.common.exception.BusinessException;

import com.winforrd.taskflow.entity.Task;

import com.winforrd.taskflow.mapper.TaskMapper;

import com.winforrd.taskflow.service.DataPermissionService;

import com.winforrd.taskflow.service.ProjectMemberService;


import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class DataPermissionServiceImpl implements DataPermissionService {


    private final ProjectMemberService projectMemberService;


    private final TaskMapper taskMapper;


    /**
     * =====================================================
     * 项目数据权限
     * =====================================================
     *
     * 这里没有重新写一套角色判断。
     *
     * 直接复用你现有：
     *
     * ProjectMemberService.checkCanView()
     *
     * 避免出现：
     *
     * Task一套权限
     * Statistics一套权限
     * Log又一套权限
     *
     * 导致规则越来越乱。
     */
    @Override
    public void checkProjectView(Long projectId) {


        if (projectId == null) {

            throw new BusinessException(
                    "项目ID不能为空"
            );
        }


        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        projectMemberService.checkCanView(
                projectId,
                currentUserId
        );
    }


    /**
     * =====================================================
     * 任务数据权限
     * =====================================================
     */
    @Override
    public void checkTaskView(
            Long taskId
    ) {


        if (taskId == null) {

            throw new BusinessException(
                    "任务ID不能为空"
            );
        }


        Task task =
                taskMapper.selectById(
                        taskId
                );


        if (task == null) {

            throw new BusinessException(
                    "任务不存在"
            );
        }


        checkProjectView(
                task.getProjectId()
        );
    }


}