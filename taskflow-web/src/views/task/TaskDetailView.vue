<template>

  <div
    v-if="task"
    class="task-detail-page"
  >

    <el-row :gutter="20">

      <!-- 左侧 -->
      <el-col :span="16">

        <!-- ============================== -->
        <!-- 任务信息 -->
        <!-- ============================== -->

        <el-card>

          <div class="header">

            <div>

              <el-tag
                :type="
                  getPriorityType(
                    task.priority
                  )
                "
              >

                {{
                  getPriorityName(
                    task.priority
                  )
                }}

              </el-tag>


              <h2>
                {{ task.title }}
              </h2>

            </div>


            <el-button
              @click="goBoard"
            >

              返回看板

            </el-button>

          </div>


          <el-divider />


          <p class="description">

            {{
              task.description
              || '暂无任务描述'
            }}

          </p>


          <el-descriptions
            :column="2"
            border
          >

            <el-descriptions-item
              label="状态"
            >

              <el-tag
                :type="
                  getStatusType(
                    task.status
                  )
                "
              >

                {{
                  getStatusName(
                    task.status
                  )
                }}

              </el-tag>

            </el-descriptions-item>


            <el-descriptions-item
              label="创建人"
            >

              {{
                task.creatorName
                || '-'
              }}

            </el-descriptions-item>


            <el-descriptions-item
              label="开始日期"
            >

              {{
                task.startDate
                || '-'
              }}

            </el-descriptions-item>


            <el-descriptions-item
              label="截止日期"
            >

              {{
                task.deadline
                || '-'
              }}

            </el-descriptions-item>


            <!-- 负责人 -->
            <el-descriptions-item
              label="负责人"
              :span="2"
            >

              <div class="assignee-area">

                <div class="assignee-list">

                  <el-tag
                    v-for="assignee in task.assignees"
                    :key="assignee.userId"
                  >

                    {{
                      assignee.nickname
                      ||
                      assignee.username
                    }}

                  </el-tag>


                  <span
                    v-if="
                      task.assignees.length
                      === 0
                    "
                    class="empty-text"
                  >

                    暂无负责人

                  </span>

                </div>


                <el-button
                  v-if="
                    task.canManageAssignees
                  "
                  size="small"
                  type="primary"
                  plain
                  @click="openAssigneeDialog"
                >

                  管理负责人

                </el-button>

              </div>

            </el-descriptions-item>

          </el-descriptions>


          <!-- 状态流转 -->
          <el-divider>

            状态流转

          </el-divider>


          <div class="status-actions">

            <el-button
              v-for="status in task.allowedNextStatuses"
              :key="status"
              :type="
                getStatusButtonType(
                  status
                )
              "
              @click="
                handleChangeStatus(
                  status
                )
              "
            >

              {{
                getStatusActionName(
                  task.status,
                  status
                )
              }}

            </el-button>


            <span
              v-if="
                task.allowedNextStatuses.length
                === 0
              "
              class="empty-action"
            >

              {{
                getNoActionText()
              }}

            </span>

          </div>

        </el-card>


        <!-- ============================== -->
        <!-- 评论区域 -->
        <!-- ============================== -->

        <el-card class="comment-card">

          <template #header>

            <div class="comment-header">

              <span>
                任务评论
              </span>


              <el-tag
                size="small"
                type="info"
              >

                {{ comments.length }}

              </el-tag>

            </div>

          </template>


          <!-- 发表评论 -->
          <div
            v-if="canComment"
            class="comment-editor"
          >

            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="4"
              maxlength="2000"
              show-word-limit
              placeholder="输入评论内容，例如进度说明、问题反馈、验收说明……"
            />


            <div class="comment-submit">

              <el-button
                type="primary"
                :loading="commentSubmitting"
                @click="handleCreateComment"
              >

                发表评论

              </el-button>

            </div>

          </div>


          <!-- 无评论权限 -->
          <el-alert
            v-else
            title="当前账号只有查看评论权限"
            type="info"
            :closable="false"
            class="comment-permission-tip"
          />


          <el-divider />


          <!-- 评论列表 -->
          <div
            v-if="
              comments.length > 0
            "
            class="comment-list"
          >

            <div
              v-for="comment in comments"
              :key="comment.id"
              class="comment-item"
            >

              <!-- 头像 -->
              <el-avatar
                :size="40"
                :src="comment.avatar"
              >

                {{
                  getCommentUserName(
                    comment
                  ).charAt(0)
                }}

              </el-avatar>


              <!-- 评论主体 -->
              <div class="comment-main">

                <div class="comment-user-row">

                  <div>

                    <span class="comment-user">

                      {{
                        getCommentUserName(
                          comment
                        )
                      }}

                    </span>


                    <span
                      v-if="comment.username"
                      class="comment-username"
                    >

                      @{{ comment.username }}

                    </span>

                  </div>


                  <el-button
                    v-if="comment.canDelete"
                    link
                    type="danger"
                    @click="
                      handleDeleteComment(
                        comment
                      )
                    "
                  >

                    删除

                  </el-button>

                </div>


                <div class="comment-content">

                  {{ comment.content }}

                </div>


                <div class="comment-time">

                  {{
                    formatTime(
                      comment.createdTime
                    )
                  }}

                </div>

              </div>

            </div>

          </div>


          <el-empty
            v-else
            :image-size="70"
            description="暂无评论"
          />

        </el-card>

      </el-col>


      <!-- ============================== -->
      <!-- 右侧任务动态 -->
      <!-- ============================== -->

      <el-col :span="8">

        <el-card>

          <template #header>

            任务动态

          </template>


          <el-timeline>

            <el-timeline-item
              v-for="activity in activities"
              :key="activity.id"
              :timestamp="
                formatTime(
                  activity.createdTime
                )
              "
            >

              <strong>

                {{ activity.operatorName }}

              </strong>


              {{ activity.content }}

            </el-timeline-item>

          </el-timeline>


          <el-empty
            v-if="
              activities.length === 0
            "
            :image-size="70"
            description="暂无任务动态"
          />

        </el-card>

      </el-col>

    </el-row>


    <!-- ============================== -->
    <!-- 管理负责人 -->
    <!-- ============================== -->

    <el-dialog
      v-model="assigneeDialogVisible"
      title="管理任务负责人"
      width="600px"
    >

      <div class="section-title">

        当前负责人

      </div>


      <div
        v-if="
          task.assignees.length > 0
        "
        class="current-assignees"
      >

        <div
          v-for="assignee in task.assignees"
          :key="assignee.userId"
          class="current-assignee-item"
        >

          <span>

            {{
              assignee.nickname
              ||
              assignee.username
            }}

          </span>


          <el-tooltip
            v-if="
              cannotRemoveLastAssignee
            "
            content="进行中或待验收的任务必须至少保留一名负责人"
          >

            <el-button
              link
              type="danger"
              disabled
            >

              移除

            </el-button>

          </el-tooltip>


          <el-button
            v-else
            link
            type="danger"
            @click="
              handleRemoveAssignee(
                assignee.userId,
                assignee.nickname
                || assignee.username
              )
            "
          >

            移除

          </el-button>

        </div>

      </div>


      <el-empty
        v-else
        :image-size="60"
        description="当前暂无负责人"
      />


      <el-divider />


      <div class="section-title">

        添加负责人

      </div>


      <el-select
        v-model="selectedUserId"
        filterable
        clearable
        placeholder="请选择项目成员"
        style="width: 100%"
      >

        <el-option
          v-for="member in availableMembers"
          :key="member.userId"
          :value="member.userId"
          :label="
            member.nickname
            ||
            member.username
          "
        >

          <span>

            {{
              member.nickname
              ||
              member.username
            }}

          </span>


          <span class="role-name">

            {{
              getMemberIdentity(
                member
              )
            }}

          </span>

        </el-option>

      </el-select>


      <el-button
        type="primary"
        class="add-button"
        :disabled="
          selectedUserId === null
        "
        :loading="assigneeLoading"
        @click="handleAddAssignee"
      >

        添加负责人

      </el-button>


      <template #footer>

        <el-button
          @click="
            assigneeDialogVisible = false
          "
        >

          完成

        </el-button>

      </template>

    </el-dialog>

  </div>

</template>


<script setup lang="ts">

import {
  computed,
  onMounted,
  ref
} from 'vue'

import {
  useRoute,
  useRouter
} from 'vue-router'

import {
  ElMessage,
  ElMessageBox
} from 'element-plus'

import {
  addTaskAssigneeApi,
  changeTaskStatusApi,
  getTaskActivitiesApi,
  getTaskDetailApi,
  removeTaskAssigneeApi
} from '@/api/task'

import {
  getProjectMembersApi
} from '@/api/projectMember'

import {
  createTaskCommentApi,
  deleteTaskCommentApi,
  getTaskCommentPermissionApi,
  getTaskCommentsApi
} from '@/api/taskComment'

import type {
  ProjectMember
} from '@/types/project'

import type {
  Task,
  TaskActivity,
  TaskPriority,
  TaskStatus
} from '@/types/task'

import type {
  TaskComment
} from '@/types/taskComment'


const route =
  useRoute()


const router =
  useRouter()


const taskId =
  Number(
    route.params.taskId
  )


const task =
  ref<Task | null>(
    null
  )


const activities =
  ref<TaskActivity[]>([])


const comments =
  ref<TaskComment[]>([])


const canComment =
  ref(false)


const commentContent =
  ref('')


const commentSubmitting =
  ref(false)


const projectMembers =
  ref<ProjectMember[]>([])


const assigneeDialogVisible =
  ref(false)


const selectedUserId =
  ref<number | null>(
    null
  )


const assigneeLoading =
  ref(false)


/**
 * 是否禁止删除最后一个负责人
 */
const cannotRemoveLastAssignee =
  computed(
    () => {

      if (
        !task.value
      ) {

        return false
      }


      return (
        (
          task.value.status === 'IN_PROGRESS'
          ||
          task.value.status === 'IN_REVIEW'
        )
        &&
        task.value.assignees.length <= 1
      )
    }
  )


/**
 * 可以新增的负责人
 */
const availableMembers =
  computed(
    () => {

      if (
        !task.value
      ) {

        return []
      }


      const ids =
        new Set(

          task.value.assignees.map(
            item =>
              item.userId
          )

        )


      return projectMembers.value.filter(
        member =>
          member.roleType !== 'READ_ONLY'
          &&
          !ids.has(
            member.userId
          )
      )
    }
  )


onMounted(
  async () => {

    await loadPage()

  }
)


/**
 * 加载整个页面
 */
async function loadPage() {

  await Promise.all([

    loadTask(),

    loadActivities(),

    loadComments(),

    loadCommentPermission()

  ])
}


/**
 * 任务
 */
async function loadTask() {

  const result =
    await getTaskDetailApi(
      taskId
    )


  task.value =
    result.data
}


/**
 * 动态
 */
async function loadActivities() {

  const result =
    await getTaskActivitiesApi(
      taskId
    )


  activities.value =
    result.data
}


/**
 * 评论
 */
async function loadComments() {

  const result =
    await getTaskCommentsApi(
      taskId
    )


  comments.value =
    result.data
}


/**
 * 评论权限
 */
async function loadCommentPermission() {

  const result =
    await getTaskCommentPermissionApi(
      taskId
    )


  canComment.value =
    result.data
}


/**
 * 发表评论
 */
async function handleCreateComment() {

  const content =
    commentContent.value.trim()


  if (!content) {

    ElMessage.warning(
      '请输入评论内容'
    )

    return
  }


  if (
    content.length > 2000
  ) {

    ElMessage.warning(
      '评论内容不能超过2000个字符'
    )

    return
  }


  commentSubmitting.value =
    true


  try {

    await createTaskCommentApi(
      taskId,
      {
        content
      }
    )


    ElMessage.success(
      '评论发表成功'
    )


    commentContent.value =
      ''


    await loadComments()

  }

  finally {

    commentSubmitting.value =
      false
  }
}


/**
 * 删除评论
 */
async function handleDeleteComment(
  comment: TaskComment
) {

  try {

    await ElMessageBox.confirm(
      '确定要删除这条评论吗？',
      '删除评论',
      {
        type:
          'warning',

        confirmButtonText:
          '确定删除',

        cancelButtonText:
          '取消'
      }
    )

  }

  catch {

    return
  }


  await deleteTaskCommentApi(
    comment.id
  )


  ElMessage.success(
    '评论已删除'
  )


  await loadComments()
}


/**
 * 打开负责人管理
 */
async function openAssigneeDialog() {

  if (
    !task.value
  ) {

    return
  }


  const result =
    await getProjectMembersApi(
      task.value.projectId
    )


  projectMembers.value =
    result.data


  selectedUserId.value =
    null


  assigneeDialogVisible.value =
    true
}


/**
 * 添加负责人
 */
async function handleAddAssignee() {

  if (
    selectedUserId.value
    === null
  ) {

    return
  }


  assigneeLoading.value =
    true


  try {

    await addTaskAssigneeApi(
      taskId,
      selectedUserId.value
    )


    ElMessage.success(
      '负责人添加成功'
    )


    selectedUserId.value =
      null


    await Promise.all([

      loadTask(),

      loadActivities()

    ])

  }

  finally {

    assigneeLoading.value =
      false
  }
}


/**
 * 移除负责人
 */
async function handleRemoveAssignee(
  userId: number,
  name: string
) {

  if (
    cannotRemoveLastAssignee.value
  ) {

    ElMessage.warning(
      '进行中或待验收的任务至少需要保留一名负责人'
    )

    return
  }


  try {

    await ElMessageBox.confirm(
      `确定要移除负责人“${name}”吗？`,
      '移除负责人',
      {
        type:
          'warning'
      }
    )

  }

  catch {

    return
  }


  await removeTaskAssigneeApi(
    taskId,
    userId
  )


  ElMessage.success(
    '负责人已移除'
  )


  await Promise.all([

    loadTask(),

    loadActivities()

  ])
}


/**
 * 状态流转
 */
async function handleChangeStatus(
  targetStatus: TaskStatus
) {

  if (!task.value) {

    return
  }


  const action =
    getStatusActionName(
      task.value.status,
      targetStatus
    )


  if (
    targetStatus === 'DONE'
    ||
    targetStatus === 'CANCELLED'
    ||
    (
      task.value.status === 'IN_REVIEW'
      &&
      targetStatus === 'IN_PROGRESS'
    )
  ) {

    try {

      await ElMessageBox.confirm(
        `确定执行“${action}”吗？`,
        '任务状态确认',
        {
          type:
            'warning'
        }
      )

    }

    catch {

      return
    }
  }


  await changeTaskStatusApi(
    taskId,
    targetStatus,
    task.value.version
  )


  ElMessage.success(
    `${action}成功`
  )


  await Promise.all([

    loadTask(),

    loadActivities()

  ])
}


/**
 * 返回看板
 */
function goBoard() {

  if (!task.value) {

    return
  }


  router.push(
    `/projects/${task.value.projectId}/tasks`
  )
}


/**
 * 评论用户名称
 */
function getCommentUserName(
  comment: TaskComment
) {

  return (
    comment.nickname
    ||
    comment.username
    ||
    '未知用户'
  )
}


/**
 * 成员身份
 */
function getMemberIdentity(
  member: ProjectMember
) {

  if (
    member.projectOwner
  ) {

    return '项目负责人'
  }


  switch (
    member.roleType
  ) {

    case 'MANAGER':

      return '项目管理员'


    case 'MEMBER':

      return '普通成员'


    case 'READ_ONLY':

      return '只读成员'


    default:

      return member.roleType
  }
}


/**
 * 状态名称
 */
function getStatusName(
  status: TaskStatus
) {

  const map:
    Record<TaskStatus, string> = {

      TODO:
        '待处理',

      IN_PROGRESS:
        '进行中',

      IN_REVIEW:
        '待验收',

      DONE:
        '已完成',

      CANCELLED:
        '已取消'

    }


  return map[status]
}


/**
 * 状态操作
 */
function getStatusActionName(
  current: TaskStatus,
  target: TaskStatus
) {

  if (
    current === 'TODO'
    &&
    target === 'IN_PROGRESS'
  ) {

    return '开始处理'
  }


  if (
    current === 'IN_PROGRESS'
    &&
    target === 'IN_REVIEW'
  ) {

    return '提交验收'
  }


  if (
    current === 'IN_REVIEW'
    &&
    target === 'IN_PROGRESS'
  ) {

    return '驳回处理'
  }


  if (
    current === 'IN_REVIEW'
    &&
    target === 'DONE'
  ) {

    return '验收完成'
  }


  if (
    target === 'CANCELLED'
  ) {

    return '取消任务'
  }


  return getStatusName(
    target
  )
}


/**
 * 无状态操作提示
 */
function getNoActionText() {

  if (!task.value) {

    return ''
  }


  if (
    task.value.status === 'DONE'
  ) {

    return '任务已完成'
  }


  if (
    task.value.status === 'CANCELLED'
  ) {

    return '任务已取消'
  }


  if (
    task.value.status === 'TODO'
    &&
    task.value.assignees.length === 0
  ) {

    return task.value.canManageAssignees
      ? '当前任务尚未分配负责人，请先分配负责人'
      : '当前任务尚未分配负责人，等待管理员进行任务调度'
  }


  if (
    task.value.status === 'IN_REVIEW'
  ) {

    return '任务正在等待有验收权限的成员处理'
  }


  return '当前账号没有可执行的状态操作'
}


/**
 * 状态按钮
 */
function getStatusButtonType(
  status: TaskStatus
):
  'primary'
  | 'success'
  | 'warning'
  | 'danger'
  | 'info' {

  switch (status) {

    case 'DONE':
      return 'success'

    case 'CANCELLED':
      return 'danger'

    case 'IN_REVIEW':
      return 'warning'

    case 'IN_PROGRESS':
      return 'primary'

    default:
      return 'info'
  }
}


/**
 * 优先级名称
 */
function getPriorityName(
  priority: TaskPriority
) {

  const map:
    Record<TaskPriority, string> = {

      LOW:
        '低',

      MEDIUM:
        '中',

      HIGH:
        '高',

      URGENT:
        '紧急'

    }


  return map[priority]
}


/**
 * 优先级样式
 */
function getPriorityType(
  priority: TaskPriority
):
  'info'
  | 'primary'
  | 'warning'
  | 'danger' {

  switch (priority) {

    case 'LOW':
      return 'info'

    case 'MEDIUM':
      return 'primary'

    case 'HIGH':
      return 'warning'

    case 'URGENT':
      return 'danger'
  }
}


/**
 * 状态样式
 */
function getStatusType(
  status: TaskStatus
):
  'info'
  | 'primary'
  | 'warning'
  | 'success'
  | 'danger' {

  switch (status) {

    case 'TODO':
      return 'info'

    case 'IN_PROGRESS':
      return 'primary'

    case 'IN_REVIEW':
      return 'warning'

    case 'DONE':
      return 'success'

    case 'CANCELLED':
      return 'danger'
  }
}


/**
 * 时间
 */
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

.task-detail-page {

  width: 100%;

}


.header {

  display: flex;

  justify-content: space-between;

  align-items: flex-start;

}


.header h2 {

  margin-top: 12px;

}


.description {

  color: #606266;

  line-height: 1.8;

}


.assignee-area {

  display: flex;

  justify-content: space-between;

  align-items: center;

  gap: 20px;

}


.assignee-list {

  display: flex;

  flex-wrap: wrap;

  gap: 8px;

}


.empty-text,
.empty-action {

  color: #909399;

}


.status-actions {

  display: flex;

  flex-wrap: wrap;

  gap: 12px;

}


/**
 * 评论
 */

.comment-card {

  margin-top: 20px;

}


.comment-header {

  display: flex;

  align-items: center;

  gap: 10px;

  font-weight: 600;

}


.comment-editor {

  width: 100%;

}


.comment-submit {

  display: flex;

  justify-content: flex-end;

  margin-top: 12px;

}


.comment-permission-tip {

  margin-bottom: 10px;

}


.comment-list {

  display: flex;

  flex-direction: column;

}


.comment-item {

  display: flex;

  gap: 12px;

  padding: 18px 0;

  border-bottom: 1px solid #ebeef5;

}


.comment-item:last-child {

  border-bottom: none;

}


.comment-main {

  flex: 1;

  min-width: 0;

}


.comment-user-row {

  display: flex;

  justify-content: space-between;

  align-items: center;

}


.comment-user {

  font-weight: 600;

  color: #303133;

}


.comment-username {

  margin-left: 8px;

  color: #909399;

  font-size: 12px;

}


.comment-content {

  margin-top: 9px;

  color: #606266;

  line-height: 1.7;

  white-space: pre-wrap;

  word-break: break-word;

}


.comment-time {

  margin-top: 8px;

  color: #a8abb2;

  font-size: 12px;

}


/**
 * 负责人弹窗
 */

.section-title {

  margin-bottom: 14px;

  font-weight: 600;

}


.current-assignees {

  display: flex;

  flex-direction: column;

  gap: 10px;

}


.current-assignee-item {

  display: flex;

  justify-content: space-between;

  align-items: center;

  padding: 10px 12px;

  border: 1px solid #ebeef5;

  border-radius: 6px;

}


.role-name {

  float: right;

  margin-left: 20px;

  color: #909399;

  font-size: 12px;

}


.add-button {

  margin-top: 16px;

}

</style>