export type NotificationType =
  | 'TASK_ASSIGNED'
  | 'TASK_UNASSIGNED'
  | 'TASK_SUBMITTED'
  | 'TASK_APPROVED'
  | 'TASK_REJECTED'
  | 'TASK_CANCELLED'
  | 'TASK_COMMENT'


export interface Notification {

  id: number

  type: NotificationType

  title: string

  content?: string

  relatedType?: string

  relatedId?: number

  read: boolean

  readTime?: string

  createdTime: string
}