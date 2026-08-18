package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.winforrd.taskflow.common.enums.ErrorCode;
import com.winforrd.taskflow.common.enums.ProjectRole;
import com.winforrd.taskflow.common.enums.ProjectStatus;
import com.winforrd.taskflow.common.enums.TeamRole;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.dto.CreateTaskCommentRequest;
import com.winforrd.taskflow.entity.Project;
import com.winforrd.taskflow.entity.ProjectMember;
import com.winforrd.taskflow.entity.SysUser;
import com.winforrd.taskflow.entity.Task;
import com.winforrd.taskflow.entity.TaskComment;
import com.winforrd.taskflow.entity.TeamMember;
import com.winforrd.taskflow.mapper.ProjectMapper;
import com.winforrd.taskflow.mapper.SysUserMapper;
import com.winforrd.taskflow.mapper.TaskCommentMapper;
import com.winforrd.taskflow.mapper.TaskMapper;
import com.winforrd.taskflow.service.ProjectMemberService;
import com.winforrd.taskflow.service.TaskCommentService;
import com.winforrd.taskflow.service.TeamMemberService;
import com.winforrd.taskflow.vo.TaskCommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl
        implements TaskCommentService {

    private final TaskCommentMapper taskCommentMapper;

    private final TaskMapper taskMapper;

    private final ProjectMapper projectMapper;

    private final SysUserMapper sysUserMapper;

    private final ProjectMemberService projectMemberService;

    private final TeamMemberService teamMemberService;


    /**
     * =====================================================
     * 查询任务评论
     * =====================================================
     */
    @Override
    public List<TaskCommentVO> getTaskComments(
            Long taskId
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        Task task =
                getTaskOrThrow(
                        taskId
                );


        /**
         * 必须拥有项目查看权限。
         */
        projectMemberService.checkCanView(
                task.getProjectId(),
                currentUserId
        );


        List<TaskComment> comments =
                taskCommentMapper.selectList(

                        new LambdaQueryWrapper<TaskComment>()

                                .eq(
                                        TaskComment::getTaskId,
                                        taskId
                                )

                                .orderByAsc(
                                        TaskComment::getCreatedTime
                                )

                );


        List<TaskCommentVO> result =
                new ArrayList<>();


        for (
                TaskComment comment :
                comments
        ) {

            result.add(
                    convertCommentVO(
                            comment,
                            currentUserId
                    )
            );
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
    public Long createComment(
            Long taskId,
            CreateTaskCommentRequest request
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


        /**
         * 项目归档后禁止评论。
         */
        if (
                ProjectStatus.ARCHIVED.name()
                        .equals(
                                project.getStatus()
                        )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_COMMENT_PERMISSION_DENIED
            );
        }


        /**
         * 评论权限。
         */
        if (
                !canComment(
                        taskId,
                        currentUserId
                )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_COMMENT_PERMISSION_DENIED
            );
        }


        String content =
                request.getContent() == null
                        ? ""
                        : request.getContent().trim();


        if (
                content.isEmpty()
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_COMMENT_CONTENT_EMPTY
            );
        }


        if (
                content.length() > 2000
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_COMMENT_CONTENT_TOO_LONG
            );
        }


        TaskComment comment =
                new TaskComment();


        comment.setTaskId(
                taskId
        );


        comment.setUserId(
                currentUserId
        );


        comment.setContent(
                content
        );


        comment.setCreatedTime(
                LocalDateTime.now()
        );


        comment.setUpdatedTime(
                LocalDateTime.now()
        );


        taskCommentMapper.insert(
                comment
        );


        return comment.getId();
    }


    /**
     * =====================================================
     * 删除评论
     * =====================================================
     *
     * 权限：
     *
     * 1. 所有人都可以删除自己的评论
     *
     * 2. 团队 OWNER / ADMIN
     *    可以删除任意人的评论
     *
     * 3. 项目负责人
     *    可以删除任意人的评论
     *
     * 4. MANAGER
     *    只能删除自己
     *
     * 5. MEMBER
     *    只能删除自己
     *
     * 6. READ_ONLY
     *    本身不能发表评论，
     *    因此正常情况下不会拥有评论
     *
     * 7. 项目归档以后
     *    所有评论冻结
     */
    @Override
    @Transactional
    public void deleteComment(
            Long commentId
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        TaskComment comment =
                taskCommentMapper.selectById(
                        commentId
                );


        if (
                comment == null
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_COMMENT_NOT_FOUND
            );
        }


        Task task =
                getTaskOrThrow(
                        comment.getTaskId()
                );


        Project project =
                getProjectOrThrow(
                        task.getProjectId()
                );


        /**
         * 项目归档以后，
         * 评论作为历史记录冻结。
         */
        if (
                ProjectStatus.ARCHIVED.name()
                        .equals(
                                project.getStatus()
                        )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_COMMENT_PERMISSION_DENIED
            );
        }


        /**
         * ==============================
         * 情况1：
         * 删除自己的评论
         * ==============================
         */
        if (
                comment.getUserId()
                        .equals(
                                currentUserId
                        )
        ) {

            taskCommentMapper.deleteById(
                    commentId
            );


            return;
        }


        /**
         * ==============================
         * 情况2：
         * 删除别人的评论
         *
         * 必须拥有评论管理权限
         * ==============================
         */
        if (
                !canManageAllComments(
                        project,
                        currentUserId
                )
        ) {

            throw new BusinessException(
                    ErrorCode.TASK_COMMENT_PERMISSION_DENIED
            );
        }


        taskCommentMapper.deleteById(
                commentId
        );
    }


    /**
     * =====================================================
     * 当前用户是否可以发表评论
     * =====================================================
     */
    @Override
    public boolean canComment(
            Long taskId,
            Long userId
    ) {

        Task task =
                getTaskOrThrow(
                        taskId
                );


        Project project =
                getProjectOrThrow(
                        task.getProjectId()
                );


        /**
         * 项目归档：
         * 全部只读。
         */
        if (
                ProjectStatus.ARCHIVED.name()
                        .equals(
                                project.getStatus()
                        )
        ) {

            return false;
        }


        TeamMember teamMember =
                teamMemberService.getTeamMember(
                        project.getTeamId(),
                        userId
                );


        if (
                teamMember == null
        ) {

            return false;
        }


        /**
         * 团队 OWNER / ADMIN
         */
        if (
                isTeamAdmin(
                        teamMember
                )
        ) {

            return true;
        }


        /**
         * 项目负责人
         */
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


        if (
                projectMember == null
        ) {

            return false;
        }


        /**
         * READ_ONLY：
         * 只能查看。
         */
        if (
                ProjectRole.READ_ONLY.name()
                        .equals(
                                projectMember.getRoleType()
                        )
        ) {

            return false;
        }


        /**
         * MANAGER / MEMBER
         */
        return true;
    }


    /**
     * =====================================================
     * 是否拥有“删除任意评论”的权限
     * =====================================================
     *
     * 只有：
     *
     * 团队 OWNER
     * 团队 ADMIN
     * 项目负责人
     */
    private boolean canManageAllComments(
            Project project,
            Long userId
    ) {

        TeamMember teamMember =
                teamMemberService.getTeamMember(
                        project.getTeamId(),
                        userId
                );


        /**
         * 团队 OWNER / ADMIN
         */
        if (
                teamMember != null
                        &&
                        isTeamAdmin(
                                teamMember
                        )
        ) {

            return true;
        }


        /**
         * 项目负责人
         */
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
     * TaskComment → VO
     * =====================================================
     */
    private TaskCommentVO convertCommentVO(
            TaskComment comment,
            Long currentUserId
    ) {

        TaskCommentVO vo =
                new TaskCommentVO();


        SysUser user =
                sysUserMapper.selectById(
                        comment.getUserId()
                );


        Task task =
                getTaskOrThrow(
                        comment.getTaskId()
                );


        Project project =
                getProjectOrThrow(
                        task.getProjectId()
                );


        vo.setId(
                comment.getId()
        );


        vo.setTaskId(
                comment.getTaskId()
        );


        vo.setUserId(
                comment.getUserId()
        );


        if (
                user != null
        ) {

            vo.setUsername(
                    user.getUsername()
            );


            vo.setNickname(
                    user.getNickname()
            );


            vo.setAvatar(
                    user.getAvatar()
            );
        }


        vo.setContent(
                comment.getContent()
        );


        /**
         * =================================================
         * 前端删除按钮权限
         * =================================================
         *
         * canDelete = true：
         *
         * 自己的评论
         * 或
         * 团队 OWNER / ADMIN
         * 或
         * 项目负责人
         *
         * 项目归档后永远 false。
         */
        boolean projectArchived =
                ProjectStatus.ARCHIVED.name()
                        .equals(
                                project.getStatus()
                        );


        boolean ownComment =
                comment.getUserId()
                        .equals(
                                currentUserId
                        );


        boolean canManageAll =
                canManageAllComments(
                        project,
                        currentUserId
                );


        vo.setCanDelete(
                !projectArchived
                        &&
                        (
                                ownComment
                                        ||
                                        canManageAll
                        )
        );


        vo.setCreatedTime(
                comment.getCreatedTime()
        );


        vo.setUpdatedTime(
                comment.getUpdatedTime()
        );


        return vo;
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
}