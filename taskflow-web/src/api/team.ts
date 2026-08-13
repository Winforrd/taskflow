import request from '@/utils/request'

import type {
  ApiResponse
} from '@/types/api'

import type {
  Team,
  TeamDetail,
  CreateTeamRequest,
  UpdateTeamRequest
} from '@/types/team'


export function createTeamApi(
  data: CreateTeamRequest
) {

  return request.post<
    any,
    ApiResponse<number>
  >(
    '/teams',
    data
  )
}


export function getMyTeamsApi() {

  return request.get<
    any,
    ApiResponse<Team[]>
  >(
    '/teams/my'
  )
}


export function getTeamDetailApi(
  teamId: number
) {

  return request.get<
    any,
    ApiResponse<TeamDetail>
  >(
    `/teams/${teamId}`
  )
}


export function updateTeamApi(
  teamId: number,
  data: UpdateTeamRequest
) {

  return request.put<
    any,
    ApiResponse<null>
  >(
    `/teams/${teamId}`,
    data
  )
}