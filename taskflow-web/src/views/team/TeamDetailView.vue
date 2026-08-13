<template>

  <div>

    <el-card
      v-if="team"
    >

      <div class="header">

        <div>

          <h2>
            {{ team.name }}
          </h2>

          <p>
            {{
              team.description
              || '暂无团队简介'
            }}
          </p>

        </div>

        <div>

          <el-button
            @click="goMembers"
          >
            成员管理
          </el-button>

          <el-button
            v-if="
              team.myRole === 'OWNER'
              ||
              team.myRole === 'ADMIN'
            "
            type="primary"
            @click="openEdit"
          >
            编辑团队
          </el-button>

        </div>

      </div>


      <el-divider />


      <el-descriptions
        :column="2"
      >

        <el-descriptions-item
          label="成员数量"
        >
          {{ team.memberCount }}
        </el-descriptions-item>

        <el-descriptions-item
          label="我的角色"
        >
          {{ getRoleName(team.myRole) }}
        </el-descriptions-item>

        <el-descriptions-item
          label="创建时间"
        >
          {{ team.createdTime }}
        </el-descriptions-item>

      </el-descriptions>

    </el-card>


    <el-dialog
      v-model="editVisible"
      title="编辑团队"
      width="500px"
    >

      <el-form
        :model="editForm"
        label-width="80px"
      >

        <el-form-item
          label="团队名称"
        >

          <el-input
            v-model="editForm.name"
          />

        </el-form-item>

        <el-form-item
          label="团队简介"
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
  ElMessage
} from 'element-plus'

import {
  getTeamDetailApi,
  updateTeamApi
} from '@/api/team'

import type {
  TeamDetail
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


const editVisible =
  ref(false)


const editForm =
  reactive({

    name: '',

    description: ''

  })


onMounted(
  async () => {

    await loadTeam()

  }
)


async function loadTeam() {

  const result =
    await getTeamDetailApi(
      teamId
    )

  team.value =
    result.data
}


function openEdit() {

  if (!team.value) {
    return
  }

  editForm.name =
    team.value.name

  editForm.description =
    team.value.description
    || ''

  editVisible.value = true
}


async function handleUpdate() {

  await updateTeamApi(
    teamId,
    {
      name:
        editForm.name,

      description:
        editForm.description
        || undefined
    }
  )

  ElMessage.success(
    '团队信息修改成功'
  )

  editVisible.value = false

  await loadTeam()
}


function goMembers() {

  router.push(
    `/teams/${teamId}/members`
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

.header {

  display: flex;

  justify-content: space-between;

  align-items: flex-start;

}


.header p {

  color: #909399;

}

</style>