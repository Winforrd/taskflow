<template>

  <div>

    <el-card>

      <h2>

        欢迎使用 TaskFlow

      </h2>


      <p>

        当前用户：
        {{
          userStore.userInfo?.nickname
          ||
          userStore.userInfo?.username
        }}

      </p>


      <p>

        在线协同任务管理平台

      </p>

    </el-card>

  </div>

</template>


<script setup lang="ts">

import {
  useUserStore
} from '@/stores/user'


const userStore =
  useUserStore()

</script>