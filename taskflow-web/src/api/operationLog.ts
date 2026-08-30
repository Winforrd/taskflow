import request from '@/utils/request'


import type {
  ApiResponse
} from '@/types/api'


import type {
  OperationLog,
  OperationLogQuery
} from '@/types/operationLog'


/**
 * 项目操作日志。
 */
export function getProjectOperationLogsApi(

  projectId: number,

  params?: OperationLogQuery

) {

  return request.get<
    any,
    ApiResponse<OperationLog[]>
  >(
    `/projects/${projectId}/operation-logs`,
    {
      params
    }
  )
}