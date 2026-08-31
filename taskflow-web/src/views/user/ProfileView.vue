<template>

  <div class="profile-page">

    <AvatarUploadCard />

    <el-row
      :gutter="20"
    >

      <!-- 左侧用户信息 -->
      <el-col
        :span="8"
      >

        <el-card>

          <div
            class="user-card"
          >

            <el-avatar
              :size="90"
              :src="userStore.userInfo?.avatar"
            >

              {{
                userStore.userInfo?.nickname
                  ?.charAt(0)
                ||
                userStore.userInfo?.username
                  ?.charAt(0)
              }}

            </el-avatar>


            <h2>

              {{
                userStore.userInfo?.nickname
              }}

            </h2>


            <p>

              @{{ userStore.userInfo?.username }}

            </p>


            <p class="email">

              {{
                userStore.userInfo?.email
                ||
                '暂未设置邮箱'
              }}

            </p>

          </div>

        </el-card>

      </el-col>



      <!-- 右侧 -->
      <el-col
        :span="16"
      >

        <el-card>

          <el-tabs
            v-model="activeTab"
          >

            <!-- 个人资料 -->
            <el-tab-pane
              label="个人资料"
              name="profile"
            >

              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="90px"
              >

                <el-form-item
                  label="用户名"
                >

                  <el-input
                    :model-value="
                      userStore.userInfo?.username
                    "
                    disabled
                  />

                </el-form-item>


                <el-form-item
                  label="昵称"
                  prop="nickname"
                >

                  <el-input
                    v-model="
                      profileForm.nickname
                    "
                    placeholder="请输入昵称"
                  />

                </el-form-item>


                <el-form-item
                  label="邮箱"
                  prop="email"
                >

                  <el-input
                    v-model="
                      profileForm.email
                    "
                    placeholder="请输入邮箱"
                  />

                </el-form-item>


                <el-form-item
                  label="手机号"
                  prop="phone"
                >

                  <el-input
                    v-model="
                      profileForm.phone
                    "
                    placeholder="请输入手机号"
                  />

                </el-form-item>


                <el-form-item>

                  <el-button
                    type="primary"
                    :loading="
                      profileLoading
                    "
                    @click="
                      handleUpdateProfile
                    "
                  >

                    保存修改

                  </el-button>

                </el-form-item>

              </el-form>

            </el-tab-pane>



            <!-- 修改密码 -->
            <el-tab-pane
              label="修改密码"
              name="password"
            >

              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
              >

                <el-form-item
                  label="原密码"
                  prop="oldPassword"
                >

                  <el-input
                    v-model="
                      passwordForm.oldPassword
                    "
                    type="password"
                    show-password
                  />

                </el-form-item>


                <el-form-item
                  label="新密码"
                  prop="newPassword"
                >

                  <el-input
                    v-model="
                      passwordForm.newPassword
                    "
                    type="password"
                    show-password
                  />

                </el-form-item>


                <el-form-item
                  label="确认密码"
                  prop="confirmPassword"
                >

                  <el-input
                    v-model="
                      passwordForm.confirmPassword
                    "
                    type="password"
                    show-password
                  />

                </el-form-item>


                <el-form-item>

                  <el-button
                    type="primary"
                    :loading="
                      passwordLoading
                    "
                    @click="
                      handleChangePassword
                    "
                  >

                    修改密码

                  </el-button>

                </el-form-item>

              </el-form>

            </el-tab-pane>

          </el-tabs>

        </el-card>

      </el-col>

    </el-row>

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
  ElMessage,
  ElMessageBox
} from 'element-plus'

import type {
  FormInstance,
  FormRules
} from 'element-plus'

import {
  useUserStore
} from '@/stores/user'

import {
  changePasswordApi
} from '@/api/user'

import AvatarUploadCard
  from '@/components/AvatarUploadCard.vue'


const router =
  useRouter()


const userStore =
  useUserStore()


const activeTab =
  ref('profile')


const profileFormRef =
  ref<FormInstance>()


const passwordFormRef =
  ref<FormInstance>()


const profileLoading =
  ref(false)


const passwordLoading =
  ref(false)



/**
 * 个人资料表单
 */
const profileForm =
  reactive({

    nickname: '',

    email: '',

    phone: ''

  })



/**
 * 密码表单
 */
const passwordForm =
  reactive({

    oldPassword: '',

    newPassword: '',

    confirmPassword: ''

  })



const profileRules:
FormRules = {

  nickname: [

    {
      required: true,
      message: '请输入昵称',
      trigger: 'blur'
    },

    {
      max: 50,
      message: '昵称不能超过50个字符',
      trigger: 'blur'
    }

  ],


  email: [

    {
      type: 'email',
      message: '邮箱格式不正确',
      trigger: 'blur'
    }

  ]

}



const validateConfirmPassword = (
  rule: any,
  value: string,
  callback: any
) => {

  if (!value) {

    callback(
      new Error(
        '请输入确认密码'
      )
    )

    return
  }


  if (
    value !==
    passwordForm.newPassword
  ) {

    callback(
      new Error(
        '两次输入的密码不一致'
      )
    )

    return
  }


  callback()
}



const passwordRules:
FormRules = {

  oldPassword: [

    {
      required: true,
      message: '请输入原密码',
      trigger: 'blur'
    }

  ],


  newPassword: [

    {
      required: true,
      message: '请输入新密码',
      trigger: 'blur'
    },

    {
      min: 6,
      max: 30,
      message: '密码长度必须为6-30位',
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



/**
 * 页面初始化
 */
onMounted(
  async () => {

    if (
      !userStore.userInfo
    ) {

      await userStore.getUserInfo()
    }


    fillProfileForm()

  }
)



/**
 * 用户信息填充表单
 */
function fillProfileForm() {

  profileForm.nickname =
    userStore.userInfo?.nickname
    || ''

  profileForm.email =
    userStore.userInfo?.email
    || ''

  profileForm.phone =
    userStore.userInfo?.phone
    || ''
}



/**
 * 修改资料
 */
async function handleUpdateProfile() {

  if (
    !profileFormRef.value
  ) {

    return
  }


  const valid =
    await profileFormRef.value
      .validate()
      .catch(
        () => false
      )


  if (!valid) {

    return
  }


  profileLoading.value = true


  try {

    await userStore.updateProfile({

      nickname:
        profileForm.nickname,

      email:
        profileForm.email
        || undefined,

      phone:
        profileForm.phone
        || undefined

    })


    ElMessage.success(
      '个人资料修改成功'
    )


    fillProfileForm()

  }

  finally {

    profileLoading.value = false

  }

}



/**
 * 修改密码
 */
async function handleChangePassword() {

  if (
    !passwordFormRef.value
  ) {

    return
  }


  const valid =
    await passwordFormRef.value
      .validate()
      .catch(
        () => false
      )


  if (!valid) {

    return
  }


  await ElMessageBox.confirm(
    '修改密码后需要重新登录，确定继续吗？',
    '提示',
    {
      type: 'warning'
    }
  )


  passwordLoading.value = true


  try {

    await changePasswordApi({

      oldPassword:
        passwordForm.oldPassword,

      newPassword:
        passwordForm.newPassword

    })


    ElMessage.success(
      '密码修改成功，请重新登录'
    )


    /**
     * 后端已经StpUtil.logout()
     *
     * 前端也清除Token
     */
    userStore.clearLogin()


    router.push(
      '/login'
    )

  }

  finally {

    passwordLoading.value = false

  }

}

</script>


<style scoped>

.profile-page {

  padding: 10px;

}


.user-card {

  display: flex;

  flex-direction: column;

  align-items: center;

  padding: 30px 10px;

}


.user-card h2 {

  margin-top: 20px;

  margin-bottom: 5px;

}


.user-card p {

  color: #909399;

  margin: 5px 0;

}


.email {

  font-size: 14px;

}

</style>