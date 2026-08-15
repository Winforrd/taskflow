/**
 * 团队

 */
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

/**
 * 团队详情
 */
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

/**
 * 团队成员
 */
export interface TeamMember {

  userId: number

  username: string

  nickname: string

  avatar?: string

  roleType: string

  memberStatus: number

  joinedTime?: string
}

/**
 * 创建团队请求
 */
export interface CreateTeamRequest {

  name: string

  description?: string
}
/**
 * 更新团队请求
 */

export interface UpdateTeamRequest {

  name: string

  description?: string
}
/**
 * 团队邀请码
 */
export interface TeamInvitation {

  id: number

  teamId: number

  teamName: string

  invitationCode: string

  defaultRole: string

  expiresAt: string

  maxUses: number

  usedCount: number

  status: number

  createdTime?: string

}


/**
 * 创建邀请码参数
 */
export interface CreateTeamInvitationRequest {

  defaultRole: string

  expiresHours: number

  maxUses: number

}


/**
 * 邀请码预览
 */
export interface InvitationPreview {

  teamId: number

  teamName: string

  teamDescription?: string

  defaultRole: string

  expiresAt: string

}