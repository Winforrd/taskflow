package com.winforrd.taskflow.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.winforrd.taskflow.common.enums.NotificationType;
import com.winforrd.taskflow.entity.ReminderRecord;
import com.winforrd.taskflow.entity.Task;
import com.winforrd.taskflow.entity.TaskAssignee;
import com.winforrd.taskflow.mapper.ReminderRecordMapper;
import com.winforrd.taskflow.mapper.TaskAssigneeMapper;
import com.winforrd.taskflow.mapper.TaskMapper;
import com.winforrd.taskflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeadlineReminderTask {

    private final TaskMapper taskMapper;
    private final NotificationService notificationService;
    private final ReminderRecordMapper reminderRecordMapper;
    private final TaskAssigneeMapper taskAssigneeMapper;

    /**
     * 每5分钟扫描一次任务到期情况
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void checkDeadline() {

        // 查询所有设置了截止日期的任务
        List<Task> tasks = taskMapper.selectList(new LambdaQueryWrapper<Task>().isNotNull(Task::getDeadline));
        for (Task task : tasks) {
            // 查询该任务的所有负责人（适配多负责人架构）
            List<TaskAssignee> assigneeList = taskAssigneeMapper.selectList(new LambdaQueryWrapper<TaskAssignee>().eq(TaskAssignee::getTaskId, task.getId()));
            // 无负责人直接跳过
            if (assigneeList.isEmpty()) {
                continue;
            }
            // LocalDate 转 LocalDateTime：默认任务截止到当天 23:59:59，解决类型不兼容报错
            LocalDate deadlineDate = task.getDeadline();
            LocalDateTime deadlineTime = deadlineDate.atTime(23, 59, 59);
            // 计算距离截止的剩余小时数
            long hours = Duration.between(LocalDateTime.now(), deadlineTime).toHours();
            // 24小时内到期且未过期，触发提醒
            if (hours <= 24 && hours > 0) {
                // 遍历每个负责人，分别发送提醒（每个负责人独立幂等控制）
                for (TaskAssignee assignee : assigneeList) {
                    sendDeadlineReminder(task, assignee.getUserId(), "DEADLINE_24H", "任务即将到期", "你的任务《" + task.getTitle() + "》将在24小时内到期"
                    );
                }
            }
        }
    }

    /**
     * 给单个用户发送到期提醒（幂等控制）
     * 注意：私有方法无法触发Spring事务，若需严格事务一致性，建议抽取到Service层公共方法并加@Transactional
     */
    private void sendDeadlineReminder(Task task, Long userId, String reminderType, String title, String content) {
        // 幂等校验：查询该用户该任务该类型是否已经发送过提醒
        Long count = reminderRecordMapper.selectCount(new LambdaQueryWrapper<ReminderRecord>()
                        .eq(ReminderRecord::getTaskId, task.getId())
                        .eq(ReminderRecord::getUserId, userId)
                        .eq(ReminderRecord::getReminderType, reminderType));
        if (count > 0) {
            // 已发送过，直接跳过
            return;
        }
        // 创建通知（内部包含 Redis 计数更新 + WebSocket 实时推送）
        notificationService.createNotification(userId, NotificationType.TASK_DEADLINE, title, content, "TASK", task.getId());
        // 写入幂等记录，标记该提醒已发送
        ReminderRecord record = new ReminderRecord();
        record.setTaskId(task.getId());
        record.setUserId(userId);
        record.setReminderType(reminderType);
        record.setCreatedTime(LocalDateTime.now());
        reminderRecordMapper.insert(record);
    }

}
