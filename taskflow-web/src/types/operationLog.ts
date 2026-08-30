export type OperationLogModule =
  | 'TEAM'
  | 'PROJECT'
  | 'TASK'
  | 'COMMENT'
  | 'NOTIFICATION'
  | 'USER'
  | 'OTHER'


export interface OperationLog {

  id: number

  userId?: number

  username?: string

  teamId?: number

  projectId?: number

  taskId?: number

  module: OperationLogModule | string

  operation: string

  httpMethod?: string

  requestUri?: string

  requestParams?: string

  ip?: string

  userAgent?: string

  success: boolean

  errorMessage?: string

  costMs?: number

  createdTime: string
}


export interface OperationLogQuery {

  module?: string

  success?: boolean

  keyword?: string
}