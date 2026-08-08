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
  getProfileApi
} from '@/api/user'


import {
  getToken,
  setToken,
  removeToken
} from '@/utils/auth'


import type {
  LoginRequest,
  UserInfo
} from '@/types/user'



export const useUserStore =
defineStore(
  'user',
  () => {


    /**
     * Token
     */
    const token = ref<string | null>(
      getToken()
    )


    /**
     * 当前用户
     */
    const userInfo =
      ref<UserInfo | null>(null)



    /**
     * 登录
     */
    async function login(
      loginForm: LoginRequest
    ) {

      const result =
        await loginApi(loginForm)


      token.value =
        result.data.token


      setToken(
        result.data.token
      )


      return result.data

    }



    /**
     * 获取用户信息
     */
    async function getUserInfo() {

      const result =
        await getProfileApi()


      userInfo.value =
        result.data


      return result.data

    }



    /**
     * 退出登录
     */
    async function logout() {

      try {

        await logoutApi()

      }

      finally {

        token.value = null

        userInfo.value = null

        removeToken()

      }

    }



    /**
     * 本地清除登录状态
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

      logout,

      clearLogin

    }

  }
)