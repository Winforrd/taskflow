package com.winforrd.taskflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.winforrd.taskflow.common.constant.RedisKeyConstant;
import com.winforrd.taskflow.common.enums.NotificationType;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.entity.Notification;
import com.winforrd.taskflow.mapper.NotificationMapper;
import com.winforrd.taskflow.service.NotificationService;
import com.winforrd.taskflow.vo.NotificationVO;
import com.winforrd.taskflow.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    /**
     * Redis 缓存：未读通知数量
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * WebSocket 实时推送处理器
     */
    private final NotificationWebSocketHandler webSocketHandler;


    /**
     * 查询我的通知
     */
    @Override
    public List<NotificationVO> getMyNotifications() {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 查询通知列表
        List<Notification> list = notificationMapper.selectList(new LambdaQueryWrapper<Notification>().eq(Notification::getUserId, currentUserId).orderByAsc(Notification::getIsRead).orderByDesc(Notification::getCreatedTime).last("LIMIT 100"));
        // 转换为 VO
        List<NotificationVO> result = new ArrayList<>();
        for (Notification notification : list) {
            result.add(convertVO(notification));
        }
        return result;
    }

    /**
     * 未读数量
     * 优先读取 Redis 缓存，缓存未命中再查数据库并回填
     */
    @Override
    public Long getUnreadCount() {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 构建 Redis key
        String key = RedisKeyConstant.NOTIFICATION_UNREAD_COUNT + currentUserId;
        // 优先从 Redis 缓存获取
        String countStr = stringRedisTemplate.opsForValue().get(key);
        if (countStr != null) {
            // 缓存命中，直接返回
            return Long.parseLong(countStr);
        }
        // 缓存未命中，查询数据库
        Long dbCount = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                                                        .eq(Notification::getUserId, currentUserId)
                                                        .eq(Notification::getIsRead, 0));
        // 回填缓存
        stringRedisTemplate.opsForValue().set(key, String.valueOf(dbCount));
        return dbCount;
    }

    /**
     * 单条已读
     */
    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 查询通知
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        // 校验通知属于当前用户
        if (!notification.getUserId().equals(currentUserId)) {
            throw new BusinessException("没有操作该通知的权限");
        }
        // 如果已经读过，则直接返回
        if (Integer.valueOf(1).equals(notification.getIsRead())) {
            return;
        }
        // 更新通知为已读
        Notification update = new Notification();
        // 设置更新条件
        update.setId(notificationId);
        update.setIsRead(1);
        update.setReadTime(LocalDateTime.now());
        notificationMapper.updateById(update);
        // 同步更新 Redis 未读数量
        String key = RedisKeyConstant.NOTIFICATION_UNREAD_COUNT + currentUserId;
        // 减少未读数量
        stringRedisTemplate.opsForValue().decrement(key);
    }

    /**
     * 全部已读
     */
    @Override
    @Transactional
    public void markAllAsRead() {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 更新所有未读通知为已读
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getUserId, currentUserId)
                        .eq(Notification::getIsRead, 0)
                        .set(Notification::getIsRead, 1)
                        .set(Notification::getReadTime, LocalDateTime.now()));
        // 同步更新 Redis 未读数量为 0
        String key = RedisKeyConstant.NOTIFICATION_UNREAD_COUNT + currentUserId;
        stringRedisTemplate.opsForValue().set(key, "0");
    }

    /**
     * 创建单个通知
     * 落库同时：更新 Redis 未读数 + WebSocket 实时推送给在线用户
     */
    @Override
    @Transactional
    public void createNotification(Long userId, NotificationType type, String title, String content, String relatedType, Long relatedId) {
        // 校验用户ID
        if (userId == null) {
            return;
        }
        // 创建通知对象
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type.name());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notification.setCreatedTime(LocalDateTime.now());
        notificationMapper.insert(notification);
        // ===== 新增：更新 Redis 未读数量 =====
        String key = RedisKeyConstant.NOTIFICATION_UNREAD_COUNT + userId;
        stringRedisTemplate.opsForValue().increment(key);
        // ===== 新增：WebSocket 实时推送给目标用户 =====
        Map<String, Object> message = new HashMap<>();
        message.put("type", type.name());
        message.put("title", title);
        message.put("content", content);
        message.put("relatedType", relatedType);
        message.put("relatedId", relatedId);
        webSocketHandler.send(userId, JSON.toJSONString(message));
    }

    /**
     * 批量创建通知
     * 循环复用单条创建逻辑，自动同步 Redis 和 WebSocket
     */
    @Override
    @Transactional
    public void createNotifications(Collection<Long> userIds, NotificationType type, String title, String content, String relatedType, Long relatedId) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(userIds);
        for (Long userId : uniqueIds) {
            createNotification(userId, type, title, content, relatedType, relatedId);
        }
    }

    /**
     * 实体转VO
     */
    private NotificationVO convertVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setType(notification.getType());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setRelatedType(notification.getRelatedType());
        vo.setRelatedId(notification.getRelatedId());
        vo.setRead(Integer.valueOf(1).equals(notification.getIsRead()));
        vo.setReadTime(notification.getReadTime());
        vo.setCreatedTime(notification.getCreatedTime());
        return vo;
    }
}
