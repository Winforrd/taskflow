export type StatisticsTaskStatus =
  | 'TODO'
  | 'IN_PROGRESS'
  | 'IN_REVIEW'
  | 'DONE'
  | 'CANCELLED'


/**
 * 状态统计
 */
export interface TaskStatusStatistic {

  status:
    StatisticsTaskStatus

  count:
    number
}


/**
 * 完成趋势
 */
export interface ProjectTrendPoint {

  date:
    string

  completedCount:
    number
}


/**
 * 成员工作量
 */
export interface MemberWorkload {

  userId:
    number

  memberName:
    string

  avatar?:
    string

  assignedCount:
    number

  todoCount:
    number

  inProgressCount:
    number

  inReviewCount:
    number

  doneCount:
    number

  cancelledCount:
    number

  overdueCount:
    number
}


/**
 * 项目统计
 */
export interface ProjectStatistics {

  projectId:
    number

  totalTaskCount:
    number

  validTaskCount:
    number

  doneTaskCount:
    number

  overdueTaskCount:
    number

  completionRate:
    number

  statusDistribution:
    TaskStatusStatistic[]

  completionTrend:
    ProjectTrendPoint[]

  memberWorkloads:
    MemberWorkload[]
}