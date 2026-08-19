import request from '@/utils/request'

import type {
  ApiResponse
} from '@/types/api'

import type {
  DashboardData
} from '@/types/dashboard'


/**
 * 获取工作台数据
 */
export function getDashboardApi() {

  return request.get<
    any,
    ApiResponse<DashboardData>
  >(
    '/dashboard'
  )
}