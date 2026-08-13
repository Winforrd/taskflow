<template>

  <el-container
    class="layout"
  >

    <!-- 左侧菜单 -->
    <el-aside
      width="220px"
      class="sidebar"
    >

      <!-- Logo -->
      <div
        class="logo"
        @click="goDashboard"
      >

        TaskFlow

      </div>


      <!-- 左侧导航菜单 -->
      <el-menu
        router
        :default-active="activeMenu"
      >

        <!-- 工作台 -->
        <el-menu-item
          index="/dashboard"
        >

          工作台

        </el-menu-item>


        <!-- 我的团队 -->
        <el-menu-item
          index="/teams"
        >

          我的团队

        </el-menu-item>


        <!-- 个人中心 -->
        <el-menu-item
          index="/profile"
        >

          个人中心

        </el-menu-item>

      </el-menu>

    </el-aside>


    <!-- 右侧主体 -->
    <el-container>

      <!-- 顶部 -->
      <el-header
        class="header"
      >

        <!-- 左侧标题 -->
        <div
          class="header-title"
        >

          在线协同任务管理平台

        </div>


        <!-- 右侧用户区域 -->
        <div
          class="user-area"
        >

          <el-dropdown
            trigger="click"
            @command="handleCommand"
          >

            <div
              class="user-dropdown"
            >

              <!-- 用户头像 -->
              <el-avatar
                :size="34"
                :src="
                  userStore.userInfo?.avatar
                "
              >

                {{
                  getAvatarText()
                }}

              </el-avatar>


              <!-- 用户昵称 -->
              <span
                class="username"
              >

                {{
                  userStore.userInfo?.nickname
                  ||
                  userStore.userInfo?.username
                  ||
                  '用户'
                }}

              </span>


              <!-- 下拉箭头 -->
              <span
                class="arrow"
              >

                ▼

              </span>

            </div>


            <template #dropdown>

              <el-dropdown-menu>

                <!-- 个人中心 -->
                <el-dropdown-item
                  command="profile"
                >

                  个人中心

                </el-dropdown-item>


                <!-- 退出登录 -->
                <el-dropdown-item
                  divided
                  command="logout"
                >

                  退出登录

                </el-dropdown-item>

              </el-dropdown-menu>

            </template>

          </el-dropdown>

        </div>

      </el-header>


      <!-- 页面主体 -->
      <el-main
        class="main"
      >

        <router-view />

      </el-main>

    </el-container>

  </el-container>

</template>


<script setup lang="ts">

import {
  computed,
  onMounted
} from 'vue'

import {
  useRoute,
  useRouter
} from 'vue-router'

import {
  useUserStore
} from '@/stores/user'

import {
  ElMessage,
  ElMessageBox
} from 'element-plus'


const route =
  useRoute()


const router =
  useRouter()


const userStore =
  useUserStore()


/**
 * 当前左侧菜单高亮项
 *
 * /teams
 * /teams/1
 * /teams/1/members
 *
 * 都让“我的团队”保持高亮
 */
const activeMenu =
  computed(
    () => {

      if (
        route.path.startsWith(
          '/teams'
        )
      ) {

        return '/teams'
      }


      if (
        route.path.startsWith(
          '/profile'
        )
      ) {

        return '/profile'
      }


      return '/dashboard'

    }
  )


/**
 * 页面初始化
 *
 * 刷新浏览器后：
 * Pinia 中 userInfo 会丢失
 *
 * 但 localStorage 中 Token 仍然存在
 *
 * 所以重新获取用户信息
 */
onMounted(
  async () => {

    if (
      !userStore.userInfo
      &&
      userStore.token
    ) {

      try {

        await userStore.getUserInfo()

      }

      catch {

        userStore.clearLogin()


        router.push(
          '/login'
        )

      }

    }

  }
)


/**
 * 获取头像显示文字
 */
function getAvatarText() {

  const nickname =
    userStore.userInfo?.nickname


  if (nickname) {

    return nickname.charAt(0)

  }


  const username =
    userStore.userInfo?.username


  if (username) {

    return username.charAt(0)

  }


  return 'U'
}


/**
 * 点击 Logo
 */
function goDashboard() {

  router.push(
    '/dashboard'
  )

}


/**
 * 用户下拉菜单
 */
async function handleCommand(
  command: string
) {

  /**
   * 个人中心
   */
  if (
    command === 'profile'
  ) {

    router.push(
      '/profile'
    )

    return
  }


  /**
   * 退出登录
   */
  if (
    command === 'logout'
  ) {

    await handleLogout()

  }

}


/**
 * 退出登录
 */
async function handleLogout() {

  try {

    await ElMessageBox.confirm(
      '确定要退出当前账号吗？',
      '退出登录',
      {
        confirmButtonText:
          '确定',

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

    await userStore.logout()


    ElMessage.success(
      '已退出登录'
    )


    router.push(
      '/login'
    )

  }

  catch {

    /**
     * 即使后端退出接口异常
     * 也清理前端 Token
     */
    userStore.clearLogin()


    router.push(
      '/login'
    )

  }

}

</script>


<style scoped>

/**
 * 整体布局
 */
.layout {

  width: 100%;

  height: 100vh;

}


/**
 * 左侧菜单
 */
.sidebar {

  background: white;

  border-right:
    1px solid #ebeef5;

}


/**
 * Logo
 */
.logo {

  height: 64px;

  display: flex;

  align-items: center;

  justify-content: center;

  font-size: 24px;

  font-weight: bold;

  border-bottom:
    1px solid #ebeef5;

  cursor: pointer;

  user-select: none;

}


/**
 * 顶部栏
 */
.header {

  height: 64px;

  background: white;

  border-bottom:
    1px solid #ebeef5;

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  padding:
    0 24px;

}


/**
 * 系统标题
 */
.header-title {

  font-size: 18px;

  font-weight: 500;

  color: #303133;

}


/**
 * 用户区域
 */
.user-area {

  display: flex;

  align-items: center;

}


/**
 * 用户下拉区域
 */
.user-dropdown {

  display: flex;

  align-items: center;

  gap: 8px;

  cursor: pointer;

  padding:
    5px 8px;

  border-radius:
    6px;

  transition:
    background 0.2s;

}


/**
 * 鼠标移入
 */
.user-dropdown:hover {

  background:
    #f5f7fa;

}


/**
 * 用户名
 */
.username {

  font-size: 14px;

  color: #303133;

}


/**
 * 下拉箭头
 */
.arrow {

  font-size: 10px;

  color: #909399;

}


/**
 * 页面主体
 */
.main {

  background:
    #f5f7fa;

  padding:
    20px;

  overflow-y:
    auto;

}

</style>