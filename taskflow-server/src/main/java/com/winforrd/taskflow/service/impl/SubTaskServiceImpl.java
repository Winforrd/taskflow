package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

        Long currentUserId =
                StpUtil.getLoginIdAsLong();

        Task task =
                getTaskOrThrow(taskId);

        Project project =
                getProjectOrThrow(task.getProjectId());

        projectMemberService.checkCanView(
                project.getId(),
                currentUserId
        );

        List<SubTask> list =
                subTaskMapper.selectList(

                        new LambdaQueryWrapper<SubTask>()

                                .eq(
                                        SubTask::getTaskId,
                                        taskId
                                )

                                .orderByAsc(
                                        SubTask::getSortOrder
                                )

                                .orderByAsc(
                                        SubTask::getId
                                )
                );

        List<SubTaskVO> result =
                new ArrayList<>();

        for (SubTask subTask : list) {

            result.add(
                    convert(
                            subTask,
                            task,
                            project,
                            currentUserId
                    )
            );
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
    public void create(
            Long taskId,
            CreateSubTaskRequest request
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();

        Task task =
                getTaskOrThrow(taskId);

        Project project =
                getProjectOrThrow(task.getProjectId());

        assertProjectWritable(project);
        assertTaskWritable(task);

        if (!canCreateSubTask(project, task, currentUserId)) {

            throw new BusinessException(
                    ErrorCode.SUB_TASK_PERMISSION_DENIED
            );
        }

        String title =
                normalizeTitle(request.getTitle());

        Integer requiredFlag =
                normalizeRequiredFlag(
                        request.getRequiredFlag()
                );

        Integer sortOrder =
                request.getSortOrder() == null
                        ? 0
                        : request.getSortOrder();

        SubTask subTask =
                new SubTask();

        subTask.setTaskId(taskId);
        subTask.setTitle(title);
        subTask.setDescription(
                normalizeDescription(
                        request.getDescription()
                )
        );
        subTask.setStatus(TaskStatus.TODO.name());
        subTask.setRequiredFlag(requiredFlag);
        subTask.setSortOrder(sortOrder);
        subTask.setCreatorId(currentUserId);

        subTaskMapper.insert(subTask);

        saveActivity(
                taskId,
                currentUserId,
                TaskActivityType.CREATE_SUB_TASK,
                "创建了子任务：" + title
        );
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
    public void update(
            Long taskId,
            Long id,
            UpdateSubTaskRequest request
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();

        SubTask subTask =
                getById(id);

        assertBelongsToTask(subTask, taskId);

        Task task =
                getTaskOrThrow(subTask.getTaskId());

        Project project =
                getProjectOrThrow(task.getProjectId());

        assertProjectWritable(project);
        assertTaskWritable(task);

        if (!canEditSubTask(project, task, subTask, currentUserId)) {

            throw new BusinessException(
                    ErrorCode.SUB_TASK_PERMISSION_DENIED
            );
        }

        String title =
                normalizeTitle(request.getTitle());

        subTask.setTitle(title);
        subTask.setDescription(
                normalizeDescription(
                        request.getDescription()
                )
        );
        subTask.setRequiredFlag(
                normalizeRequiredFlag(
                        request.getRequiredFlag()
                )
        );

        if (request.getSortOrder() != null) {

            subTask.setSortOrder(
                    request.getSortOrder()
            );
        }

        subTaskMapper.updateById(subTask);

        saveActivity(
                task.getId(),
                currentUserId,
                TaskActivityType.UPDATE_SUB_TASK,
                "更新了子任务：" + title
        );
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
    public void delete(
            Long taskId,
            Long id
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();

        SubTask subTask =
                getById(id);

        assertBelongsToTask(subTask, taskId);

        Task task =
                getTaskOrThrow(subTask.getTaskId());

        Project project =
                getProjectOrThrow(task.getProjectId());

        assertProjectWritable(project);
        assertTaskWritable(task);

        if (!canDeleteSubTask(project, task, currentUserId)) {

            throw new BusinessException(
                    ErrorCode.SUB_TASK_PERMISSION_DENIED
            );
        }

        String title =
                subTask.getTitle();

        subTaskMapper.deleteById(id);

        saveActivity(
                task.getId(),
                currentUserId,
                TaskActivityType.DELETE_SUB_TASK,
                "删除了子任务：" + title
        );
    }


    /**
     * =====================================================
     * 完成 / 取消完成子任务
     *
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
    public void changeStatus(
            Long taskId,
            Long id
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();

        SubTask subTask =
                getById(id);

        assertBelongsToTask(subTask, taskId);

        Task task =
                getTaskOrThrow(subTask.getTaskId());

        Project project =
                getProjectOrThrow(task.getProjectId());

        assertProjectWritable(project);
        assertTaskWritable(task);

        if (!canChangeSubTaskStatus(project, task, currentUserId)) {

            throw new BusinessException(
                    ErrorCode.SUB_TASK_PERMISSION_DENIED
            );
        }

        boolean completed;

        if (
                TaskStatus.DONE.name()
                        .equals(subTask.getStatus())
        ) {

            subTask.setStatus(TaskStatus.TODO.name());
            subTask.setCompletedBy(null);
            subTask.setCompletedTime(null);
            completed = false;

        } else {

            subTask.setStatus(TaskStatus.DONE.name());
            subTask.setCompletedBy(currentUserId);
            subTask.setCompletedTime(LocalDateTime.now());
            completed = true;
        }

        subTaskMapper.updateById(subTask);

        if (completed) {

            saveActivity(
                    task.getId(),
                    currentUserId,
                    TaskActivityType.COMPLETE_SUB_TASK,
                    "完成了子任务：" + subTask.getTitle()
            );
        }
    }


    /**
     * =====================================================
     * 是否存在未完成的必需子任务
     * =====================================================
     */
    @Override
    public boolean existsRequiredUnfinished(Long taskId) {

        Long count =
                subTaskMapper.selectCount(

                        new LambdaQueryWrapper<SubTask>()

                                .eq(
                                        SubTask::getTaskId,
                                        taskId
                                )

                                .eq(
                                        SubTask::getRequiredFlag,
                                        1
                                )

                                .notIn(
                                        SubTask::getStatus,
                                        TaskStatus.DONE.name(),
                                        TaskStatus.CANCELLED.name()
                                )
                );

        return count != null && count > 0;
    }


    /**
     * =====================================================
     * 当前用户是否可以创建子任务
     * =====================================================
     */
    @Override
    public boolean canCreateSubTask(
            Long taskId,
            Long userId
    ) {

        Task task =
                getTaskOrThrow(taskId);

        Project project =
                getProjectOrThrow(task.getProjectId());

        if (
                ProjectStatus.ARCHIVED.name()
                        .equals(project.getStatus())
        ) {

            return false;
        }

        if (isTerminalTask(task)) {

            return false;
        }

        return canCreateSubTask(project, task, userId);
    }


    private boolean canCreateSubTask(
            Project project,
            Task task,
            Long userId
    ) {

        if (hasManagePermission(project, userId)) {

            return true;
        }

        if (isEffectiveReadOnly(project, userId)) {

            return false;
        }

        if (
                task.getCreatorId() != null
                        && task.getCreatorId().equals(userId)
        ) {

            return true;
        }

        return isTaskAssignee(task.getId(), userId);
    }


    private boolean canEditSubTask(
            Project project,
            Task task,
            SubTask subTask,
            Long userId
    ) {

        if (hasManagePermission(project, userId)) {

            return true;
        }

        if (isEffectiveReadOnly(project, userId)) {

            return false;
        }

        if (
                task.getCreatorId() != null
                        && task.getCreatorId().equals(userId)
        ) {

            return true;
        }

        return subTask.getCreatorId() != null
                && subTask.getCreatorId().equals(userId);
    }


    private boolean canDeleteSubTask(
            Project project,
            Task task,
            Long userId
    ) {

        if (hasManagePermission(project, userId)) {

            return true;
        }

        if (isEffectiveReadOnly(project, userId)) {

            return false;
        }

        return task.getCreatorId() != null
                && task.getCreatorId().equals(userId);
    }


    private boolean canChangeSubTaskStatus(
            Project project,
            Task task,
            Long userId
    ) {

        if (hasManagePermission(project, userId)) {

            return true;
        }

        if (isEffectiveReadOnly(project, userId)) {

            return false;
        }

        if (
                task.getCreatorId() != null
                        && task.getCreatorId().equals(userId)
        ) {

            return true;
        }

        return isTaskAssignee(task.getId(), userId);
    }


    /**
     * 团队 OWNER / ADMIN
     * 项目负责人
     * 项目 MANAGER
     */
    private boolean hasManagePermission(
            Project project,
            Long userId
    ) {

        TeamMember teamMember =
                teamMemberService.getTeamMember(
                        project.getTeamId(),
                        userId
                );

        if (isTeamAdmin(teamMember)) {

            return true;
        }

        if (
                project.getOwnerId() != null
                        && project.getOwnerId().equals(userId)
        ) {

            return true;
        }

        ProjectMember projectMember =
                projectMemberService.getProjectMember(
                        project.getId(),
                        userId
                );

        return projectMember != null
                && ProjectRole.MANAGER.name()
                .equals(projectMember.getRoleType());
    }


    private boolean isEffectiveReadOnly(
            Project project,
            Long userId
    ) {

        TeamMember teamMember =
                teamMemberService.getTeamMember(
                        project.getTeamId(),
                        userId
                );

        if (isTeamAdmin(teamMember)) {

            return false;
        }

        if (
                project.getOwnerId() != null
                        && project.getOwnerId().equals(userId)
        ) {

            return false;
        }

        ProjectMember projectMember =
                projectMemberService.getProjectMember(
                        project.getId(),
                        userId
                );

        return projectMember != null
                && ProjectRole.READ_ONLY.name()
                .equals(projectMember.getRoleType());
    }


    private boolean isTeamAdmin(TeamMember teamMember) {

        if (teamMember == null) {

            return false;
        }

        return TeamRole.OWNER.name()
                .equals(teamMember.getRoleType())
                || TeamRole.ADMIN.name()
                .equals(teamMember.getRoleType());
    }


    private boolean isTaskAssignee(
            Long taskId,
            Long userId
    ) {

        Long count =
                taskAssigneeMapper.selectCount(

                        new LambdaQueryWrapper<TaskAssignee>()

                                .eq(
                                        TaskAssignee::getTaskId,
                                        taskId
                                )

                                .eq(
                                        TaskAssignee::getUserId,
                                        userId
                                )
                );

        return count != null && count > 0;
    }


    private boolean isTerminalTask(Task task) {

        return TaskStatus.DONE.name()
                .equals(task.getStatus())
                || TaskStatus.CANCELLED.name()
                .equals(task.getStatus());
    }


    private void assertProjectWritable(Project project) {

        if (
                ProjectStatus.ARCHIVED.name()
                        .equals(project.getStatus())
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_ARCHIVED_PROJECT
            );
        }
    }


    private void assertTaskWritable(Task task) {

        if (isTerminalTask(task)) {

            throw new BusinessException(
                    ErrorCode.SUB_TASK_PERMISSION_DENIED
            );
        }
    }


    private String normalizeTitle(String title) {

        String value =
                title == null
                        ? ""
                        : title.trim();

        if (!StringUtils.hasText(value)) {

            throw new BusinessException(
                    ErrorCode.SUB_TASK_TITLE_EMPTY
            );
        }

        if (value.length() > 200) {

            throw new BusinessException(
                    ErrorCode.SUB_TASK_TITLE_TOO_LONG
            );
        }

        return value;
    }


    private String normalizeDescription(String description) {

        if (description == null) {

            return null;
        }

        String value =
                description.trim();

        if (value.isEmpty()) {

            return null;
        }

        if (value.length() > 1000) {

            throw new BusinessException(
                    ErrorCode.PARAM_ERROR
            );
        }

        return value;
    }


    private Integer normalizeRequiredFlag(Integer requiredFlag) {

        if (requiredFlag == null) {

            return 1;
        }

        if (
                requiredFlag != 0
                        && requiredFlag != 1
        ) {

            throw new BusinessException(
                    ErrorCode.PARAM_ERROR
            );
        }

        return requiredFlag;
    }


    private void saveActivity(
            Long taskId,
            Long operatorId,
            TaskActivityType type,
            String content
    ) {

        TaskActivity activity =
                new TaskActivity();

        activity.setTaskId(taskId);
        activity.setOperatorId(operatorId);
        activity.setActivityType(type.name());
        activity.setContent(content);
        activity.setCreatedTime(LocalDateTime.now());

        taskActivityMapper.insert(activity);
    }


    private SubTask getById(Long id) {

        SubTask subTask =
                subTaskMapper.selectById(id);

        if (subTask == null) {

            throw new BusinessException(
                    ErrorCode.SUB_TASK_NOT_FOUND
            );
        }

        return subTask;
    }


    private void assertBelongsToTask(
            SubTask subTask,
            Long taskId
    ) {

        if (
                subTask.getTaskId() == null
                        || !subTask.getTaskId().equals(taskId)
        ) {

            throw new BusinessException(
                    ErrorCode.SUB_TASK_NOT_FOUND
            );
        }
    }


    private Task getTaskOrThrow(Long taskId) {

        Task task =
                taskMapper.selectById(taskId);

        if (task == null) {

            throw new BusinessException(
                    ErrorCode.TASK_NOT_FOUND
            );
        }

        return task;
    }


    private Project getProjectOrThrow(Long projectId) {

        Project project =
                projectMapper.selectById(projectId);

        if (project == null) {

            throw new BusinessException(
                    ErrorCode.PROJECT_NOT_FOUND
            );
        }

        return project;
    }


    private SubTaskVO convert(
            SubTask subTask,
            Task task,
            Project project,
            Long currentUserId
    ) {

        SubTaskVO vo =
                new SubTaskVO();

        vo.setId(subTask.getId());
        vo.setTaskId(subTask.getTaskId());
        vo.setTitle(subTask.getTitle());
        vo.setDescription(subTask.getDescription());
        vo.setStatus(subTask.getStatus());
        vo.setRequiredFlag(subTask.getRequiredFlag());
        vo.setSortOrder(subTask.getSortOrder());
        vo.setCreatorId(subTask.getCreatorId());
        vo.setCompletedTime(subTask.getCompletedTime());
        vo.setCreatorName(
                resolveUserName(subTask.getCreatorId())
        );

        boolean writable =
                !ProjectStatus.ARCHIVED.name()
                        .equals(project.getStatus())
                        && !isTerminalTask(task);

        vo.setCanEdit(
                writable
                        && canEditSubTask(
                        project,
                        task,
                        subTask,
                        currentUserId
                )
        );

        vo.setCanDelete(
                writable
                        && canDeleteSubTask(
                        project,
                        task,
                        currentUserId
                )
        );

        vo.setCanChangeStatus(
                writable
                        && canChangeSubTaskStatus(
                        project,
                        task,
                        currentUserId
                )
        );

        return vo;
    }


    private String resolveUserName(Long userId) {

        if (userId == null) {

            return null;
        }

        SysUser user =
                sysUserMapper.selectById(userId);

        if (user == null) {

            return null;
        }

        if (
                StringUtils.hasText(user.getNickname())
        ) {

            return user.getNickname();
        }

        return user.getUsername();
    }
}
