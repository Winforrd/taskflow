package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.winforrd.taskflow.common.enums.ErrorCode;
import com.winforrd.taskflow.common.enums.ProjectRole;
import com.winforrd.taskflow.common.enums.ProjectStatus;
import com.winforrd.taskflow.common.enums.TaskActivityType;
import com.winforrd.taskflow.common.enums.TaskStatus;
import com.winforrd.taskflow.common.enums.TeamRole;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.dto.CreateSubTaskRequest;
import com.winforrd.taskflow.dto.UpdateSubTaskRequest;
import com.winforrd.taskflow.entity.Project;
import com.winforrd.taskflow.entity.ProjectMember;
import com.winforrd.taskflow.entity.SubTask;
import com.winforrd.taskflow.entity.SysUser;
import com.winforrd.taskflow.entity.Task;
import com.winforrd.taskflow.entity.TaskActivity;
import com.winforrd.taskflow.entity.TaskAssignee;
import com.winforrd.taskflow.entity.TeamMember;
import com.winforrd.taskflow.mapper.ProjectMapper;
import com.winforrd.taskflow.mapper.SubTaskMapper;
import com.winforrd.taskflow.mapper.SysUserMapper;
import com.winforrd.taskflow.mapper.TaskActivityMapper;
import com.winforrd.taskflow.mapper.TaskAssigneeMapper;
import com.winforrd.taskflow.mapper.TaskMapper;
import com.winforrd.taskflow.service.ProjectMemberService;
import com.winforrd.taskflow.service.SubTaskService;
import com.winforrd.taskflow.service.TeamMemberService;
import com.winforrd.taskflow.vo.SubTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubTaskServiceImpl implements SubTaskService {

    private final SubTaskMapper subTaskMapper;

    private final TaskMapper taskMapper;

    private final ProjectMapper projectMapper;

    private final SysUserMapper sysUserMapper;

    private final TaskAssigneeMapper taskAssigneeMapper;

    private final TaskActivityMapper taskActivityMapper;

    private final ProjectMemberService projectMemberService;

    private final TeamMemberService teamMemberService;


    /**
     * =====================================================
     * 查询子任务
     * =====================================================
     */
    @Override
    public List<SubTaskVO> list(Long taskId) {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取任务
        Task task = getTaskOrThrow(taskId);
        // 获取项目
        Project project = getProjectOrThrow(task.getProjectId());
        // 检查当前用户是否有权限查看项目
        projectMemberService.checkCanView(project.getId(), currentUserId);
        // 获取子任务列表
        List<SubTask> list = subTaskMapper.selectList(new LambdaQueryWrapper<SubTask>()
                                .eq(SubTask::getTaskId, taskId)
                                .orderByAsc(SubTask::getSortOrder)
                                .orderByAsc(SubTask::getId));
        // 转换为VO
        List<SubTaskVO> result = new ArrayList<>();
        for (SubTask subTask : list) {
            result.add(convert(subTask, task, project, currentUserId));
        }
        return result;
    }

    /**
     * =====================================================
     * 创建子任务
     *
     * 允许：
     * 团队 OWNER / ADMIN
     * 项目负责人
     * 项目 MANAGER
     * 任务创建者
     * 当前任务负责人
     *
     * 禁止：
     * 普通 MEMBER（非创建者/负责人）
     * READ_ONLY
     * =====================================================
     */
    @Override
    @Transactional
    public void create(Long taskId, CreateSubTaskRequest request) {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取任务
        Task task = getTaskOrThrow(taskId);
        // 获取项目
        Project project = getProjectOrThrow(task.getProjectId());
        // 检查项目是否可写
        assertProjectWritable(project);
        // 检查任务是否可写
        assertTaskWritable(task);
        // 检查当前用户是否有权限创建子任务
        if (!canCreateSubTask(project, task, currentUserId)) {
            throw new BusinessException(ErrorCode.SUB_TASK_PERMISSION_DENIED);
        }
        // 创建子任务
        String title = normalizeTitle(request.getTitle());
        // 获取描述
        Integer requiredFlag = normalizeRequiredFlag(request.getRequiredFlag());
        // 获取排序顺序
        Integer sortOrder = request.getSortOrder() == null ? 0 : request.getSortOrder();
        SubTask subTask = new SubTask();
        subTask.setTaskId(taskId);
        subTask.setTitle(title);
        subTask.setDescription(normalizeDescription(request.getDescription()));
        subTask.setStatus(TaskStatus.TODO.name());
        subTask.setRequiredFlag(requiredFlag);
        subTask.setSortOrder(sortOrder);
        subTask.setCreatorId(currentUserId);
        subTaskMapper.insert(subTask);
        saveActivity(taskId, currentUserId, TaskActivityType.CREATE_SUB_TASK, "创建了子任务：" + title);
    }

    /**
     * =====================================================
     * 修改子任务
     *
     * 允许：
     * 团队 OWNER / ADMIN
     * 项目负责人
     * 项目 MANAGER
     * 任务创建者
     * 子任务创建者
     * =====================================================
     */
    @Override
    @Transactional
    public void update(Long taskId, Long id, UpdateSubTaskRequest request) {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取子任务
        SubTask subTask = getById(id);
        // 检查子任务是否属于当前任务
        assertBelongsToTask(subTask, taskId);
        // 获取任务
        Task task = getTaskOrThrow(subTask.getTaskId());
        // 获取项目
        Project project = getProjectOrThrow(task.getProjectId());
        // 检查项目是否可写
        assertProjectWritable(project);
        // 检查任务是否可写
        assertTaskWritable(task);
        if (!canEditSubTask(project, task, subTask, currentUserId)) {
            throw new BusinessException(ErrorCode.SUB_TASK_PERMISSION_DENIED);
        }
        // 更新子任务
        String title = normalizeTitle(request.getTitle());
        // 获取描述
        subTask.setTitle(title);
        // 获取排序顺序
        subTask.setDescription(normalizeDescription(request.getDescription()));
        // 获取是否必需
        subTask.setRequiredFlag(normalizeRequiredFlag(request.getRequiredFlag()));
        if (request.getSortOrder() != null) {
            subTask.setSortOrder(request.getSortOrder());
        }
        // 更新子任务
        subTaskMapper.updateById(subTask);
        saveActivity(task.getId(), currentUserId, TaskActivityType.UPDATE_SUB_TASK, "更新了子任务：" + title);
    }

    /**
     * =====================================================
     * 删除子任务
     *
     * 允许：
     * 团队 OWNER / ADMIN
     * 项目负责人
     * 项目 MANAGER
     * 任务创建者
     * =====================================================
     */
    @Override
    @Transactional
    public void delete(Long taskId, Long id) {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取子任务
        SubTask subTask = getById(id);
        // 检查子任务是否属于当前任务
        assertBelongsToTask(subTask, taskId);
        // 获取任务
        Task task = getTaskOrThrow(subTask.getTaskId());
        // 获取项目
        Project project = getProjectOrThrow(task.getProjectId());
        // 检查项目是否可写
        assertProjectWritable(project);
        // 检查任务是否可写
        assertTaskWritable(task);
        // 检查当前用户是否有权限删除子任务
        if (!canDeleteSubTask(project, task, currentUserId)) {
            throw new BusinessException(ErrorCode.SUB_TASK_PERMISSION_DENIED);
        }
        // 删除子任务
        String title = subTask.getTitle();
        // 删除子任务
        subTaskMapper.deleteById(id);
        // 记录日志
        saveActivity(task.getId(), currentUserId, TaskActivityType.DELETE_SUB_TASK, "删除了子任务：" + title);
    }

    /**
     * =====================================================
     * 完成 / 取消完成子任务
     * 允许：
     * 团队 OWNER / ADMIN
     * 项目负责人
     * 项目 MANAGER
     * 任务创建者
     * 任务负责人
     * =====================================================
     */
    @Override
    @Transactional
    public void changeStatus(Long taskId, Long id) {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取子任务
        SubTask subTask = getById(id);
        // 检查子任务是否属于当前任务
        assertBelongsToTask(subTask, taskId);
        // 获取任务
        Task task = getTaskOrThrow(subTask.getTaskId());
        // 获取项目
        Project project = getProjectOrThrow(task.getProjectId());
        // 检查项目是否可写
        assertProjectWritable(project);
        // 检查任务是否可写
        assertTaskWritable(task);
        // 检查当前用户是否有权限修改子任务状态
        if (!canChangeSubTaskStatus(project, task, currentUserId)) {
            throw new BusinessException(ErrorCode.SUB_TASK_PERMISSION_DENIED);
        }
        // 切换子任务状态
        boolean completed;
        // 判断当前状态是否为完成
        if (TaskStatus.DONE.name().equals(subTask.getStatus())) {
            // 取消完成：必须用 UpdateWrapper 才能把 completed_* 写成 NULL
            subTaskMapper.update(
                    null,
                    new LambdaUpdateWrapper<SubTask>()
                            .eq(SubTask::getId, subTask.getId())
                            .set(SubTask::getStatus, TaskStatus.TODO.name())
                            .set(SubTask::getCompletedBy, null)
                            .set(SubTask::getCompletedTime, null)
            );
            completed = false;
        } else {
            // 完成
            subTask.setStatus(TaskStatus.DONE.name());
            subTask.setCompletedBy(currentUserId);
            subTask.setCompletedTime(LocalDateTime.now());
            subTaskMapper.updateById(subTask);
            completed = true;
        }
        // 记录日志
        if (completed) {
            saveActivity(task.getId(), currentUserId, TaskActivityType.COMPLETE_SUB_TASK, "完成了子任务：" + subTask.getTitle());
        }
    }

    /**
     * =====================================================
     * 是否存在未完成的必需子任务
     * =====================================================
     */
    @Override
    public boolean existsRequiredUnfinished(Long taskId) {
        // 统计未完成的必需子任务数量
        Long count = subTaskMapper.selectCount(new LambdaQueryWrapper<SubTask>()
                                .eq(SubTask::getTaskId, taskId)
                                .eq(SubTask::getRequiredFlag, 1)
                                .notIn(SubTask::getStatus, TaskStatus.DONE.name(), TaskStatus.CANCELLED.name()));
        return count != null && count > 0;
    }

    /**
     * =====================================================
     * 当前用户是否可以创建子任务
     * =====================================================
     */
    @Override
    public boolean canCreateSubTask(Long taskId, Long userId) {
        // 获取任务
        Task task = getTaskOrThrow(taskId);
        // 获取项目
        Project project = getProjectOrThrow(task.getProjectId());
        // 检查项目是否可写
        if (ProjectStatus.ARCHIVED.name().equals(project.getStatus())) {
            return false;
        }
        // 检查任务是否为终端任务
        if (isTerminalTask(task)) {
            return false;
        }
        return canCreateSubTask(project, task, userId);
    }

    /**
     * =====================================================
     * 是否可以创建子任务
     * =====================================================
     */
    private boolean canCreateSubTask(Project project, Task task, Long userId) {
        // 团队 OWNER / ADMIN
        if (hasManagePermission(project, userId)) {
            return true;
        }
        // 项目有效读权限
        if (isEffectiveReadOnly(project, userId)) {
            return false;
        }
        // 任务创建者
        if (task.getCreatorId() != null && task.getCreatorId().equals(userId)) {
            return true;
        }
        return isTaskAssignee(task.getId(), userId);
    }

    /**
     * =====================================================
     * 是否可以编辑子任务
     * =====================================================
     */
    private boolean canEditSubTask(Project project, Task task, SubTask subTask, Long userId) {
        // 团队 OWNER / ADMIN
        if (hasManagePermission(project, userId)) {
            return true;
        }
        // 项目有效读权限
        if (isEffectiveReadOnly(project, userId)) {
            return false;
        }
        // 任务创建者
        if (task.getCreatorId() != null && task.getCreatorId().equals(userId)) {
            return true;
        }
        return subTask.getCreatorId() != null && subTask.getCreatorId().equals(userId);
    }

    /**
     * =====================================================
     * 是否可以删除子任务
     * =====================================================
     */
    private boolean canDeleteSubTask(Project project, Task task, Long userId) {
        // 团队 OWNER / ADMIN
        if (hasManagePermission(project, userId)) {
            return true;
        }
        // 项目有效读权限
        if (isEffectiveReadOnly(project, userId)) {
            return false;
        }
        return task.getCreatorId() != null && task.getCreatorId().equals(userId);
    }

    /**
     * =====================================================
     * 是否可以修改子任务状态
     * =====================================================
     */
    private boolean canChangeSubTaskStatus(Project project, Task task, Long userId) {
        // 团队 OWNER / ADMIN
        if (hasManagePermission(project, userId)) {
            return true;
        }
        // 项目有效读权限
        if (isEffectiveReadOnly(project, userId)) {
            return false;
        }
        // 任务创建者
        if (task.getCreatorId() != null && task.getCreatorId().equals(userId)) {
            return true;
        }
        return isTaskAssignee(task.getId(), userId);
    }

    /**
     * 团队 OWNER / ADMIN
     * 项目负责人
     * 项目 MANAGER
     */
    private boolean hasManagePermission(Project project, Long userId) {
        // 团队 OWNER / ADMIN
        TeamMember teamMember = teamMemberService.getTeamMember(project.getTeamId(), userId);
        if (isTeamAdmin(teamMember)) {
            return true;
        }
        // 项目负责人
        if (project.getOwnerId() != null && project.getOwnerId().equals(userId)) {
            return true;
        }
        // 项目 MANAGER
        ProjectMember projectMember = projectMemberService.getProjectMember(project.getId(), userId);
        return projectMember != null && ProjectRole.MANAGER.name().equals(projectMember.getRoleType());
    }

    /**
     * 项目有效读权限
     * 项目有效读权限
     * 项目负责人
     */
    private boolean isEffectiveReadOnly(Project project, Long userId) {
        // 团队 OWNER / ADMIN
        TeamMember teamMember = teamMemberService.getTeamMember(project.getTeamId(), userId);
        if (isTeamAdmin(teamMember)) {
            return false;
        }
        // 项目有效读权限
        if (project.getOwnerId() != null && project.getOwnerId().equals(userId)) {
            return false;
        }
        // 项目有效读权限
        ProjectMember projectMember = projectMemberService.getProjectMember(project.getId(), userId);
        return projectMember != null && ProjectRole.READ_ONLY.name().equals(projectMember.getRoleType());
    }

    /**
     * 团队 OWNER / ADMIN
     */
    private boolean isTeamAdmin(TeamMember teamMember) {
        // 团队 OWNER / ADMIN
        if (teamMember == null) {return false;}
        return TeamRole.OWNER.name().equals(teamMember.getRoleType()) || TeamRole.ADMIN.name().equals(teamMember.getRoleType());
    }

    /**
     * 任务执行人
     */
    private boolean isTaskAssignee(Long taskId, Long userId) {
        // 任务执行人
        Long count = taskAssigneeMapper.selectCount(new LambdaQueryWrapper<TaskAssignee>()
                                .eq(TaskAssignee::getTaskId, taskId)
                                .eq(TaskAssignee::getUserId, userId));
        return count != null && count > 0;
    }

    /**
     * 终端任务
     */
    private boolean isTerminalTask(Task task) {
        return TaskStatus.DONE.name().equals(task.getStatus()) || TaskStatus.CANCELLED.name().equals(task.getStatus());
    }

    /**
     * =====================================================
     * 断言项目可写
     * =====================================================
     */
    private void assertProjectWritable(Project project) {
        // 项目归档
        if (ProjectStatus.ARCHIVED.name().equals(project.getStatus())) {
            throw new BusinessException(ErrorCode.TASK_ARCHIVED_PROJECT);
        }
    }

    /**
     * =====================================================
     * 断言任务可写
     * =====================================================
     */
    private void assertTaskWritable(Task task) {
        // 任务归档
        if (isTerminalTask(task)) {
            throw new BusinessException(ErrorCode.SUB_TASK_PERMISSION_DENIED);
        }
    }

    /**
     * =====================================================
     * 标题
     * =====================================================
     */
    private String normalizeTitle(String title) {
        // 标题
        String value = title == null ? "" : title.trim();
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.SUB_TASK_TITLE_EMPTY);
        }
        // 标题长度
        if (value.length() > 200) {
            throw new BusinessException(ErrorCode.SUB_TASK_TITLE_TOO_LONG);
        }
        return value;
    }

    /**
     * 描述
     */
    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String value = description.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() > 1000) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return value;
    }

    /**
     * 是否必填
     */
    private Integer normalizeRequiredFlag(Integer requiredFlag) {
        if (requiredFlag == null) {
            return 1;
        }
        if (requiredFlag != 0 && requiredFlag != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return requiredFlag;
    }

    /**
     * 保存操作日志
     */
    private void saveActivity(Long taskId, Long operatorId, TaskActivityType type, String content) {
        TaskActivity activity = new TaskActivity();
        activity.setTaskId(taskId);
        activity.setOperatorId(operatorId);
        activity.setActivityType(type.name());
        activity.setContent(content);
        activity.setCreatedTime(LocalDateTime.now());
        taskActivityMapper.insert(activity);
    }

    /**
     * 根据 ID 获取子任务
     */
    private SubTask getById(Long id) {
        // 根据 ID 获取子任务
        SubTask subTask = subTaskMapper.selectById(id);
        if (subTask == null) {
            throw new BusinessException(ErrorCode.SUB_TASK_NOT_FOUND);
        }
        return subTask;
    }

    /**
     * 断言子任务属于指定任务
     */
    private void assertBelongsToTask(SubTask subTask, Long taskId) {
        if (subTask.getTaskId() == null || !subTask.getTaskId().equals(taskId)) {
            throw new BusinessException(ErrorCode.SUB_TASK_NOT_FOUND);
        }
    }

    /**
     * 根据 ID 获取任务
     */
    private Task getTaskOrThrow(Long taskId) {
        // 根据 ID 获取任务
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        return task;
    }

    /**
     * 根据 ID 获取项目
     */
    private Project getProjectOrThrow(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        return project;
    }

    /**
     * 转换为子任务VO
     */
    private SubTaskVO convert(SubTask subTask, Task task, Project project, Long currentUserId) {
        SubTaskVO vo = new SubTaskVO();
        vo.setId(subTask.getId());
        vo.setTaskId(subTask.getTaskId());
        vo.setTitle(subTask.getTitle());
        vo.setDescription(subTask.getDescription());
        vo.setStatus(subTask.getStatus());
        vo.setRequiredFlag(subTask.getRequiredFlag());
        vo.setSortOrder(subTask.getSortOrder());
        vo.setCreatorId(subTask.getCreatorId());
        vo.setCompletedTime(subTask.getCompletedTime());
        vo.setCreatorName(resolveUserName(subTask.getCreatorId()));
        boolean writable = !ProjectStatus.ARCHIVED.name().equals(project.getStatus()) && !isTerminalTask(task);
        vo.setCanEdit(writable && canEditSubTask(project, task, subTask, currentUserId));
        vo.setCanDelete(writable && canDeleteSubTask(project, task, currentUserId));
        vo.setCanChangeStatus(writable && canChangeSubTaskStatus(project, task, currentUserId));
        return vo;
    }

    /**
     * 解析用户名称
     */
    private String resolveUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname();
        }
        return user.getUsername();
    }
}
