<template>
  <div class="notification-page">
    <div class="page-header">
      <div>
        <h2>
          通知中心
        </h2>

        <p>
          查看任务分配、验收和评论等消息
        </p>
      </div>

      <el-button
        :disabled="notificationStore.unreadCount === 0"
        @click="handleReadAll"
      >
        全部已读
      </el-button>
    </div>

    <el-card>
      <div
        v-if="notifications.length > 0"
        class="notification-list"
      >
        <div
          v-for="item in notifications"
          :key="item.id"
          class="notification-item"
          :class="{
            unread: !item.read
          }"
          @click="handleNotificationClick(item)"
        >
          <div class="notification-dot">
            <span
              v-if="!item.read"
              class="dot"
            />
          </div>

          <div class="notification-content">
            <div class="notification-title-row">
              <span class="notification-title">
                {{ item.title }}
              </span>

              <el-tag
                size="small"
                :type="
                  getTypeTag(
                    item.type
                  )
                "
              >
                {{
                  getTypeName(
                    item.type
                  )
                }}
              </el-tag>
            </div>

            <div class="notification-text">
              {{ item.content }}
            </div>

            <div class="notification-time">
              {{
                formatTime(
                  item.createdTime
                )
              }}
            </div>
          </div>
        </div>
      </div>

      <el-empty
        v-else
        description="暂无通知"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import {
  onMounted,
  ref
} from 'vue'

import {
  useRouter
} from 'vue-router'

import {
  ElMessage
} from 'element-plus'

import {
  getNotificationsApi,
  markAllNotificationsReadApi,
  markNotificationReadApi
} from '@/api/notification'

import { useNotificationStore } from '@/stores/notification'

import type {
  Notification,
  NotificationType
} from '@/types/notification'

const router =
  useRouter()

const notificationStore =
  useNotificationStore()

const notifications =
  ref<Notification[]>([])

onMounted(
  async () => {
    await loadData()
  }
)

async function loadData() {
  const [
    notificationResult
  ] =
    await Promise.all([
      getNotificationsApi(),
      notificationStore.fetchUnreadCount()
    ])

  notifications.value =
    notificationResult.data
}

async function handleNotificationClick(
  item: Notification
) {
  if (
    !item.read
  ) {
    await markNotificationReadApi(
      item.id
    )

    notificationStore.decrementUnreadCount()

    item.read = true
  }

  if (
    item.relatedType === 'TASK'
    &&
    item.relatedId
  ) {
    router.push(
      `/tasks/${item.relatedId}`
    )

    return
  }

  await loadData()
}

async function handleReadAll() {
  await markAllNotificationsReadApi()

  ElMessage.success(
    '已全部标记为已读'
  )

  notificationStore.setUnreadCount(0)

  // 本地列表也同步更新为已读状态
  notifications.value.forEach(n => n.read = true)
}

function getTypeName(
  type: NotificationType
) {
  const map:
    Record<NotificationType, string> = {
      TASK_ASSIGNED:
        '任务分配',

      TASK_UNASSIGNED:
        '负责人调整',

      TASK_SUBMITTED:
        '待验收',

      TASK_APPROVED:
        '验收通过',

      TASK_REJECTED:
        '验收驳回',

      TASK_CANCELLED:
        '任务取消',

      TASK_COMMENT:
        '任务评论'
    }

  return map[type]
}

function getTypeTag(
  type: NotificationType
):

  | 'info'
  | 'primary'
  | 'success'
  | 'warning'
  | 'danger' {
  switch (type) {
    case 'TASK_ASSIGNED':
      return 'primary'

    case 'TASK_SUBMITTED':
      return 'warning'

    case 'TASK_APPROVED':
      return 'success'

    case 'TASK_REJECTED':
      return 'danger'

    case 'TASK_CANCELLED':
      return 'danger'

    default:
      return 'info'
  }
}

function formatTime(
  value?: string
) {
  if (!value) {
    return '-'
  }

  return value
    .replace(
      'T',
      ' '
    )
    .substring(
      0,
      19
    )
}
</script>

<style scoped>
.notification-page {
  width: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.page-header p {
  margin:
    8px 0 0;

  color: #909399;
}

.notification-list {
  display: flex;
  flex-direction: column;
}

.notification-item {
  display: flex;

  padding: 18px 12px;

  border-bottom:
    1px solid #ebeef5;

  cursor: pointer;

  transition:
    background 0.2s;
}

.notification-item:hover {
  background:
    #f5f7fa;
}

.notification-item.unread {
  background:
    #f4f8ff;
}

.notification-dot {
  width: 24px;

  padding-top: 7px;
}

.dot {
  display: block;

  width: 8px;

  height: 8px;

  border-radius: 50%;

  background:
    #409eff;
}

.notification-content {
  flex: 1;
}

.notification-title-row {
  display: flex;

  align-items: center;

  gap: 10px;
}

.notification-title {
  font-weight: 600;
}

.notification-text {
  margin-top: 8px;

  color: #606266;

  line-height: 1.6;
}

.notification-time {
  margin-top: 8px;

  color: #a8abb2;

  font-size: 12px;
}
</style>