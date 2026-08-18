package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.winforrd.taskflow.common.enums.ErrorCode;
import com.winforrd.taskflow.common.enums.ProjectRole;
import com.winforrd.taskflow.common.enums.ProjectStatus;
import com.winforrd.taskflow.common.enums.TaskActivityType;
import com.winforrd.taskflow.common.enums.TaskPriority;
import com.winforrd.taskflow.common.enums.TaskStatus;
import com.winforrd.taskflow.common.enums.TeamRole;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.dto.ChangeTaskStatusRequest;
import com.winforrd.taskflow.dto.CreateTaskRequest;
import com.winforrd.taskflow.dto.UpdateTaskRequest;
import com.winforrd.taskflow.entity.Project;
import com.winforrd.taskflow.entity.ProjectMember;
import com.winforrd.taskflow.entity.SysUser;
import com.winforrd.taskflow.entity.Task;
import com.winforrd.taskflow.entity.TaskActivity;
import com.winforrd.taskflow.entity.TaskAssignee;
import com.winforrd.taskflow.entity.TaskStatusRecord;
import com.winforrd.taskflow.entity.TeamMember;
import com.winforrd.taskflow.mapper.ProjectMapper;
import com.winforrd.taskflow.mapper.SysUserMapper;
import com.winforrd.taskflow.mapper.TaskActivityMapper;
import com.winforrd.taskflow.mapper.TaskAssigneeMapper;
import com.winforrd.taskflow.mapper.TaskMapper;
import com.winforrd.taskflow.mapper.TaskStatusRecordMapper;
import com.winforrd.taskflow.service.ProjectMemberService;
import com.winforrd.taskflow.service.TaskService;
import com.winforrd.taskflow.service.TeamMemberService;
import com.winforrd.taskflow.vo.TaskActivityVO;
import com.winforrd.taskflow.vo.TaskAssigneeVO;
import com.winforrd.taskflow.vo.TaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl
        implements TaskService {

    private final TaskMapper taskMapper;

    private final TaskAssigneeMapper taskAssigneeMapper;

    private final TaskStatusRecordMapper taskStatusRecordMapper;

    private final TaskActivityMapper taskActivityMapper;

    private final ProjectMapper projectMapper;

    private final SysUserMapper sysUserMapper;

    private final TeamMemberService teamMemberService;

    private final ProjectMemberService projectMemberService;


    /**
     * =====================================================
     * 创建任务
     * =====================================================
     */
    @Override
    @Transactional
    public Long createTask(
            Long projectId,
            CreateTaskRequest request
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        Project project =
                getProjectOrThrow(
                        projectId
                );


        checkProjectNotArchived(
                project
        );


        checkCanCreateTask(
                project,
                currentUserId
        );


        validatePriority(
                request.getPriority()
        );


        validateDate(
                request.getStartDate(),
                request.getDeadline()
        );


        Task task =
                new Task();


        task.setProjectId(
                projectId
        );


        task.setTitle(
                request.getTitle()
        );


        task.setDescription(
                request.getDescription()
        );


        task.setStatus(
                TaskStatus.TODO.name()
        );


        task.setPriority(

                StringUtils.hasText(
                        request.getPriority()
                )

                        ? request.getPriority()

                        : TaskPriority.MEDIUM.name()

        );


        task.setCreatorId(
                currentUserId
        );


        task.setStartDate(
                request.getStartDate()
        );


        task.setDeadline(
                request.getDeadline()
        );


        task.setVersion(0);


        taskMapper.insert(
                task
        );


        /**
         * 初始状态
         */
        saveStatusRecord(
                task.getId(),
                null,
                TaskStatus.TODO.name(),
                currentUserId
        );


        saveActivity(
                task.getId(),
                currentUserId,
                TaskActivityType.CREATED,
                "创建了任务"
        );


        /**
         * =================================================
         * 普通 MEMBER 创建任务
         *
         * 自动由自己负责
         * =================================================
         */
        if (
                isOrdinaryMember(
                        project,
                        currentUserId
                )
        ) {

            /**
             * 即使绕过前端传其他人的 ID，
             * 后端也拒绝。
             */
            if (
                    request.getAssigneeIds() != null
                            &&
                            request.getAssigneeIds()
                                    .stream()
                                    .anyMatch(
                                            userId ->
                                                    !currentUserId.equals(
                                                            userId
                                                    )
                                    )
            ) {

                throw new BusinessException(
                        ErrorCode.TASK_PERMISSION_DENIED
                );
            }


            addAssigneeInternal(
                    task,
                    currentUserId,
                    currentUserId
            );


            return task.getId();
        }


        /**
         * =================================================
         * OWNER / ADMIN
         * 项目负责人
         * MANAGER
         *
         * 可以：
         *
         * 1. 创建无人 TODO
         * 2. 创建时直接指定负责人
         * =================================================
         */
        if (
                request.getAssigneeIds() != null
                        &&
                        !request.getAssigneeIds()
                                .isEmpty()
        ) {

            Set<Long> assigneeIds =
                    new LinkedHashSet<>(
                            request.getAssigneeIds()
                    );


            for (
                    Long assigneeId :
                    assigneeIds
            ) {

                checkCanAssignUser(
                        project,
                        currentUserId,
                        assigneeId
                );


                addAssigneeInternal(
                        task,
                        assigneeId,
                        currentUserId
                );
            }
        }


        return task.getId();
    }


    /**
     * =====================================================
     * 项目任务列表
     * =====================================================
     */
    @Override
    public List<TaskVO> getProjectTasks(
            Long projectId,
            String status,
            String keyword
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        projectMemberService.checkCanView(
                projectId,
                currentUserId
        );


        LambdaQueryWrapper<Task> wrapper =
                new LambdaQueryWrapper<Task>()

                        .eq(
                                Task::getProjectId,
                                projectId
                        )

                        .orderByDesc(
                                Task::getCreatedTime
                        );


        if (
                StringUtils.hasText(
                        status
                )
        ) {

            wrapper.eq(
                    Task::getStatus,
                    status
            );
        }


        if (
                StringUtils.hasText(
                        keyword
                )
        ) {

            wrapper.like(
                    Task::getTitle,
                    keyword
            );
        }


        List<Task> tasks =
                taskMapper.selectList(
                        wrapper
                );


        List<TaskVO> result =
                new ArrayList<>();


        for (
                Task task :
                tasks
        ) {

            result.add(
                    convertTaskVO(
                            task,
                            currentUserId
                    )
            );
        }


        return result;
    }


    /**
     * =====================================================
     * 任务详情
     * =====================================================
     */
    @Override
    public TaskVO getTaskDetail(
            Long taskId
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        Task task =
                getTaskOrThrow(
                        taskId
                );


        projectMemberService.checkCanView(
                task.getProjectId(),
                currentUserId
        );


        return convertTaskVO(
                task,
                currentUserId
        );
    }


    /**
     * =====================================================
     * 修改任务基本信息
     * =====================================================
     */
    @Override
    @Transactional
    public void updateTask(
            Long taskId,
            UpdateTaskRequest request
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        Task task =
                getTaskOrThrow(
                        taskId
                );


        Project project =
                getProjectOrThrow(
                        task.getProjectId()
                );


        checkProjectNotArchived(
                project
        );


        checkCanEditTask(
                project,
                task,
                currentUserId
        );


        validatePriority(
                request.getPriority()
        );


        validateDate(
                request.getStartDate(),
                request.getDeadline()
        );


        Task update =
                new Task();


        update.setId(
                taskId
        );


        update.setTitle(
                request.getTitle()
        );


        update.setDescription(
                request.getDescription()
        );


        update.setPriority(
                request.getPriority()
        );


        update.setStartDate(
                request.getStartDate()
        );


        update.setDeadline(
                request.getDeadline()
        );


        update.setVersion(
                request.getVersion()
        );


        int rows =
                taskMapper.updateById(
                        update
                );


        if (
                rows == 0
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_CONCURRENT_MODIFICATION
            );
        }


        saveActivity(
                taskId,
                currentUserId,
                TaskActivityType.UPDATED,
                "修改了任务信息"
        );
    }


    /**
     * =====================================================
     * 状态流转
     * =====================================================
     */
    @Override
    @Transactional
    public void changeTaskStatus(
            Long taskId,
            ChangeTaskStatusRequest request
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        Task task =
                getTaskOrThrow(
                        taskId
                );


        Project project =
                getProjectOrThrow(
                        task.getProjectId()
                );


        checkProjectNotArchived(
                project
        );


        String oldStatus =
                task.getStatus();


        String targetStatus =
                request.getTargetStatus();


        /**
         * 状态机合法性
         */
        if (
                !canTransition(
                        oldStatus,
                        targetStatus
                )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_STATUS_TRANSITION_INVALID
            );
        }


        /**
         * 用户权限
         */
        checkCanTransition(
                project,
                task,
                currentUserId,
                oldStatus,
                targetStatus
        );


        /**
         * =================================================
         * 防御性检查：
         *
         * TODO → IN_PROGRESS
         *
         * 必须确保当前至少有一个负责人。
         *
         * 正常情况下，因为执行者自己必须是负责人，
         * 这里一定成立。
         *
         * 但增加这个判断可以防数据库脏数据。
         * =================================================
         */
        if (
                TaskStatus.IN_PROGRESS.name()
                        .equals(
                                targetStatus
                        )
                        &&
                        getTaskAssigneeCount(
                                taskId
                        ) <= 0
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }


        Task update =
                new Task();


        update.setId(
                taskId
        );


        update.setStatus(
                targetStatus
        );


        update.setVersion(
                request.getVersion()
        );


        /**
         * 完成时间
         */
        if (
                TaskStatus.DONE.name()
                        .equals(
                                targetStatus
                        )
        ) {

            update.setCompletedTime(
                    LocalDateTime.now()
            );
        }


        int rows =
                taskMapper.updateById(
                        update
                );


        if (
                rows == 0
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_CONCURRENT_MODIFICATION
            );
        }


        saveStatusRecord(
                taskId,
                oldStatus,
                targetStatus,
                currentUserId
        );


        saveActivity(
                taskId,
                currentUserId,
                TaskActivityType.STATUS_CHANGED,
                buildStatusActivityContent(
                        oldStatus,
                        targetStatus
                )
        );
    }


    /**
     * =====================================================
     * 添加任务负责人
     * =====================================================
     */
    @Override
    @Transactional
    public void addAssignee(
            Long taskId,
            Long userId
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        Task task =
                getTaskOrThrow(
                        taskId
                );


        Project project =
                getProjectOrThrow(
                        task.getProjectId()
                );


        checkProjectNotArchived(
                project
        );


        /**
         * DONE / CANCELLED
         * 不允许再调整负责人。
         */
        checkTaskCanManageAssignees(
                task
        );


        /**
         * 只有：
         *
         * 团队 OWNER / ADMIN
         * 项目负责人
         * MANAGER
         *
         * 可以调度负责人。
         */
        if (
                !hasTaskManagePermission(
                        project,
                        currentUserId
                )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }


        /**
         * 被添加者必须：
         *
         * 1. 是项目成员
         * 2. 不是 READ_ONLY
         */
        checkAssigneeEligible(
                project,
                userId
        );


        addAssigneeInternal(
                task,
                userId,
                currentUserId
        );
    }


    /**
     * =====================================================
     * 移除任务负责人
     * =====================================================
     */
    @Override
    @Transactional
    public void removeAssignee(
            Long taskId,
            Long userId
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        Task task =
                getTaskOrThrow(
                        taskId
                );


        Project project =
                getProjectOrThrow(
                        task.getProjectId()
                );


        checkProjectNotArchived(
                project
        );


        /**
         * DONE / CANCELLED
         * 不允许调整负责人。
         */
        checkTaskCanManageAssignees(
                task
        );


        /**
         * 管理权限。
         */
        if (
                !hasTaskManagePermission(
                        project,
                        currentUserId
                )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }


        TaskAssignee assignee =
                taskAssigneeMapper.selectOne(

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


        if (
                assignee == null
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_ASSIGNEE_NOT_FOUND
            );
        }


        /**
         * =================================================
         * 最后一个负责人保护
         *
         * TODO：
         * 可以把最后一个负责人移除，
         * 重新变为无人待调度。
         *
         * IN_PROGRESS：
         * 不允许没有负责人。
         *
         * IN_REVIEW：
         * 不允许没有负责人，
         * 必须保留至少一个实际执行人。
         * =================================================
         */
        if (
                TaskStatus.IN_PROGRESS.name()
                        .equals(
                                task.getStatus()
                        )
                        ||
                        TaskStatus.IN_REVIEW.name()
                                .equals(
                                        task.getStatus()
                                )
        ) {

            Long assigneeCount =
                    getTaskAssigneeCount(
                            taskId
                    );


            if (
                    assigneeCount <= 1
            ) {

                throw new BusinessException(
                        ErrorCode.TASK_LAST_ASSIGNEE_CANNOT_REMOVE
                );
            }
        }


        taskAssigneeMapper.deleteById(
                assignee.getId()
        );


        SysUser user =
                sysUserMapper.selectById(
                        userId
                );


        saveActivity(
                taskId,
                currentUserId,
                TaskActivityType.ASSIGNEE_REMOVED,
                "移除了负责人："
                        +
                        getUserDisplayName(
                                user
                        )
        );
    }


    /**
     * =====================================================
     * 查询任务动态
     * =====================================================
     */
    @Override
    public List<TaskActivityVO> getTaskActivities(
            Long taskId
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        Task task =
                getTaskOrThrow(
                        taskId
                );


        projectMemberService.checkCanView(
                task.getProjectId(),
                currentUserId
        );


        List<TaskActivity> activities =
                taskActivityMapper.selectList(

                        new LambdaQueryWrapper<TaskActivity>()

                                .eq(
                                        TaskActivity::getTaskId,
                                        taskId
                                )

                                .orderByDesc(
                                        TaskActivity::getCreatedTime
                                )

                );


        List<TaskActivityVO> result =
                new ArrayList<>();


        for (
                TaskActivity activity :
                activities
        ) {

            SysUser operator =
                    sysUserMapper.selectById(
                            activity.getOperatorId()
                    );


            TaskActivityVO vo =
                    new TaskActivityVO();


            vo.setId(
                    activity.getId()
            );


            vo.setOperatorId(
                    activity.getOperatorId()
            );


            vo.setOperatorName(
                    getUserDisplayName(
                            operator
                    )
            );


            vo.setActivityType(
                    activity.getActivityType()
            );


            vo.setContent(
                    activity.getContent()
            );


            vo.setCreatedTime(
                    activity.getCreatedTime()
            );


            result.add(
                    vo
            );
        }


        return result;
    }


    /**
     * =====================================================
     * 创建任务时：
     * 是否可以指定某个用户
     * =====================================================
     */
    private void checkCanAssignUser(
            Project project,
            Long operatorId,
            Long targetUserId
    ) {

        checkAssigneeEligible(
                project,
                targetUserId
        );


        /**
         * 高权限调度角色。
         */
        if (
                hasTaskManagePermission(
                        project,
                        operatorId
                )
        ) {

            return;
        }


        /**
         * 普通 MEMBER
         * 只能指定自己。
         */
        if (
                operatorId.equals(
                        targetUserId
                )
        ) {

            return;
        }


        throw new BusinessException(
                ErrorCode.TASK_PERMISSION_DENIED
        );
    }


    /**
     * =====================================================
     * 是否可以成为任务负责人
     * =====================================================
     */
    private void checkAssigneeEligible(
            Project project,
            Long userId
    ) {

        ProjectMember projectMember =
                projectMemberService.getProjectMember(
                        project.getId(),
                        userId
                );


        if (
                projectMember == null
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_ASSIGNEE_NOT_PROJECT_MEMBER
            );
        }


        /**
         * READ_ONLY
         * 永远不能成为负责人。
         */
        if (
                ProjectRole.READ_ONLY.name()
                        .equals(
                                projectMember.getRoleType()
                        )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }
    }


    /**
     * =====================================================
     * 内部添加负责人
     * =====================================================
     */
    private void addAssigneeInternal(
            Task task,
            Long userId,
            Long operatorId
    ) {

        Project project =
                getProjectOrThrow(
                        task.getProjectId()
                );


        /**
         * 内部调用也重新校验资格。
         */
        checkAssigneeEligible(
                project,
                userId
        );


        TaskAssignee exists =
                taskAssigneeMapper.selectOne(

                        new LambdaQueryWrapper<TaskAssignee>()

                                .eq(
                                        TaskAssignee::getTaskId,
                                        task.getId()
                                )

                                .eq(
                                        TaskAssignee::getUserId,
                                        userId
                                )

                );


        if (
                exists != null
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_ASSIGNEE_ALREADY_EXISTS
            );
        }


        TaskAssignee assignee =
                new TaskAssignee();


        assignee.setTaskId(
                task.getId()
        );


        assignee.setUserId(
                userId
        );


        assignee.setAssignedBy(
                operatorId
        );


        assignee.setAssignedTime(
                LocalDateTime.now()
        );


        try {

            taskAssigneeMapper.insert(
                    assignee
            );

        }

        catch (
                DuplicateKeyException e
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_ASSIGNEE_ALREADY_EXISTS
            );
        }


        SysUser user =
                sysUserMapper.selectById(
                        userId
                );


        saveActivity(
                task.getId(),
                operatorId,
                TaskActivityType.ASSIGNEE_ADDED,
                "添加了负责人："
                        +
                        getUserDisplayName(
                                user
                        )
        );
    }


    /**
     * =====================================================
     * DONE / CANCELLED
     * 禁止再管理负责人
     * =====================================================
     */
    private void checkTaskCanManageAssignees(
            Task task
    ) {

        if (
                TaskStatus.DONE.name()
                        .equals(
                                task.getStatus()
                        )
                        ||
                        TaskStatus.CANCELLED.name()
                                .equals(
                                        task.getStatus()
                                )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }
    }


    /**
     * =====================================================
     * 负责人数量
     * =====================================================
     */
    private Long getTaskAssigneeCount(
            Long taskId
    ) {

        return taskAssigneeMapper.selectCount(

                new LambdaQueryWrapper<TaskAssignee>()

                        .eq(
                                TaskAssignee::getTaskId,
                                taskId
                        )

        );
    }


    /**
     * =====================================================
     * 创建任务权限
     * =====================================================
     */
    private void checkCanCreateTask(
            Project project,
            Long userId
    ) {

        TeamMember teamMember =
                teamMemberService.getTeamMember(
                        project.getTeamId(),
                        userId
                );


        if (
                teamMember == null
        ) {

            throw new BusinessException(
                    ErrorCode.NOT_TEAM_MEMBER
            );
        }


        /**
         * 团队 OWNER / ADMIN
         */
        if (
                isTeamAdmin(
                        teamMember
                )
        ) {

            return;
        }


        ProjectMember projectMember =
                projectMemberService.getProjectMember(
                        project.getId(),
                        userId
                );


        if (
                projectMember == null
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }


        /**
         * READ_ONLY不能创建任务。
         */
        if (
                ProjectRole.READ_ONLY.name()
                        .equals(
                                projectMember.getRoleType()
                        )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }
    }


    /**
     * =====================================================
     * 是否普通 MEMBER
     * =====================================================
     */
    private boolean isOrdinaryMember(
            Project project,
            Long userId
    ) {

        TeamMember teamMember =
                teamMemberService.getTeamMember(
                        project.getTeamId(),
                        userId
                );


        if (
                teamMember != null
                        &&
                        isTeamAdmin(
                                teamMember
                        )
        ) {

            return false;
        }


        /**
         * 项目负责人优先。
         */
        if (
                project.getOwnerId()
                        .equals(
                                userId
                        )
        ) {

            return false;
        }


        ProjectMember projectMember =
                projectMemberService.getProjectMember(
                        project.getId(),
                        userId
                );


        return projectMember != null
                &&
                ProjectRole.MEMBER.name()
                        .equals(
                                projectMember.getRoleType()
                        );
    }


    /**
     * =====================================================
     * 编辑任务权限
     * =====================================================
     */
    private void checkCanEditTask(
            Project project,
            Task task,
            Long userId
    ) {

        /**
         * 高级调度权限。
         */
        if (
                hasTaskManagePermission(
                        project,
                        userId
                )
        ) {

            return;
        }


        ProjectMember projectMember =
                projectMemberService.getProjectMember(
                        project.getId(),
                        userId
                );


        if (
                projectMember == null
                        ||
                        ProjectRole.READ_ONLY.name()
                                .equals(
                                        projectMember.getRoleType()
                                )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }


        /**
         * MEMBER只能编辑自己创建的任务。
         */
        if (
                task.getCreatorId()
                        .equals(
                                userId
                        )
        ) {

            return;
        }


        throw new BusinessException(
                ErrorCode.TASK_PERMISSION_DENIED
        );
    }


    /**
     * =====================================================
     * 状态迁移权限
     * =====================================================
     */
    private void checkCanTransition(
            Project project,
            Task task,
            Long userId,
            String currentStatus,
            String targetStatus
    ) {

        /**
         * READ_ONLY完全不能操作。
         */
        if (
                isEffectiveReadOnly(
                        project,
                        userId
                )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }


        /**
         * TODO → IN_PROGRESS
         *
         * 只有负责人能开始执行。
         */
        if (
                TaskStatus.TODO.name()
                        .equals(
                                currentStatus
                        )
                        &&
                        TaskStatus.IN_PROGRESS.name()
                                .equals(
                                        targetStatus
                                )
        ) {

            if (
                    isTaskAssignee(
                            task.getId(),
                            userId
                    )
            ) {

                return;
            }


            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }


        /**
         * IN_PROGRESS → IN_REVIEW
         *
         * 只有负责人可以提交验收。
         */
        if (
                TaskStatus.IN_PROGRESS.name()
                        .equals(
                                currentStatus
                        )
                        &&
                        TaskStatus.IN_REVIEW.name()
                                .equals(
                                        targetStatus
                                )
        ) {

            if (
                    isTaskAssignee(
                            task.getId(),
                            userId
                    )
            ) {

                return;
            }


            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }


        /**
         * IN_REVIEW → DONE
         *
         * 团队 OWNER / ADMIN
         * 项目负责人
         */
        if (
                TaskStatus.IN_REVIEW.name()
                        .equals(
                                currentStatus
                        )
                        &&
                        TaskStatus.DONE.name()
                                .equals(
                                        targetStatus
                                )
        ) {

            if (
                    hasReviewPermission(
                            project,
                            userId
                    )
            ) {

                return;
            }


            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }


        /**
         * IN_REVIEW → IN_PROGRESS
         *
         * 验收驳回。
         */
        if (
                TaskStatus.IN_REVIEW.name()
                        .equals(
                                currentStatus
                        )
                        &&
                        TaskStatus.IN_PROGRESS.name()
                                .equals(
                                        targetStatus
                                )
        ) {

            if (
                    hasReviewPermission(
                            project,
                            userId
                    )
            ) {

                return;
            }


            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }


        /**
         * TODO / IN_PROGRESS → CANCELLED
         *
         * 团队 OWNER / ADMIN
         * 项目负责人
         *
         * 或任务创建人。
         */
        if (
                TaskStatus.CANCELLED.name()
                        .equals(
                                targetStatus
                        )
        ) {

            if (
                    hasCancelAnyTaskPermission(
                            project,
                            userId
                    )
                            ||
                            task.getCreatorId()
                                    .equals(
                                            userId
                                    )
            ) {

                return;
            }


            throw new BusinessException(
                    ErrorCode.TASK_PERMISSION_DENIED
            );
        }


        throw new BusinessException(
                ErrorCode.TASK_PERMISSION_DENIED
        );
    }


    /**
     * =====================================================
     * 任务调度权限
     *
     * 团队 OWNER
     * 团队 ADMIN
     * 项目负责人
     * 项目 MANAGER
     * =====================================================
     */
    private boolean hasTaskManagePermission(
            Project project,
            Long userId
    ) {

        TeamMember teamMember =
                teamMemberService.getTeamMember(
                        project.getTeamId(),
                        userId
                );


        if (
                teamMember != null
                        &&
                        isTeamAdmin(
                                teamMember
                        )
        ) {

            return true;
        }


        if (
                project.getOwnerId()
                        .equals(
                                userId
                        )
        ) {

            return true;
        }


        ProjectMember projectMember =
                projectMemberService.getProjectMember(
                        project.getId(),
                        userId
                );


        return projectMember != null
                &&
                ProjectRole.MANAGER.name()
                        .equals(
                                projectMember.getRoleType()
                        );
    }


    /**
     * =====================================================
     * 任意任务取消权限
     *
     * MANAGER 不包括
     * =====================================================
     */
    private boolean hasCancelAnyTaskPermission(
            Project project,
            Long userId
    ) {

        TeamMember teamMember =
                teamMemberService.getTeamMember(
                        project.getTeamId(),
                        userId
                );


        if (
                teamMember != null
                        &&
                        isTeamAdmin(
                                teamMember
                        )
        ) {

            return true;
        }


        return project.getOwnerId()
                .equals(
                        userId
                );
    }


    /**
     * =====================================================
     * 验收权限
     * =====================================================
     */
    private boolean hasReviewPermission(
            Project project,
            Long userId
    ) {

        TeamMember teamMember =
                teamMemberService.getTeamMember(
                        project.getTeamId(),
                        userId
                );


        if (
                teamMember != null
                        &&
                        isTeamAdmin(
                                teamMember
                        )
        ) {

            return true;
        }


        return project.getOwnerId()
                .equals(
                        userId
                );
    }


    /**
     * =====================================================
     * 是否团队 OWNER / ADMIN
     * =====================================================
     */
    private boolean isTeamAdmin(
            TeamMember teamMember
    ) {

        if (
                teamMember == null
        ) {

            return false;
        }


        return TeamRole.OWNER.name()
                .equals(
                        teamMember.getRoleType()
                )
                ||
                TeamRole.ADMIN.name()
                        .equals(
                                teamMember.getRoleType()
                        );
    }


    /**
     * =====================================================
     * READ_ONLY
     * =====================================================
     */
    private boolean isEffectiveReadOnly(
            Project project,
            Long userId
    ) {

        TeamMember teamMember =
                teamMemberService.getTeamMember(
                        project.getTeamId(),
                        userId
                );


        /**
         * 团队 OWNER / ADMIN 优先。
         */
        if (
                teamMember != null
                        &&
                        isTeamAdmin(
                                teamMember
                        )
        ) {

            return false;
        }


        /**
         * 项目负责人优先。
         */
        if (
                project.getOwnerId()
                        .equals(
                                userId
                        )
        ) {

            return false;
        }


        ProjectMember projectMember =
                projectMemberService.getProjectMember(
                        project.getId(),
                        userId
                );


        return projectMember != null
                &&
                ProjectRole.READ_ONLY.name()
                        .equals(
                                projectMember.getRoleType()
                        );
    }


    /**
     * =====================================================
     * 是否任务负责人
     * =====================================================
     */
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


        return count > 0;
    }


    /**
     * =====================================================
     * 状态机
     * =====================================================
     */
    private boolean canTransition(
            String currentStatus,
            String targetStatus
    ) {

        if (
                currentStatus == null
                        ||
                        targetStatus == null
        ) {

            return false;
        }


        return switch (
                currentStatus
                ) {

            case "TODO" ->

                    Set.of(
                            "IN_PROGRESS",
                            "CANCELLED"
                    ).contains(
                            targetStatus
                    );


            case "IN_PROGRESS" ->

                    Set.of(
                            "IN_REVIEW",
                            "CANCELLED"
                    ).contains(
                            targetStatus
                    );


            case "IN_REVIEW" ->

                    Set.of(
                            "IN_PROGRESS",
                            "DONE"
                    ).contains(
                            targetStatus
                    );


            /**
             * DONE / CANCELLED
             * 都是终态。
             */
            default -> false;
        };
    }


    /**
     * =====================================================
     * 计算当前用户能够执行的状态
     * =====================================================
     */
    private List<String> calculateAllowedNextStatuses(
            Project project,
            Task task,
            Long userId
    ) {

        List<String> result =
                new ArrayList<>();


        String currentStatus =
                task.getStatus();


        /**
         * DONE / CANCELLED
         */
        if (
                TaskStatus.DONE.name()
                        .equals(
                                currentStatus
                        )
                        ||
                        TaskStatus.CANCELLED.name()
                                .equals(
                                        currentStatus
                                )
        ) {

            return result;
        }


        /**
         * READ_ONLY
         */
        if (
                isEffectiveReadOnly(
                        project,
                        userId
                )
        ) {

            return result;
        }


        /**
         * TODO
         */
        if (
                TaskStatus.TODO.name()
                        .equals(
                                currentStatus
                        )
        ) {

            /**
             * 只有负责人能开始。
             */
            if (
                    isTaskAssignee(
                            task.getId(),
                            userId
                    )
            ) {

                result.add(
                        TaskStatus.IN_PROGRESS.name()
                );
            }


            /**
             * 有取消权限。
             */
            if (
                    hasCancelAnyTaskPermission(
                            project,
                            userId
                    )
                            ||
                            task.getCreatorId()
                                    .equals(
                                            userId
                                    )
            ) {

                result.add(
                        TaskStatus.CANCELLED.name()
                );
            }


            return result;
        }


        /**
         * IN_PROGRESS
         */
        if (
                TaskStatus.IN_PROGRESS.name()
                        .equals(
                                currentStatus
                        )
        ) {

            /**
             * 负责人提交验收。
             */
            if (
                    isTaskAssignee(
                            task.getId(),
                            userId
                    )
            ) {

                result.add(
                        TaskStatus.IN_REVIEW.name()
                );
            }


            if (
                    hasCancelAnyTaskPermission(
                            project,
                            userId
                    )
                            ||
                            task.getCreatorId()
                                    .equals(
                                            userId
                                    )
            ) {

                result.add(
                        TaskStatus.CANCELLED.name()
                );
            }


            return result;
        }


        /**
         * IN_REVIEW
         */
        if (
                TaskStatus.IN_REVIEW.name()
                        .equals(
                                currentStatus
                        )
        ) {

            if (
                    hasReviewPermission(
                            project,
                            userId
                    )
            ) {

                result.add(
                        TaskStatus.IN_PROGRESS.name()
                );


                result.add(
                        TaskStatus.DONE.name()
                );
            }
        }


        return result;
    }


    /**
     * =====================================================
     * 是否允许编辑任务
     * =====================================================
     */
    private boolean calculateCanEditTask(
            Project project,
            Task task,
            Long userId
    ) {

        if (
                ProjectStatus.ARCHIVED.name()
                        .equals(
                                project.getStatus()
                        )
        ) {

            return false;
        }


        /**
         * 已完成 / 已取消，
         * 建议任务内容也进入只读。
         */
        if (
                TaskStatus.DONE.name()
                        .equals(
                                task.getStatus()
                        )
                        ||
                        TaskStatus.CANCELLED.name()
                                .equals(
                                        task.getStatus()
                                )
        ) {

            return false;
        }


        if (
                hasTaskManagePermission(
                        project,
                        userId
                )
        ) {

            return true;
        }


        if (
                isEffectiveReadOnly(
                        project,
                        userId
                )
        ) {

            return false;
        }


        return task.getCreatorId()
                .equals(
                        userId
                );
    }


    /**
     * =====================================================
     * 是否允许管理负责人
     * =====================================================
     */
    private boolean calculateCanManageAssignees(
            Project project,
            Task task,
            Long userId
    ) {

        /**
         * 项目归档
         */
        if (
                ProjectStatus.ARCHIVED.name()
                        .equals(
                                project.getStatus()
                        )
        ) {

            return false;
        }


        /**
         * DONE / CANCELLED
         * 任务已经结束，不允许修改人员关系。
         */
        if (
                TaskStatus.DONE.name()
                        .equals(
                                task.getStatus()
                        )
                        ||
                        TaskStatus.CANCELLED.name()
                                .equals(
                                        task.getStatus()
                                )
        ) {

            return false;
        }


        return hasTaskManagePermission(
                project,
                userId
        );
    }


    /**
     * =====================================================
     * 是否可以给其他成员指派任务
     * =====================================================
     */
    private boolean calculateCanAssignOthers(
            Project project,
            Task task,
            Long userId
    ) {

        if (
                ProjectStatus.ARCHIVED.name()
                        .equals(
                                project.getStatus()
                        )
        ) {

            return false;
        }


        if (
                TaskStatus.DONE.name()
                        .equals(
                                task.getStatus()
                        )
                        ||
                        TaskStatus.CANCELLED.name()
                                .equals(
                                        task.getStatus()
                                )
        ) {

            return false;
        }


        return hasTaskManagePermission(
                project,
                userId
        );
    }


    /**
     * =====================================================
     * 保存状态日志
     * =====================================================
     */
    private void saveStatusRecord(
            Long taskId,
            String oldStatus,
            String newStatus,
            Long operatorId
    ) {

        TaskStatusRecord record =
                new TaskStatusRecord();


        record.setTaskId(
                taskId
        );


        record.setOldStatus(
                oldStatus
        );


        record.setNewStatus(
                newStatus
        );


        record.setOperatorId(
                operatorId
        );


        record.setChangedTime(
                LocalDateTime.now()
        );


        taskStatusRecordMapper.insert(
                record
        );
    }


    /**
     * =====================================================
     * 保存任务动态
     * =====================================================
     */
    private void saveActivity(
            Long taskId,
            Long operatorId,
            TaskActivityType type,
            String content
    ) {

        TaskActivity activity =
                new TaskActivity();


        activity.setTaskId(
                taskId
        );


        activity.setOperatorId(
                operatorId
        );


        activity.setActivityType(
                type.name()
        );


        activity.setContent(
                content
        );


        activity.setCreatedTime(
                LocalDateTime.now()
        );


        taskActivityMapper.insert(
                activity
        );
    }


    /**
     * =====================================================
     * 状态动态内容
     * =====================================================
     */
    private String buildStatusActivityContent(
            String oldStatus,
            String newStatus
    ) {

        if (
                TaskStatus.IN_REVIEW.name()
                        .equals(
                                oldStatus
                        )
                        &&
                        TaskStatus.IN_PROGRESS.name()
                                .equals(
                                        newStatus
                                )
        ) {

            return "验收未通过，任务被驳回重新处理";
        }


        if (
                TaskStatus.IN_REVIEW.name()
                        .equals(
                                oldStatus
                        )
                        &&
                        TaskStatus.DONE.name()
                                .equals(
                                        newStatus
                                )
        ) {

            return "验收通过，任务已完成";
        }


        return "任务状态从 "
                +
                oldStatus
                +
                " 变更为 "
                +
                newStatus;
    }


    /**
     * =====================================================
     * 获取任务
     * =====================================================
     */
    private Task getTaskOrThrow(
            Long taskId
    ) {

        Task task =
                taskMapper.selectById(
                        taskId
                );


        if (
                task == null
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_NOT_FOUND
            );
        }


        return task;
    }


    /**
     * =====================================================
     * 获取项目
     * =====================================================
     */
    private Project getProjectOrThrow(
            Long projectId
    ) {

        Project project =
                projectMapper.selectById(
                        projectId
                );


        if (
                project == null
        ) {

            throw new BusinessException(
                    ErrorCode.PROJECT_NOT_FOUND
            );
        }


        return project;
    }


    /**
     * =====================================================
     * 项目归档保护
     * =====================================================
     */
    private void checkProjectNotArchived(
            Project project
    ) {

        if (
                ProjectStatus.ARCHIVED.name()
                        .equals(
                                project.getStatus()
                        )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_ARCHIVED_PROJECT
            );
        }
    }


    /**
     * =====================================================
     * 优先级
     * =====================================================
     */
    private void validatePriority(
            String priority
    ) {

        if (
                !StringUtils.hasText(
                        priority
                )
        ) {

            return;
        }


        for (
                TaskPriority value :
                TaskPriority.values()
        ) {

            if (
                    value.name()
                            .equals(
                                    priority
                            )
            ) {

                return;
            }
        }


        throw new BusinessException(
                ErrorCode.TASK_PRIORITY_INVALID
        );
    }


    /**
     * =====================================================
     * 日期
     * =====================================================
     */
    private void validateDate(
            LocalDate startDate,
            LocalDate deadline
    ) {

        if (
                startDate != null
                        &&
                        deadline != null
                        &&
                        deadline.isBefore(
                                startDate
                        )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_DATE_INVALID
            );
        }
    }


    /**
     * =====================================================
     * Task → TaskVO
     * =====================================================
     */
    private TaskVO convertTaskVO(
            Task task,
            Long currentUserId
    ) {

        TaskVO vo =
                new TaskVO();


        SysUser creator =
                sysUserMapper.selectById(
                        task.getCreatorId()
                );


        Project project =
                getProjectOrThrow(
                        task.getProjectId()
                );


        vo.setId(
                task.getId()
        );


        vo.setProjectId(
                task.getProjectId()
        );


        vo.setTitle(
                task.getTitle()
        );


        vo.setDescription(
                task.getDescription()
        );


        vo.setStatus(
                task.getStatus()
        );


        vo.setPriority(
                task.getPriority()
        );


        vo.setCreatorId(
                task.getCreatorId()
        );


        vo.setCreatorName(
                getUserDisplayName(
                        creator
                )
        );


        vo.setStartDate(
                task.getStartDate()
        );


        vo.setDeadline(
                task.getDeadline()
        );


        vo.setCompletedTime(
                task.getCompletedTime()
        );


        vo.setVersion(
                task.getVersion()
        );


        vo.setCreatedTime(
                task.getCreatedTime()
        );


        vo.setAssignees(
                getAssigneeVOs(
                        task.getId()
                )
        );


        vo.setAllowedNextStatuses(
                calculateAllowedNextStatuses(
                        project,
                        task,
                        currentUserId
                )
        );


        vo.setCanEdit(
                calculateCanEditTask(
                        project,
                        task,
                        currentUserId
                )
        );


        vo.setCanManageAssignees(
                calculateCanManageAssignees(
                        project,
                        task,
                        currentUserId
                )
        );


        vo.setCanAssignOthers(
                calculateCanAssignOthers(
                        project,
                        task,
                        currentUserId
                )
        );


        return vo;
    }


    /**
     * =====================================================
     * 负责人VO
     * =====================================================
     */
    private List<TaskAssigneeVO> getAssigneeVOs(
            Long taskId
    ) {

        List<TaskAssignee> assignees =
                taskAssigneeMapper.selectList(

                        new LambdaQueryWrapper<TaskAssignee>()

                                .eq(
                                        TaskAssignee::getTaskId,
                                        taskId
                                )

                );


        List<TaskAssigneeVO> result =
                new ArrayList<>();


        for (
                TaskAssignee assignee :
                assignees
        ) {

            SysUser user =
                    sysUserMapper.selectById(
                            assignee.getUserId()
                    );


            if (
                    user == null
            ) {

                continue;
            }


            TaskAssigneeVO vo =
                    new TaskAssigneeVO();


            vo.setUserId(
                    user.getId()
            );


            vo.setUsername(
                    user.getUsername()
            );


            vo.setNickname(
                    user.getNickname()
            );


            vo.setAvatar(
                    user.getAvatar()
            );


            result.add(
                    vo
            );
        }


        return result;
    }


    /**
     * =====================================================
     * 用户名称
     * =====================================================
     */
    private String getUserDisplayName(
            SysUser user
    ) {

        if (
                user == null
        ) {

            return "未知用户";
        }


        if (
                StringUtils.hasText(
                        user.getNickname()
                )
        ) {

            return user.getNickname();
        }


        return user.getUsername();
    }
}