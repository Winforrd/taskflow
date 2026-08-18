<template>

  <div
    v-if="task"
    class="task-detail-page"
  >

    <el-row :gutter="20">

      <!-- 左侧任务主体 -->
      <el-col :span="16">

        <el-card>

          <!-- 顶部 -->
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


          <!-- 描述 -->
          <p class="description">

            {{
              task.description
              || '暂无任务描述'
            }}

          </p>


          <!-- 基本信息 -->
          <el-descriptions
            :column="2"
            border
          >

            <!-- 状态 -->
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


            <!-- 创建人 -->
            <el-descriptions-item
              label="创建人"
            >

              {{
                task.creatorName
                || '-'
              }}

            </el-descriptions-item>


            <!-- 开始日期 -->
            <el-descriptions-item
              label="开始日期"
            >

              {{
                task.startDate
                || '-'
              }}

            </el-descriptions-item>


            <!-- 截止日期 -->
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
                    class="assignee-tag"
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


                <!--
                  DONE / CANCELLED 时
                  后端 canManageAssignees=false，
                  所以按钮自动消失。
                -->
                <el-button
                  v-if="task.canManageAssignees"
                  size="small"
                  type="primary"
                  plain
                  @click="openAssigneeDialog"
                >

                  管理负责人

                </el-button>

              </div>


              <!-- TODO无人待办 -->
              <div
                v-if="
                  task.status === 'TODO'
                  &&
                  task.assignees.length === 0
                "
                class="unassigned-tip"
              >

                当前任务属于待调度任务，需要由项目负责人、
                项目管理员或团队管理员分配负责人后才能开始处理。

              </div>

          </el-descriptions-item>

          </el-descriptions>


          <!-- 状态流转 -->
          <el-divider>

            状态流转

          </el-divider>


          <div class="status-actions">

            <el-button
              v-for="
                status
                in task.allowedNextStatuses
              "
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

      </el-col>


      <!-- 右侧动态 -->
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


              <span>

                {{ activity.content }}

              </span>

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


    <!-- ================================================= -->
    <!-- 管理负责人 -->
    <!-- ================================================= -->

    <el-dialog
      v-model="assigneeDialogVisible"
      title="管理任务负责人"
      width="600px"
    >

      <!-- 当前负责人 -->
      <div class="dialog-section">

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

            <div class="member-info">

              <el-avatar
                :size="36"
                :src="assignee.avatar"
              >

                {{
                  (
                    assignee.nickname
                    ||
                    assignee.username
                  ).charAt(0)
                }}

              </el-avatar>


              <div>

                <div class="member-name">

                  {{
                    assignee.nickname
                    ||
                    assignee.username
                  }}

                </div>


                <div class="member-username">

                  @{{ assignee.username }}

                </div>

              </div>

            </div>


            <div class="remove-area">

              <!--
                当前是最后一名负责人，
                且任务处于进行中/待验收，
                不允许移除。
              -->
              <el-tooltip
                v-if="
                  cannotRemoveLastAssignee
                "
                content="进行中或待验收的任务必须至少保留一名负责人"
                placement="top"
              >

                <el-button
                  type="danger"
                  link
                  disabled
                >

                  移除

                </el-button>

              </el-tooltip>


              <!-- 正常可移除 -->
              <el-button
                v-else
                type="danger"
                link
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

        </div>


        <el-empty
          v-else
          :image-size="60"
          description="当前暂无负责人"
        />


        <!-- 状态提示 -->
        <div
          v-if="
            task.status === 'IN_PROGRESS'
            ||
            task.status === 'IN_REVIEW'
          "
          class="protect-tip"
        >

          当前任务处于
          {{
            getStatusName(
              task.status
            )
          }}，
          必须至少保留一名负责人。

        </div>

      </div>


      <el-divider />


      <!-- 添加负责人 -->
      <div class="dialog-section">

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

            <div class="member-option">

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

            </div>

          </el-option>

        </el-select>


        <div class="dialog-tip">

          只允许选择已经加入项目且不是只读成员的用户。

        </div>


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

      </div>


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

import type {
  ProjectMember
} from '@/types/project'

import type {
  Task,
  TaskActivity,
  TaskPriority,
  TaskStatus
} from '@/types/task'


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
 * ==================================================
 * 是否禁止移除最后一个负责人
 *
 * IN_PROGRESS
 * IN_REVIEW
 *
 * 且负责人只剩1人。
 * ==================================================
 */
const cannotRemoveLastAssignee =
  computed(
    () => {

      if (
        !task.value
      ) {

        return false
      }


      const protectedStatus =
        task.value.status === 'IN_PROGRESS'
        ||
        task.value.status === 'IN_REVIEW'


      return (
        protectedStatus
        &&
        task.value.assignees.length <= 1
      )
    }
  )


/**
 * ==================================================
 * 可新增负责人
 * ==================================================
 */
const availableMembers =
  computed(
    () => {

      if (
        !task.value
      ) {

        return []
      }


      const currentAssigneeIds =
        new Set(

          task.value.assignees.map(
            assignee =>
              assignee.userId
          )

        )


      return projectMembers.value.filter(
        member => {

          /**
           * READ_ONLY不能负责。
           */
          if (
            member.roleType
            === 'READ_ONLY'
          ) {

            return false
          }


          /**
           * 已经是负责人。
           */
          if (
            currentAssigneeIds.has(
              member.userId
            )
          ) {

            return false
          }


          return true
        }
      )
    }
  )


onMounted(
  async () => {

    await loadData()

  }
)


/**
 * ==================================================
 * 加载任务和动态
 * ==================================================
 */
async function loadData() {

  const [
    taskResult,
    activityResult
  ] =
    await Promise.all([

      getTaskDetailApi(
        taskId
      ),

      getTaskActivitiesApi(
        taskId
      )

    ])


  task.value =
    taskResult.data


  activities.value =
    activityResult.data
}


/**
 * ==================================================
 * 打开负责人管理
 * ==================================================
 */
async function openAssigneeDialog() {

  if (
    !task.value
  ) {

    return
  }


  if (
    !task.value.canManageAssignees
  ) {

    ElMessage.error(
      '当前任务不允许管理负责人'
    )

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
 * ==================================================
 * 添加负责人
 * ==================================================
 */
async function handleAddAssignee() {

  if (
    !task.value
  ) {

    return
  }


  if (
    !task.value.canManageAssignees
  ) {

    ElMessage.error(
      '没有管理任务负责人的权限'
    )

    return
  }


  if (
    selectedUserId.value
    === null
  ) {

    ElMessage.warning(
      '请选择要添加的负责人'
    )

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


    await loadData()

  }

  finally {

    assigneeLoading.value =
      false
  }
}


/**
 * ==================================================
 * 移除负责人
 * ==================================================
 */
async function handleRemoveAssignee(
  userId: number,
  name: string
) {

  if (
    !task.value
  ) {

    return
  }


  if (
    !task.value.canManageAssignees
  ) {

    ElMessage.error(
      '没有管理任务负责人的权限'
    )

    return
  }


  /**
   * 前端保护：
   *
   * IN_PROGRESS / IN_REVIEW
   * 不能删除最后一个负责人。
   *
   * 后端仍然会再次检查。
   */
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
        confirmButtonText:
          '确定移除',

        cancelButtonText:
          '取消',

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


  await loadData()
}


/**
 * ==================================================
 * 状态流转
 * ==================================================
 */
async function handleChangeStatus(
  targetStatus: TaskStatus
) {

  if (
    !task.value
  ) {

    return
  }


  const actionName =
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
      task.value.status
      === 'IN_REVIEW'
      &&
      targetStatus
      === 'IN_PROGRESS'
    )
  ) {

    try {

      await ElMessageBox.confirm(
        `确定要执行“${actionName}”吗？`,
        '任务状态确认',
        {
          confirmButtonText:
            '确定',

          cancelButtonText:
            '取消',

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
    `${actionName}成功`
  )


  await loadData()
}


/**
 * ==================================================
 * 返回看板
 * ==================================================
 */
function goBoard() {

  if (
    !task.value
  ) {

    return
  }


  router.push(
    `/projects/${task.value.projectId}/tasks`
  )
}


/**
 * ==================================================
 * 项目成员身份
 * ==================================================
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
 * ==================================================
 * 状态中文
 * ==================================================
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
 * ==================================================
 * 状态操作名称
 * ==================================================
 */
function getStatusActionName(
  currentStatus: TaskStatus,
  targetStatus: TaskStatus
) {

  if (
    currentStatus === 'IN_REVIEW'
    &&
    targetStatus === 'IN_PROGRESS'
  ) {

    return '驳回处理'
  }


  if (
    currentStatus === 'IN_REVIEW'
    &&
    targetStatus === 'DONE'
  ) {

    return '验收完成'
  }


  if (
    currentStatus === 'TODO'
    &&
    targetStatus === 'IN_PROGRESS'
  ) {

    return '开始处理'
  }


  if (
    currentStatus === 'IN_PROGRESS'
    &&
    targetStatus === 'IN_REVIEW'
  ) {

    return '提交验收'
  }


  if (
    targetStatus === 'CANCELLED'
  ) {

    return '取消任务'
  }


  return getStatusName(
    targetStatus
  )
}


/**
 * ==================================================
 * 无操作提示
 * ==================================================
 */
function getNoActionText() {

  if (
    !task.value
  ) {

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

    if (
      task.value.canManageAssignees
    ) {

      return '当前任务尚未分配负责人，请先分配负责人'
    }


    return '当前任务尚未分配负责人，等待管理员进行任务调度'
  }


  if (
    task.value.status === 'IN_REVIEW'
  ) {

    return '任务正在等待有验收权限的成员处理'
  }


  return '当前账号没有可执行的状态操作'
}


/**
 * ==================================================
 * 状态按钮
 * ==================================================
 */
function getStatusButtonType(
  status: TaskStatus
):
  'primary'
  | 'success'
  | 'warning'
  | 'danger'
  | 'info' {

  switch (
    status
  ) {

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
 * ==================================================
 * 优先级
 * ==================================================
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
 * ==================================================
 * 优先级样式
 * ==================================================
 */
function getPriorityType(
  priority: TaskPriority
):
  'info'
  | 'primary'
  | 'warning'
  | 'danger' {

  switch (
    priority
  ) {

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
 * ==================================================
 * 状态样式
 * ==================================================
 */
function getStatusType(
  status: TaskStatus
):
  'info'
  | 'primary'
  | 'warning'
  | 'success'
  | 'danger' {

  switch (
    status
  ) {

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
 * ==================================================
 * 时间
 * ==================================================
 */
function formatTime(
  value?: string
) {

  if (
    !value
  ) {

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

  justify-content:
    space-between;

  align-items:
    flex-start;

}


.header h2 {

  margin-top: 12px;

}


.description {

  line-height: 1.8;

  color: #606266;

}


/**
 * 负责人
 */

.assignee-area {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  gap: 20px;

}


.assignee-list {

  display: flex;

  align-items: center;

  flex-wrap: wrap;

  gap: 8px;

}


.assignee-tag {

  margin-right: 4px;

}


.empty-text {

  color: #909399;

}


.unassigned-tip {

  margin-top: 10px;

  padding: 8px 12px;

  background: #f5f7fa;

  border-radius: 4px;

  color: #909399;

  font-size: 13px;

  line-height: 1.6;

}


/**
 * 状态
 */

.status-actions {

  display: flex;

  align-items: center;

  flex-wrap: wrap;

  gap: 12px;

}


.empty-action {

  color: #909399;

  font-size: 14px;

}


/**
 * 负责人弹窗
 */

.dialog-section {

  width: 100%;

}


.section-title {

  margin-bottom: 14px;

  font-size: 16px;

  font-weight: 600;

}


.current-assignees {

  display: flex;

  flex-direction: column;

  gap: 10px;

}


.current-assignee-item {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  padding: 10px 12px;

  border:
    1px solid #ebeef5;

  border-radius: 6px;

}


.member-info {

  display: flex;

  align-items: center;

  gap: 10px;

}


.member-name {

  color: #303133;

  font-weight: 500;

}


.member-username {

  margin-top: 3px;

  color: #909399;

  font-size: 12px;

}


.remove-area {

  display: flex;

  align-items: center;

}


.member-option {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  width: 100%;

}


.role-name {

  margin-left: 20px;

  color: #909399;

  font-size: 12px;

}


.dialog-tip {

  margin-top: 8px;

  color: #909399;

  font-size: 12px;

}


.add-button {

  margin-top: 16px;

}


.protect-tip {

  margin-top: 12px;

  padding: 8px 12px;

  background: #fdf6ec;

  border-radius: 4px;

  color: #e6a23c;

  font-size: 13px;

  line-height: 1.6;

}

</style>