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


          <el-button
            @click="goBack"
          >
            返回团队
          </el-button>

        </div>

      </template>


      <!-- 成员表格 -->
      <el-table
        v-loading="loading"
        :data="members"
        style="width: 100%"
      >

        <!-- 成员信息 -->
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


        <!-- 角色 -->
        <el-table-column
          label="角色"
          width="160"
        >

          <template #default="{ row }">

            <el-tag
              :type="getRoleTagType(row.roleType)"
            >

              {{
                getRoleName(row.roleType)
              }}

            </el-tag>

          </template>

        </el-table-column>


        <!-- 加入时间 -->
        <el-table-column
          label="加入时间"
          width="200"
        >

          <template #default="{ row }">

            {{
              formatTime(row.joinedTime)
            }}

          </template>

        </el-table-column>


        <!-- 操作 -->
        <el-table-column
          label="操作"
          width="300"
          fixed="right"
        >

          <template #default="{ row }">

            <div class="action-area">

              <!--
                只有 OWNER 可以修改角色。

                不允许通过普通角色修改接口
                修改 OWNER 本人的角色。
              -->
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


              <!--
                OWNER / ADMIN 都可以移除普通成员。

                但是：
                1. 不能移除 OWNER
                2. 不显示“移除自己”
              -->
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


              <!-- 所有者 -->
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


      <!-- 没有成员 -->
      <el-empty
        v-if="
          !loading
          &&
          members.length === 0
        "
        description="暂无团队成员"
      />


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
  getTeamDetailApi
} from '@/api/team'

import {
  getTeamMembersApi,
  updateTeamMemberRoleApi,
  removeTeamMemberApi,
  quitTeamApi
} from '@/api/teamMember'

import {
  useUserStore
} from '@/stores/user'

import type {
  TeamDetail,
  TeamMember
} from '@/types/team'


/**
 * 路由
 */
const route =
  useRoute()


const router =
  useRouter()


/**
 * 用户 Store
 */
const userStore =
  useUserStore()


/**
 * 当前团队 ID
 */
const teamId =
  Number(
    route.params.teamId
  )


/**
 * 团队详情
 */
const team =
  ref<TeamDetail | null>(
    null
  )


/**
 * 团队成员列表
 */
const members =
  ref<TeamMember[]>([])


/**
 * 页面加载状态
 */
const loading =
  ref(false)


/**
 * 当前登录用户 ID
 */
const currentUserId =
  computed(
    () =>
      userStore.userInfo?.id
      ?? null
  )


/**
 * 是否可以修改成员角色
 *
 * 当前后端规则：
 * 只有 OWNER 可以修改角色
 */
const canChangeRole =
  computed(
    () =>
      team.value?.myRole
      === 'OWNER'
  )


/**
 * 是否可以移除成员
 *
 * 当前后端规则：
 * OWNER 或 ADMIN 可以移除普通成员
 */
const canRemoveMember =
  computed(
    () =>
      team.value?.myRole === 'OWNER'
      ||
      team.value?.myRole === 'ADMIN'
  )


/**
 * 页面加载
 */
onMounted(
  async () => {

    /**
     * 如果刷新页面后
     * userInfo 丢失，
     * 重新获取当前登录用户
     */
    if (
      !userStore.userInfo
    ) {

      await userStore.getUserInfo()

    }


    await loadData()

  }
)


/**
 * 加载团队详情和成员列表
 */
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

    loading.value = false

  }

}


/**
 * 修改成员角色
 *
 * Element Plus 的 el-select
 * change 事件值在模板中不容易准确推断。
 *
 * 因此这里接收 unknown，
 * 然后在函数内部进行类型检查。
 *
 * 这样就不会出现：
 * 参数 value 隐式具有 any 类型
 */
async function handleRoleChange(
  userId: number,
  value: unknown
) {

  if (
    typeof value !== 'string'
  ) {

    ElMessage.error(
      '角色参数不正确'
    )

    return

  }


  /**
   * 再次限制允许的角色
   */
  const validRoles = [
    'ADMIN',
    'MEMBER',
    'READ_ONLY'
  ]


  if (
    !validRoles.includes(value)
  ) {

    ElMessage.error(
      '角色参数不正确'
    )

    return

  }


  try {

    await updateTeamMemberRoleApi(
      teamId,
      userId,
      value
    )


    ElMessage.success(
      '成员角色修改成功'
    )


    /**
     * 重新加载成员列表
     */
    await loadData()

  }

  catch {

    /**
     * request.ts
     * 已经统一展示错误消息。
     *
     * 这里无需再次提示。
     */

  }

}


/**
 * 移除成员
 */
async function handleRemove(
  member: TeamMember
) {

  try {

    await ElMessageBox.confirm(

      `确定要将 ${
        member.nickname
        || member.username
      } 移出团队吗？`,

      '移除成员',

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

    /**
     * 用户取消
     */
    return

  }


  try {

    await removeTeamMemberApi(
      teamId,
      member.userId
    )


    ElMessage.success(
      '成员已移除'
    )


    await loadData()

  }

  catch {

    /**
     * 错误由 Axios
     * 响应拦截器统一处理
     */

  }

}


/**
 * 当前用户主动退出团队
 */
async function handleQuit() {

  try {

    await ElMessageBox.confirm(
      '确定退出当前团队吗？退出后将无法继续访问该团队。',
      '退出团队',
      {
        confirmButtonText:
          '确定退出',

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


  try {

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

  catch {

    /**
     * 后端异常由
     * Axios统一处理
     */

  }

}


/**
 * 返回团队详情
 */
function goBack() {

  router.push(
    `/teams/${teamId}`
  )

}


/**
 * 获取头像文字
 */
function getAvatarText(
  member: TeamMember
) {

  if (
    member.nickname
  ) {

    return member.nickname
      .charAt(0)

  }


  if (
    member.username
  ) {

    return member.username
      .charAt(0)

  }


  return 'U'

}


/**
 * 角色中文名称
 */
function getRoleName(
  role: string
) {

  const roleMap:
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


  return roleMap[role]
    ?? role

}


/**
 * 角色标签样式
 */
function getRoleTagType(
  role: string
):
  'primary'
  | 'success'
  | 'warning'
  | 'danger'
  | 'info'
  | undefined {

  switch (role) {

    case 'OWNER':

      return 'danger'


    case 'ADMIN':

      return 'warning'


    case 'MEMBER':

      return 'primary'


    case 'READ_ONLY':

      return 'info'


    default:

      return undefined

  }

}


/**
 * 简单格式化时间
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


.page-title {

  font-size: 20px;

  font-weight: 600;

  color: #303133;

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

  font-size: 14px;

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

  align-items: center;

  justify-content:
    space-between;

}


.danger-title {

  color: #f56c6c;

  font-weight: 500;

}


.danger-description {

  margin-top: 5px;

  font-size: 13px;

  color: #909399;

}

</style>