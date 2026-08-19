<template>

  <div class="dashboard-page">

    <!-- 页面标题 -->
    <div class="page-header">

      <div>

        <h2>
          工作台
        </h2>

        <p>
          查看当前任务进度和最近协作动态
        </p>

      </div>


      <el-button
        type="primary"
        @click="goMyTasks"
      >

        查看我的任务

      </el-button>

    </div>


    <div
      v-loading="loading"
    >

      <!-- ============================== -->
      <!-- 数据统计 -->
      <!-- ============================== -->

      <el-row
        :gutter="20"
        class="stats-row"
      >

        <el-col :span="6">

          <el-card
            class="stat-card clickable"
            shadow="hover"
            @click="
              goMyTaskType(
                'ASSIGNED',
                'TODO'
              )
            "
          >

            <div class="stat-title">

              待处理

            </div>


            <div class="stat-value">

              {{ dashboard.todoCount }}

            </div>

          </el-card>

        </el-col>


        <el-col :span="6">

          <el-card
            class="stat-card clickable"
            shadow="hover"
            @click="
              goMyTaskType(
                'ASSIGNED',
                'IN_PROGRESS'
              )
            "
          >

            <div class="stat-title">

              进行中

            </div>


            <div class="stat-value">

              {{ dashboard.inProgressCount }}

            </div>

          </el-card>

        </el-col>


        <el-col :span="6">

          <el-card
            class="stat-card clickable"
            shadow="hover"
            @click="
              goMyTaskType(
                'REVIEW'
              )
            "
          >

            <div class="stat-title">

              待我验收

            </div>


            <div class="stat-value">

              {{ dashboard.reviewCount }}

            </div>

          </el-card>

        </el-col>


        <el-col :span="6">

          <el-card
            class="stat-card clickable"
            shadow="hover"
            @click="
              goMyTaskType(
                'OVERDUE'
              )
            "
          >

            <div class="stat-title">

              已逾期

            </div>


            <div
              class="stat-value overdue-value"
            >

              {{ dashboard.overdueCount }}

            </div>

          </el-card>

        </el-col>

      </el-row>


      <!-- ============================== -->
      <!-- 完成率 -->
      <!-- ============================== -->

      <el-card class="completion-card">

        <div class="completion-header">

          <div>

            <div class="section-title">

              我的任务完成率

            </div>


            <div class="completion-description">

              已完成
              {{ dashboard.doneCount }}
              /
              {{
                dashboard.assignedTotalCount
              }}

            </div>

          </div>


          <div class="completion-number">

            {{
              dashboard.completionRate
            }}%

          </div>

        </div>


        <el-progress
          :percentage="
            dashboard.completionRate
          "
          :stroke-width="14"
        />

      </el-card>


      <!-- ============================== -->
      <!-- 下半区 -->
      <!-- ============================== -->

      <el-row
        :gutter="20"
        class="content-row"
      >

        <!-- 最近任务 -->
        <el-col :span="15">

          <el-card>

            <template #header>

              <div class="card-header">

                <span>
                  最近任务
                </span>


                <el-button
                  link
                  type="primary"
                  @click="goMyTasks"
                >

                  查看全部

                </el-button>

              </div>

            </template>


            <el-table
              v-if="
                dashboard.recentTasks.length
                > 0
              "
              :data="
                dashboard.recentTasks
              "
              @row-click="
                handleTaskClick
              "
            >

              <!-- 项目 -->
              <el-table-column
                prop="projectName"
                label="项目"
                min-width="120"
              />


              <!-- 任务 -->
              <el-table-column
                prop="title"
                label="任务"
                min-width="180"
              />


              <!-- 状态 -->
              <el-table-column
                label="状态"
                width="100"
              >

                <template #default="{ row }">

                  <el-tag
                    size="small"
                    :type="
                      getStatusType(
                        row.status
                      )
                    "
                  >

                    {{
                      getStatusName(
                        row.status
                      )
                    }}

                  </el-tag>

                </template>

              </el-table-column>


              <!-- 优先级 -->
              <el-table-column
                label="优先级"
                width="100"
              >

                <template #default="{ row }">

                  <el-tag
                    size="small"
                    :type="
                      getPriorityType(
                        row.priority
                      )
                    "
                  >

                    {{
                      getPriorityName(
                        row.priority
                      )
                    }}

                  </el-tag>

                </template>

              </el-table-column>


              <!-- 截止 -->
              <el-table-column
                label="截止时间"
                width="120"
              >

                <template #default="{ row }">

                  <span
                    :class="{
                      'deadline-overdue':
                        row.overdue
                    }"
                  >

                    {{
                      row.deadline
                      || '-'
                    }}

                  </span>

                </template>

              </el-table-column>

            </el-table>


            <el-empty
              v-else
              description="暂无最近任务"
            />

          </el-card>

        </el-col>


        <!-- 最近动态 -->
        <el-col :span="9">

          <el-card>

            <template #header>

              <span>
                最近任务动态
              </span>

            </template>


            <el-timeline
              v-if="
                dashboard
                  .recentActivities
                  .length > 0
              "
            >

              <el-timeline-item
                v-for="
                  activity
                  in dashboard
                    .recentActivities
                "
                :key="
                  activity.id
                "
                :timestamp="
                  formatTime(
                    activity.createdTime
                  )
                "
              >

                <div
                  class="activity-item"
                  @click="
                    goTask(
                      activity.taskId
                    )
                  "
                >

                  <div class="activity-task">

                    {{
                      activity.taskTitle
                    }}

                  </div>


                  <div class="activity-content">

                    <strong>

                      {{
                        activity.operatorName
                      }}

                    </strong>

                    {{ activity.content }}

                  </div>

                </div>

              </el-timeline-item>

            </el-timeline>


            <el-empty
              v-else
              description="暂无最近动态"
            />

          </el-card>

        </el-col>

      </el-row>

    </div>

  </div>

</template>


<script setup lang="ts">

import {
  onMounted,
  reactive,
  ref
} from 'vue'

import {
  useRouter
} from 'vue-router'

import {
  getDashboardApi
} from '@/api/dashboard'

import type {
  DashboardData,
  DashboardTask
} from '@/types/dashboard'

import type {
  TaskPriority,
  TaskStatus
} from '@/types/task'


const router =
  useRouter()


const loading =
  ref(false)


const dashboard =
  reactive<DashboardData>({

    todoCount: 0,

    inProgressCount: 0,

    reviewCount: 0,

    overdueCount: 0,

    doneCount: 0,

    assignedTotalCount: 0,

    completionRate: 0,

    recentTasks: [],

    recentActivities: []

  })


onMounted(
  async () => {

    await loadDashboard()

  }
)


/**
 * 加载工作台
 */
async function loadDashboard() {

  loading.value =
    true


  try {

    const result =
      await getDashboardApi()


    Object.assign(
      dashboard,
      result.data
    )

  }

  finally {

    loading.value =
      false
  }
}


/**
 * 进入我的任务
 */
function goMyTasks() {

  router.push(
    '/my-tasks'
  )
}


/**
 * 从统计卡跳转
 */
function goMyTaskType(
  type:
    'ASSIGNED'
    | 'CREATED'
    | 'REVIEW'
    | 'OVERDUE',

  status?: TaskStatus
) {

  router.push({

    path:
      '/my-tasks',

    query: {

      type,

      status

    }

  })
}


/**
 * 最近任务点击
 */
function handleTaskClick(
  row: DashboardTask
) {

  goTask(
    row.id
  )
}


/**
 * 进入任务详情
 */
function goTask(
  taskId: number
) {

  router.push(
    `/tasks/${taskId}`
  )
}


/**
 * 状态中文
 */
function getStatusName(
  status: TaskStatus
) {

  const map:
    Record<TaskStatus, string> = {

      TODO:
        '待处理',

      IN_PROGRESS:
        '进行中',

      IN_REVIEW:
        '待验收',

      DONE:
        '已完成',

      CANCELLED:
        '已取消'

    }


  return map[status]
}


/**
 * 状态样式
 */
function getStatusType(
  status: TaskStatus
):
  'info'
  | 'primary'
  | 'warning'
  | 'success'
  | 'danger' {

  switch (status) {

    case 'TODO':

      return 'info'


    case 'IN_PROGRESS':

      return 'primary'


    case 'IN_REVIEW':

      return 'warning'


    case 'DONE':

      return 'success'


    case 'CANCELLED':

      return 'danger'
  }
}


/**
 * 优先级中文
 */
function getPriorityName(
  priority: TaskPriority
) {

  const map:
    Record<TaskPriority, string> = {

      LOW:
        '低',

      MEDIUM:
        '中',

      HIGH:
        '高',

      URGENT:
        '紧急'

    }


  return map[priority]
}


/**
 * 优先级样式
 */
function getPriorityType(
  priority: TaskPriority
):
  'info'
  | 'primary'
  | 'warning'
  | 'danger' {

  switch (priority) {

    case 'LOW':

      return 'info'


    case 'MEDIUM':

      return 'primary'


    case 'HIGH':

      return 'warning'


    case 'URGENT':

      return 'danger'
  }
}


/**
 * 时间格式化
 */
function formatTime(
  value?: string
) {

  if (!value) {

    return '-'
  }


  return value
    .replace(
      'T',
      ' '
    )
    .substring(
      0,
      19
    )
}

</script>


<style scoped>

.dashboard-page {

  width: 100%;

}


.page-header {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  margin-bottom: 20px;

}


.page-header h2 {

  margin: 0;

}


.page-header p {

  margin:
    8px 0 0;

  color: #909399;

}


.stats-row {

  margin-bottom: 20px;

}


.stat-card {

  min-height: 130px;

}


.clickable {

  cursor: pointer;

}


.stat-title {

  color: #909399;

  font-size: 14px;

}


.stat-value {

  margin-top: 18px;

  color: #303133;

  font-size: 36px;

  font-weight: 600;

}


.overdue-value {

  color: #f56c6c;

}


.completion-card {

  margin-bottom: 20px;

}


.completion-header {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  margin-bottom: 16px;

}


.section-title {

  font-size: 16px;

  font-weight: 600;

}


.completion-description {

  margin-top: 6px;

  color: #909399;

  font-size: 13px;

}


.completion-number {

  font-size: 28px;

  font-weight: 600;

}


.content-row {

  align-items: flex-start;

}


.card-header {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

}


.deadline-overdue {

  color: #f56c6c;

  font-weight: 500;

}


.activity-item {

  cursor: pointer;

}


.activity-task {

  color: #303133;

  font-weight: 500;

}


.activity-content {

  margin-top: 5px;

  color: #606266;

  font-size: 13px;

  line-height: 1.5;

}

</style>