import request from '@/utils/request'

import type {
  ApiResponse
} from '@/types/api'

import type {
  MyTask,
  MyTaskQuery,
  PageResult
} from '@/types/myTask'


/**
 * 查询我的任务
 */
export function getMyTasksApi(
  params: MyTaskQuery
) {

  return request.get<
    any,
    ApiResponse<PageResult<MyTask>>
  >(
    '/tasks/my',
    {
      params
    }
  )
}