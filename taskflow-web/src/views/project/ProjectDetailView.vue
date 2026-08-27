<template>

  <div
    v-if="project"
    class="project-detail-page"
  >

    <el-card>

      <!-- 顶部 -->
      <div class="header">

        <div>

          <div class="code">

            {{ project.projectCode }}

          </div>


          <h2>

            {{ project.name }}

          </h2>


          <p>

            {{
              project.description
              || '暂无项目描述'
            }}

          </p>

        </div>


        <div class="header-actions">

          <!-- 返回项目列表 -->
          <el-button
            @click="goProjectList"
          >

            返回项目列表

          </el-button>

          <el-button
            type="primary"
            plain
            @click="goTasks"
          >
            任务看板
          </el-button>


          <!-- 所有人只要能看项目
               就可以查看成员 -->
          <el-button
            @click="goMembers"
          >

            项目成员

          </el-button>

          <el-button
              @click="
                router.push(
                  `/projects/${projectId}/statistics`
                )
              "
            >
              项目统计
            </el-button>


          <!--
            编辑项目：

            团队 OWNER / ADMIN
            项目负责人
            MANAGER
          -->
          <el-button
            v-if="
              project.canEdit
              &&
              project.status !== 'ARCHIVED'
            "
            type="primary"
            @click="openEdit"
          >

            编辑项目

          </el-button>


          <!--
            归档项目：

            团队 OWNER / ADMIN
            项目负责人

            MANAGER 看不到这个按钮
          -->
          <el-button
            v-if="
              project.canManageProject
              &&
              project.status !== 'ARCHIVED'
            "
            type="danger"
            plain
            @click="handleArchive"
          >

            归档项目

          </el-button>

        </div>

      </div>


      <el-divider />


      <!-- 项目详细信息 -->
      <el-descriptions
        :column="2"
        border
      >

        <el-descriptions-item
          label="项目负责人"
        >

          {{
            project.ownerName
            || '-'
          }}

        </el-descriptions-item>


        <el-descriptions-item
          label="项目状态"
        >

          <el-tag
            :type="
              getStatusType(
                project.status
              )
            "
          >

            {{
              getStatusName(
                project.status
              )
            }}

          </el-tag>

        </el-descriptions-item>


        <el-descriptions-item
          label="成员数量"
        >

          {{ project.memberCount }}

        </el-descriptions-item>


        <el-descriptions-item
          label="我的项目身份"
        >

          {{
            getMyIdentity()
          }}

        </el-descriptions-item>


        <el-descriptions-item
          label="开始日期"
        >

          {{
            project.startDate
            || '-'
          }}

        </el-descriptions-item>


        <el-descriptions-item
          label="截止日期"
        >

          {{
            project.deadline
            || '-'
          }}

        </el-descriptions-item>


        <el-descriptions-item
          label="创建时间"
        >

          {{
            formatTime(
              project.createdTime
            )
          }}

        </el-descriptions-item>

      </el-descriptions>

    </el-card>


    <!-- 编辑项目 -->
    <el-dialog
      v-model="editVisible"
      title="编辑项目"
      width="550px"
    >

      <el-form
        :model="editForm"
        label-width="90px"
      >

        <el-form-item
          label="项目名称"
        >

          <el-input
            v-model="editForm.name"
          />

        </el-form-item>


        <el-form-item
          label="项目状态"
        >

          <el-select
            v-model="editForm.status"
            style="width: 100%"
          >

            <el-option
              label="未开始"
              value="NOT_STARTED"
            />

            <el-option
              label="进行中"
              value="IN_PROGRESS"
            />

            <el-option
              label="已完成"
              value="COMPLETED"
            />

          </el-select>

        </el-form-item>


        <el-form-item
          label="开始日期"
        >

          <el-date-picker
            v-model="editForm.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />

        </el-form-item>


        <el-form-item
          label="截止日期"
        >

          <el-date-picker
            v-model="editForm.deadline"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />

        </el-form-item>


        <el-form-item
          label="项目描述"
        >

          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="4"
          />

        </el-form-item>

      </el-form>


      <template #footer>

        <el-button
          @click="editVisible = false"
        >

          取消

        </el-button>


        <el-button
          type="primary"
          @click="handleUpdate"
        >

          保存

        </el-button>

      </template>

    </el-dialog>

  </div>

</template>


<script setup lang="ts">

import {
  onMounted,
  reactive,
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
  archiveProjectApi,
  getProjectDetailApi,
  updateProjectApi
} from '@/api/project'

import {
  useUserStore
} from '@/stores/user'

import type {
  ProjectDetail,
  ProjectRole,
  ProjectStatus
} from '@/types/project'


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


const editVisible =
  ref(false)


const editForm =
  reactive({

    name: '',

    description: '',

    status:  'NOT_STARTED'  as ProjectStatus,

    startDate: '',

    deadline: ''

  })


onMounted(
  async () => {

    if (
      !userStore.userInfo
    ) {

      await userStore.getUserInfo()
    }


    await loadProject()

  }
)


/**
 * 加载项目详情
 */
async function loadProject() {

  const result =
    await getProjectDetailApi(
      projectId
    )


  project.value =
    result.data
}


/**
 * 打开编辑
 */
function openEdit() {

  if (
    !project.value
  ) {

    return
  }


  editForm.name =
    project.value.name


  editForm.description =
    project.value.description
    || ''


  editForm.status =
    project.value.status


  editForm.startDate =
    project.value.startDate
    || ''


  editForm.deadline =
    project.value.deadline
    || ''


  editVisible.value =
    true
}


/**
 * 保存修改
 */
async function handleUpdate() {

  if (
    !project.value
  ) {

    return
  }


  /**
   * 前端再次保护
   */
  if (
    !project.value.canEdit
  ) {

    ElMessage.error(
      '没有编辑项目的权限'
    )

    return
  }


  await updateProjectApi(
    projectId,
    {
      name:
        editForm.name,

      description:
        editForm.description
        || undefined,

      status:
        editForm.status,

      startDate:
        editForm.startDate
        || undefined,

      deadline:
        editForm.deadline
        || undefined
    }
  )


  ElMessage.success(
    '项目修改成功'
  )


  editVisible.value =
    false


  await loadProject()
}


/**
 * 归档项目
 */
async function handleArchive() {

  if (
    !project.value
  ) {

    return
  }


  /**
   * 前端再次校验
   *
   * MANAGER 会在这里被拦住
   */
  if (
    !project.value.canManageProject
  ) {

    ElMessage.error(
      '没有归档项目的权限'
    )

    return
  }


  try {

    await ElMessageBox.confirm(
      '项目归档后将进入只读状态，确定要归档吗？',
      '归档项目',
      {
        confirmButtonText:
          '确定归档',

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


  await archiveProjectApi(
    projectId
  )


  ElMessage.success(
    '项目已归档'
  )


  await loadProject()
}


/**
 * 项目成员页面
 */
function goMembers() {

  router.push(
    `/projects/${projectId}/members`
  )
}


/**
 * 返回项目列表
 */
function goProjectList() {

  if (
    !project.value
  ) {

    return
  }


  router.push(
    `/teams/${project.value.teamId}/projects`
  )
}

function goTasks() {

  router.push(
    `/projects/${projectId}/tasks`
  )
}


/**
 * 当前身份
 */
function getMyIdentity() {

  if (
    !project.value
  ) {

    return '-'
  }


  /**
   * 判断项目负责人
   *
   * 注意：
   * 负责人也是 MANAGER，
   * 所以必须先判断 ownerId
   */
  if (
    userStore.userInfo?.id ===
    project.value.ownerId
  ) {

    return '项目负责人'
  }


  /**
   * 团队 OWNER
   */
  if (
    project.value.myTeamRole
    === 'OWNER'
  ) {

    return '团队所有者'
  }


  /**
   * 团队 ADMIN
   */
  if (
    project.value.myTeamRole
    === 'ADMIN'
  ) {

    return '团队管理员'
  }


  if (
    project.value.myProjectRole
  ) {

    return getRoleName(
      project.value.myProjectRole
    )
  }


  return '-'
}


/**
 * 项目角色中文
 */
function getRoleName(
  role: ProjectRole
) {

  const map:
    Record<ProjectRole, string> = {

      MANAGER:
        '项目管理员',

      MEMBER:
        '普通成员',

      READ_ONLY:
        '只读成员'

    }


  return map[role]
}


/**
 * 状态中文
 */
function getStatusName(
  status: ProjectStatus
) {

  const map:
    Record<ProjectStatus, string> = {

      NOT_STARTED:
        '未开始',

      IN_PROGRESS:
        '进行中',

      COMPLETED:
        '已完成',

      ARCHIVED:
        '已归档'

    }


  return map[status]
}


/**
 * 状态 Tag
 */
function getStatusType(
  status: ProjectStatus
):
  'primary'
  | 'success'
  | 'warning'
  | 'info' {

  switch (status) {

    case 'IN_PROGRESS':

      return 'primary'


    case 'COMPLETED':

      return 'success'


    case 'ARCHIVED':

      return 'info'


    default:

      return 'warning'
  }
}


/**
 * 时间格式化
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

.project-detail-page {

  width: 100%;

}


.header {

  display: flex;

  justify-content:
    space-between;

  align-items:
    flex-start;

}


.header-actions {

  display: flex;

  gap: 10px;

}


.header h2 {

  margin:
    6px 0;

}


.header p {

  color: #909399;

}


.code {

  color: #909399;

  font-size: 13px;

}

</style>