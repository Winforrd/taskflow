export type TaskStatus =
  | 'TODO'
  | 'IN_PROGRESS'
  | 'IN_REVIEW'
  | 'DONE'
  | 'CANCELLED'


export type TaskPriority =
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH'
  | 'URGENT'


/**
 * 任务负责人
 */
export interface TaskAssignee {

  userId: number

  username: string

  nickname: string

  avatar?: string
}


/**
 * 任务
 */
export interface Task {

  id: number

  projectId: number

  title: string

  description?: string

  status: TaskStatus

  priority: TaskPriority

  creatorId: number

  creatorName?: string

  startDate?: string

  deadline?: string

  completedTime?: string

  version: number

  assignees: TaskAssignee[]

  /**
   * 后端根据：
   *
   * 状态
   * +
   * 权限
   *
   * 计算出来的下一状态。
   */
  allowedNextStatuses: TaskStatus[]

  /**
   * 能否编辑任务。
   */
  canEdit: boolean

  /**
   * 能否管理任务负责人。
   *
   * MEMBER = false
   */
  canManageAssignees: boolean

  /**
   * 能否给其他成员分配任务。
   *
   * MEMBER = false
   */
  canAssignOthers: boolean

  createdTime?: string
}


/**
 * 创建任务
 */
export interface CreateTaskRequest {

  title: string

  description?: string

  priority: TaskPriority

  startDate?: string

  deadline?: string

  assigneeIds?: number[]
}


/**
 * 修改任务
 */
export interface UpdateTaskRequest {

  title: string

  description?: string

  priority: TaskPriority

  startDate?: string

  deadline?: string

  version: number
}


/**
 * 任务动态
 */
export interface TaskActivity {

  id: number

  operatorId: number

  operatorName: string

  activityType: string

  content: string

  createdTime: string
}