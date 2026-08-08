<template>

  <el-container
    class="layout"
  >

    <!-- 左侧菜单 -->
    <el-aside
      width="220px"
      class="sidebar"
    >

      <div class="logo">

        TaskFlow

      </div>


      <el-menu
        router
        :default-active="$route.path"
      >

        <el-menu-item
          index="/dashboard"
        >

          工作台

        </el-menu-item>

      </el-menu>

    </el-aside>



    <el-container>

      <!-- 顶部 -->
      <el-header
        class="header"
      >

        <div>

          在线协同任务管理平台

        </div>


        <div
          class="user-area"
        >

          <span>

            {{
              userStore.userInfo?.nickname
              ||
              userStore.userInfo?.username
            }}

          </span>


          <el-button
            link
            type="danger"
            @click="handleLogout"
          >

            退出登录

          </el-button>

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
  onMounted
} from 'vue'


import {
  useRouter
} from 'vue-router'


import {
  useUserStore
} from '@/stores/user'


import {
  ElMessage
} from 'element-plus'



const router =
  useRouter()


const userStore =
  useUserStore()



onMounted(
  async () => {

    /**
     * 浏览器刷新以后
     * Pinia内存中的userInfo会丢失
     *
     * 但是localStorage里面Token还在
     *
     * 所以重新获取用户信息
     */
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

        router.push('/login')

      }

    }

  }
)



async function handleLogout() {

  await userStore.logout()


  ElMessage.success(
    '已退出登录'
  )


  router.push(
    '/login'
  )

}

</script>


<style scoped>

.layout {

  width: 100%;

  height: 100vh;

}


.sidebar {

  background: white;

  border-right:
    1px solid #ebeef5;

}


.logo {

  height: 64px;

  display: flex;

  align-items: center;

  justify-content: center;

  font-size: 24px;

  font-weight: bold;

  border-bottom:
    1px solid #ebeef5;

}


.header {

  background: white;

  border-bottom:
    1px solid #ebeef5;

  display: flex;

  align-items: center;

  justify-content:
    space-between;

}


.user-area {

  display: flex;

  align-items: center;

  gap: 20px;

}


.main {

  background:
    #f5f7fa;

}

</style>