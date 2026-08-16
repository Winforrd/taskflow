import request from '@/utils/request'

import type {
  ApiResponse
} from '@/types/api'

import type {
  Project,
  ProjectDetail,
  CreateProjectRequest,
  UpdateProjectRequest
} from '@/types/project'


/**
 * 创建项目
 */
export function createProjectApi(
  teamId: number,
  data: CreateProjectRequest
) {

  return request.post<
    any,
    ApiResponse<number>
  >(
    `/teams/${teamId}/projects`,
    data
  )
}


/**
 * 团队项目列表
 */
export function getTeamProjectsApi(
  teamId: number
) {

  return request.get<
    any,
    ApiResponse<Project[]>
  >(
    `/teams/${teamId}/projects`
  )
}


/**
 * 项目详情
 */
export function getProjectDetailApi(
  projectId: number
) {

  return request.get<
    any,
    ApiResponse<ProjectDetail>
  >(
    `/projects/${projectId}`
  )
}


/**
 * 编辑项目
 */
export function updateProjectApi(
  projectId: number,
  data: UpdateProjectRequest
) {

  return request.put<
    any,
    ApiResponse<null>
  >(
    `/projects/${projectId}`,
    data
  )
}


/**
 * 归档项目
 */
export function archiveProjectApi(
  projectId: number
) {

  return request.post<
    any,
    ApiResponse<null>
  >(
    `/projects/${projectId}/archive`
  )
}