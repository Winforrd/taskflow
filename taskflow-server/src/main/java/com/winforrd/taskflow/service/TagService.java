package com.winforrd.taskflow.service;


import com.winforrd.taskflow.dto.CreateTagRequest;
import com.winforrd.taskflow.dto.UpdateTagRequest;
import com.winforrd.taskflow.dto.UpdateTaskTagsRequest;


import com.winforrd.taskflow.vo.TagCollectionVO;
import com.winforrd.taskflow.vo.TagVO;
import com.winforrd.taskflow.vo.TaskTagContextVO;


public interface TagService {


    /**
     * 团队标签。
     */
    TagCollectionVO getTeamTags(
            Long teamId
    );


    /**
     * 项目标签。
     *
     * 这里只返回项目自己创建的标签，
     * 用于项目标签管理页面。
     */
    TagCollectionVO getProjectTags(
            Long projectId
    );


    /**
     * 创建团队标签。
     */
    TagVO createTeamTag(
            Long teamId,
            CreateTagRequest request
    );


    /**
     * 创建项目标签。
     */
    TagVO createProjectTag(
            Long projectId,
            CreateTagRequest request
    );


    /**
     * 修改标签。
     */
    void updateTag(
            Long tagId,
            UpdateTagRequest request
    );


    /**
     * 删除标签。
     */
    void deleteTag(
            Long tagId
    );


    /**
     * 任务标签上下文。
     */
    TaskTagContextVO getTaskTagContext(
            Long taskId
    );


    /**
     * 完整更新任务标签。
     */
    void updateTaskTags(
            Long taskId,
            UpdateTaskTagsRequest request
    );


}