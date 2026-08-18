import request from '@/utils/request'

import type {
  ApiResponse
} from '@/types/api'

import type {
  CreateTaskRequest,
  Task,
  TaskActivity,
  TaskStatus,
  UpdateTaskRequest
} from '@/types/task'


/**
 * 创建任务
 */
export function createTaskApi(
  projectId: number,
  data: CreateTaskRequest
) {

  return request.post<
    any,
    ApiResponse<number>
  >(
    `/projects/${projectId}/tasks`,
    data
  )
}


/**
 * 查询项目任务
 */
export function getProjectTasksApi(
  projectId: number,
  params?: {
    status?: string
    keyword?: string
  }
) {

  return request.get<
    any,
    ApiResponse<Task[]>
  >(
    `/projects/${projectId}/tasks`,
    {
      params
    }
  )
}


/**
 * 任务详情
 */
export function getTaskDetailApi(
  taskId: number
) {

  return request.get<
    any,
    ApiResponse<Task>
  >(
    `/tasks/${taskId}`
  )
}


/**
 * 编辑任务
 */
export function updateTaskApi(
  taskId: number,
  data: UpdateTaskRequest
) {

  return request.put<
    any,
    ApiResponse<null>
  >(
    `/tasks/${taskId}`,
    data
  )
}


/**
 * 状态流转
 */
export function changeTaskStatusApi(
  taskId: number,
  targetStatus: TaskStatus,
  version: number
) {

  return request.post<
    any,
    ApiResponse<null>
  >(
    `/tasks/${taskId}/status`,
    {
      targetStatus,
      version
    }
  )
}


/**
 * 添加负责人
 */
export function addTaskAssigneeApi(
  taskId: number,
  userId: number
) {

  return request.post<
    any,
    ApiResponse<null>
  >(
    `/tasks/${taskId}/assignees/${userId}`
  )
}


/**
 * 移除负责人
 */
export function removeTaskAssigneeApi(
  taskId: number,
  userId: number
) {

  return request.delete<
    any,
    ApiResponse<null>
  >(
    `/tasks/${taskId}/assignees/${userId}`
  )
}


/**
 * 任务动态
 */
export function getTaskActivitiesApi(
  taskId: number
) {

  return request.get<
    any,
    ApiResponse<TaskActivity[]>
  >(
    `/tasks/${taskId}/activities`
  )
}