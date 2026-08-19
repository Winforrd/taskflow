<template>

  <div class="my-task-page">

    <!-- ============================== -->
    <!-- 标题 -->
    <!-- ============================== -->

    <div class="page-header">

      <div>

        <h2>
          我的任务
        </h2>

        <p>
          查看我负责、我创建以及需要处理的任务
        </p>

      </div>

    </div>


    <el-card>

      <!-- ============================== -->
      <!-- 分类 -->
      <!-- ============================== -->

      <el-tabs
        v-model="activeType"
        @tab-change="handleTabChange"
      >

        <el-tab-pane
          label="我负责的"
          name="ASSIGNED"
        />

        <el-tab-pane
          label="我创建的"
          name="CREATED"
        />

        <el-tab-pane
          label="待我验收"
          name="REVIEW"
        />

        <el-tab-pane
          label="已逾期"
          name="OVERDUE"
        />

      </el-tabs>


      <!-- ============================== -->
      <!-- 搜索条件 -->
      <!-- ============================== -->

      <div class="filter-row">

        <el-input
          v-model="keyword"
          placeholder="搜索任务标题"
          clearable
          style="width: 280px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />


        <!--
          REVIEW 状态固定为 IN_REVIEW
          OVERDUE 状态由后端决定

          所以这两个 Tab
          不需要状态选择框。
        -->
        <el-select
          v-if="
            activeType === 'ASSIGNED'
            ||
            activeType === 'CREATED'
          "
          v-model="status"
          clearable
          placeholder="全部状态"
          style="width: 160px"
        >

          <el-option
            label="待处理"
            value="TODO"
          />

          <el-option
            label="进行中"
            value="IN_PROGRESS"
          />

          <el-option
            label="待验收"
            value="IN_REVIEW"
          />

          <el-option
            label="已完成"
            value="DONE"
          />

          <el-option
            label="已取消"
            value="CANCELLED"
          />

        </el-select>


        <el-button
          type="primary"
          @click="handleSearch"
        >

          查询

        </el-button>


        <el-button
          @click="handleReset"
        >

          重置

        </el-button>

      </div>


      <!-- ============================== -->
      <!-- 表格 -->
      <!-- ============================== -->

      <el-table
        v-loading="loading"
        :data="tasks"
        stripe
        class="task-table"
        @row-click="handleRowClick"
      >

        <!-- 项目 -->
        <el-table-column
          label="项目"
          min-width="150"
        >

          <template #default="{ row }">

            <span class="project-name">

              {{ row.projectName }}

            </span>

          </template>

        </el-table-column>


        <!-- 任务 -->
        <el-table-column
          label="任务"
          min-width="220"
        >

          <template #default="{ row }">

            <div class="task-title">

              {{ row.title }}

            </div>


            <div
              v-if="row.overdue"
              class="overdue-text"
            >

              已逾期

            </div>

          </template>

        </el-table-column>


        <!-- 优先级 -->
        <el-table-column
          label="优先级"
          width="100"
        >

          <template #default="{ row }">

            <el-tag
              :type="
                getPriorityType(
                  row.priority
                )
              "
              size="small"
            >

              {{
                getPriorityName(
                  row.priority
                )
              }}

            </el-tag>

          </template>

        </el-table-column>


        <!-- 状态 -->
        <el-table-column
          label="状态"
          width="110"
        >

          <template #default="{ row }">

            <el-tag
              :type="
                getStatusType(
                  row.status
                )
              "
              size="small"
            >

              {{
                getStatusName(
                  row.status
                )
              }}

            </el-tag>

          </template>

        </el-table-column>


        <!-- 创建人 -->
        <el-table-column
          prop="creatorName"
          label="创建人"
          width="120"
        />


        <!-- 负责人 -->
        <el-table-column
          label="负责人"
          min-width="150"
        >

          <template #default="{ row }">

            <span
              v-if="row.assigneeNames"
            >

              {{ row.assigneeNames }}

            </span>


            <span
              v-else
              class="empty-text"
            >

              待调度

            </span>

          </template>

        </el-table-column>


        <!-- 截止时间 -->
        <el-table-column
          label="截止时间"
          width="130"
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


        <!-- 操作 -->
        <el-table-column
          label="操作"
          width="100"
          fixed="right"
        >

          <template #default="{ row }">

            <el-button
              type="primary"
              link
              @click.stop="
                goTask(
                  row.id
                )
              "
            >

              查看

            </el-button>

          </template>

        </el-table-column>

      </el-table>


      <!-- 无数据 -->
      <el-empty
        v-if="
          !loading
          &&
          tasks.length === 0
        "
        description="暂无相关任务"
      />


      <!-- ============================== -->
      <!-- 分页 -->
      <!-- ============================== -->

      <div
        v-if="total > 0"
        class="pagination"
      >

        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[
            10,
            20,
            50
          ]"
          layout="
            total,
            sizes,
            prev,
            pager,
            next,
            jumper
          "
          @current-change="loadTasks"
          @size-change="handleSizeChange"
        />

      </div>

    </el-card>

  </div>

</template>


<script setup lang="ts">

import {
  onMounted,
  ref
} from 'vue'

import {
  useRoute,
  useRouter
} from 'vue-router'

import {
  getMyTasksApi
} from '@/api/myTask'

import type {
  MyTask,
  MyTaskPriority,
  MyTaskStatus,
  MyTaskType
} from '@/types/myTask'


const router =useRouter()

const route =useRoute()


/**
 * 当前分类
 */
const activeType =
  ref<MyTaskType>(

    (
      route.query.type as MyTaskType
    )
    ||
    'ASSIGNED'

  )


/**
 * 查询条件
 */
const keyword =
  ref('')


const status =
  ref<MyTaskStatus | undefined>(
    undefined
  )


/**
 * 数据
 */
const tasks =
  ref<MyTask[]>([])


/**
 * 分页
 */
const pageNum =
  ref(1)


const pageSize =
  ref(10)


const total =
  ref(0)


/**
 * Loading
 */
const loading =
  ref(false)


onMounted(
  async () => {

    await loadTasks()

  }
)


/**
 * ==============================
 * 加载任务
 * ==============================
 */
async function loadTasks() {

  loading.value =
    true


  try {

    const result =
      await getMyTasksApi(
        {
          type:
            activeType.value,

          pageNum:
            pageNum.value,

          pageSize:
            pageSize.value,

          keyword:
            keyword.value.trim()
            || undefined,

          status:
            (
              activeType.value === 'ASSIGNED'
              ||
              activeType.value === 'CREATED'
            )
              ? status.value
              : undefined
        }
      )


    tasks.value =
      result.data.records


    total.value =
      result.data.total


    /**
     * 如果删除/筛选后页码越界，
     * 回到最后有效页。
     */
    if (
      tasks.value.length === 0
      &&
      total.value > 0
      &&
      pageNum.value > 1
    ) {

      pageNum.value--


      await loadTasks()
    }

  }

  finally {

    loading.value =
      false
  }
}


/**
 * ==============================
 * Tab切换
 * ==============================
 */
async function handleTabChange() {

  /**
   * 切换分类以后：
   * 页码回到1。
   */
  pageNum.value =
    1


  /**
   * REVIEW / OVERDUE
   * 不使用status。
   */
  if (
    activeType.value === 'REVIEW'
    ||
    activeType.value === 'OVERDUE'
  ) {

    status.value =
      undefined
  }


  await loadTasks()
}


/**
 * ==============================
 * 搜索
 * ==============================
 */
async function handleSearch() {

  pageNum.value =
    1


  await loadTasks()
}


/**
 * ==============================
 * 重置
 * ==============================
 */
async function handleReset() {

  keyword.value =
    ''


  status.value =
    undefined


  pageNum.value =
    1


  await loadTasks()
}


/**
 * ==============================
 * 页大小
 * ==============================
 */
async function handleSizeChange() {

  pageNum.value =
    1


  await loadTasks()
}


/**
 * ==============================
 * 点击行
 * ==============================
 */
function handleRowClick(
  row: MyTask
) {

  goTask(
    row.id
  )
}


/**
 * ==============================
 * 任务详情
 * ==============================
 */
function goTask(
  taskId: number
) {

  router.push(
    `/tasks/${taskId}`
  )
}


/**
 * ==============================
 * 状态
 * ==============================
 */
function getStatusName(
  value: MyTaskStatus
) {

  const map:
    Record<MyTaskStatus, string> = {

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


  return map[value]
}


/**
 * ==============================
 * 状态Tag
 * ==============================
 */
function getStatusType(
  value: MyTaskStatus
):
  'info'
  | 'primary'
  | 'warning'
  | 'success'
  | 'danger' {

  switch (
    value
  ) {

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
 * ==============================
 * 优先级
 * ==============================
 */
function getPriorityName(
  value: MyTaskPriority
) {

  const map:
    Record<MyTaskPriority, string> = {

      LOW:
        '低',

      MEDIUM:
        '中',

      HIGH:
        '高',

      URGENT:
        '紧急'

    }


  return map[value]
}


/**
 * ==============================
 * 优先级Tag
 * ==============================
 */
function getPriorityType(
  value: MyTaskPriority
):
  'info'
  | 'primary'
  | 'warning'
  | 'danger' {

  switch (
    value
  ) {

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

</script>


<style scoped>

.my-task-page {

  width: 100%;

}


.page-header {

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


.filter-row {

  display: flex;

  align-items: center;

  gap: 12px;

  margin:
    10px 0 20px;

}


.task-table {

  width: 100%;

  cursor: pointer;

}


.project-name {

  color: #606266;

}


.task-title {

  color: #303133;

  font-weight: 500;

}


.overdue-text {

  margin-top: 4px;

  color: #f56c6c;

  font-size: 12px;

}


.deadline-overdue {

  color: #f56c6c;

  font-weight: 500;

}


.empty-text {

  color: #909399;

}


.pagination {

  display: flex;

  justify-content: flex-end;

  margin-top: 20px;

}

</style>