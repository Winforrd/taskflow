package com.winforrd.taskflow.service;

import com.winforrd.taskflow.dto.CreateSubTaskRequest;
import com.winforrd.taskflow.dto.UpdateSubTaskRequest;
import com.winforrd.taskflow.vo.SubTaskVO;

import java.util.List;

public interface SubTaskService {

    /**
     * 查询子任务
     */
    List<SubTaskVO> list(Long taskId);

    /**
     * 创建子任务
     */
    void create(Long taskId, CreateSubTaskRequest request);

    /**
     * 修改子任务
     */
    void update(Long taskId, Long id, UpdateSubTaskRequest request);

    /**
     * 删除子任务
     */
    void delete(Long taskId, Long id);

    /**
     * 完成 / 取消完成
     */
    void changeStatus(Long taskId, Long id);

    /**
     * 是否存在未完成必需子任务
     */
    boolean existsRequiredUnfinished(Long taskId);

    /**
     * 当前用户是否可以创建子任务
     */
    boolean canCreateSubTask(Long taskId, Long userId);
}
