import request from '@/utils/request'

import type {
  LoginRequest,
  LoginResult,
  RegisterRequest
} from '@/types/user'

import type {
  ApiResponse
} from '@/types/api'



/**
 * 用户登录
 */
export function loginApi(
  data: LoginRequest
) {

  return request.post<
    any,
    ApiResponse<LoginResult>
  >(
    '/auth/login',
    data
  )

}



/**
 * 用户注册
 */
export function registerApi(
  data: RegisterRequest
) {

  return request.post<
    any,
    ApiResponse<null>
  >(
    '/auth/register',
    data
  )

}



/**
 * 用户退出
 */
export function logoutApi() {

  return request.post<
    any,
    ApiResponse<null>
  >(
    '/auth/logout'
  )

}