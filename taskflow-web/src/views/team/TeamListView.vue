<template>

  <div class="team-page">

    <!-- 页面头部 -->
    <div class="page-header">

      <div>

        <h2>
          我的团队
        </h2>

        <p class="page-description">
          管理你创建或加入的团队空间
        </p>

      </div>


      <div class="header-actions">

        <!-- 加入团队 -->
        <el-button
          @click="openJoinDialog"
        >

          加入团队

        </el-button>


        <!-- 创建团队 -->
        <el-button
          type="primary"
          @click="openCreateDialog"
        >

          创建团队

        </el-button>

      </div>

    </div>


    <!-- 团队列表 -->
    <el-row
      v-if="teams.length > 0"
      :gutter="20"
    >

      <el-col
        v-for="team in teams"
        :key="team.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
      >

        <el-card
          class="team-card"
          shadow="hover"
          @click="goTeam(team.id)"
        >

          <div class="team-card-header">

            <el-avatar
              :size="42"
              :src="team.avatar"
            >

              {{
                team.name.charAt(0)
              }}

            </el-avatar>


            <div class="team-title">

              {{ team.name }}

            </div>

          </div>


          <p class="team-description">

            {{
              team.description
              || '暂无团队简介'
            }}

          </p>


          <div class="team-meta">

            <span>

              成员 {{ team.memberCount }}

            </span>


            <el-tag
              size="small"
            >

              {{
                getRoleName(
                  team.myRole
                )
              }}

            </el-tag>

          </div>

        </el-card>

      </el-col>

    </el-row>


    <!-- 空状态 -->
    <el-empty
      v-else
      description="还没有加入任何团队"
    />


    <!-- ===================== -->
    <!-- 创建团队 -->
    <!-- ===================== -->

    <el-dialog
      v-model="createVisible"
      title="创建团队"
      width="500px"
    >

      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="90px"
      >

        <el-form-item
          label="团队名称"
          prop="name"
        >

          <el-input
            v-model="createForm.name"
            placeholder="请输入团队名称"
            maxlength="100"
            show-word-limit
          />

        </el-form-item>


        <el-form-item
          label="团队简介"
          prop="description"
        >

          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="简单介绍一下团队"
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
          :loading="createLoading"
          @click="handleCreate"
        >

          创建

        </el-button>

      </template>

    </el-dialog>


    <!-- ===================== -->
    <!-- 加入团队 -->
    <!-- ===================== -->

    <el-dialog
      v-model="joinVisible"
      title="加入团队"
      width="500px"
    >

      <!-- 第一步：邀请码 -->
      <template
        v-if="!invitationPreview"
      >

        <el-form
          label-width="80px"
        >

          <el-form-item
            label="邀请码"
          >

            <el-input
              v-model="invitationCode"
              placeholder="请输入团队邀请码"
              maxlength="64"
              clearable
              @keyup.enter="handlePreviewInvitation"
            />

          </el-form-item>

        </el-form>


        <div class="join-tip">

          输入团队管理员提供的邀请码，
          验证后即可加入团队。

        </div>

      </template>


      <!-- 第二步：确认团队 -->
      <template
        v-else
      >

        <el-result
          icon="info"
          title="确认加入团队"
        >

          <template #sub-title>

            <div class="preview-info">

              <div class="preview-team-name">

                {{
                  invitationPreview.teamName
                }}

              </div>


              <div>

                {{
                  invitationPreview.teamDescription
                  || '暂无团队简介'
                }}

              </div>


              <div>

                加入后的角色：

                <strong>

                  {{
                    getRoleName(
                      invitationPreview.defaultRole
                    )
                  }}

                </strong>

              </div>


              <div>

                邀请码有效期至：

                {{
                  formatTime(
                    invitationPreview.expiresAt
                  )
                }}

              </div>

            </div>

          </template>

        </el-result>

      </template>


      <template #footer>

        <!-- 还未验证 -->
        <template
          v-if="!invitationPreview"
        >

          <el-button
            @click="joinVisible = false"
          >

            取消

          </el-button>


          <el-button
            type="primary"
            :loading="previewLoading"
            @click="handlePreviewInvitation"
          >

            验证邀请码

          </el-button>

        </template>


        <!-- 已验证 -->
        <template
          v-else
        >

          <el-button
            @click="resetInvitationPreview"
          >

            返回

          </el-button>


          <el-button
            type="primary"
            :loading="joinLoading"
            @click="handleAcceptInvitation"
          >

            确认加入

          </el-button>

        </template>

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

import {
  acceptInvitationApi,
  getInvitationPreviewApi
} from '@/api/teamInvitation'

import type {
  InvitationPreview,
  Team
} from '@/types/team'


const router =
  useRouter()


/**
 * 团队列表
 */
const teams =
  ref<Team[]>([])


/**
 * ============================
 * 创建团队
 * ============================
 */

const createVisible =
  ref(false)


const createLoading =
  ref(false)


const createFormRef =
  ref<FormInstance>()


const createForm =
  reactive({

    name: '',

    description: ''

  })


const createRules:
FormRules = {

  name: [

    {
      required: true,
      message: '请输入团队名称',
      trigger: 'blur'
    }

  ]

}


/**
 * ============================
 * 加入团队
 * ============================
 */

const joinVisible =
  ref(false)


const invitationCode =
  ref('')


const invitationPreview =
  ref<InvitationPreview | null>(
    null
  )


const previewLoading =
  ref(false)


const joinLoading =
  ref(false)


/**
 * 页面初始化
 */
onMounted(
  async () => {

    await loadTeams()

  }
)


/**
 * 查询我的团队
 */
async function loadTeams() {

  const result =
    await getMyTeamsApi()


  teams.value =
    result.data
}


/**
 * ============================
 * 创建团队
 * ============================
 */

function openCreateDialog() {

  createForm.name = ''

  createForm.description = ''

  createVisible.value = true
}


async function handleCreate() {

  if (
    !createFormRef.value
  ) {

    return
  }


  const valid =
    await createFormRef.value
      .validate()
      .catch(
        () => false
      )


  if (!valid) {

    return
  }


  createLoading.value = true


  try {

    const result =
      await createTeamApi({

        name:
          createForm.name,

        description:
          createForm.description
          || undefined

      })


    ElMessage.success(
      '团队创建成功'
    )


    createVisible.value =
      false


    await loadTeams()


    router.push(
      `/teams/${result.data}`
    )

  }

  finally {

    createLoading.value = false

  }

}


/**
 * ============================
 * 加入团队
 * ============================
 */

function openJoinDialog() {

  invitationCode.value = ''

  invitationPreview.value =
    null

  joinVisible.value = true
}


/**
 * 验证邀请码
 */
async function handlePreviewInvitation() {

  const code =
    invitationCode.value
      .trim()
      .toUpperCase()


  if (!code) {

    ElMessage.warning(
      '请输入邀请码'
    )

    return
  }


  previewLoading.value = true


  try {

    const result =
      await getInvitationPreviewApi(
        code
      )


    invitationCode.value =
      code


    invitationPreview.value =
      result.data

  }

  finally {

    previewLoading.value = false

  }

}


/**
 * 加入团队
 */
async function handleAcceptInvitation() {

  if (
    !invitationPreview.value
  ) {

    return
  }


  joinLoading.value = true


  try {

    const result =
      await acceptInvitationApi(
        invitationCode.value
      )


    ElMessage.success(
      '加入团队成功'
    )


    joinVisible.value = false


    /**
     * 更新我的团队
     */
    await loadTeams()


    /**
     * 进入新团队
     */
    router.push(
      `/teams/${result.data}`
    )

  }

  finally {

    joinLoading.value = false

  }

}


/**
 * 返回邀请码输入
 */
function resetInvitationPreview() {

  invitationPreview.value =
    null
}


/**
 * 进入团队详情
 */
function goTeam(
  teamId: number
) {

  router.push(
    `/teams/${teamId}`
  )

}


/**
 * 角色中文
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
        '普通成员',

      READ_ONLY:
        '只读成员'

    }


  return map[role]
    ?? role

}


/**
 * 格式化时间
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

.team-page {

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


.page-description {

  margin-top: 6px;

  margin-bottom: 0;

  color: #909399;

}


.header-actions {

  display: flex;

  gap: 10px;

}


.team-card {

  cursor: pointer;

  margin-bottom: 20px;

}


.team-card-header {

  display: flex;

  align-items: center;

  gap: 12px;

}


.team-title {

  font-size: 17px;

  font-weight: 600;

}


.team-description {

  height: 42px;

  margin:
    18px 0;

  color: #909399;

  overflow: hidden;

}


.team-meta {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  color: #909399;

  font-size: 13px;

}


.join-tip {

  padding-left: 80px;

  font-size: 13px;

  color: #909399;

}


.preview-info {

  display: flex;

  flex-direction: column;

  gap: 10px;

  color: #606266;

}


.preview-team-name {

  font-size: 18px;

  font-weight: 600;

  color: #303133;

}

</style>