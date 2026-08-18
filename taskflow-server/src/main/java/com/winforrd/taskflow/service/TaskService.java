package com.winforrd.taskflow.service;

import com.winforrd.taskflow.dto.ChangeTaskStatusRequest;
import com.winforrd.taskflow.dto.CreateTaskRequest;
import com.winforrd.taskflow.dto.UpdateTaskRequest;
import com.winforrd.taskflow.vo.TaskActivityVO;
import com.winforrd.taskflow.vo.TaskVO;

import java.util.List;

public interface TaskService {

    /**
     * 创建任务
     */
    Long createTask(Long projectId, CreateTaskRequest request);

    /**
     * 获取项目任务列表
     */
    List<TaskVO> getProjectTasks(Long projectId, String status, String keyword);

    /**
     * 获取任务详情
     */
    TaskVO getTaskDetail(Long taskId);

    /**
     * 更新任务
     */
    void updateTask(Long taskId, UpdateTaskRequest request);

    /**
     * 修改任务状态
     */
    void changeTaskStatus(Long taskId, ChangeTaskStatusRequest request);

    /**
     * 添加任务执行人
     */
    void addAssignee(Long taskId, Long userId);

    /**
     * 移除任务执行人
     */
    void removeAssignee(Long taskId, Long userId);

    /**
     * 获取任务活动列表
     */
    List<TaskActivityVO> getTaskActivities(Long taskId);
}