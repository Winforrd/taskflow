export type ProjectStatus =
  | 'NOT_STARTED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'ARCHIVED'


export type ProjectRole =
  | 'MANAGER'
  | 'MEMBER'
  | 'READ_ONLY'


/**
 * 项目列表
 */
export interface Project {

  id: number

  teamId: number

  projectCode: string

  name: string

  description?: string

  ownerId: number

  ownerName?: string

  status: ProjectStatus

  startDate?: string

  deadline?: string

  myRole?: ProjectRole

  memberCount: number

  createdTime?: string
}


/**
 * 项目详情
 */
export interface ProjectDetail {

  id: number

  teamId: number

  projectCode: string

  name: string

  description?: string

  ownerId: number

  ownerName?: string

  status: ProjectStatus

  startDate?: string

  deadline?: string

  myProjectRole?: ProjectRole

  myTeamRole?: string

  memberCount: number

  /**
   * 是否可以编辑项目
   *
   * OWNER / ADMIN
   * 项目负责人
   * MANAGER
   */
  canEdit: boolean

  /**
   * 是否拥有核心项目管理权限
   *
   * OWNER / ADMIN
   * 项目负责人
   *
   * MANAGER 不具备
   */
  canManageProject: boolean

  createdTime?: string
}


/**
 * 项目成员
 */
export interface ProjectMember {

  userId: number

  username: string

  nickname: string

  avatar?: string

  roleType: ProjectRole

  /**
   * 是否为项目负责人
   *
   * 不要通过 MANAGER 判断
   */
  projectOwner: boolean

  joinedTime?: string
}


/**
 * 创建项目
 */
export interface CreateProjectRequest {

  name: string

  description?: string

  ownerId: number

  startDate?: string

  deadline?: string
}


/**
 * 编辑项目
 */
export interface UpdateProjectRequest {

  name: string

  description?: string

  status: ProjectStatus

  startDate?: string

  deadline?: string
}


/**
 * 添加项目成员
 */
export interface AddProjectMemberRequest {

  userId: number

  roleType: ProjectRole
}