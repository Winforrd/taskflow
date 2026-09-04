<template>

  <div class="team-detail-page">

    <!-- ================================================= -->
    <!-- 团队基本信息 -->
    <!-- ================================================= -->
    <el-card
      v-if="team"
      class="team-info-card"
    >

      <div class="header">

        <!-- 左侧信息 -->
        <div class="team-info">

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


        <!-- 右侧操作 -->
        <div class="header-actions">

          <el-button
            type="primary"
            plain
            @click="goProjects"
          >
            项目
          </el-button>


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


      <!-- ================================================= -->
      <!-- 团队详细信息 -->
      <!-- ================================================= -->
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


    <!-- ================================================= -->
    <!-- 团队标签 -->
    <!-- ================================================= -->
    <!--
      注意：

      TagManager不能放在.header-actions里面。

      因为TagManager自身就是一个el-card。

      如果放在按钮区域，会导致：
      1. 按钮区域被撑开
      2. 标签卡片变成狭窄侧栏
      3. 团队主体布局错位

      现在改成独立整行区域。
    -->
    <TagManager
      v-if="team"
      :scope-id="teamId"
      scope-type="TEAM"
      title="团队标签"
    />


    <!-- ================================================= -->
    <!-- 编辑团队 -->
    <!-- ================================================= -->
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


import TagManager
  from '@/components/TagManager.vue'


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


/**
 * =========================================================
 * 加载团队详情
 * =========================================================
 */
async function loadTeam() {

  const result =
    await getTeamDetailApi(
      teamId
    )


  team.value =
    result.data
}


/**
 * =========================================================
 * 打开编辑团队
 * =========================================================
 */
function openEdit() {

  if (!team.value) {

    return
  }


  editForm.name =
    team.value.name


  editForm.description =
    team.value.description
    || ''


  editVisible.value =
    true
}


/**
 * =========================================================
 * 保存团队
 * =========================================================
 */
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


  editVisible.value =
    false


  await loadTeam()
}


/**
 * =========================================================
 * 项目列表
 * =========================================================
 */
function goProjects() {

  router.push(
    `/teams/${teamId}/projects`
  )
}


/**
 * =========================================================
 * 成员管理
 * =========================================================
 */
function goMembers() {

  router.push(
    `/teams/${teamId}/members`
  )
}


/**
 * =========================================================
 * 团队角色中文
 * =========================================================
 */
function getRoleName(
  role: string
) {

  const map:
    Record<string, string> = {

      OWNER:
        '所有者',

      ADMIN:
        '管理员',

      MEMBER:
        '成员',

      READ_ONLY:
        '只读'

    }


  return map[role]
    || role
}

</script>


<style scoped>

.team-detail-page {

  width: 100%;

  display: flex;

  flex-direction: column;

  gap: 20px;

}


.team-info-card {

  width: 100%;

}


.header {

  display: flex;

  justify-content: space-between;

  align-items: flex-start;

  gap: 24px;

}


.team-info {

  min-width: 0;

  flex: 1;

}


.team-info h2 {

  margin:
    0 0 12px;

}


.team-info p {

  margin: 0;

  color: #909399;

  line-height: 1.7;

}


.header-actions {

  display: flex;

  align-items: center;

  flex-wrap: wrap;

  gap: 10px;

  flex-shrink: 0;

}


/*
 * Element Plus按钮自己存在margin-left。
 * 配合gap使用时清除，避免间距不统一。
 */
.header-actions :deep(.el-button + .el-button) {

  margin-left: 0;

}


@media (
  max-width: 900px
) {

  .header {

    flex-direction: column;

  }


  .header-actions {

    width: 100%;

  }

}

</style>