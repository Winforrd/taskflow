import request from '@/utils/request'


import type {
  ApiResponse
} from '@/types/api'


import type {
  ExtensionCapability
} from '@/types/extension'


/**
 * =========================================================
 * 获取项目扩展能力
 * =========================================================
 */
export function getProjectExtensionCapabilitiesApi(
  projectId: number
) {

  return request.get<
    any,
    ApiResponse<ExtensionCapability[]>
  >(
    `/projects/${projectId}/extensions/capabilities`
  )
}