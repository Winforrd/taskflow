<template>

  <div class="member-page">

    <el-card>

      <!-- 页面头部 -->
      <template #header>

        <div class="page-header">

          <div>

            <div class="page-title">

              团队成员

            </div>


            <div
              v-if="team"
              class="team-name"
            >

              {{ team.name }}

            </div>

          </div>


          <div class="header-actions">

            <!-- 邀请成员 -->
            <el-button
              v-if="canCreateInvitation"
              type="primary"
              @click="openInvitationDialog"
            >

              邀请成员

            </el-button>


            <el-button
              @click="goBack"
            >

              返回团队

            </el-button>

          </div>

        </div>

      </template>


      <!-- 成员表格 -->
      <el-table
        v-loading="loading"
        :data="members"
        style="width: 100%"
      >

        <el-table-column
          label="成员"
          min-width="220"
        >

          <template #default="{ row }">

            <div class="member-user">

              <el-avatar
                :size="40"
                :src="row.avatar"
              >

                {{
                  getAvatarText(row)
                }}

              </el-avatar>


              <div class="member-info">

                <div class="nickname">

                  {{
                    row.nickname
                    || row.username
                  }}

                </div>


                <div class="username">

                  @{{ row.username }}

                </div>

              </div>

            </div>

          </template>

        </el-table-column>


        <el-table-column
          label="角色"
          width="160"
        >

          <template #default="{ row }">

            <el-tag>

              {{
                getRoleName(
                  row.roleType
                )
              }}

            </el-tag>

          </template>

        </el-table-column>


        <el-table-column
          label="加入时间"
          width="200"
        >

          <template #default="{ row }">

            {{
              formatTime(
                row.joinedTime
              )
            }}

          </template>

        </el-table-column>


        <el-table-column
          label="操作"
          width="300"
          fixed="right"
        >

          <template #default="{ row }">

            <div class="action-area">

              <!-- OWNER修改角色 -->
              <el-select
                v-if="
                  canChangeRole
                  &&
                  row.roleType !== 'OWNER'
                "
                :model-value="row.roleType"
                size="small"
                style="width: 115px"
                @change="
                  handleRoleChange(
                    row.userId,
                    $event
                  )
                "
              >

                <el-option
                  label="管理员"
                  value="ADMIN"
                />

                <el-option
                  label="普通成员"
                  value="MEMBER"
                />

                <el-option
                  label="只读成员"
                  value="READ_ONLY"
                />

              </el-select>


              <!-- 移除成员 -->
              <el-button
                v-if="
                  canRemoveMember
                  &&
                  row.roleType !== 'OWNER'
                  &&
                  row.userId !== currentUserId
                "
                link
                type="danger"
                @click="
                  handleRemove(row)
                "
              >

                移除

              </el-button>


              <span
                v-if="
                  row.roleType === 'OWNER'
                "
                class="owner-text"
              >

                团队所有者

              </span>

            </div>

          </template>

        </el-table-column>

      </el-table>


      <!-- 退出团队 -->
      <template
        v-if="
          team
          &&
          team.myRole !== 'OWNER'
        "
      >

        <el-divider />


        <div class="danger-zone">

          <div>

            <div class="danger-title">

              退出团队

            </div>


            <div class="danger-description">

              退出后将无法继续访问该团队的数据。

            </div>

          </div>


          <el-button
            type="danger"
            plain
            @click="handleQuit"
          >

            退出团队

          </el-button>

        </div>

      </template>

    </el-card>


    <!-- ====================== -->
    <!-- 邀请成员 Dialog -->
    <!-- ====================== -->

    <el-dialog
      v-model="invitationVisible"
      title="邀请成员"
      width="650px"
    >

      <!-- 创建邀请码 -->
      <el-form
        :model="invitationForm"
        label-width="120px"
      >

        <el-form-item
          label="加入后的角色"
        >

          <el-select
            v-model="
              invitationForm.defaultRole
            "
            style="width: 100%"
          >

            <el-option
              label="普通成员"
              value="MEMBER"
            />

            <el-option
              label="只读成员"
              value="READ_ONLY"
            />

          </el-select>

        </el-form-item>


        <el-form-item
          label="有效时间"
        >

          <el-select
            v-model="
              invitationForm.expiresHours
            "
            style="width: 100%"
          >

            <el-option
              label="1小时"
              :value="1"
            />

            <el-option
              label="12小时"
              :value="12"
            />

            <el-option
              label="24小时"
              :value="24"
            />

            <el-option
              label="3天"
              :value="72"
            />

            <el-option
              label="7天"
              :value="168"
            />

          </el-select>

        </el-form-item>


        <el-form-item
          label="最大使用次数"
        >

          <el-input-number
            v-model="
              invitationForm.maxUses
            "
            :min="1"
            :max="100"
          />

        </el-form-item>


        <el-form-item>

          <el-button
            type="primary"
            :loading="createInvitationLoading"
            @click="handleCreateInvitation"
          >

            生成邀请码

          </el-button>

        </el-form-item>

      </el-form>


      <!-- 新生成的邀请码 -->
      <template
        v-if="newInvitation"
      >

        <el-divider />


        <el-alert
          title="邀请码生成成功"
          type="success"
          :closable="false"
        />


        <div class="invitation-code-box">

          <span class="invitation-code">

            {{
              newInvitation.invitationCode
            }}

          </span>


          <el-button
            type="primary"
            link
            @click="
              copyInvitationCode(
                newInvitation.invitationCode
              )
            "
          >

            复制邀请码

          </el-button>

        </div>

      </template>


      <el-divider>
        已创建的邀请码
      </el-divider>


      <!-- 邀请码列表 -->
      <el-table
        :data="invitations"
        max-height="300"
      >

        <el-table-column
          prop="invitationCode"
          label="邀请码"
          min-width="150"
        />


        <el-table-column
          label="角色"
          width="100"
        >

          <template #default="{ row }">

            {{
              getRoleName(
                row.defaultRole
              )
            }}

          </template>

        </el-table-column>


        <el-table-column
          label="使用次数"
          width="110"
        >

          <template #default="{ row }">

            {{
              row.usedCount
            }}

            /

            {{
              row.maxUses
            }}

          </template>

        </el-table-column>


        <el-table-column
          label="状态"
          width="100"
        >

          <template #default="{ row }">

            <el-tag
              :type="
                getInvitationStatusType(row)
              "
            >

              {{
                getInvitationStatus(row)
              }}

            </el-tag>

          </template>

        </el-table-column>


        <el-table-column
          label="操作"
          width="140"
        >

          <template #default="{ row }">

            <el-button
              link
              @click="
                copyInvitationCode(
                  row.invitationCode
                )
              "
            >

              复制

            </el-button>


            <el-button
              v-if="row.status === 1"
              link
              type="danger"
              @click="
                handleDisableInvitation(row)
              "
            >

              禁用

            </el-button>

          </template>

        </el-table-column>

      </el-table>

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
  ElMessage,
  ElMessageBox
} from 'element-plus'

import {
  getTeamDetailApi
} from '@/api/team'

import {
  getTeamMembersApi,
  updateTeamMemberRoleApi,
  removeTeamMemberApi,
  quitTeamApi
} from '@/api/teamMember'

import {
  createTeamInvitationApi,
  disableInvitationApi,
  getTeamInvitationsApi
} from '@/api/teamInvitation'

import {
  useUserStore
} from '@/stores/user'

import type {
  TeamDetail,
  TeamInvitation,
  TeamMember
} from '@/types/team'


const route =
  useRoute()


const router =
  useRouter()


const userStore =
  useUserStore()


const teamId =
  Number(
    route.params.teamId
  )


const team =
  ref<TeamDetail | null>(
    null
  )


const members =
  ref<TeamMember[]>([])


const loading =
  ref(false)


/**
 * 当前用户
 */
const currentUserId =
  computed(
    () =>
      userStore.userInfo?.id
      ?? null
  )


/**
 * OWNER才能修改角色
 */
const canChangeRole =
  computed(
    () =>
      team.value?.myRole
      === 'OWNER'
  )


/**
 * OWNER / ADMIN可以移除成员
 */
const canRemoveMember =
  computed(
    () =>
      team.value?.myRole === 'OWNER'
      ||
      team.value?.myRole === 'ADMIN'
  )


/**
 * OWNER / ADMIN可以创建邀请码
 */
const canCreateInvitation =
  computed(
    () =>
      team.value?.myRole === 'OWNER'
      ||
      team.value?.myRole === 'ADMIN'
  )


/**
 * 邀请弹窗
 */
const invitationVisible =
  ref(false)


const createInvitationLoading =
  ref(false)


const invitations =
  ref<TeamInvitation[]>([])


const newInvitation =
  ref<TeamInvitation | null>(
    null
  )


const invitationForm =
  reactive({

    defaultRole:
      'MEMBER',

    expiresHours:
      24,

    maxUses:
      1

  })


onMounted(
  async () => {

    if (
      !userStore.userInfo
    ) {

      await userStore.getUserInfo()
    }


    await loadData()

  }
)


async function loadData() {

  loading.value = true


  try {

    const [
      teamResult,
      memberResult
    ] =
      await Promise.all([

        getTeamDetailApi(
          teamId
        ),

        getTeamMembersApi(
          teamId
        )

      ])


    team.value =
      teamResult.data


    members.value =
      memberResult.data

  }

  finally {

    loading.value =
      false

  }

}


/**
 * 打开邀请成员
 */
async function openInvitationDialog() {

  newInvitation.value =
    null


  invitationVisible.value =
    true


  await loadInvitations()
}


/**
 * 查询邀请码
 */
async function loadInvitations() {

  const result =
    await getTeamInvitationsApi(
      teamId
    )


  invitations.value =
    result.data
}


/**
 * 创建邀请码
 */
async function handleCreateInvitation() {

  createInvitationLoading.value =
    true


  try {

    const result =
      await createTeamInvitationApi(
        teamId,
        {
          defaultRole:
            invitationForm.defaultRole,

          expiresHours:
            invitationForm.expiresHours,

          maxUses:
            invitationForm.maxUses
        }
      )


    newInvitation.value =
      result.data


    ElMessage.success(
      '邀请码生成成功'
    )


    await loadInvitations()

  }

  finally {

    createInvitationLoading.value =
      false

  }

}


/**
 * 禁用邀请码
 */
async function handleDisableInvitation(
  invitation: TeamInvitation
) {

  try {

    await ElMessageBox.confirm(
      '确定要禁用该邀请码吗？',
      '提示',
      {
        type:
          'warning'
      }
    )

  }

  catch {

    return
  }


  await disableInvitationApi(
    teamId,
    invitation.id
  )


  ElMessage.success(
    '邀请码已禁用'
  )


  await loadInvitations()
}


/**
 * 复制邀请码
 */
async function copyInvitationCode(
  code: string
) {

  try {

    await navigator.clipboard
      .writeText(code)


    ElMessage.success(
      '邀请码已复制'
    )

  }

  catch {

    ElMessage.error(
      '复制失败，请手动复制'
    )

  }

}


/**
 * 修改角色
 */
async function handleRoleChange(
  userId: number,
  value: unknown
) {

  if (
    typeof value !== 'string'
  ) {

    return
  }


  await updateTeamMemberRoleApi(
    teamId,
    userId,
    value
  )


  ElMessage.success(
    '成员角色修改成功'
  )


  await loadData()
}


/**
 * 移除成员
 */
async function handleRemove(
  member: TeamMember
) {

  try {

    await ElMessageBox.confirm(
      `确定要移除 ${
        member.nickname
        || member.username
      } 吗？`,
      '移除成员',
      {
        type:
          'warning'
      }
    )

  }

  catch {

    return
  }


  await removeTeamMemberApi(
    teamId,
    member.userId
  )


  ElMessage.success(
    '成员已移除'
  )


  await loadData()
}


/**
 * 退出团队
 */
async function handleQuit() {

  try {

    await ElMessageBox.confirm(
      '确定退出当前团队吗？',
      '退出团队',
      {
        type:
          'warning'
      }
    )

  }

  catch {

    return
  }


  await quitTeamApi(
    teamId
  )


  ElMessage.success(
    '已退出团队'
  )


  router.push(
    '/teams'
  )
}


/**
 * 返回团队
 */
function goBack() {

  router.push(
    `/teams/${teamId}`
  )

}


/**
 * 头像文字
 */
function getAvatarText(
  member: TeamMember
) {

  return (
    member.nickname
    ||
    member.username
    ||
    'U'
  ).charAt(0)

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
 * 邀请码状态
 */
function getInvitationStatus(
  invitation: TeamInvitation
) {

  if (
    invitation.status !== 1
  ) {

    return '已禁用'
  }


  if (
    invitation.usedCount
    >=
    invitation.maxUses
  ) {

    return '已用完'
  }


  if (
    new Date(
      invitation.expiresAt
    ).getTime()
    <
    Date.now()
  ) {

    return '已过期'
  }


  return '有效'
}


/**
 * 状态Tag
 */
function getInvitationStatusType(
  invitation: TeamInvitation
):
  'success'
  | 'info'
  | 'warning'
  | 'danger' {

  const status =
    getInvitationStatus(
      invitation
    )


  if (
    status === '有效'
  ) {

    return 'success'
  }


  if (
    status === '已过期'
  ) {

    return 'warning'
  }


  return 'info'
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

.member-page {

  width: 100%;

}


.page-header {

  display: flex;

  justify-content:
    space-between;

  align-items:
    center;

}


.header-actions {

  display: flex;

  gap: 10px;

}


.page-title {

  font-size: 20px;

  font-weight: 600;

}


.team-name {

  margin-top: 5px;

  font-size: 13px;

  color: #909399;

}


.member-user {

  display: flex;

  align-items: center;

  gap: 12px;

}


.member-info {

  display: flex;

  flex-direction: column;

  gap: 3px;

}


.nickname {

  color: #303133;

}


.username {

  color: #909399;

  font-size: 12px;

}


.action-area {

  display: flex;

  align-items: center;

  gap: 12px;

}


.owner-text {

  font-size: 13px;

  color: #909399;

}


.danger-zone {

  display: flex;

  justify-content:
    space-between;

  align-items:
    center;

}


.danger-title {

  color: #f56c6c;

  font-weight: 500;

}


.danger-description {

  margin-top: 5px;

  color: #909399;

  font-size: 13px;

}


.invitation-code-box {

  margin-top: 16px;

  padding: 18px;

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  border-radius: 6px;

  background: #f5f7fa;

}


.invitation-code {

  font-family: monospace;

  font-size: 22px;

  font-weight: bold;

  letter-spacing: 2px;

}

</style>