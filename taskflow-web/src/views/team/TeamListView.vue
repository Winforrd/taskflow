<template>

  <div>

    <div class="page-header">

      <h2>
        我的团队
      </h2>

      <el-button
        type="primary"
        @click="openCreateDialog"
      >
        创建团队
      </el-button>

    </div>


    <el-row
      :gutter="20"
    >

      <el-col
        v-for="team in teams"
        :key="team.id"
        :span="6"
      >

        <el-card
          class="team-card"
          shadow="hover"
          @click="goTeam(team.id)"
        >

          <h3>
            {{ team.name }}
          </h3>

          <p>
            {{
              team.description
              || '暂无简介'
            }}
          </p>

          <div class="team-meta">

            <span>
              成员 {{ team.memberCount }}
            </span>

            <span>
              {{ getRoleName(team.myRole) }}
            </span>

          </div>

        </el-card>

      </el-col>

    </el-row>


    <el-empty
      v-if="teams.length === 0"
      description="还没有团队"
    />


    <el-dialog
      v-model="dialogVisible"
      title="创建团队"
      width="500px"
    >

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >

        <el-form-item
          label="团队名称"
          prop="name"
        >

          <el-input
            v-model="form.name"
          />

        </el-form-item>

        <el-form-item
          label="团队简介"
          prop="description"
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
          @click="dialogVisible = false"
        >
          取消
        </el-button>

        <el-button
          type="primary"
          :loading="loading"
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
  onMounted,
  reactive,
  ref
} from 'vue'

import {
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
  createTeamApi,
  getMyTeamsApi
} from '@/api/team'

import type {
  Team
} from '@/types/team'


const router =
  useRouter()


const teams =
  ref<Team[]>([])


const dialogVisible =
  ref(false)


const loading =
  ref(false)


const formRef =
  ref<FormInstance>()


const form =
  reactive({

    name: '',

    description: ''

  })


const rules: FormRules = {

  name: [

    {
      required: true,
      message: '请输入团队名称',
      trigger: 'blur'
    }

  ]

}


onMounted(
  async () => {

    await loadTeams()

  }
)


async function loadTeams() {

  const result =
    await getMyTeamsApi()

  teams.value =
    result.data
}


function openCreateDialog() {

  form.name = ''

  form.description = ''

  dialogVisible.value = true
}


async function handleCreate() {

  if (!formRef.value) {
    return
  }

  const valid =
    await formRef.value
      .validate()
      .catch(() => false)

  if (!valid) {
    return
  }

  loading.value = true

  try {

    const result =
      await createTeamApi({

        name:
          form.name,

        description:
          form.description
          || undefined

      })

    ElMessage.success(
      '团队创建成功'
    )

    dialogVisible.value = false

    await loadTeams()

    router.push(
      `/teams/${result.data}`
    )

  }

  finally {

    loading.value = false
  }
}


function goTeam(
  teamId: number
) {

  router.push(
    `/teams/${teamId}`
  )
}


function getRoleName(
  role: string
) {

  const map:
    Record<string, string> = {

      OWNER: '所有者',

      ADMIN: '管理员',

      MEMBER: '成员',

      READ_ONLY: '只读'

    }

  return map[role]
    || role
}

</script>


<style scoped>

.page-header {

  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-bottom: 20px;

}


.team-card {

  cursor: pointer;

  margin-bottom: 20px;

}


.team-card p {

  min-height: 40px;

  color: #909399;

}


.team-meta {

  display: flex;

  justify-content: space-between;

  color: #909399;

  font-size: 13px;

}

</style>