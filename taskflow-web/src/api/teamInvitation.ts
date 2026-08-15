import request from '@/utils/request'

import type {
  ApiResponse
} from '@/types/api'

import type {
  TeamInvitation,
  CreateTeamInvitationRequest,
  InvitationPreview
} from '@/types/team'


/**
 * 创建邀请码
 */
export function createTeamInvitationApi(
  teamId: number,
  data: CreateTeamInvitationRequest
) {

  return request.post<
    any,
    ApiResponse<TeamInvitation>
  >(
    `/teams/${teamId}/invitations`,
    data
  )
}


/**
 * 查询团队邀请码
 */
export function getTeamInvitationsApi(
  teamId: number
) {

  return request.get<
    any,
    ApiResponse<TeamInvitation[]>
  >(
    `/teams/${teamId}/invitations`
  )
}


/**
 * 查看邀请码
 */
export function getInvitationPreviewApi(
  invitationCode: string
) {

  return request.get<
    any,
    ApiResponse<InvitationPreview>
  >(
    `/invitations/${invitationCode}`
  )
}


/**
 * 接受团队邀请
 */
export function acceptInvitationApi(
  invitationCode: string
) {

  return request.post<
    any,
    ApiResponse<number>
  >(
    `/invitations/${invitationCode}/accept`
  )
}


/**
 * 禁用邀请码
 */
export function disableInvitationApi(
  teamId: number,
  invitationId: number
) {

  return request.delete<
    any,
    ApiResponse<null>
  >(
    `/teams/${teamId}/invitations/${invitationId}`
  )
}