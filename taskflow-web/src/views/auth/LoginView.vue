<template>

  <div class="login-page">

    <el-card
      class="login-card"
      shadow="always"
    >

      <div class="title">

        在线协同任务管理平台

      </div>



      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleLogin"
      >

        <el-form-item
          label="用户名"
          prop="username"
        >

          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            clearable
            size="large"
          />

        </el-form-item>



        <el-form-item
          label="密码"
          prop="password"
        >

          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            size="large"
          />

        </el-form-item>



        <el-button
          type="primary"
          size="large"
          class="login-button"
          :loading="loading"
          @click="handleLogin"
        >

          登录

        </el-button>

      </el-form>



      <div class="footer">

        还没有账号？

        <el-link
          type="primary"
          @click="goRegister"
        >

          立即注册

        </el-link>

      </div>

    </el-card>

  </div>

</template>


<script setup lang="ts">

import {
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


import type {
  FormInstance,
  FormRules
} from 'element-plus'


import {
  useUserStore
} from '@/stores/user'



const router =
  useRouter()


const route =
  useRoute()


const userStore =
  useUserStore()


const formRef =
  ref<FormInstance>()


const loading =
  ref(false)



const form = reactive({

  username: '',

  password: ''

})



const rules: FormRules = {

  username: [

    {
      required: true,

      message:
        '请输入用户名',

      trigger: 'blur'
    }

  ],


  password: [

    {
      required: true,

      message:
        '请输入密码',

      trigger: 'blur'
    },

    {
      min: 6,

      message:
        '密码至少6位',

      trigger: 'blur'
    }

  ]

}



async function handleLogin() {

  if (!formRef.value) {

    return

  }


  const valid =
    await formRef.value.validate()
      .catch(
        () => false
      )


  if (!valid) {

    return

  }


  loading.value = true


  try {

    await userStore.login({

      username:
        form.username,

      password:
        form.password

    })


    /**
     * 登录成功后
     * 获取当前用户
     */
    await userStore.getUserInfo()


    ElMessage.success(
      '登录成功'
    )


    /**
     * 如果之前因为未登录
     * 被重定向到登录页
     */
    const redirect = route.query.redirect as string


    router.push(
      redirect || '/dashboard'
    )

  }

  finally {

    loading.value = false

  }

}



function goRegister() {

  router.push(
    '/register'
  )

}

</script>


<style scoped>

.login-page {

  width: 100%;

  height: 100vh;

  display: flex;

  justify-content: center;

  align-items: center;

  background: #f5f7fa;

}


.login-card {

  width: 420px;

  padding: 20px;

}


.title {

  text-align: center;

  font-size: 32px;

  font-weight: bold;

  margin-bottom: 8px;

}


.subtitle {

  text-align: center;

  color: #909399;

  margin-bottom: 30px;

}


.login-button {

  width: 100%;

  margin-top: 8px;

}


.footer {

  margin-top: 22px;

  text-align: center;

  color: #606266;

}

</style>