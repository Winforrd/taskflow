import request from '@/utils/request'

import type {
  ApiResponse
} from '@/types/api'

import type {
  CreateTaskCommentRequest,
  TaskComment
} from '@/types/taskComment'


/**
 * 查询任务评论
 */
export function getTaskCommentsApi(
  taskId: number
) {

  return request.get<
    any,
    ApiResponse<TaskComment[]>
  >(
    `/tasks/${taskId}/comments`
  )
}


/**
 * 发表评论
 */
export function createTaskCommentApi(
  taskId: number,
  data: CreateTaskCommentRequest
) {

  return request.post<
    any,
    ApiResponse<number>
  >(
    `/tasks/${taskId}/comments`,
    data
  )
}


/**
 * 删除评论
 */
export function deleteTaskCommentApi(
  commentId: number
) {

  return request.delete<
    any,
    ApiResponse<null>
  >(
    `/task-comments/${commentId}`
  )
}


/**
 * 评论权限
 */
export function getTaskCommentPermissionApi(
  taskId: number
) {

  return request.get<
    any,
    ApiResponse<boolean>
  >(
    `/tasks/${taskId}/comment-permission`
  )
}