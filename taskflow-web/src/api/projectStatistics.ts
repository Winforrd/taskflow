import request from '@/utils/request'


import type {
  ApiResponse
} from '@/types/api'


import type {
  ProjectStatistics
} from '@/types/projectStatistics'


/**
 * 获取项目统计。
 */
export function getProjectStatisticsApi(
  projectId: number
) {

  return request.get<
    any,
    ApiResponse<ProjectStatistics>
  >(
    `/projects/${projectId}/statistics`
  )
}