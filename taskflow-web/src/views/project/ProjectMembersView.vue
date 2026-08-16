<template>

  <div
    v-if="project"
    class="project-members-page"
  >

    <el-card>

      <template #header>

        <div class="header">

          <div>

            <strong class="title">

              项目成员

            </strong>


            <span class="project-name">

              {{ project.name }}

            </span>

          </div>


          <div>

            <el-button
              @click="goBack"
            >

              返回项目

            </el-button>


            <!--
              只有：
              团队 OWNER / ADMIN
              项目负责人

              才能添加成员。

              MANAGER 看不到。
            -->
            <el-button
              v-if="
                project.canManageProject
                &&
                project.status !== 'ARCHIVED'
              "
              type="primary"
              @click="openAddDialog"
            >

              添加成员

            </el-button>

          </div>

        </div>

      </template>


      <el-table
        :data="members"
        style="width: 100%"
      >

        <!-- 成员 -->
        <el-table-column
          label="成员"
          min-width="220"
        >

          <template #default="{ row }">

            <div class="member">

              <el-avatar
                :size="38"
                :src="row.avatar"
              >

                {{
                  getAvatarText(row)
                }}

              </el-avatar>


              <div>

                <div class="nickname">

                  {{
                    row.nickname
                    ||
                    row.username
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
          label="项目角色"
          width="190"
        >

          <template #default="{ row }">

            <!-- 项目负责人 -->
            <el-tag
              v-if="row.projectOwner"
              type="danger"
            >

              项目负责人

            </el-tag>


            <!-- 可以管理成员 -->
            <el-select
              v-else-if="
                project.canManageProject
                &&
                project.status !== 'ARCHIVED'
              "
              :model-value="row.roleType"
              size="small"
              style="width: 125px"
              @change="
                handleRoleChange(
                  row.userId,
                  $event
                )
              "
            >

              <el-option
                label="项目管理员"
                value="MANAGER"
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


            <!-- 没管理权限只显示文本 -->
            <el-tag
              v-else
            >

              {{
                getRoleName(
                  row.roleType
                )
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
              formatTime(
                row.joinedTime
              )
            }}

          </template>

        </el-table-column>


        <!-- 操作 -->
        <el-table-column
          label="操作"
          width="120"
        >

          <template #default="{ row }">

            <el-button
              v-if="
                project.canManageProject
                &&
                !row.projectOwner
                &&
                project.status !== 'ARCHIVED'
              "
              link
              type="danger"
              @click="
                handleRemove(row)
              "
            >

              移除

            </el-button>

          </template>

        </el-table-column>

      </el-table>

    </el-card>


    <!-- 添加项目成员 -->
    <el-dialog
      v-model="addVisible"
      title="添加项目成员"
      width="500px"
    >

      <el-form
        :model="addForm"
        label-width="90px"
      >

        <el-form-item
          label="团队成员"
        >

          <el-select
            v-model="addForm.userId"
            filterable
            placeholder="请选择团队成员"
            style="width: 100%"
          >

            <el-option
              v-for="member in availableMembers"
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
          label="项目角色"
        >

          <el-select
            v-model="addForm.roleType"
            style="width: 100%"
          >

            <el-option
              label="项目管理员"
              value="MANAGER"
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

        </el-form-item>

      </el-form>


      <template #footer>

        <el-button
          @click="addVisible = false"
        >

          取消

        </el-button>


        <el-button
          type="primary"
          @click="handleAdd"
        >

          添加

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
  ElMessage,
  ElMessageBox
} from 'element-plus'

import {
  getProjectDetailApi
} from '@/api/project'

import {
  addProjectMemberApi,
  getProjectMembersApi,
  removeProjectMemberApi,
  updateProjectMemberRoleApi
} from '@/api/projectMember'

import {
  getTeamMembersApi
} from '@/api/teamMember'

import type {
  ProjectDetail,
  ProjectMember,
  ProjectRole
} from '@/types/project'

import type {
  TeamMember
} from '@/types/team'


const route =
  useRoute()


const router =
  useRouter()


const projectId =
  Number(
    route.params.projectId
  )


const project =
  ref<ProjectDetail | null>(
    null
  )


const members =
  ref<ProjectMember[]>([])


const teamMembers =
  ref<TeamMember[]>([])


const addVisible =
  ref(false)


const addForm =
  reactive({

    userId:
      undefined as
        number
        | undefined,

    roleType:   'MEMBER'   as ProjectRole

  })


/**
 * 还没有加入当前项目的团队成员
 */
const availableMembers =
  computed(
    () => {

      const projectMemberIds =
        new Set(

          members.value.map(
            member =>
              member.userId
          )

        )


      return teamMembers.value.filter(
        member =>
          !projectMemberIds.has(
            member.userId
          )
      )
    }
  )


onMounted(
  async () => {

    await loadData()

  }
)


/**
 * 加载数据
 */
async function loadData() {

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


  members.value =
    memberResult.data
}


/**
 * 打开添加成员
 */
async function openAddDialog() {

  if (
    !project.value
  ) {

    return
  }


  /**
   * 前端权限保护
   */
  if (
    !project.value.canManageProject
  ) {

    ElMessage.error(
      '没有管理项目成员的权限'
    )

    return
  }


  const result =
    await getTeamMembersApi(
      project.value.teamId
    )


  teamMembers.value =
    result.data


  addForm.userId =
    undefined


  addForm.roleType =
    'MEMBER'


  addVisible.value =
    true
}


/**
 * 添加成员
 */
async function handleAdd() {

  if (
    !project.value
  ) {

    return
  }


  if (
    !project.value.canManageProject
  ) {

    ElMessage.error(
      '没有管理项目成员的权限'
    )

    return
  }


  if (
    !addForm.userId
  ) {

    ElMessage.warning(
      '请选择团队成员'
    )

    return
  }


  await addProjectMemberApi(
    projectId,
    {
      userId:
        addForm.userId,

      roleType:
        addForm.roleType
    }
  )


  ElMessage.success(
    '项目成员添加成功'
  )


  addVisible.value =
    false


  await loadData()
}


/**
 * 修改项目成员角色
 */
async function handleRoleChange(
  userId: number,
  value: unknown
) {

  if (
    !project.value
    ||
    !project.value.canManageProject
  ) {

    ElMessage.error(
      '没有修改项目成员角色的权限'
    )

    return
  }


  if (
    typeof value !== 'string'
  ) {

    return
  }


  const validRoles:
    ProjectRole[] = [

      'MANAGER',

      'MEMBER',

      'READ_ONLY'

    ]


  if (
    !validRoles.includes(
      value as ProjectRole
    )
  ) {

    return
  }


  await updateProjectMemberRoleApi(
    projectId,
    userId,
    value as ProjectRole
  )


  ElMessage.success(
    '项目成员角色修改成功'
  )


  await loadData()
}


/**
 * 移除成员
 */
async function handleRemove(
  member: ProjectMember
) {

  if (
    !project.value
    ||
    !project.value.canManageProject
  ) {

    ElMessage.error(
      '没有移除项目成员的权限'
    )

    return
  }


  try {

    await ElMessageBox.confirm(
      `确定要移除 ${
        member.nickname
        || member.username
      } 吗？`,
      '移除项目成员',
      {
        type:
          'warning'
      }
    )

  }

  catch {

    return
  }


  await removeProjectMemberApi(
    projectId,
    member.userId
  )


  ElMessage.success(
    '项目成员已移除'
  )


  await loadData()
}


/**
 * 返回项目
 */
function goBack() {

  router.push(
    `/projects/${projectId}`
  )
}


/**
 * 用户头像文字
 */
function getAvatarText(
  member: ProjectMember
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

.project-members-page {

  width: 100%;

}


.header {

  display: flex;

  justify-content:
    space-between;

  align-items:
    center;

}


.title {

  font-size: 18px;

}


.project-name {

  margin-left: 12px;

  color: #909399;

}


.member {

  display: flex;

  align-items: center;

  gap: 10px;

}


.nickname {

  color: #303133;

}


.username {

  margin-top: 3px;

  font-size: 12px;

  color: #909399;

}

</style>