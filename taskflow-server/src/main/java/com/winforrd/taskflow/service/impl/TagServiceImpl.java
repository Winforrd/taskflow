package com.winforrd.taskflow.service.impl;


import cn.dev33.satoken.stp.StpUtil;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;


import com.winforrd.taskflow.common.enums.ProjectRole;
import com.winforrd.taskflow.common.enums.ProjectStatus;
import com.winforrd.taskflow.common.enums.TeamRole;


import com.winforrd.taskflow.common.exception.BusinessException;


import com.winforrd.taskflow.dto.CreateTagRequest;
import com.winforrd.taskflow.dto.UpdateTagRequest;
import com.winforrd.taskflow.dto.UpdateTaskTagsRequest;


import com.winforrd.taskflow.entity.Project;
import com.winforrd.taskflow.entity.ProjectMember;
import com.winforrd.taskflow.entity.Tag;
import com.winforrd.taskflow.entity.Task;
import com.winforrd.taskflow.entity.TaskActivity;
import com.winforrd.taskflow.entity.TaskTag;
import com.winforrd.taskflow.entity.TeamMember;


import com.winforrd.taskflow.mapper.ProjectMapper;
import com.winforrd.taskflow.mapper.TagMapper;
import com.winforrd.taskflow.mapper.TaskActivityMapper;
import com.winforrd.taskflow.mapper.TaskMapper;
import com.winforrd.taskflow.mapper.TaskTagMapper;


import com.winforrd.taskflow.service.ProjectMemberService;
import com.winforrd.taskflow.service.TagService;
import com.winforrd.taskflow.service.TeamMemberService;


import com.winforrd.taskflow.vo.TagCollectionVO;
import com.winforrd.taskflow.vo.TagVO;
import com.winforrd.taskflow.vo.TaskTagContextVO;


import lombok.RequiredArgsConstructor;


import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import java.time.LocalDateTime;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private static final String SCOPE_TEAM = "TEAM";

    private static final String SCOPE_PROJECT = "PROJECT";

    private static final String DEFAULT_COLOR = "#409EFF";

    private final TagMapper tagMapper;

    private final TaskTagMapper taskTagMapper;

    private final TaskMapper taskMapper;

    private final ProjectMapper projectMapper;

    private final TaskActivityMapper taskActivityMapper;

    private final TeamMemberService teamMemberService;

    private final ProjectMemberService projectMemberService;

    /**
     * =====================================================
     * 团队标签
     * =====================================================
     */
    @Override
    public TagCollectionVO getTeamTags(Long teamId) {
        // 获取当前用户
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取团队成员
        TeamMember teamMember = teamMemberService.getTeamMember(teamId, userId);
        if (teamMember == null) {
            throw new BusinessException("没有团队访问权限");
        }
        // 准备VO
        TagCollectionVO vo = new TagCollectionVO();
        vo.setCanManage(isTeamAdmin(teamMember));
        vo.setTags(getTagsByScope(SCOPE_TEAM, teamId));
        return vo;
    }

    /**
     * =====================================================
     * 项目标签
     * =====================================================
     */
    @Override
    public TagCollectionVO getProjectTags(Long projectId) {
        // 获取当前用户
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取项目
        Project project =getProjectOrThrow(projectId);
        // 检查项目可见性
        projectMemberService.checkCanView(projectId, userId);
        // 准备VO
        TagCollectionVO vo = new TagCollectionVO();
        vo.setCanManage(canManageProjectTag(project, userId));
        vo.setTags(getTagsByScope(SCOPE_PROJECT, projectId));
        return vo;
    }

    /**
     * =====================================================
     * 创建团队标签
     * =====================================================
     */
    @Override
    @Transactional
    public TagVO createTeamTag(Long teamId, CreateTagRequest request) {
        // 获取当前用户
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取团队成员
        TeamMember teamMember = teamMemberService.getTeamMember(teamId, userId);
        if (teamMember == null || !isTeamAdmin(teamMember)) {
            throw new BusinessException("没有管理团队标签的权限");
        }
        return createTag(SCOPE_TEAM, teamId, userId, request);
    }

    /**
     * =====================================================
     * 创建项目标签
     * =====================================================
     */
    @Override
    @Transactional
    public TagVO createProjectTag(Long projectId, CreateTagRequest request) {
        // 获取当前用户
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取项目
        Project project = getProjectOrThrow(projectId);
        // 检查项目归档状态
        checkProjectNotArchived(project);
        if (!canManageProjectTag(project, userId)) {
            throw new BusinessException("没有管理项目标签的权限");
        }
        return createTag(SCOPE_PROJECT, projectId, userId, request);
    }

    /**
     * =====================================================
     * 修改标签
     * =====================================================
     */
    @Override
    @Transactional
    public void updateTag(Long tagId, UpdateTagRequest request) {
        // 获取当前用户
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取标签
        Tag tag = getTagOrThrow(tagId);
        // 检查标签可管理性
        checkCanManageTag(tag, userId);
        // 准备更新数据
        String name = normalizeName(request.getName());
        validateNameDuplicated(tag.getScopeType(), tag.getScopeId(), name, tag.getId());
        tag.setName(name);
        tag.setColor(normalizeColor(request.getColor()));
        try {
            tagMapper.updateById(tag);
        } catch (
                DuplicateKeyException exception
        )
        {
            throw new BusinessException("当前范围已存在同名标签");
        }
    }

    /**
     * =====================================================
     * 删除标签
     * =====================================================
     */
    @Override
    @Transactional
    public void deleteTag(Long tagId) {
        // 获取当前用户
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取标签
        Tag tag = getTagOrThrow(tagId);
        // 检查标签可管理性
        checkCanManageTag(tag, userId);
        /**
         * 先解绑所有任务。
         */
        taskTagMapper.delete(new LambdaQueryWrapper<TaskTag>().eq(TaskTag::getTagId, tagId));
        /**
         * Tag使用@TableLogic，
         * deleteById后：
         * deleted = 1
         */
        tagMapper.deleteById(tagId);
    }

    /**
     * =====================================================
     * 获取任务标签上下文
     * =====================================================
     */
    @Override
    public TaskTagContextVO getTaskTagContext(Long taskId) {
        // 获取当前用户
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取任务
        Task task = getTaskOrThrow(taskId);
        // 获取项目
        Project project = getProjectOrThrow(task.getProjectId());
        /**
         * 首先拥有任务查看权限。
         */
        projectMemberService.checkCanView(project.getId(), userId);
        // 准备VO
        TaskTagContextVO vo = new TaskTagContextVO();
        vo.setTaskId(taskId);
        vo.setSelectedTags(getTaskTagsInternal(taskId));
        vo.setAvailableTags(getAvailableProjectTags(project));
        vo.setCanEdit(canEditTaskTags(project, task, userId));
        vo.setCanManageProjectTags(canManageProjectTag(project, userId));
        return vo;
    }

    /**
     * =====================================================
     * 更新任务标签
     * =====================================================
     */
    @Override
    @Transactional
    public void updateTaskTags(Long taskId, UpdateTaskTagsRequest request) {
        // 获取当前用户
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取任务
        Task task = getTaskOrThrow(taskId);
        // 获取项目
        Project project = getProjectOrThrow(task.getProjectId());
        // 检查项目归档状态
        checkProjectNotArchived(project);
        if (!canEditTaskTags(project, task, userId)) {
            throw new BusinessException("没有修改任务标签的权限");
        }
        // 准备请求数据
        List<Long> requestTagIds = request == null || request.getTagIds() == null ? new ArrayList<>() : request.getTagIds();
        /**
         * 自动去重。
         */
        Set<Long> newTagIds = new LinkedHashSet<>(requestTagIds);
        /**
         * =================================================
         * 安全校验
         * =================================================
         * 只能绑定：
         * 1. 当前团队的团队标签
         * 2. 当前项目的项目标签
         */
        for (Long tagId : newTagIds) {
            if (tagId == null) {
                throw new BusinessException("标签ID不能为空");
            }
            Tag tag = getTagOrThrow(tagId);
            if (!isTagAvailableForProject(tag, project)) {
                throw new BusinessException("不能绑定其他团队或项目的标签");
            }
        }
        /**
         * 当前已有。
         */
        List<TaskTag> oldRelations = taskTagMapper.selectList(new LambdaQueryWrapper<TaskTag>().eq(TaskTag::getTaskId, taskId));
        Set<Long> oldTagIds = new LinkedHashSet<>();
        for (TaskTag relation : oldRelations) {
            oldTagIds.add(relation.getTagId());
        }

        /**
         * 没变化不重复写库，也不产生无意义动态。
         */
        if (oldTagIds.equals(newTagIds)) {
            return;
        }
        /**
         * 使用完整覆盖模式。
         * 前端提交的是最终结果。
         */
        taskTagMapper.delete(new LambdaQueryWrapper<TaskTag>().eq(TaskTag::getTaskId, taskId));
        for (Long tagId : newTagIds) {
            TaskTag relation = new TaskTag();
            relation.setTaskId(taskId);
            relation.setTagId(tagId);
            relation.setCreatedBy(userId);
            relation.setCreatedTime(LocalDateTime.now());
            taskTagMapper.insert(relation);
        }
        saveTaskActivity(taskId, userId, "TAG_UPDATED", buildTagActivityContent(newTagIds));
    }
    /**
     * =====================================================
     * 内部创建标签
     * =====================================================
     */
    private TagVO createTag(String scopeType, Long scopeId, Long userId, CreateTagRequest request) {
        // 准备数据
        String name = normalizeName(request.getName());
        // 验证标签名重复
        validateNameDuplicated(scopeType, scopeId, name, null);
        // 准备标签
        Tag tag = new Tag();
        tag.setScopeType(scopeType);
        tag.setScopeId(scopeId);
        tag.setName(name);
        tag.setColor(normalizeColor(request.getColor()));
        tag.setCreatorId(userId);
        tag.setCreatedTime(LocalDateTime.now());
        tag.setUpdatedTime(LocalDateTime.now());
        try {
            tagMapper.insert(tag);
        } catch (
                DuplicateKeyException exception
        )
        {
            throw new BusinessException("当前范围已存在同名标签");
        }
        return convertTagVO(tag);
    }

    /**
     * =====================================================
     * 当前项目可使用的全部标签
     * =====================================================
     */
    private List<TagVO> getAvailableProjectTags(Project project) {
        // 准备结果
        List<TagVO> result = new ArrayList<>();
        /**
         * 团队公共标签。
         */
        result.addAll(getTagsByScope(SCOPE_TEAM, project.getTeamId()));
        /**
         * 项目专属标签。
         */
        result.addAll(getTagsByScope(SCOPE_PROJECT, project.getId()));
        return result;
    }

    /**
     * =====================================================
     * 查询作用域标签
     * =====================================================
     */
    private List<TagVO> getTagsByScope(String scopeType, Long scopeId) {
        // 查询标签
        List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getScopeType, scopeType)
                        .eq(Tag::getScopeId, scopeId)
                        .orderByAsc(Tag::getName)
                        .orderByAsc(Tag::getId));
        List<TagVO> result = new ArrayList<>();
        for (Tag tag : tags) {
            result.add(convertTagVO(tag));
        }
        return result;
    }

    /**
     * =====================================================
     * 当前任务已有标签
     * =====================================================
     */
    private List<TagVO> getTaskTagsInternal(Long taskId) {
        List<TaskTag> relations = taskTagMapper.selectList(new LambdaQueryWrapper<TaskTag>()
                                .eq(TaskTag::getTaskId, taskId)
                                .orderByAsc(TaskTag::getId));
        List<TagVO> result = new ArrayList<>();
        for (TaskTag relation : relations) {
            Tag tag = tagMapper.selectById(relation.getTagId());
            /**
             * 标签已经逻辑删除的防御。
             */
            if (tag == null) {
                continue;
            }
            result.add(convertTagVO(tag));
        }
        return result;
    }

    /**
     * =====================================================
     * 管理标签权限
     * =====================================================
     */
    private void checkCanManageTag(Tag tag, Long userId) {
        if (SCOPE_TEAM.equals(tag.getScopeType())) {
            TeamMember member = teamMemberService.getTeamMember(tag.getScopeId(), userId);
            if (member != null && isTeamAdmin(member)) {
                return;
            }
            throw new BusinessException("没有管理团队标签的权限");
        }
        if (SCOPE_PROJECT.equals(tag.getScopeType())) {
            Project project = getProjectOrThrow(tag.getScopeId());
            checkProjectNotArchived(project);
            if (canManageProjectTag(project, userId)) {
                return;
            }
            throw new BusinessException("没有管理项目标签的权限");
        }
        throw new BusinessException("非法标签作用域");
    }

    /**
     * =====================================================
     * 项目标签管理权限
     * =====================================================
     */
    private boolean canManageProjectTag(Project project, Long userId) {
        TeamMember teamMember = teamMemberService.getTeamMember(project.getTeamId(), userId);
        if (teamMember == null) {
            return false;
        }
        if (isTeamAdmin(teamMember)) {
            return true;
        }
        /**
         * 项目负责人。
         */
        if (project.getOwnerId().equals(userId)) {
            return true;
        }
        ProjectMember projectMember = projectMemberService.getProjectMember(project.getId(), userId);
        return projectMember != null && ProjectRole.MANAGER.name().equals(projectMember.getRoleType());
    }

    /**
     * =====================================================
     * 修改任务标签权限
     * =====================================================
     *
     * 和TaskServiceImpl.canEdit保持一致：
     *
     * 团队OWNER/ADMIN
     * 项目负责人
     * 项目MANAGER
     * 任务创建者
     */
    private boolean canEditTaskTags(Project project, Task task, Long userId) {
        if (ProjectStatus.ARCHIVED.name().equals(project.getStatus())) {
            return false;
        }
        TeamMember teamMember = teamMemberService.getTeamMember(project.getTeamId(), userId);
        if (teamMember == null) {
            return false;
        }
        if (isTeamAdmin(teamMember)) {
            return true;
        }
        if (project.getOwnerId().equals(userId)) {
            return true;
        }
        ProjectMember projectMember = projectMemberService.getProjectMember(project.getId(), userId);
        if (projectMember == null || ProjectRole.READ_ONLY.name().equals(projectMember.getRoleType())) {
            return false;
        }
        if (ProjectRole.MANAGER.name().equals(projectMember.getRoleType())) {
            return true;
        }
        return task.getCreatorId().equals(userId);
    }


    /**
     * =====================================================
     * 标签是否属于该项目允许范围
     * =====================================================
     */
    private boolean isTagAvailableForProject(Tag tag, Project project) {
        if (SCOPE_TEAM.equals(tag.getScopeType())) {
            return project.getTeamId().equals(tag.getScopeId());
        }
        if (SCOPE_PROJECT.equals(tag.getScopeType())) {
            return project.getId().equals(tag.getScopeId());
        }
        return false;
    }

    /**
     * =====================================================
     * 重名校验
     * =====================================================
     */
    private void validateNameDuplicated(String scopeType, Long scopeId, String name, Long excludeTagId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<Tag>().eq(Tag::getScopeType, scopeType)
                                            .eq(Tag::getScopeId, scopeId)
                                            .eq(Tag::getName, name);
        if (excludeTagId != null) {
            wrapper.ne(Tag::getId, excludeTagId);
        }
        Long count = tagMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("当前范围已存在同名标签");
        }
    }

    /**
     * =====================================================
     * 标签名称
     * =====================================================
     */
    private String normalizeName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException("标签名称不能为空");
        }
        String result = name.trim();
        if (result.length() > 50) {
            throw new BusinessException("标签名称不能超过50个字符");
        }
        return result;
    }


    /**
     * =====================================================
     * 标签颜色
     * =====================================================
     */
    private String normalizeColor(String color) {
        if (!StringUtils.hasText(color)) {
            return DEFAULT_COLOR;
        }
        String result = color.trim();
        /**
         * 只接受：
         *
         * #RRGGBB
         */
        if (!result.matches("^#[0-9a-fA-F]{6}$")) {
            throw new BusinessException("标签颜色格式不正确");
        }
        return result.toUpperCase();
    }

    /**
     * =====================================================
     * 任务动态
     * =====================================================
     */
    private void saveTaskActivity(Long taskId, Long userId, String type, String content) {
        TaskActivity activity = new TaskActivity();
        activity.setTaskId(taskId);
        activity.setOperatorId(userId);
        activity.setActivityType(type);
        activity.setContent(content);
        activity.setCreatedTime(LocalDateTime.now());
        taskActivityMapper.insert(activity);
    }
    /**
     * =====================================================
     * 标签动态内容
     * =====================================================
     */
    private String buildTagActivityContent(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return "清空了任务标签";
        }
        List<String> names = new ArrayList<>();
        for (Long tagId : tagIds) {
            Tag tag = tagMapper.selectById(tagId);
            if (tag != null) {
                names.add(tag.getName());
            }
        }
        return "更新了任务标签：" + String.join("、", names);
    }

    /**
     * =====================================================
     * 团队管理员
     * =====================================================
     */
    private boolean isTeamAdmin(TeamMember member) {
        if (member == null) {
            return false;
        }
        return TeamRole.OWNER.name().equals(member.getRoleType()) || TeamRole.ADMIN.name().equals(member.getRoleType());
    }

    /**
     * =====================================================
     * 获取任务
     * =====================================================
     */
    private Task getTaskOrThrow(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        return task;
    }

    /**
     * =====================================================
     * 获取项目
     * =====================================================
     */
    private Project getProjectOrThrow(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return project;
    }

    /**
     * =====================================================
     * 获取标签
     * =====================================================
     */
    private Tag getTagOrThrow(Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        return tag;
    }

    /**
     * =====================================================
     * 归档项目禁止修改
     * =====================================================
     */
    private void checkProjectNotArchived(Project project) {
        if (ProjectStatus.ARCHIVED.name().equals(project.getStatus())) {
            throw new BusinessException("归档项目不能修改标签");
        }
    }

    /**
     * =====================================================
     * 标签VO
     * =====================================================
     */
    private TagVO convertTagVO(Tag tag) {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setScopeType(tag.getScopeType());
        vo.setScopeId(tag.getScopeId());
        vo.setName(tag.getName());
        vo.setColor(tag.getColor());
        vo.setCreatorId(tag.getCreatorId());
        vo.setCreatedTime(tag.getCreatedTime());
        return vo;
    }


}