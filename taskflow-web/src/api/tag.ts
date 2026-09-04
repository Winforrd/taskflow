import request from '@/utils/request'


import type {
  ApiResponse
} from '@/types/api'


import type {
  CreateTagRequest,
  Tag,
  TagCollection,
  TaskTagContext,
  UpdateTagRequest,
  UpdateTaskTagsRequest
} from '@/types/tag'


/**
 * =========================================================
 * 团队标签
 * =========================================================
 */
export function getTeamTagsApi(
  teamId: number
) {

  return request.get<
    any,
    ApiResponse<TagCollection>
  >(
    `/teams/${teamId}/tags`
  )
}


export function createTeamTagApi(
  teamId: number,
  data: CreateTagRequest
) {

  return request.post<
    any,
    ApiResponse<Tag>
  >(
    `/teams/${teamId}/tags`,
    data
  )
}


/**
 * =========================================================
 * 项目标签
 * =========================================================
 */
export function getProjectTagsApi(
  projectId: number
) {

  return request.get<
    any,
    ApiResponse<TagCollection>
  >(
    `/projects/${projectId}/tags`
  )
}


export function createProjectTagApi(
  projectId: number,
  data: CreateTagRequest
) {

  return request.post<
    any,
    ApiResponse<Tag>
  >(
    `/projects/${projectId}/tags`,
    data
  )
}


/**
 * =========================================================
 * 标签本身
 * =========================================================
 */
export function updateTagApi(
  tagId: number,
  data: UpdateTagRequest
) {

  return request.put<
    any,
    ApiResponse<void>
  >(
    `/tags/${tagId}`,
    data
  )
}


export function deleteTagApi(
  tagId: number
) {

  return request.delete<
    any,
    ApiResponse<void>
  >(
    `/tags/${tagId}`
  )
}


/**
 * =========================================================
 * 任务标签
 * =========================================================
 */
export function getTaskTagContextApi(
  taskId: number
) {

  return request.get<
    any,
    ApiResponse<TaskTagContext>
  >(
    `/tasks/${taskId}/tags`
  )
}


export function updateTaskTagsApi(
  taskId: number,
  data: UpdateTaskTagsRequest
) {

  return request.put<
    any,
    ApiResponse<void>
  >(
    `/tasks/${taskId}/tags`,
    data
  )
}