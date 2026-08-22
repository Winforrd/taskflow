package com.winforrd.taskflow.controller;

import com.winforrd.taskflow.common.result.Result;
import com.winforrd.taskflow.service.NotificationService;
import com.winforrd.taskflow.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    /**
     * 我的通知
     */
    @GetMapping
    public Result<List<NotificationVO>> getMyNotifications() {

        return Result.success(notificationService.getMyNotifications());
    }


    /**
     * 未读数量
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {

        return Result.success(notificationService.getUnreadCount());
    }


    /**
     * 单条已读
     */
    @PutMapping("/{notificationId}/read")
    public Result<Void> markAsRead(@PathVariable Long notificationId) {

        notificationService.markAsRead(notificationId);
        return Result.success();
    }

    /**
     * 全部已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();return Result.success();
    }
}