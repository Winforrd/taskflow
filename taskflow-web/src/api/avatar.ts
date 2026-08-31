import request from '@/utils/request'


import type {
  ApiResponse
} from '@/types/api'


/**
 * =========================================================
 * 头像上传结果
 * =========================================================
 */
export interface AvatarUploadResult {

  avatar: string

}


/**
 * =========================================================
 * 上传头像
 * =========================================================
 *
 * POST
 *
 * /users/avatar
 *
 * request.ts已经配置/api前缀，
 * 所以这里不手写/api。
 */
export function uploadAvatarApi(
  file: File
) {


  const formData =
    new FormData()


  formData.append(
    'file',
    file
  )


  return request.post<
    any,
    ApiResponse<AvatarUploadResult>
  >(
    '/users/avatar',

    formData,

    {
      headers: {

        'Content-Type':
          'multipart/form-data'

      }
    }
  )
}


/**
 * =========================================================
 * 删除头像
 * =========================================================
 */
export function removeAvatarApi() {


  return request.delete<
    any,
    ApiResponse<void>
  >(
    '/users/avatar'
  )
}