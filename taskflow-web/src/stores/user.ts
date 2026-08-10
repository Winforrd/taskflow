import {
  defineStore
} from 'pinia'

import {
  ref
} from 'vue'

import {
  loginApi,
  logoutApi
} from '@/api/auth'

import {
  getProfileApi,
  updateProfileApi
} from '@/api/user'

import {
  getToken,
  setToken,
  removeToken
} from '@/utils/auth'

import type {
  LoginRequest,
  UserInfo,
  UpdateProfileRequest
} from '@/types/user'


export const useUserStore =
defineStore(
  'user',
  () => {

    const token =
      ref<string | null>(
        getToken()
      )

    const userInfo =
      ref<UserInfo | null>(
        null
      )


    /**
     * 登录
     */
    async function login(
      form: LoginRequest
    ) {

      const result =
        await loginApi(form)

      token.value =
        result.data.token

      setToken(
        result.data.token
      )

      return result.data
    }


    /**
     * 获取当前用户
     */
    async function getUserInfo() {

      const result =
        await getProfileApi()

      userInfo.value =
        result.data

      return result.data
    }


    /**
     * 修改用户资料
     */
    async function updateProfile(
      form: UpdateProfileRequest
    ) {

      await updateProfileApi(
        form
      )

      /*
       * 修改成功后
       * 重新获取最新用户信息
       */
      await getUserInfo()
    }


    /**
     * 退出登录
     */
    async function logout() {

      try {

        await logoutApi()

      }

      finally {

        clearLogin()

      }
    }


    /**
     * 清除本地登录状态
     */
    function clearLogin() {

      token.value = null

      userInfo.value = null

      removeToken()
    }


    return {

      token,

      userInfo,

      login,

      getUserInfo,

      updateProfile,

      logout,

      clearLogin
    }

  }
)