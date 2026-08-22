package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.winforrd.taskflow.common.enums.NotificationType;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.entity.Notification;
import com.winforrd.taskflow.mapper.NotificationMapper;
import com.winforrd.taskflow.service.NotificationService;
import com.winforrd.taskflow.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationMapper notificationMapper;


    /**
     * 查询我的通知
     */
    @Override
    public List<NotificationVO> getMyNotifications() {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        List<Notification> list =
                notificationMapper.selectList(

                        new LambdaQueryWrapper<Notification>()

                                .eq(
                                        Notification::getUserId,
                                        currentUserId
                                )

                                .orderByAsc(
                                        Notification::getIsRead
                                )

                                .orderByDesc(
                                        Notification::getCreatedTime
                                )

                                .last(
                                        "LIMIT 100"
                                )
                );


        List<NotificationVO> result =
                new ArrayList<>();


        for (
                Notification notification :
                list
        ) {

            result.add(
                    convertVO(
                            notification
                    )
            );
        }


        return result;
    }


    /**
     * 未读数量
     */
    @Override
    public Long getUnreadCount() {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        return notificationMapper.selectCount(

                new LambdaQueryWrapper<Notification>()

                        .eq(
                                Notification::getUserId,
                                currentUserId
                        )

                        .eq(
                                Notification::getIsRead,
                                0
                        )
        );
    }


    /**
     * 单条已读
     */
    @Override
    @Transactional
    public void markAsRead(
            Long notificationId
    ) {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        Notification notification =
                notificationMapper.selectById(
                        notificationId
                );


        if (
                notification == null
        ) {

            throw new BusinessException(
                    "通知不存在"
            );
        }


        /**
         * 只能操作自己的通知。
         */
        if (
                !notification.getUserId()
                        .equals(
                                currentUserId
                        )
        ) {

            throw new BusinessException(
                    "没有操作该通知的权限"
            );
        }


        /**
         * 已读就不重复更新。
         */
        if (
                Integer.valueOf(1)
                        .equals(
                                notification.getIsRead()
                        )
        ) {

            return;
        }


        Notification update =
                new Notification();


        update.setId(
                notificationId
        );


        update.setIsRead(
                1
        );


        update.setReadTime(
                LocalDateTime.now()
        );


        notificationMapper.updateById(
                update
        );
    }


    /**
     * 全部已读
     */
    @Override
    @Transactional
    public void markAllAsRead() {

        Long currentUserId =
                StpUtil.getLoginIdAsLong();


        notificationMapper.update(

                null,

                new LambdaUpdateWrapper<Notification>()

                        .eq(
                                Notification::getUserId,
                                currentUserId
                        )

                        .eq(
                                Notification::getIsRead,
                                0
                        )

                        .set(
                                Notification::getIsRead,
                                1
                        )

                        .set(
                                Notification::getReadTime,
                                LocalDateTime.now()
                        )
        );
    }


    /**
     * 创建单个通知
     */
    @Override
    @Transactional
    public void createNotification(
            Long userId,
            NotificationType type,
            String title,
            String content,
            String relatedType,
            Long relatedId
    ) {

        if (
                userId == null
        ) {

            return;
        }


        Notification notification =
                new Notification();


        notification.setUserId(
                userId
        );


        notification.setType(
                type.name()
        );


        notification.setTitle(
                title
        );


        notification.setContent(
                content
        );


        notification.setRelatedType(
                relatedType
        );


        notification.setRelatedId(
                relatedId
        );


        notification.setIsRead(
                0
        );


        notification.setCreatedTime(
                LocalDateTime.now()
        );


        notificationMapper.insert(
                notification
        );
    }


    /**
     * 批量创建通知
     */
    @Override
    @Transactional
    public void createNotifications(
            Collection<Long> userIds,
            NotificationType type,
            String title,
            String content,
            String relatedType,
            Long relatedId
    ) {

        if (
                userIds == null
                        ||
                        userIds.isEmpty()
        ) {

            return;
        }


        /**
         * 去重。
         */
        Set<Long> uniqueIds =
                new LinkedHashSet<>(
                        userIds
                );


        for (
                Long userId :
                uniqueIds
        ) {

            createNotification(
                    userId,
                    type,
                    title,
                    content,
                    relatedType,
                    relatedId
            );
        }
    }


    /**
     * 实体转VO
     */
    private NotificationVO convertVO(
            Notification notification
    ) {

        NotificationVO vo =
                new NotificationVO();


        vo.setId(
                notification.getId()
        );


        vo.setType(
                notification.getType()
        );


        vo.setTitle(
                notification.getTitle()
        );


        vo.setContent(
                notification.getContent()
        );


        vo.setRelatedType(
                notification.getRelatedType()
        );


        vo.setRelatedId(
                notification.getRelatedId()
        );


        vo.setRead(
                Integer.valueOf(1)
                        .equals(
                                notification.getIsRead()
                        )
        );


        vo.setReadTime(
                notification.getReadTime()
        );


        vo.setCreatedTime(
                notification.getCreatedTime()
        );


        return vo;
    }
}