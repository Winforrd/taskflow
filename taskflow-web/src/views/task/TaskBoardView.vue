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


    <!-- 任务看板 -->
    <div class="board">

      <div
        v-for="column in columns"
        :key="column.status"
        class="board-column"
      >

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


        <div class="task-list">

          <el-card
            v-for="task in getTasksByStatus(
              column.status
            )"
            :key="task.id"
            class="task-card"
            shadow="hover"
            @click="goTask(task.id)"
          >

            <!-- 标题 -->
            <div class="task-title">

              {{ task.title }}

            </div>


            <!-- 优先级/截止日期 -->
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

          </el-card>


          <el-empty
            v-if="
              getTasksByStatus(
                column.status
              ).length === 0
            "
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
        <el-form-item
          label="任务标题"
        >

          <el-input
            v-model="createForm.title"
            maxlength="200"
            show-word-limit
          />

        </el-form-item>


        <!-- 优先级 -->
        <el-form-item
          label="优先级"
        >

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


        <!--
          高权限角色：
          可以选择负责人
        -->
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

            项目负责人、项目管理员可调度项目成员；
            只读成员不能成为任务负责人。

          </div>

        </el-form-item>


        <!--
          MEMBER：
          不再让他选择别人
        -->
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
        <el-form-item
          label="开始日期"
        >

          <el-date-picker
            v-model="createForm.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />

        </el-form-item>


        <!-- 截止日期 -->
        <el-form-item
          label="截止日期"
        >

          <el-date-picker
            v-model="createForm.deadline"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />

        </el-form-item>


        <!-- 描述 -->
        <el-form-item
          label="任务描述"
        >

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


const createVisible =
  ref(false)


const creating =
  ref(false)


/**
 * 五列看板
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


const createForm =
  reactive({

    title: '',

    description: '',

    priority:  'MEDIUM' as TaskPriority,

    startDate: '',

    deadline: '',

    assigneeIds:
      [] as number[]

  })


/**
 * 当前用户ID
 */
const currentUserId =
  computed(
    () =>
      userStore.userInfo?.id
      ?? null
  )


/**
 * 是否可以创建任务
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


      /**
       * 团队OWNER / ADMIN
       */
      if (
        project.value.myTeamRole
        === 'OWNER'
        ||
        project.value.myTeamRole
        === 'ADMIN'
      ) {

        return true
      }


      /**
       * 项目负责人
       */
      if (
        currentUserId.value
        ===
        project.value.ownerId
      ) {

        return true
      }


      /**
       * MANAGER / MEMBER
       */
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
 * 是否能够给别人分配任务
 *
 * MEMBER=false
 */
const canAssignOthers =
  computed(
    () => {

      if (!project.value) {

        return false
      }


      /**
       * 团队OWNER / ADMIN
       */
      if (
        project.value.myTeamRole
        === 'OWNER'
        ||
        project.value.myTeamRole
        === 'ADMIN'
      ) {

        return true
      }


      /**
       * 项目负责人
       */
      if (
        currentUserId.value
        ===
        project.value.ownerId
      ) {

        return true
      }


      /**
       * 只有MANAGER
       */
      return project.value.myProjectRole
        === 'MANAGER'
    }
  )


/**
 * 可以成为负责人的项目成员
 *
 * READ_ONLY排除
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
 * 加载页面
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
 * 加载任务
 */
async function loadTasks() {

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
}


/**
 * 按状态分类
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
 * 打开创建任务
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


  createForm.title = ''

  createForm.description = ''

  createForm.priority =
    'MEDIUM'

  createForm.startDate = ''

  createForm.deadline = ''

  createForm.assigneeIds = []


  /**
   * MEMBER前端也自动设置自己。
   *
   * 后端仍然会再强制校验，
   * 所以前端不是安全边界。
   */
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
 * 创建任务
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

    /**
     * MEMBER：
     *
     * 即使前端数据被修改，
     * 这里仍强制只传自己。
     */
    const assigneeIds =
      canAssignOthers.value

        ? createForm.assigneeIds

        : (
          currentUserId.value !== null
            ? [currentUserId.value]
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
          || undefined,

        priority:
          createForm.priority,

        startDate:
          createForm.startDate
          || undefined,

        deadline:
          createForm.deadline
          || undefined,

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
 * 进入任务
 */
function goTask(
  taskId: number
) {

  router.push(
    `/tasks/${taskId}`
  )
}


/**
 * 返回项目
 */
function goProject() {

  router.push(
    `/projects/${projectId}`
  )
}


/**
 * ==================================================
 * 项目成员身份
 *
 * 重点：
 *
 * 项目负责人优先于MANAGER
 * ==================================================
 */
function getMemberIdentity(
  member: ProjectMember
) {

  /**
   * 第一优先级：
   * project.owner_id对应的人
   */
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
 * 当前用户名称
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
 * 优先级中文
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


.filter-card {

  margin-bottom: 20px;

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
      minmax(250px, 1fr)
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


.task-card {

  margin-bottom: 12px;

  cursor: pointer;

}


.task-title {

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