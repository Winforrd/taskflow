export type SubTaskStatus =
  | 'TODO'
  | 'IN_PROGRESS'
  | 'IN_REVIEW'
  | 'DONE'
  | 'CANCELLED'


export interface SubTask {

  id: number

  taskId: number

  title: string

  description?: string

  status: SubTaskStatus

  requiredFlag: number

  sortOrder: number

  creatorId: number

  creatorName?: string

  completedTime?: string

  canEdit: boolean

  canDelete: boolean

  canChangeStatus: boolean
}


export interface CreateSubTaskRequest {

  title: string

  description?: string

  requiredFlag: number

  sortOrder: number
}


export interface UpdateSubTaskRequest {

  title: string

  description?: string

  requiredFlag: number

  sortOrder: number
}
