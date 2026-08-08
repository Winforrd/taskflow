<template>

  <div class="register-page">

    <el-card
      class="register-card"
      shadow="always"
    >

      <div class="title">

        在线协同任务管理平台

      </div>


      <div class="subtitle">

        创建你的账号

      </div>


      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >

        <el-form-item
          label="用户名"
          prop="username"
        >

          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            clearable
          />

        </el-form-item>



        <el-form-item
          label="昵称"
          prop="nickname"
        >

          <el-input
            v-model="form.nickname"
            placeholder="请输入昵称"
            clearable
          />

        </el-form-item>



        <el-form-item
          label="邮箱"
          prop="email"
        >

          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
            clearable
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
          />

        </el-form-item>



        <el-form-item
          label="确认密码"
          prop="confirmPassword"
        >

          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />

        </el-form-item>



        <el-button
          type="primary"
          class="submit-button"
          :loading="loading"
          @click="handleRegister"
        >

          注册

        </el-button>

      </el-form>



      <div class="footer">

        已有账号？

        <el-link
          type="primary"
          @click="goLogin"
        >

          去登录

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
  registerApi
} from '@/api/auth'



const router =
  useRouter()


const formRef =
  ref<FormInstance>()


const loading =
  ref(false)



const form = reactive({

  username: '',

  nickname: '',

  email: '',

  password: '',

  confirmPassword: ''

})



const validateConfirmPassword = (
  rule: any,
  value: string,
  callback: any
) => {

  if (!value) {

    callback(
      new Error(
        '请再次输入密码'
      )
    )

  }

  else if (
    value !== form.password
  ) {

    callback(
      new Error(
        '两次输入的密码不一致'
      )
    )

  }

  else {

    callback()

  }

}



const rules: FormRules = {

  username: [

    {
      required: true,
      message: '请输入用户名',
      trigger: 'blur'
    },

    {
      min: 3,
      max: 20,
      message: '用户名长度为3-20个字符',
      trigger: 'blur'
    }

  ],


  nickname: [

    {
      required: true,
      message: '请输入昵称',
      trigger: 'blur'
    }

  ],


  email: [

    {
      type: 'email',
      message: '邮箱格式不正确',
      trigger: 'blur'
    }

  ],


  password: [

    {
      required: true,
      message: '请输入密码',
      trigger: 'blur'
    },

    {
      min: 6,
      max: 30,
      message: '密码长度为6-30个字符',
      trigger: 'blur'
    }

  ],


  confirmPassword: [

    {
      validator:
        validateConfirmPassword,

      trigger: 'blur'
    }

  ]

}



async function handleRegister() {

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

    await registerApi({

      username:
        form.username,

      nickname:
        form.nickname,

      email:
        form.email || undefined,

      password:
        form.password

    })


    ElMessage.success(
      '注册成功，请登录'
    )


    router.push(
      '/login'
    )

  }

  finally {

    loading.value = false

  }

}



function goLogin() {

  router.push('/login')

}

</script>


<style scoped>

.register-page {

  width: 100%;

  height: 100vh;

  display: flex;

  justify-content: center;

  align-items: center;

  background: #f5f7fa;

}


.register-card {

  width: 420px;

  padding: 12px 20px;

}


.title {

  font-size: 30px;

  font-weight: bold;

  text-align: center;

  margin-bottom: 8px;

}


.subtitle {

  text-align: center;

  color: #909399;

  margin-bottom: 25px;

}


.submit-button {

  width: 100%;

  margin-top: 5px;

}


.footer {

  margin-top: 20px;

  text-align: center;

  color: #606266;

}

</style>