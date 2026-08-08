import axios from 'axios'
import type {
  AxiosInstance,
  AxiosResponse,
  InternalAxiosRequestConfig
} from 'axios'

import { ElMessage } from 'element-plus'

import {
  getToken,
  removeToken
} from './auth'


const request: AxiosInstance = axios.create({

  baseURL: import.meta.env.VITE_API_BASE_URL,

  timeout: 10000

})



/**
 * 请求拦截器
 */
request.interceptors.request.use(

  (config: InternalAxiosRequestConfig) => {

    const token = getToken()


    if (token) {

      config.headers['taskflow-token'] = token

    }


    return config

  },

  (error) => {

    return Promise.reject(error)

  }

)



/**
 * 响应拦截器
 */
request.interceptors.response.use(

  (response: AxiosResponse) => {

    const result =
      response.data


    if (
      result.code === 200
    ) {

      return result

    }


    /**
     * Sa-Token未登录
     */
    if (
      result.code === 401
    ) {

      removeToken()


      ElMessage.error(
        '登录状态已失效，请重新登录'
      )


      window.location.href =
        '/login'


      return Promise.reject(
        new Error(
          result.message
        )
      )

    }


    ElMessage.error(
      result.message
      ||
      '请求失败'
    )


    return Promise.reject(
      new Error(
        result.message
        ||
        '请求失败'
      )
    )

  },


  (error) => {

    ElMessage.error(
      error.response
        ?.data
        ?.message
      ||
      error.message
      ||
      '网络异常'
    )


    return Promise.reject(
      error
    )

  }

)


export default request