package com.winforrd.taskflow.service;

import com.winforrd.taskflow.common.enums.NotificationType;
import com.winforrd.taskflow.vo.NotificationVO;

import java.util.Collection;
import java.util.List;

public interface NotificationService {

    /**
     * 当前用户通知列表
     */
    List<NotificationVO> getMyNotifications();


    /**
     * 未读数量
     */
    Long getUnreadCount();


    /**
     * 单条已读
     */
    void markAsRead(Long notificationId);


    /**
     * 全部已读
     */
    void markAllAsRead();


    /**
     * 创建单用户通知
     */
    void createNotification(Long userId, NotificationType type, String title, String content, String relatedType, Long relatedId);


    /**
     * 批量创建通知
     */
    void createNotifications(Collection<Long> userIds, NotificationType type, String title, String content, String relatedType, Long relatedId);
}