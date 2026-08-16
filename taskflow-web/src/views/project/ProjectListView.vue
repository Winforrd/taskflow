<template>

  <div class="project-page">

    <div class="page-header">

      <div>

        <h2>
          项目
        </h2>

        <p class="description">

          {{
            team?.name
            || '团队项目'
          }}

        </p>

      </div>


      <div>

        <el-button
          @click="goTeam"
        >
          返回团队
        </el-button>


        <el-button
          v-if="canCreateProject"
          type="primary"
          @click="openCreateDialog"
        >
          创建项目
        </el-button>

      </div>

    </div>


    <!-- 项目列表 -->
    <el-row
      v-if="projects.length > 0"
      :gutter="20"
    >

      <el-col
        v-for="project in projects"
        :key="project.id"
        :xs="24"
        :sm="12"
        :md="8"
      >

        <el-card
          class="project-card"
          shadow="hover"
          @click="goProject(project.id)"
        >

          <div class="project-top">

            <span class="project-code">

              {{ project.projectCode }}

            </span>


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

          </div>


          <h3>
            {{ project.name }}
          </h3>


          <p class="project-description">

            {{
              project.description
              || '暂无项目描述'
            }}

          </p>


          <div class="meta">

            <span>

              负责人：
              {{
                project.ownerName
                || '-'
              }}

            </span>


            <span>

              成员：
              {{ project.memberCount }}

            </span>

          </div>


          <div class="deadline">

            截止：
            {{
              project.deadline
              || '未设置'
            }}

          </div>

        </el-card>

      </el-col>

    </el-row>


    <el-empty
      v-else
      description="暂无项目"
    />


    <!-- 创建项目 -->
    <el-dialog
      v-model="createVisible"
      title="创建项目"
      width="550px"
    >

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >

        <el-form-item
          label="项目名称"
          prop="name"
        >

          <el-input
            v-model="form.name"
          />

        </el-form-item>


        <el-form-item
          label="项目负责人"
          prop="ownerId"
        >

          <el-select
            v-model="form.ownerId"
            style="width: 100%"
          >

            <el-option
              v-for="member in teamMembers"
              :key="member.userId"
              :label="
                member.nickname
                || member.username
              "
              :value="member.userId"
            />

          </el-select>

        </el-form-item>


        <el-form-item
          label="开始日期"
        >

          <el-date-picker
            v-model="form.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />

        </el-form-item>


        <el-form-item
          label="截止日期"
        >

          <el-date-picker
            v-model="form.deadline"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />

        </el-form-item>


        <el-form-item
          label="项目描述"
        >

          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
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

import type {
  FormInstance,
  FormRules
} from 'element-plus'

import {
  createProjectApi,
  getTeamProjectsApi
} from '@/api/project'

import {
  getTeamDetailApi
} from '@/api/team'

import {
  getTeamMembersApi
} from '@/api/teamMember'

import type {
  Project,
  ProjectStatus
} from '@/types/project'

import type {
  TeamDetail,
  TeamMember
} from '@/types/team'


const route =
  useRoute()

const router =
  useRouter()

const teamId =
  Number(
    route.params.teamId
  )


const team =
  ref<TeamDetail | null>(
    null
  )

const projects =
  ref<Project[]>([])

const teamMembers =
  ref<TeamMember[]>([])

const createVisible =
  ref(false)

const creating =
  ref(false)

const formRef =
  ref<FormInstance>()


const form =
  reactive({

    name: '',

    description: '',

    ownerId:
      undefined as number | undefined,

    startDate: '',

    deadline: ''

  })


const rules: FormRules = {

  name: [
    {
      required: true,
      message: '请输入项目名称',
      trigger: 'blur'
    }
  ],

  ownerId: [
    {
      required: true,
      message: '请选择项目负责人',
      trigger: 'change'
    }
  ]

}


const canCreateProject =
  computed(
    () =>
      team.value?.myRole === 'OWNER'
      ||
      team.value?.myRole === 'ADMIN'
  )


onMounted(
  async () => {

    await loadData()

  }
)


async function loadData() {

  const [
    teamResult,
    projectResult
  ] =
    await Promise.all([

      getTeamDetailApi(
        teamId
      ),

      getTeamProjectsApi(
        teamId
      )

    ])


  team.value =
    teamResult.data


  projects.value =
    projectResult.data
}


async function openCreateDialog() {

  form.name = ''

  form.description = ''

  form.ownerId = undefined

  form.startDate = ''

  form.deadline = ''


  const result =
    await getTeamMembersApi(
      teamId
    )


  teamMembers.value =
    result.data


  createVisible.value =
    true
}


async function handleCreate() {

  if (!formRef.value) {

    return
  }


  const valid =
    await formRef.value
      .validate()
      .catch(
        () => false
      )


  if (
    !valid
    ||
    !form.ownerId
  ) {

    return
  }


  creating.value =
    true


  try {

    const result =
      await createProjectApi(
        teamId,
        {
          name:
            form.name,

          description:
            form.description
            || undefined,

          ownerId:
            form.ownerId,

          startDate:
            form.startDate
            || undefined,

          deadline:
            form.deadline
            || undefined
        }
      )


    ElMessage.success(
      '项目创建成功'
    )


    createVisible.value =
      false


    router.push(
      `/projects/${result.data}`
    )

  }

  finally {

    creating.value =
      false
  }
}


function goProject(
  projectId: number
) {

  router.push(
    `/projects/${projectId}`
  )
}


function goTeam() {

  router.push(
    `/teams/${teamId}`
  )
}


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

</script>


<style scoped>

.page-header {

  display: flex;

  justify-content:
    space-between;

  align-items:
    center;

  margin-bottom: 20px;

}


.page-header h2 {

  margin: 0;

}


.description {

  margin-top: 6px;

  color: #909399;

}


.project-card {

  margin-bottom: 20px;

  cursor: pointer;

}


.project-top {

  display: flex;

  justify-content:
    space-between;

}


.project-code {

  color: #909399;

  font-size: 12px;

}


.project-description {

  min-height: 42px;

  color: #909399;

}


.meta {

  display: flex;

  justify-content:
    space-between;

  font-size: 13px;

  color: #606266;

}


.deadline {

  margin-top: 12px;

  font-size: 13px;

  color: #909399;

}

</style>