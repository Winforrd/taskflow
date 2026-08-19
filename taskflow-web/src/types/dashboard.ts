import type {
  TaskPriority,
  TaskStatus
} from '@/types/task'


export interface DashboardTask {

  id: number

  projectId: number

  projectName: string

  title: string

  status: TaskStatus

  priority: TaskPriority

  deadline?: string

  overdue: boolean
}


export interface DashboardActivity {

  id: number

  taskId: number

  taskTitle: string

  operatorName: string

  activityType: string

  content: string

  createdTime: string
}


export interface DashboardData {

  todoCount: number

  inProgressCount: number

  reviewCount: number

  overdueCount: number

  doneCount: number

  assignedTotalCount: number

  completionRate: number

  recentTasks: DashboardTask[]

  recentActivities: DashboardActivity[]
}