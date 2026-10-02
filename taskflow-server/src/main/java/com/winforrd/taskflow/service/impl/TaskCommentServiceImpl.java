package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.winforrd.taskflow.common.enums.ErrorCode;
import com.winforrd.taskflow.common.enums.NotificationType;
import com.winforrd.taskflow.common.enums.ProjectRole;
import com.winforrd.taskflow.common.enums.ProjectStatus;
import com.winforrd.taskflow.common.enums.TeamRole;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.dto.CreateTaskCommentRequest;
import com.winforrd.taskflow.entity.Project;
import com.winforrd.taskflow.entity.ProjectMember;
import com.winforrd.taskflow.entity.SysUser;
import com.winforrd.taskflow.entity.Task;
import com.winforrd.taskflow.entity.TaskAssignee;
import com.winforrd.taskflow.entity.TaskComment;
import com.winforrd.taskflow.entity.TeamMember;
import com.winforrd.taskflow.mapper.ProjectMapper;
import com.winforrd.taskflow.mapper.SysUserMapper;
import com.winforrd.taskflow.mapper.TaskAssigneeMapper;
import com.winforrd.taskflow.mapper.TaskCommentMapper;
import com.winforrd.taskflow.mapper.TaskMapper;
import com.winforrd.taskflow.service.NotificationService;
import com.winforrd.taskflow.service.ProjectMemberService;
import com.winforrd.taskflow.service.TaskCommentService;
import com.winforrd.taskflow.service.TeamMemberService;
import com.winforrd.taskflow.vo.TaskCommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskCommentMapper taskCommentMapper;

    private final TaskMapper taskMapper;

    private final ProjectMapper projectMapper;

    private final SysUserMapper sysUserMapper;

    private final ProjectMemberService projectMemberService;

    private final TeamMemberService teamMemberService;

    /**
     * 通知服务
     */
    private final NotificationService notificationService;

    /**
     * 查询任务负责人
     */
    private final TaskAssigneeMapper taskAssigneeMapper;


    /**
     * =====================================================
     * 查询任务评论
     * =====================================================
     */
    @Override
    public List<TaskCommentVO> getTaskComments(Long taskId) {
        // 当前用户
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取任务
        Task task = getTaskOrThrow(taskId);
        /**
         * 必须有项目查看权限。
         */
        projectMemberService.checkCanView(task.getProjectId(), currentUserId);
        // 获取任务评论
        List<TaskComment> comments = taskCommentMapper.selectList(new LambdaQueryWrapper<TaskComment>()
                                .eq(TaskComment::getTaskId, taskId)
                                .orderByAsc(TaskComment::getCreatedTime));
        // 转换结果
        List<TaskCommentVO> result = new ArrayList<>();
        for (TaskComment comment : comments) {
            result.add(convertCommentVO(comment, currentUserId));
        }
        return result;
    }

    /**
     * =====================================================
     * 发表评论
     * =====================================================
     */
    @Override
    @Transactional
    public Long createComment(Long taskId, CreateTaskCommentRequest request) {
        // 当前用户
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取任务
        Task task = getTaskOrThrow(taskId);
        // 获取项目
        Project project = getProjectOrThrow(task.getProjectId());
        /**
         * 项目归档：
         * 评论冻结。
         */
        if (ProjectStatus.ARCHIVED.name().equals(project.getStatus())) {
            throw new BusinessException(ErrorCode.TASK_COMMENT_PERMISSION_DENIED);
        }
        /**
         * 评论权限检查。
         */
        if (!canComment(taskId, currentUserId)) {
            throw new BusinessException(ErrorCode.TASK_COMMENT_PERMISSION_DENIED);
        }
        // 获取评论内容
        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (content.isEmpty()) {
            throw new BusinessException(ErrorCode.TASK_COMMENT_CONTENT_EMPTY);
        }
        // 评论内容长度检查
        if (content.length() > 2000) {
            throw new BusinessException(ErrorCode.TASK_COMMENT_CONTENT_TOO_LONG);
        }
        // 创建评论
        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setUserId(currentUserId);
        comment.setContent(content);
        comment.setCreatedTime(LocalDateTime.now());
        comment.setUpdatedTime(LocalDateTime.now());
        taskCommentMapper.insert(comment);
        /**
         * =================================================
         * 评论通知
         * =================================================
         */
        sendCommentNotification(task, project, currentUserId, content);
        return comment.getId();
    }

    /**
     * =====================================================
     * 删除评论
     * 规则：
     * 普通用户：
     * 删除自己的评论
     * OWNER / ADMIN：
     * 删除任意评论
     * 项目负责人：
     * 删除任意评论
     * 项目归档：
     * 所有评论冻结
     * =====================================================
     */
    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        // 当前用户
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取评论
        TaskComment comment = taskCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.TASK_COMMENT_NOT_FOUND);
        }
        // 获取任务
        Task task = getTaskOrThrow(comment.getTaskId());
        // 获取项目
        Project project = getProjectOrThrow(task.getProjectId());
        /**
         * 项目归档以后，
         * 评论作为历史记录冻结。
         */
        if (ProjectStatus.ARCHIVED.name().equals(project.getStatus())) {
            throw new BusinessException(ErrorCode.TASK_COMMENT_PERMISSION_DENIED);
        }
        /**
         * 自己的评论直接允许删除。
         */
        if (comment.getUserId().equals(currentUserId)) {
            taskCommentMapper.deleteById(commentId);
            return;
        }
        /**
         * 删除别人的评论必须拥有治理权限。
         */
        if (!canManageAllComments(project, currentUserId)) {
            throw new BusinessException(ErrorCode.TASK_COMMENT_PERMISSION_DENIED);
        }
        taskCommentMapper.deleteById(commentId);
    }
    /**
     * =====================================================
     * 当前用户是否可以发表评论
     * =====================================================
     */
    @Override
    public boolean canComment(Long taskId, Long userId) {
        // 当前用户
        Task task = getTaskOrThrow(taskId);
        // 获取项目
        Project project = getProjectOrThrow(task.getProjectId());
        /**
         * 项目归档以后只读。
         */
        if (ProjectStatus.ARCHIVED.name().equals(project.getStatus())) {
            return false;
        }
        // 获取团队成员
        TeamMember teamMember = teamMemberService.getTeamMember(project.getTeamId(), userId);
        if (teamMember == null) {
            return false;
        }
        /**
         * 团队 OWNER / ADMIN。
         */
        if (isTeamAdmin(teamMember)) {return true;}
        /**
         * 项目负责人。
         */
        if (project.getOwnerId().equals(userId)) {
            return true;
        }
        // 获取项目成员
        ProjectMember projectMember = projectMemberService.getProjectMember(project.getId(), userId);
        if (projectMember == null) {
            return false;
        }
        /**
         * READ_ONLY：
         * 只能查看。
         */
        if (ProjectRole.READ_ONLY.name().equals(projectMember.getRoleType())) {
            return false;
        }
        /**
         * MANAGER / MEMBER。
         */
        return true;
    }

    /**
     * =====================================================
     * 评论通知
     *
     * 接收人：
     *
     * 1. 所有任务负责人
     * 2. 项目负责人
     *
     * 排除：
     * 评论者本人
     * =====================================================
     */
    private void sendCommentNotification(Task task, Project project, Long operatorId, String content) {
        // 接收者ID集合
        Set<Long> receiverIds = new LinkedHashSet<>();
        /**
         * 任务负责人。
         */
        List<TaskAssignee> assignees = taskAssigneeMapper.selectList(new LambdaQueryWrapper<TaskAssignee>().eq(TaskAssignee::getTaskId, task.getId()));
        for (TaskAssignee assignee : assignees) {
            receiverIds.add(assignee.getUserId());
        }
        /**
         * 项目负责人。
         */
        receiverIds.add(project.getOwnerId());
        /**
         * 评论人不通知自己。
         */
        receiverIds.remove(operatorId);
        /**
         * 通知内容最多显示前 50 个字符。
         */
        String preview = content.length() > 50 ? content.substring(0, 50) + "..." : content;
        // 创建通知
        notificationService.createNotifications(receiverIds, NotificationType.TASK_COMMENT, "任务有新评论", "任务“" + task.getTitle() + "”有新评论：" + preview, "TASK", task.getId());
    }


    /**
     * =====================================================
     * 是否拥有删除任意评论权限
     *
     * 团队 OWNER
     * 团队 ADMIN
     * 项目负责人
     * =====================================================
     */
    private boolean canManageAllComments(Project project, Long userId) {
        TeamMember teamMember = teamMemberService.getTeamMember(project.getTeamId(), userId);
        if (teamMember != null && isTeamAdmin(teamMember)) {
            return true;
        }
        return project.getOwnerId().equals(userId);
    }

    /**
     * =====================================================
     * 团队管理员
     * =====================================================
     */
    private boolean isTeamAdmin(TeamMember teamMember) {
        if (teamMember == null) {
            return false;
        }
        return TeamRole.OWNER.name().equals(teamMember.getRoleType()) || TeamRole.ADMIN.name().equals(teamMember.getRoleType());
    }

    /**
     * =====================================================
     * Comment → VO
     * =====================================================
     */
    private TaskCommentVO convertCommentVO(TaskComment comment, Long currentUserId) {
        // 创建 VO
        TaskCommentVO vo = new TaskCommentVO();
        SysUser user = sysUserMapper.selectById(comment.getUserId());
        Task task = getTaskOrThrow(comment.getTaskId());
        Project project = getProjectOrThrow(task.getProjectId());
        vo.setId(comment.getId());
        vo.setTaskId(comment.getTaskId());
        vo.setUserId(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        vo.setContent(comment.getContent());
        boolean projectArchived = ProjectStatus.ARCHIVED.name().equals(project.getStatus());
        boolean ownComment = comment.getUserId().equals(currentUserId);
        boolean canManageAll = canManageAllComments(project, currentUserId);
        vo.setCanDelete(!projectArchived && (ownComment || canManageAll));
        vo.setCreatedTime(comment.getCreatedTime());
        vo.setUpdatedTime(comment.getUpdatedTime());
        return vo;
    }
    /**
     * =====================================================
     * 获取任务
     * =====================================================
     */
    private Task getTaskOrThrow(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
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
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        return project;
    }
}