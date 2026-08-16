import request from '@/utils/request'

import type {
  ApiResponse
} from '@/types/api'

import type {
  AddProjectMemberRequest,
  ProjectMember,
  ProjectRole
} from '@/types/project'


export function getProjectMembersApi(
  projectId: number
) {

  return request.get<
    any,
    ApiResponse<ProjectMember[]>
  >(
    `/projects/${projectId}/members`
  )
}


export function addProjectMemberApi(
  projectId: number,
  data: AddProjectMemberRequest
) {

  return request.post<
    any,
    ApiResponse<null>
  >(
    `/projects/${projectId}/members`,
    data
  )
}


export function updateProjectMemberRoleApi(
  projectId: number,
  userId: number,
  roleType: ProjectRole
) {

  return request.put<
    any,
    ApiResponse<null>
  >(
    `/projects/${projectId}/members/${userId}/role`,
    {
      roleType
    }
  )
}


export function removeProjectMemberApi(
  projectId: number,
  userId: number
) {

  return request.delete<
    any,
    ApiResponse<null>
  >(
    `/projects/${projectId}/members/${userId}`
  )
}