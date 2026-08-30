<template>

  <div class="task-board-page">

    <!-- 页面头部 -->
    <div class="page-header">

      <div>

        <h2>
          任务看板
        </h2>

        <div
          v-if="project"
          class="project-name"
        >
          {{ project.name }}
        </div>

      </div>


      <div>

        <el-button
          @click="goProject"
        >
          返回项目
        </el-button>


        <el-button
          v-if="canCreateTask"
          type="primary"
          @click="openCreateDialog"
        >
          创建任务
        </el-button>

      </div>

    </div>


    <!-- 拖拽说明 -->
    <el-alert
      class="board-tip"
      type="info"
      :closable="false"
      show-icon
    >
      <template #title>
        可拖动任务卡片变更状态，最终是否允许流转由后端状态机和权限规则决定。
      </template>
    </el-alert>


    <!-- 搜索 -->
    <el-card class="filter-card">

      <div class="filter-row">

        <el-input
          v-model="keyword"
          placeholder="搜索任务标题"
          clearable
          style="width: 300px"
          @keyup.enter="loadTasks"
          @clear="loadTasks"
        />


        <el-button
          type="primary"
          @click="loadTasks"
        >
          搜索
        </el-button>

      </div>

    </el-card>


    <!-- 看板 -->
    <div
      v-loading="loading"
      class="board"
    >

      <div
        v-for="column in columns"
        :key="column.status"
        class="board-column"
      >

        <!-- 列标题 -->
        <div class="column-header">

          <span>
            {{ column.label }}
          </span>


          <el-tag size="small">
            {{
              getTasksByStatus(
                column.status
              ).length
            }}
          </el-tag>

        </div>


        <!-- 拖拽区域 -->
        <div
          class="task-list"
          :data-status="column.status"
        >

          <el-card
            v-for="task in getTasksByStatus(
              column.status
            )"
            :key="task.id"
            class="task-card"
            :class="{
              'task-card-moving':
                changingTaskId === task.id
            }"
            :data-task-id="task.id"
            shadow="hover"
            @click="handleTaskClick(task.id)"
          >

            <!-- 拖拽手柄 -->
            <div
              class="task-drag-handle"
              title="拖拽改变任务状态"
            >
              ⋮⋮
            </div>


            <!-- 标题 -->
            <div class="task-title">
              {{ task.title }}
            </div>


            <!-- 优先级 + 截止日期 -->
            <div class="task-meta">

              <el-tag
                size="small"
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


              <span
                v-if="task.deadline"
                class="deadline"
              >
                {{ task.deadline }}
              </span>

            </div>


            <!-- 负责人 -->
            <div class="assignees">

              <el-avatar
                v-for="assignee in task.assignees"
                :key="assignee.userId"
                :size="28"
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


              <span
                v-if="
                  task.assignees.length === 0
                "
                class="no-assignee"
              >
                未指派
              </span>

            </div>


            <!--
              allowedNextStatuses只负责提示。

              不在前端阻止状态请求，
              最终由后端状态机做权限和状态判断。
            -->
            <div
              v-if="
                task.allowedNextStatuses
                &&
                task.allowedNextStatuses.length > 0
              "
              class="next-status-tip"
            >
              可流转：
              {{
                task.allowedNextStatuses
                  .map(getStatusName)
                  .join('、')
              }}
            </div>


            <div
              v-else
              class="next-status-tip"
            >
              当前无可用状态流转
            </div>

          </el-card>


          <!-- 空列 -->
          <el-empty
            v-if="
              getTasksByStatus(
                column.status
              ).length === 0
            "
            class="empty-placeholder"
            :image-size="60"
            description="暂无任务"
          />

        </div>

      </div>

    </div>


    <!-- 创建任务 -->
    <el-dialog
      v-model="createVisible"
      title="创建任务"
      width="600px"
    >

      <el-form
        :model="createForm"
        label-width="90px"
      >

        <!-- 标题 -->
        <el-form-item label="任务标题">

          <el-input
            v-model="createForm.title"
            maxlength="200"
            show-word-limit
          />

        </el-form-item>


        <!-- 优先级 -->
        <el-form-item label="优先级">

          <el-select
            v-model="createForm.priority"
            style="width: 100%"
          >

            <el-option
              label="低"
              value="LOW"
            />

            <el-option
              label="中"
              value="MEDIUM"
            />

            <el-option
              label="高"
              value="HIGH"
            />

            <el-option
              label="紧急"
              value="URGENT"
            />

          </el-select>

        </el-form-item>


        <!-- 管理角色可以指定负责人 -->
        <el-form-item
          v-if="canAssignOthers"
          label="负责人"
        >

          <el-select
            v-model="createForm.assigneeIds"
            multiple
            filterable
            clearable
            placeholder="可不选择，创建无人负责的待办任务"
            style="width: 100%"
          >

            <el-option
              v-for="member in assignableMembers"
              :key="member.userId"
              :label="
                member.nickname
                || member.username
              "
              :value="member.userId"
            >

              <span>
                {{
                  member.nickname
                  || member.username
                }}
              </span>


              <span class="member-role">
                {{
                  getMemberIdentity(
                    member
                  )
                }}
              </span>

            </el-option>

          </el-select>


          <div class="form-tip">
            项目负责人、项目管理员可调度项目成员；只读成员不能成为任务负责人。
          </div>

        </el-form-item>


        <!-- MEMBER只能自己负责 -->
        <el-form-item
          v-else
          label="负责人"
        >

          <div class="self-assignee">

            <el-avatar
              :size="30"
            >
              {{
                getCurrentUserName()
                  .charAt(0)
              }}
            </el-avatar>


            <div>

              <div>
                {{ getCurrentUserName() }}
              </div>


              <div class="self-tip">
                普通成员创建任务后自动由自己负责
              </div>

            </div>

          </div>

        </el-form-item>


        <!-- 开始日期 -->
        <el-form-item label="开始日期">

          <el-date-picker
            v-model="createForm.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />

        </el-form-item>


        <!-- 截止日期 -->
        <el-form-item label="截止日期">

          <el-date-picker
            v-model="createForm.deadline"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />

        </el-form-item>


        <!-- 任务描述 -->
        <el-form-item label="任务描述">

          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="5"
          />

        </el-form-item>

      </el-form>


      <template #footer>

        <el-button
          @click="createVisible = false"
        >
          取消
        </el-button>


        <el-button
          type="primary"
          :loading="creating"
          @click="handleCreate"
        >
          创建
        </el-button>

      </template>

    </el-dialog>

  </div>

</template>


<script setup lang="ts">

import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref
} from 'vue'


import {
  useRoute,
  useRouter
} from 'vue-router'


import {
  ElMessage
} from 'element-plus'


import Sortable, {
  type SortableEvent
} from 'sortablejs'


import request from '@/utils/request'


import {
  createTaskApi,
  getProjectTasksApi
} from '@/api/task'


import {
  getProjectDetailApi
} from '@/api/project'


import {
  getProjectMembersApi
} from '@/api/projectMember'


import {
  useUserStore
} from '@/stores/user'


import type {
  ProjectDetail,
  ProjectMember
} from '@/types/project'


import type {
  ChangeTaskStatusRequest,
  Task,
  TaskPriority,
  TaskStatus
} from '@/types/task'


const route =
  useRoute()


const router =
  useRouter()


const userStore =
  useUserStore()


const projectId =
  Number(
    route.params.projectId
  )


const project =
  ref<ProjectDetail | null>(
    null
  )


const projectMembers =
  ref<ProjectMember[]>([])


const tasks =
  ref<Task[]>([])


const keyword =
  ref('')


const loading =
  ref(false)


const createVisible =
  ref(false)


const creating =
  ref(false)


/**
 * 当前正在变更状态的任务。
 */
const changingTaskId =
  ref<number | null>(
    null
  )


/**
 * 区分普通点击和拖拽。
 */
const dragging =
  ref(false)


/**
 * Sortable实例。
 */
const sortableInstances:
  Sortable[] = []


/**
 * 五列任务看板。
 */
const columns = [

  {
    status:
      'TODO' as TaskStatus,

    label:
      '待处理'
  },

  {
    status:
      'IN_PROGRESS' as TaskStatus,

    label:
      '进行中'
  },

  {
    status:
      'IN_REVIEW' as TaskStatus,

    label:
      '待验收'
  },

  {
    status:
      'DONE' as TaskStatus,

    label:
      '已完成'
  },

  {
    status:
      'CANCELLED' as TaskStatus,

    label:
      '已取消'
  }

]


/**
 * 创建任务表单。
 */
const createForm =
  reactive({

    title: '',

    description: '',

    priority:
      'MEDIUM' as TaskPriority,

    startDate: '',

    deadline: '',

    assigneeIds:
      [] as number[]

  })


/**
 * 当前用户ID。
 */
const currentUserId =
  computed(
    () =>
      userStore.userInfo?.id
      ?? null
  )


/**
 * 是否可以创建任务。
 */
const canCreateTask =
  computed(
    () => {

      if (
        !project.value
        ||
        project.value.status
          === 'ARCHIVED'
      ) {

        return false
      }


      if (
        project.value.myTeamRole
          === 'OWNER'
        ||
        project.value.myTeamRole
          === 'ADMIN'
      ) {

        return true
      }


      if (
        currentUserId.value
        ===
        project.value.ownerId
      ) {

        return true
      }


      return (
        project.value.myProjectRole
          === 'MANAGER'
        ||
        project.value.myProjectRole
          === 'MEMBER'
      )
    }
  )


/**
 * 是否能够给其他人分配任务。
 */
const canAssignOthers =
  computed(
    () => {

      if (!project.value) {

        return false
      }


      if (
        project.value.myTeamRole
          === 'OWNER'
        ||
        project.value.myTeamRole
          === 'ADMIN'
      ) {

        return true
      }


      if (
        currentUserId.value
        ===
        project.value.ownerId
      ) {

        return true
      }


      return project.value.myProjectRole
        === 'MANAGER'
    }
  )


/**
 * 可成为负责人的成员。
 */
const assignableMembers =
  computed(
    () =>
      projectMembers.value.filter(
        member =>
          member.roleType
            !== 'READ_ONLY'
      )
  )


/**
 * 页面初始化。
 */
onMounted(
  async () => {

    if (
      !userStore.userInfo
    ) {

      await userStore.getUserInfo()
    }


    await loadPage()

  }
)


/**
 * 页面销毁。
 */
onBeforeUnmount(
  () => {

    destroySortables()

  }
)


/**
 * 加载页面。
 */
async function loadPage() {

  const [
    projectResult,
    memberResult
  ] =
    await Promise.all([

      getProjectDetailApi(
        projectId
      ),

      getProjectMembersApi(
        projectId
      )

    ])


  project.value =
    projectResult.data


  projectMembers.value =
    memberResult.data


  await loadTasks()
}


/**
 * 加载任务。
 */
async function loadTasks() {

  loading.value =
    true


  try {

    const result =
      await getProjectTasksApi(
        projectId,
        {
          keyword:
            keyword.value
              || undefined
        }
      )


    tasks.value =
      result.data


    await nextTick()


    initSortables()

  }
  finally {

    loading.value =
      false
  }
}


/**
 * 初始化Sortable。
 */
function initSortables() {

  destroySortables()


  const containers =
    document.querySelectorAll<HTMLElement>(
      '.task-list'
    )


  containers.forEach(
    container => {

      const sortable =
        Sortable.create(
          container,
          {

            group: {
              name:
                'task-board',

              pull:
                true,

              put:
                true
            },


            animation:
              180,


            handle:
              '.task-drag-handle',


            ghostClass:
              'task-card-ghost',


            chosenClass:
              'task-card-chosen',


            dragClass:
              'task-card-dragging',


            onStart: () => {

              dragging.value =
                true
            },


            onEnd:
              async (
                event:
                  SortableEvent
              ) => {

                await handleDragEnd(
                  event
                )


                window.setTimeout(
                  () => {

                    dragging.value =
                      false

                  },
                  100
                )
              }

          }
        )


      sortableInstances.push(
        sortable
      )

    }
  )
}


/**
 * 销毁Sortable。
 */
function destroySortables() {

  while (
    sortableInstances.length > 0
  ) {

    const instance =
      sortableInstances.pop()


    instance?.destroy()
  }
}


/**
 * =========================================================
 * 拖拽修改任务状态
 * =========================================================
 *
 * 注意：
 *
 * 错误提示统一由request.ts处理。
 *
 * 本页面catch只负责：
 *
 * 1. 捕获请求失败
 * 2. 重新读取真实数据
 * 3. 让拖拽卡片自动回滚
 *
 * 不再重复ElMessage.error。
 */
async function handleDragEnd(
  event: SortableEvent
) {

  const taskId =
    Number(
      event.item.dataset.taskId
    )


  const fromStatus =
    event.from.dataset.status as TaskStatus | undefined


  const targetStatus =
    event.to.dataset.status  as TaskStatus | undefined


  /**
   * DOM数据异常。
   */
  if (
    !taskId
    ||
    !fromStatus
    ||
    !targetStatus
  ) {

    await loadTasks()

    return
  }


  /**
   * 同一个状态列内移动。
   *
   * 当前没有排序字段，
   * 因此不保存排序。
   */
  if (
    fromStatus
    === targetStatus
  ) {

    await loadTasks()

    return
  }


  const task =
    tasks.value.find(
      item =>
        item.id === taskId
    )


  if (!task) {

    await loadTasks()

    return
  }


  /**
   * 防止同时提交多个状态修改。
   */
  if (
    changingTaskId.value
    !== null
  ) {

    ElMessage.warning(
      '正在更新任务状态，请稍候'
    )


    await loadTasks()

    return
  }


  changingTaskId.value =
    taskId


  try {

    const requestBody:
      ChangeTaskStatusRequest = {

        targetStatus,

        version:
          task.version

      }


    /**
     * 真正交给后端状态机判断。
     */
    await request.post(
      `/tasks/${taskId}/status`,
      requestBody
    )


    /**
     * 成功提示由当前页面负责。
     */
    ElMessage.success(
      `任务已移动到“${getStatusName(targetStatus)}”`
    )


    /**
     * 重新读取最新：
     *
     * status
     * version
     * allowedNextStatuses
     * completedTime
     */
    await loadTasks()

  }
  catch (error) {

    /**
     * =====================================================
     * 这里不再ElMessage.error。
     * =====================================================
     *
     * 原因：
     *
     * request.ts已经根据后端响应显示：
     *
     * 当前任务状态不允许执行该操作
     *
     * 如果这里再次：
     *
     * ElMessage.error(...)
     *
     * 就会出现两个错误提示。
     */
    console.warn(
      '任务状态变更被后端拒绝，正在恢复任务位置',
      error
    )


    /**
     * 重新加载服务器真实状态。
     *
     * 这一步负责视觉上的拖拽回退。
     */
    await loadTasks()

  }
  finally {

    changingTaskId.value =
      null
  }
}


/**
 * 按状态筛选任务。
 */
function getTasksByStatus(
  status: TaskStatus
) {

  return tasks.value.filter(
    task =>
      task.status
        === status
  )
}


/**
 * 打开创建任务窗口。
 */
function openCreateDialog() {

  if (
    !canCreateTask.value
  ) {

    ElMessage.error(
      '没有创建任务的权限'
    )

    return
  }


  createForm.title =
    ''


  createForm.description =
    ''


  createForm.priority =
    'MEDIUM'


  createForm.startDate =
    ''


  createForm.deadline =
    ''


  createForm.assigneeIds =
    []


  if (
    !canAssignOthers.value
    &&
    currentUserId.value
      !== null
  ) {

    createForm.assigneeIds = [
      currentUserId.value
    ]
  }


  createVisible.value =
    true
}


/**
 * 创建任务。
 */
async function handleCreate() {

  if (
    !createForm.title.trim()
  ) {

    ElMessage.warning(
      '请输入任务标题'
    )

    return
  }


  if (
    createForm.startDate
    &&
    createForm.deadline
    &&
    createForm.deadline
      <
    createForm.startDate
  ) {

    ElMessage.warning(
      '截止日期不能早于开始日期'
    )

    return
  }


  creating.value =
    true


  try {

    const assigneeIds =
      canAssignOthers.value

        ? createForm.assigneeIds

        : (
          currentUserId.value
            !== null

            ? [
                currentUserId.value
              ]

            : []
        )


    await createTaskApi(
      projectId,
      {

        title:
          createForm.title.trim(),


        description:
          createForm.description
            .trim()
            ||
            undefined,


        priority:
          createForm.priority,


        startDate:
          createForm.startDate
            ||
            undefined,


        deadline:
          createForm.deadline
            ||
            undefined,


        assigneeIds

      }
    )


    ElMessage.success(
      '任务创建成功'
    )


    createVisible.value =
      false


    await loadTasks()

  }
  finally {

    creating.value =
      false
  }
}


/**
 * 卡片点击。
 */
function handleTaskClick(
  taskId: number
) {

  if (
    dragging.value
  ) {

    return
  }


  goTask(
    taskId
  )
}


/**
 * 任务详情。
 */
function goTask(
  taskId: number
) {

  router.push(
    `/tasks/${taskId}`
  )
}


/**
 * 返回项目。
 */
function goProject() {

  router.push(
    `/projects/${projectId}`
  )
}


/**
 * 项目成员身份。
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
 * 当前用户名称。
 */
function getCurrentUserName() {

  return (
    userStore.userInfo?.nickname
    ||
    userStore.userInfo?.username
    ||
    '当前用户'
  )
}


/**
 * 状态中文。
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
 * 优先级中文。
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
 * 优先级标签。
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

</script>


<style scoped>

.task-board-page {

  width: 100%;

}


.page-header {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  margin-bottom: 20px;

}


.page-header h2 {

  margin: 0;

}


.project-name {

  margin-top: 6px;

  color: #909399;

}


.board-tip {

  margin-bottom: 16px;

}


.filter-card {

  margin-bottom: 16px;

}


.filter-row {

  display: flex;

  gap: 10px;

}


.board {

  display: grid;

  grid-template-columns:
    repeat(
      5,
      minmax(
        250px,
        1fr
      )
    );

  gap: 16px;

  align-items: start;

  overflow-x: auto;

}


.board-column {

  min-height: 500px;

  padding: 12px;

  background: #f5f7fa;

  border-radius: 8px;

}


.column-header {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  margin-bottom: 12px;

  font-weight: 600;

}


.task-list {

  min-height: 430px;

}


.task-card {

  position: relative;

  margin-bottom: 12px;

  cursor: pointer;

  transition:
    opacity 0.2s,
    transform 0.2s,
    box-shadow 0.2s;

}


.task-card-moving {

  opacity: 0.6;

  pointer-events: none;

}


.task-drag-handle {

  position: absolute;

  top: 8px;

  right: 10px;

  padding: 2px 4px;

  color: #909399;

  font-size: 18px;

  line-height: 1;

  cursor: grab;

  user-select: none;

}


.task-drag-handle:active {

  cursor: grabbing;

}


.task-card-ghost {

  opacity: 0.35;

}


.task-card-chosen {

  box-shadow:
    0 4px 16px
    rgba(
      0,
      0,
      0,
      0.15
    );

}


.task-card-dragging {

  opacity: 0.8;

}


.task-title {

  padding-right: 28px;

  font-weight: 500;

  line-height: 1.5;

}


.task-meta {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  margin-top: 12px;

}


.deadline {

  color: #909399;

  font-size: 12px;

}


.assignees {

  display: flex;

  align-items: center;

  gap: 5px;

  margin-top: 12px;

}


.no-assignee {

  color: #909399;

  font-size: 12px;

}


.next-status-tip {

  margin-top: 10px;

  color: #909399;

  font-size: 12px;

  line-height: 1.5;

}


.empty-placeholder {

  pointer-events: none;

}


.form-tip {

  margin-top: 6px;

  color: #909399;

  font-size: 12px;

  line-height: 1.5;

}


.member-role {

  float: right;

  margin-left: 20px;

  color: #909399;

  font-size: 12px;

}


.self-assignee {

  display: flex;

  align-items: center;

  gap: 10px;

}


.self-tip {

  margin-top: 3px;

  color: #909399;

  font-size: 12px;

}

</style>