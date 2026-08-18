export interface TaskComment {

  id: number

  taskId: number

  userId: number

  username?: string

  nickname?: string

  avatar?: string

  content: string

  canDelete: boolean

  createdTime: string

  updatedTime?: string
}


export interface CreateTaskCommentRequest {

  content: string
}