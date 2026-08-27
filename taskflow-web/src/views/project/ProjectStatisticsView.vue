<template>

  <div
    v-loading="loading"
    class="statistics-page"
  >

    <!-- 页面头部 -->
    <div class="page-header">

      <div>

        <h2>
          项目统计
        </h2>

        <div
          v-if="project"
          class="project-name"
        >
          {{ project.name }}
        </div>

      </div>


      <el-button
        @click="goProject"
      >
        返回项目
      </el-button>

    </div>


    <template
      v-if="statistics"
    >

      <!-- ========================================= -->
      <!-- 顶部数字指标 -->
      <!-- ========================================= -->
      <div class="summary-grid">

        <el-card
          class="summary-card"
          shadow="hover"
        >

          <div class="summary-label">
            总任务
          </div>

          <div class="summary-value">
            {{ statistics.totalTaskCount }}
          </div>

        </el-card>


        <el-card
          class="summary-card"
          shadow="hover"
        >

          <div class="summary-label">
            有效任务
          </div>

          <div class="summary-value">
            {{ statistics.validTaskCount }}
          </div>

        </el-card>


        <el-card
          class="summary-card"
          shadow="hover"
        >

          <div class="summary-label">
            已完成
          </div>

          <div class="summary-value">
            {{ statistics.doneTaskCount }}
          </div>

        </el-card>


        <el-card
          class="summary-card"
          shadow="hover"
        >

          <div class="summary-label">
            逾期任务
          </div>

          <div
            class="summary-value"
            :class="{
              danger:
                statistics.overdueTaskCount > 0
            }"
          >
            {{ statistics.overdueTaskCount }}
          </div>

        </el-card>

      </div>


      <!-- ========================================= -->
      <!-- 项目进度 -->
      <!-- ========================================= -->
      <el-card
        class="progress-card"
      >

        <div class="card-title">
          项目完成进度
        </div>


        <div class="progress-content">

          <el-progress
            :percentage="
              Number(
                statistics.completionRate
              )
            "
            :stroke-width="18"
          />


          <div class="progress-description">

            已完成
            {{ statistics.doneTaskCount }}

            /

            有效任务
            {{ statistics.validTaskCount }}

          </div>

        </div>

      </el-card>


      <!-- ========================================= -->
      <!-- 第一行图表 -->
      <!-- ========================================= -->
      <div class="chart-grid">

        <el-card>

          <div class="card-title">
            任务状态分布
          </div>

          <div
            ref="statusChartRef"
            class="chart"
          />

        </el-card>


        <el-card>

          <div class="card-title">
            最近14天完成趋势
          </div>

          <div
            ref="trendChartRef"
            class="chart"
          />

        </el-card>

      </div>


      <!-- ========================================= -->
      <!-- 工作量柱状图 -->
      <!-- ========================================= -->
      <el-card
        class="workload-chart-card"
      >

        <div class="card-title">
          成员工作量
        </div>


        <div
          ref="workloadChartRef"
          class="workload-chart"
        />

      </el-card>


      <!-- ========================================= -->
      <!-- 成员明细 -->
      <!-- ========================================= -->
      <el-card>

        <div class="card-title">
          成员工作量明细
        </div>


        <el-table
          :data="
            statistics.memberWorkloads
          "
          stripe
        >

          <el-table-column
            label="成员"
            min-width="180"
          >

            <template #default="{ row }">

              <div class="member-cell">

                <el-avatar
                  :size="32"
                  :src="row.avatar"
                >
                  {{
                    row.memberName
                      ?.charAt(0)
                  }}
                </el-avatar>


                <span>
                  {{ row.memberName }}
                </span>

              </div>

            </template>

          </el-table-column>


          <el-table-column
            prop="assignedCount"
            label="负责总数"
            width="100"
            align="center"
          />


          <el-table-column
            prop="todoCount"
            label="待处理"
            width="90"
            align="center"
          />


          <el-table-column
            prop="inProgressCount"
            label="进行中"
            width="90"
            align="center"
          />


          <el-table-column
            prop="inReviewCount"
            label="待验收"
            width="90"
            align="center"
          />


          <el-table-column
            prop="doneCount"
            label="已完成"
            width="90"
            align="center"
          />


          <el-table-column
            prop="cancelledCount"
            label="已取消"
            width="90"
            align="center"
          />


          <el-table-column
            label="逾期"
            width="90"
            align="center"
          >

            <template #default="{ row }">

              <el-tag
                v-if="
                  row.overdueCount > 0
                "
                type="danger"
              >
                {{ row.overdueCount }}
              </el-tag>


              <span v-else>
                0
              </span>

            </template>

          </el-table-column>

        </el-table>

      </el-card>

    </template>


    <el-empty
      v-else-if="!loading"
      description="暂无统计数据"
    />

  </div>

</template>


<script setup lang="ts">

import {
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref
} from 'vue'


import {
  useRoute,
  useRouter
} from 'vue-router'


import {
  ElMessage
} from 'element-plus'


import * as echarts
  from 'echarts'


import {
  getProjectDetailApi
} from '@/api/project'


import {
  getProjectStatisticsApi
} from '@/api/projectStatistics'


import type {
  ECharts
} from 'echarts'


import type {
  ProjectDetail
} from '@/types/project'


import type {
  ProjectStatistics,
  StatisticsTaskStatus
} from '@/types/projectStatistics'


const route =
  useRoute()


const router =
  useRouter()


const projectId =
  Number(
    route.params.projectId
  )


const loading =
  ref(false)


const project =
  ref<ProjectDetail | null>(
    null
  )


const statistics =
  ref<ProjectStatistics | null>(
    null
  )


const statusChartRef =
  ref<HTMLDivElement | null>(
    null
  )


const trendChartRef =
  ref<HTMLDivElement | null>(
    null
  )


const workloadChartRef =
  ref<HTMLDivElement | null>(
    null
  )


let statusChart:
  ECharts | null = null


let trendChart:
  ECharts | null = null


let workloadChart:
  ECharts | null = null


onMounted(
  async () => {

    await loadPage()


    window.addEventListener(
      'resize',
      resizeCharts
    )

  }
)


onBeforeUnmount(
  () => {

    window.removeEventListener(
      'resize',
      resizeCharts
    )


    disposeCharts()

  }
)


async function loadPage() {

  loading.value =
    true


  try {

    const [
      projectResult,
      statisticsResult
    ] =
      await Promise.all([

        getProjectDetailApi(
          projectId
        ),

        getProjectStatisticsApi(
          projectId
        )

      ])


    project.value =
      projectResult.data


    statistics.value =
      statisticsResult.data


    await nextTick()


    renderCharts()

  }
  catch (error) {

    console.error(
      '项目统计加载失败',
      error
    )


    ElMessage.error(
      '项目统计加载失败'
    )
  }
  finally {

    loading.value =
      false
  }
}


/**
 * ==================================================
 * 绘制全部图表
 * ==================================================
 */
function renderCharts() {

  if (
    !statistics.value
  ) {

    return
  }


  renderStatusChart()

  renderTrendChart()

  renderWorkloadChart()
}


/**
 * ==================================================
 * 状态分布饼图
 * ==================================================
 */
function renderStatusChart() {

  if (
    !statusChartRef.value
    ||
    !statistics.value
  ) {

    return
  }


  if (!statusChart) {

    statusChart =
      echarts.init(
        statusChartRef.value
      )
  }


  statusChart.setOption(
    {

      tooltip: {

        trigger:
          'item',

        formatter:
          '{b}: {c} ({d}%)'

      },


      legend: {

        bottom:
          0

      },


      series: [

        {

          name:
            '任务状态',

          type:
            'pie',

          radius: [
            '40%',
            '68%'
          ],

          center: [
            '50%',
            '45%'
          ],

          data:
            statistics.value
              .statusDistribution
              .map(
                item => ({
                  name:
                    getStatusName(
                      item.status
                    ),

                  value:
                    item.count
                })
              ),

          label: {

            formatter:
              '{b}\n{c}'

          }

        }

      ]

    }
  )
}


/**
 * ==================================================
 * 最近14天完成趋势
 * ==================================================
 */
function renderTrendChart() {

  if (
    !trendChartRef.value
    ||
    !statistics.value
  ) {

    return
  }


  if (!trendChart) {

    trendChart =
      echarts.init(
        trendChartRef.value
      )
  }


  trendChart.setOption(
    {

      tooltip: {

        trigger:
          'axis'

      },


      grid: {

        left:
          45,

        right:
          25,

        top:
          30,

        bottom:
          45

      },


      xAxis: {

        type:
          'category',

        data:
          statistics.value
            .completionTrend
            .map(
              item =>
                item.date.substring(
                  5
                )
            )

      },


      yAxis: {

        type:
          'value',

        minInterval:
          1

      },


      series: [

        {

          name:
            '完成任务数',

          type:
            'line',

          smooth:
            true,

          data:
            statistics.value
              .completionTrend
              .map(
                item =>
                  item.completedCount
              )

        }

      ]

    }
  )
}


/**
 * ==================================================
 * 成员工作量柱状图
 * ==================================================
 */
function renderWorkloadChart() {

  if (
    !workloadChartRef.value
    ||
    !statistics.value
  ) {

    return
  }


  if (!workloadChart) {

    workloadChart =
      echarts.init(
        workloadChartRef.value
      )
  }


  const members =
    statistics.value
      .memberWorkloads


  workloadChart.setOption(
    {

      tooltip: {

        trigger:
          'axis',

        axisPointer: {

          type:
            'shadow'

        }

      },


      legend: {

        top:
          0,

        data: [
          '负责总数',
          '进行中',
          '待验收',
          '已完成'
        ]

      },


      grid: {

        left:
          50,

        right:
          25,

        top:
          50,

        bottom:
          65

      },


      xAxis: {

        type:
          'category',

        data:
          members.map(
            member =>
              member.memberName
          ),

        axisLabel: {

          interval:
            0,

          rotate:
            members.length > 6
              ? 30
              : 0

        }

      },


      yAxis: {

        type:
          'value',

        minInterval:
          1

      },


      series: [

        {

          name:
            '负责总数',

          type:
            'bar',

          data:
            members.map(
              member =>
                member.assignedCount
            )

        },


        {

          name:
            '进行中',

          type:
            'bar',

          data:
            members.map(
              member =>
                member.inProgressCount
            )

        },


        {

          name:
            '待验收',

          type:
            'bar',

          data:
            members.map(
              member =>
                member.inReviewCount
            )

        },


        {

          name:
            '已完成',

          type:
            'bar',

          data:
            members.map(
              member =>
                member.doneCount
            )

        }

      ]

    }
  )
}


function resizeCharts() {

  statusChart?.resize()

  trendChart?.resize()

  workloadChart?.resize()
}


function disposeCharts() {

  statusChart?.dispose()

  trendChart?.dispose()

  workloadChart?.dispose()


  statusChart =
    null

  trendChart =
    null

  workloadChart =
    null
}


function getStatusName(
  status: StatisticsTaskStatus
) {

  const map:
    Record<
      StatisticsTaskStatus,
      string
    > = {

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


function goProject() {

  router.push(
    `/projects/${projectId}`
  )
}

</script>


<style scoped>

.statistics-page {

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


.project-name {

  margin-top: 6px;

  color: #909399;

}


.summary-grid {

  display: grid;

  grid-template-columns:
    repeat(
      4,
      minmax(
        180px,
        1fr
      )
    );

  gap: 16px;

  margin-bottom: 16px;

}


.summary-card {

  text-align: center;

}


.summary-label {

  color: #909399;

  font-size: 14px;

}


.summary-value {

  margin-top: 10px;

  color: #303133;

  font-size: 30px;

  font-weight: 600;

}


.summary-value.danger {

  color: #f56c6c;

}


.progress-card {

  margin-bottom: 16px;

}


.card-title {

  margin-bottom: 18px;

  color: #303133;

  font-size: 16px;

  font-weight: 600;

}


.progress-content {

  padding:
    10px 10px 4px;

}


.progress-description {

  margin-top: 10px;

  color: #909399;

  font-size: 13px;

}


.chart-grid {

  display: grid;

  grid-template-columns:
    repeat(
      2,
      minmax(
        0,
        1fr
      )
    );

  gap: 16px;

  margin-bottom: 16px;

}


.chart {

  width: 100%;

  height: 360px;

}


.workload-chart-card {

  margin-bottom: 16px;

}


.workload-chart {

  width: 100%;

  height: 420px;

}


.member-cell {

  display: flex;

  align-items: center;

  gap: 10px;

}


@media (
  max-width: 1000px
) {

  .summary-grid {

    grid-template-columns:
      repeat(
        2,
        1fr
      );

  }


  .chart-grid {

    grid-template-columns:
      1fr;

  }

}

</style>