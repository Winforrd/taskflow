export type MyTaskType =
  | 'ASSIGNED'
  | 'CREATED'
  | 'REVIEW'
  | 'OVERDUE'


export type MyTaskStatus =
  | 'TODO'
  | 'IN_PROGRESS'
  | 'IN_REVIEW'
  | 'DONE'
  | 'CANCELLED'


export type MyTaskPriority =
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH'
  | 'URGENT'


/**
 * 我的任务列表项
 */
export interface MyTask {

  id: number

  projectId: number

  projectName: string

  title: string

  status: MyTaskStatus

  priority: MyTaskPriority

  creatorId: number

  creatorName?: string

  assigneeNames?: string

  startDate?: string

  deadline?: string

  overdue: boolean

  createdTime?: string
}


/**
 * 通用分页结果
 */
export interface PageResult<T> {

  pageNum: number

  pageSize: number

  total: number

  pages: number

  records: T[]
}


/**
 * 我的任务查询参数
 */
export interface MyTaskQuery {

  type: MyTaskType

  pageNum: number

  pageSize: number

  keyword?: string

  status?: MyTaskStatus
}