import request from '@/utils/request'

import type {
  SubTask,
  CreateSubTaskRequest,
  UpdateSubTaskRequest
} from '@/types/subTask'


/**
 * 查询子任务列表
 */
export function getSubTasks(
  taskId: number
) {

  return request.get<SubTask[]>(
    `/tasks/${taskId}/subtasks`
  )
}


/**
 * 创建子任务
 */
export function createSubTask(
  taskId: number,
  data: CreateSubTaskRequest
) {

  return request.post(
    `/tasks/${taskId}/subtasks`,
    data
  )
}


/**
 * 修改子任务
 */
export function updateSubTask(
  taskId: number,
  id: number,
  data: UpdateSubTaskRequest
) {

  return request.put(
    `/tasks/${taskId}/subtasks/${id}`,
    data
  )
}


/**
 * 删除子任务
 */
export function deleteSubTask(
  taskId: number,
  id: number
) {

  return request.delete(
    `/tasks/${taskId}/subtasks/${id}`
  )
}


/**
 * 完成 / 取消完成子任务
 */
export function changeSubTaskStatus(
  taskId: number,
  id: number
) {

  return request.post(
    `/tasks/${taskId}/subtasks/${id}/status`
  )
}
