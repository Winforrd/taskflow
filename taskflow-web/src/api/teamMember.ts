import request from '@/utils/request'

import type {
  ApiResponse
} from '@/types/api'

import type {
  TeamMember
} from '@/types/team'


export function getTeamMembersApi(
  teamId: number
) {

  return request.get<
    any,
    ApiResponse<TeamMember[]>
  >(
    `/teams/${teamId}/members`
  )
}


export function updateTeamMemberRoleApi(
  teamId: number,
  userId: number,
  roleType: string
) {

  return request.put<
    any,
    ApiResponse<null>
  >(
    `/teams/${teamId}/members/${userId}/role`,
    {
      roleType
    }
  )
}


export function removeTeamMemberApi(
  teamId: number,
  userId: number
) {

  return request.delete<
    any,
    ApiResponse<null>
  >(
    `/teams/${teamId}/members/${userId}`
  )
}


export function quitTeamApi(
  teamId: number
) {

  return request.post<
    any,
    ApiResponse<null>
  >(
    `/teams/${teamId}/members/quit`
  )
}