import request from '@/utils/request'

import type {
  ApiResponse
} from '@/types/api'

import type {
  UserInfo,
  UpdateProfileRequest,
  ChangePasswordRequest
} from '@/types/user'


/**
 * 获取当前用户信息
 */
export function getProfileApi() {

  return request.get<
    any,
    ApiResponse<UserInfo>
  >(
    '/users/profile'
  )
}


/**
 * 修改个人资料
 */
export function updateProfileApi(
  data: UpdateProfileRequest
) {

  return request.put<
    any,
    ApiResponse<null>
  >(
    '/users/profile',
    data
  )
}


/**
 * 修改密码
 */
export function changePasswordApi(
  data: ChangePasswordRequest
) {

  return request.put<
    any,
    ApiResponse<null>
  >(
    '/users/password',
    data
  )
}