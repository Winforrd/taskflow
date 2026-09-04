package com.winforrd.taskflow.vo;


import lombok.Data;


import java.util.ArrayList;
import java.util.List;


@Data
public class TaskTagContextVO {


    private Long taskId;


    /**
     * 当前任务已经绑定。
     */
    private List<TagVO> selectedTags =
            new ArrayList<>();


    /**
     * 当前任务允许使用：
     *
     * 团队标签
     * +
     * 当前项目标签
     */
    private List<TagVO> availableTags =
            new ArrayList<>();


    /**
     * 当前用户是否能够修改任务标签。
     */
    private Boolean canEdit;


    /**
     * 当前用户是否能管理项目标签。
     */
    private Boolean canManageProjectTags;


}