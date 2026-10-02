package com.winforrd.taskflow.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.winforrd.taskflow.common.enums.NotificationType;
import com.winforrd.taskflow.common.enums.TaskStatus;
import com.winforrd.taskflow.entity.ReminderRecord;
import com.winforrd.taskflow.entity.Task;
import com.winforrd.taskflow.entity.TaskAssignee;
import com.winforrd.taskflow.mapper.ReminderRecordMapper;
import com.winforrd.taskflow.mapper.TaskAssigneeMapper;
import com.winforrd.taskflow.mapper.TaskMapper;
import com.winforrd.taskflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
        List<Task> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>()
                        .isNotNull(Task::getDeadline)
                        .notIn(
                                Task::getStatus,
                                TaskStatus.DONE.name(),
                                TaskStatus.CANCELLED.name()
                        )
        );

        LocalDateTime now = LocalDateTime.now();

        for (Task task : tasks) {
            List<TaskAssignee> assigneeList = taskAssigneeMapper.selectList(
                    new LambdaQueryWrapper<TaskAssignee>()
                            .eq(TaskAssignee::getTaskId, task.getId())
            );
            if (assigneeList.isEmpty()) {
                continue;
            }

            LocalDate deadlineDate = task.getDeadline();
            LocalDateTime deadlineTime = deadlineDate.atTime(23, 59, 59);
            long minutesLeft = Duration.between(now, deadlineTime).toMinutes();

            // 24小时内且尚未到期（含最后不足1小时）
            if (minutesLeft > 0 && minutesLeft <= 24 * 60) {
                for (TaskAssignee assignee : assigneeList) {
                    sendDeadlineReminder(
                            task,
                            assignee.getUserId(),
                            "DEADLINE_24H",
                            "任务即将到期",
                            "你的任务《" + task.getTitle() + "》将在24小时内到期"
                    );
                }
            }
        }
    }

    /**
     * 先占幂等记录再发通知，利用唯一索引避免并发重复提醒。
     */
    private void sendDeadlineReminder(
            Task task,
            Long userId,
            String reminderType,
            String title,
            String content
    ) {
        Long count = reminderRecordMapper.selectCount(
                new LambdaQueryWrapper<ReminderRecord>()
                        .eq(ReminderRecord::getTaskId, task.getId())
                        .eq(ReminderRecord::getUserId, userId)
                        .eq(ReminderRecord::getReminderType, reminderType)
        );
        if (count > 0) {
            return;
        }

        ReminderRecord record = new ReminderRecord();
        record.setTaskId(task.getId());
        record.setUserId(userId);
        record.setReminderType(reminderType);
        record.setCreatedTime(LocalDateTime.now());

        try {
            reminderRecordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            // 并发下已被其他线程写入，跳过
            return;
        }

        notificationService.createNotification(
                userId,
                NotificationType.TASK_DEADLINE,
                title,
                content,
                "TASK",
                task.getId()
        );
    }
}
