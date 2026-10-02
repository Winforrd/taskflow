package com.winforrd.taskflow.controller;


import com.winforrd.taskflow.common.result.Result;


import com.winforrd.taskflow.dto.CreateTagRequest;
import com.winforrd.taskflow.dto.UpdateTagRequest;
import com.winforrd.taskflow.dto.UpdateTaskTagsRequest;


import com.winforrd.taskflow.service.TagService;


import com.winforrd.taskflow.vo.TagCollectionVO;
import com.winforrd.taskflow.vo.TagVO;
import com.winforrd.taskflow.vo.TaskTagContextVO;


import jakarta.validation.Valid;


import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class TagController {


    private final TagService tagService;


    /**
     * =====================================================
     * 团队标签
     * =====================================================
     */
    @GetMapping("/api/teams/{teamId}/tags")
    public Result<TagCollectionVO> getTeamTags(@PathVariable Long teamId) {
        return Result.success(tagService.getTeamTags(teamId));
    }

    /**
     * 创建团队标签。
     */
    @PostMapping("/api/teams/{teamId}/tags")
    public Result<TagVO> createTeamTag(@PathVariable Long teamId, @Valid @RequestBody CreateTagRequest request) {
        return Result.success(tagService.createTeamTag(teamId, request));
    }


    /**
     * =====================================================
     * 项目标签
     * =====================================================
     */
    @GetMapping("/api/projects/{projectId}/tags")
    public Result<TagCollectionVO> getProjectTags(@PathVariable Long projectId) {

        return Result.success(tagService.getProjectTags(projectId));
    }


    /**
     * 创建项目标签。
     */
    @PostMapping("/api/projects/{projectId}/tags")
    public Result<TagVO> createProjectTag(@PathVariable Long projectId, @Valid @RequestBody CreateTagRequest request) {

        return Result.success(tagService.createProjectTag(projectId, request));
    }

    /**
     * 修改标签。
     */
    @PutMapping("/api/tags/{tagId}")
    public Result<Void> updateTag(@PathVariable Long tagId, @Valid @RequestBody UpdateTagRequest request) {
        tagService.updateTag(tagId, request);
        return Result.success();
    }

    /**
     * 删除标签。
     */
    @DeleteMapping("/api/tags/{tagId}")
    public Result<Void> deleteTag(@PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return Result.success();
    }

    /**
     * =====================================================
     * 任务标签
     * =====================================================
     */
    @GetMapping("/api/tasks/{taskId}/tags")
    public Result<TaskTagContextVO> getTaskTags(@PathVariable Long taskId) {

        return Result.success(tagService.getTaskTagContext(taskId));
    }


    /**
     * 完整覆盖当前任务标签。
     */
    @PutMapping("/api/tasks/{taskId}/tags")
    public Result<Void> updateTaskTags(@PathVariable Long taskId, @RequestBody UpdateTaskTagsRequest request) {

        tagService.updateTaskTags(taskId, request);

        return Result.success();
    }


}