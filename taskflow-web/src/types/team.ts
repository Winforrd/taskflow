export interface Team {

  id: number

  name: string

  description?: string

  avatar?: string

  ownerId: number

  myRole: string

  memberCount: number

  createdTime?: string
}


export interface TeamDetail {

  id: number

  name: string

  description?: string

  avatar?: string

  ownerId: number

  myRole: string

  memberCount: number

  status: number

  createdTime?: string
}


export interface TeamMember {

  userId: number

  username: string

  nickname: string

  avatar?: string

  roleType: string

  memberStatus: number

  joinedTime?: string
}


export interface CreateTeamRequest {

  name: string

  description?: string
}


export interface UpdateTeamRequest {

  name: string

  description?: string
}