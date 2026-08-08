import request from '@/utils/request'

import type {
  ApiResponse
} from '@/types/api'

import type {
  UserInfo
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