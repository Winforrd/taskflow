package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.winforrd.taskflow.common.enums.ErrorCode;
import com.winforrd.taskflow.common.enums.NotificationType;
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
import com.winforrd.taskflow.service.*;
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

    private final SubTaskService subTaskService;

    private final TaskAssigneeMapper taskAssigneeMapper;

    private final TaskStatusRecordMapper taskStatusRecordMapper;

    private final TaskActivityMapper taskActivityMapper;

    private final ProjectMapper projectMapper;

    private final SysUserMapper sysUserMapper;

    private final TeamMemberService teamMemberService;

    private final ProjectMemberService projectMemberService;

    /**
     * 通知服务
     */
    private final NotificationService notificationService;


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


        /**
         * 所有新任务都从 TODO 开始。
         */
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


        task.setVersion(
                0
        );


        taskMapper.insert(
                task
        );


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
         * 自动把自己设置成负责人。
         * =================================================
         */
        if (
                isOrdinaryMember(
                        project,
                        currentUserId
                )
        ) {

            /**
             * 防止绕过前端，
             * 手工给其他成员分配任务。
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


            /**
             * MEMBER 自动自己负责。
             *
             * 自己给自己分配，
             * 不发送通知。
             */
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
         * 1. 创建无人待调度任务
         * 2. 创建时指定负责人
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
         * 状态机合法性。
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
         * 权限校验。
         */
        checkCanTransition(
                project,
                task,
                currentUserId,
                oldStatus,
                targetStatus
        );


        /**
         * 防御：
         *
         * 进入进行中时必须至少有一个负责人。
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
         * 提交验收 / 完成任务前：
         * 必需子任务必须全部完成。
         */
        if (
                TaskStatus.IN_REVIEW.name()
                        .equals(targetStatus)
                        ||
                        TaskStatus.DONE.name()
                                .equals(targetStatus)
        ) {

            boolean exists =
                    subTaskService
                            .existsRequiredUnfinished(
                                    taskId
                            );

            if (exists) {

                throw new BusinessException(
                        ErrorCode.SUB_TASK_REQUIRED_UNFINISHED
                );
            }
        }


        if (
                TaskStatus.DONE.name()
                        .equals(targetStatus)
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


        /**
         * =================================================
         * 状态变化通知
         * =================================================
         */
        sendStatusNotification(
                project,
                task,
                currentUserId,
                oldStatus,
                targetStatus
        );
    }


    /**
     * =====================================================
     * 添加负责人
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


        checkTaskCanManageAssignees(
                task
        );


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
     * 移除负责人
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


        checkTaskCanManageAssignees(
                task
        );


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
         * IN_PROGRESS / IN_REVIEW
         * 至少保留一名负责人。
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

            Long count =
                    getTaskAssigneeCount(
                            taskId
                    );


            if (
                    count <= 1
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


        /**
         * =================================================
         * 被移除负责人通知
         *
         * 自己移除自己时不通知自己。
         * =================================================
         */
        if (
                !currentUserId.equals(
                        userId
                )
        ) {

            notificationService.createNotification(

                    userId,

                    NotificationType.TASK_UNASSIGNED,

                    "任务负责人发生变化",

                    "你已被移出任务："
                            +
                            task.getTitle(),

                    "TASK",

                    taskId
            );
        }
    }


    /**
     * =====================================================
     * 任务动态
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
     * 创建任务时是否允许指定某用户
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


        if (
                hasTaskManagePermission(
                        project,
                        operatorId
                )
        ) {

            return;
        }


        /**
         * MEMBER 只能指派自己。
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
     * 判断用户能否成为负责人
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
         * READ_ONLY 不能成为负责人。
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

        } catch (
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


        /**
         * =================================================
         * 任务分配通知
         *
         * 自己给自己分配不通知。
         * =================================================
         */
        if (
                !operatorId.equals(
                        userId
                )
        ) {

            notificationService.createNotification(

                    userId,

                    NotificationType.TASK_ASSIGNED,

                    "你有新的任务",

                    "你被分配了任务："
                            +
                            task.getTitle(),

                    "TASK",

                    task.getId()
            );
        }
    }


    /**
     * =====================================================
     * 状态变化通知
     * =====================================================
     */
    private void sendStatusNotification(
            Project project,
            Task task,
            Long operatorId,
            String oldStatus,
            String targetStatus
    ) {

        /**
         * =================================================
         * 提交验收
         * =================================================
         */
        if (
                TaskStatus.IN_PROGRESS.name()
                        .equals(
                                oldStatus
                        )
                        &&
                        TaskStatus.IN_REVIEW.name()
                                .equals(
                                        targetStatus
                                )
        ) {

            Set<Long> receiverIds =
                    new LinkedHashSet<>();


            /**
             * 项目负责人。
             */
            receiverIds.add(
                    project.getOwnerId()
            );


            /**
             * 团队 OWNER / ADMIN。
             */
            List<TeamMember> teamAdmins =
                    teamMemberService.getTeamAdmins(
                            project.getTeamId()
                    );


            if (
                    teamAdmins != null
            ) {

                for (
                        TeamMember member :
                        teamAdmins
                ) {

                    receiverIds.add(
                            member.getUserId()
                    );
                }
            }


            /**
             * 不通知操作人自己。
             */
            receiverIds.remove(
                    operatorId
            );


            notificationService.createNotifications(

                    receiverIds,

                    NotificationType.TASK_SUBMITTED,

                    "有任务等待验收",

                    "任务“"
                            +
                            task.getTitle()
                            +
                            "”已提交验收",

                    "TASK",

                    task.getId()
            );


            return;
        }


        /**
         * =================================================
         * 验收通过
         * =================================================
         */
        if (
                TaskStatus.IN_REVIEW.name()
                        .equals(
                                oldStatus
                        )
                        &&
                        TaskStatus.DONE.name()
                                .equals(
                                        targetStatus
                                )
        ) {

            sendNotificationToAssignees(

                    task.getId(),

                    operatorId,

                    NotificationType.TASK_APPROVED,

                    "任务验收通过",

                    "任务“"
                            +
                            task.getTitle()
                            +
                            "”已验收完成"
            );


            return;
        }


        /**
         * =================================================
         * 验收驳回
         * =================================================
         */
        if (
                TaskStatus.IN_REVIEW.name()
                        .equals(
                                oldStatus
                        )
                        &&
                        TaskStatus.IN_PROGRESS.name()
                                .equals(
                                        targetStatus
                                )
        ) {

            sendNotificationToAssignees(

                    task.getId(),

                    operatorId,

                    NotificationType.TASK_REJECTED,

                    "任务验收被驳回",

                    "任务“"
                            +
                            task.getTitle()
                            +
                            "”验收未通过，请继续处理"
            );


            return;
        }


        /**
         * =================================================
         * 任务取消
         * =================================================
         */
        if (
                TaskStatus.CANCELLED.name()
                        .equals(
                                targetStatus
                        )
        ) {

            sendNotificationToAssignees(

                    task.getId(),

                    operatorId,

                    NotificationType.TASK_CANCELLED,

                    "任务已取消",

                    "任务“"
                            +
                            task.getTitle()
                            +
                            "”已被取消"
            );
        }
    }


    /**
     * =====================================================
     * 通知任务所有负责人
     * =====================================================
     */
    private void sendNotificationToAssignees(
            Long taskId,
            Long operatorId,
            NotificationType type,
            String title,
            String content
    ) {

        List<TaskAssignee> assignees =
                taskAssigneeMapper.selectList(

                        new LambdaQueryWrapper<TaskAssignee>()

                                .eq(
                                        TaskAssignee::getTaskId,
                                        taskId
                                )

                );


        Set<Long> receiverIds =
                new LinkedHashSet<>();


        for (
                TaskAssignee assignee :
                assignees
        ) {

            receiverIds.add(
                    assignee.getUserId()
            );
        }


        /**
         * 不通知操作者自己。
         */
        receiverIds.remove(
                operatorId
        );


        notificationService.createNotifications(

                receiverIds,

                type,

                title,

                content,

                "TASK",

                taskId
        );
    }


    /**
     * =====================================================
     * DONE / CANCELLED 禁止修改负责人
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
     * 状态流转权限
     * =====================================================
     */
    private void checkCanTransition(
            Project project,
            Task task,
            Long userId,
            String currentStatus,
            String targetStatus
    ) {

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
         * 只有负责人能够开始。
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
         * 只有负责人提交验收。
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
         * 项目负责人。
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
         * 驳回。
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
         * 取消任务。
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
     * 调度管理权限
     *
     * 团队 OWNER / ADMIN
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
     * 团队 OWNER / ADMIN
     * 项目负责人
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
     * 团队管理员判断
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


        if (
                teamMember != null
                        &&
                        isTeamAdmin(
                                teamMember
                        )
        ) {

            return false;
        }


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


            default -> false;
        };
    }


    /**
     * =====================================================
     * 当前用户允许的下一状态
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


        if (
                isEffectiveReadOnly(
                        project,
                        userId
                )
        ) {

            return result;
        }


        if (
                TaskStatus.TODO.name()
                        .equals(
                                currentStatus
                        )
        ) {

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


        if (
                TaskStatus.IN_PROGRESS.name()
                        .equals(
                                currentStatus
                        )
        ) {

            if (
                    isTaskAssignee(
                            task.getId(),
                            userId
                    )
                            &&
                            !subTaskService
                                    .existsRequiredUnfinished(
                                            task.getId()
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


                if (
                        !subTaskService
                                .existsRequiredUnfinished(
                                        task.getId()
                                )
                ) {

                    result.add(
                            TaskStatus.DONE.name()
                    );
                }
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
     * 是否管理负责人
     * =====================================================
     */
    private boolean calculateCanManageAssignees(
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
     * 是否可给其他人分配任务
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
     * 状态记录
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
     * 动态记录
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
     * 状态动态文字
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
     * 项目归档检查
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
     * 优先级检查
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
     * 日期检查
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
     * Task → VO
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


        vo.setCanCreateSubTask(
                subTaskService.canCreateSubTask(
                        task.getId(),
                        currentUserId
                )
        );


        return vo;
    }


    /**
     * =====================================================
     * 负责人 VO
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
     * 用户显示名
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