<template>

  <div class="operation-log-page">

    <!-- ================================================= -->
    <!-- 页面头部 -->
    <!-- ================================================= -->
    <div class="page-header">

      <div>

        <h2>
          项目操作日志
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


    <!-- ================================================= -->
    <!-- 筛选 -->
    <!-- ================================================= -->
    <el-card class="filter-card">

      <div class="filter-row">

        <!-- 关键词 -->
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索用户、操作或请求地址"
          style="width: 280px"
          @keyup.enter="loadLogs"
          @clear="loadLogs"
        />


        <!--
          项目操作日志只展示项目范围模块：

          PROJECT
          TASK
          COMMENT

          TEAM / USER / NOTIFICATION
          不属于当前页面作用域。
        -->
        <el-select
          v-model="module"
          clearable
          placeholder="项目内模块"
          style="width: 160px"
          @change="loadLogs"
        >

          <el-option
            v-for="item in projectModuleOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />

        </el-select>


        <!-- 执行结果 -->
        <el-select
          v-model="successFilter"
          clearable
          placeholder="执行结果"
          style="width: 140px"
          @change="loadLogs"
        >

          <el-option
            label="成功"
            :value="true"
          />

          <el-option
            label="失败"
            :value="false"
          />

        </el-select>


        <el-button
          type="primary"
          @click="loadLogs"
        >
          查询
        </el-button>


        <el-button
          @click="resetFilters"
        >
          重置
        </el-button>

      </div>

    </el-card>


    <!-- ================================================= -->
    <!-- 日志列表 -->
    <!-- ================================================= -->
    <el-card>

      <el-table
        v-loading="loading"
        :data="logs"
        stripe
        border
      >

        <!-- 操作时间 -->
        <el-table-column
          prop="createdTime"
          label="操作时间"
          min-width="170"
        >

          <template #default="{ row }">

            {{
              formatTime(
                row.createdTime
              )
            }}

          </template>

        </el-table-column>


        <!-- 用户 -->
        <el-table-column
          label="操作用户"
          min-width="130"
        >

          <template #default="{ row }">

            {{
              row.username
              ||
              (
                row.userId
                  ? `用户${row.userId}`
                  : '-'
              )
            }}

          </template>

        </el-table-column>


        <!-- 模块 -->
        <el-table-column
          label="模块"
          width="100"
          align="center"
        >

          <template #default="{ row }">

            <el-tag
              size="small"
              :type="
                getModuleTagType(
                  row.module
                )
              "
            >

              {{
                getModuleName(
                  row.module
                )
              }}

            </el-tag>

          </template>

        </el-table-column>


        <!-- 操作 -->
        <el-table-column
          prop="operation"
          label="操作"
          min-width="170"
        />


        <!-- 执行结果 -->
        <el-table-column
          label="结果"
          width="90"
          align="center"
        >

          <template #default="{ row }">

            <el-tag
              v-if="row.success"
              type="success"
              size="small"
            >
              成功
            </el-tag>


            <el-tag
              v-else
              type="danger"
              size="small"
            >
              失败
            </el-tag>

          </template>

        </el-table-column>


        <!-- HTTP方法 -->
        <el-table-column
          prop="httpMethod"
          label="方法"
          width="90"
          align="center"
        />


        <!-- IP -->
        <el-table-column
          prop="ip"
          label="IP"
          min-width="135"
        />


        <!-- 耗时 -->
        <el-table-column
          label="耗时"
          width="100"
          align="right"
        >

          <template #default="{ row }">

            {{
              row.costMs
              ?? 0
            }}
            ms

          </template>

        </el-table-column>


        <!-- 详情 -->
        <el-table-column
          label="详情"
          width="90"
          fixed="right"
          align="center"
        >

          <template #default="{ row }">

            <el-button
              link
              type="primary"
              @click="
                openDetail(row)
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
          logs.length === 0
        "
        description="暂无项目操作日志"
      />

    </el-card>


    <!-- ================================================= -->
    <!-- 日志详情 -->
    <!-- ================================================= -->
    <el-dialog
      v-model="detailVisible"
      title="操作日志详情"
      width="760px"
    >

      <el-descriptions
        v-if="selectedLog"
        :column="2"
        border
      >

        <!-- 用户 -->
        <el-descriptions-item
          label="操作用户"
        >
          {{
            selectedLog.username
            || '-'
          }}
        </el-descriptions-item>


        <el-descriptions-item
          label="用户ID"
        >
          {{
            selectedLog.userId
            ?? '-'
          }}
        </el-descriptions-item>


        <!-- 模块 -->
        <el-descriptions-item
          label="模块"
        >
          {{
            getModuleName(
              selectedLog.module
            )
          }}
        </el-descriptions-item>


        <!-- 操作 -->
        <el-descriptions-item
          label="操作"
        >
          {{
            selectedLog.operation
          }}
        </el-descriptions-item>


        <!-- 方法 -->
        <el-descriptions-item
          label="请求方法"
        >
          {{
            selectedLog.httpMethod
            || '-'
          }}
        </el-descriptions-item>


        <!-- 耗时 -->
        <el-descriptions-item
          label="耗时"
        >
          {{
            selectedLog.costMs
            ?? 0
          }}
          ms
        </el-descriptions-item>


        <!-- 项目ID -->
        <el-descriptions-item
          label="项目ID"
        >
          {{
            selectedLog.projectId
            ?? '-'
          }}
        </el-descriptions-item>


        <!-- 任务ID -->
        <el-descriptions-item
          label="任务ID"
        >
          {{
            selectedLog.taskId
            ?? '-'
          }}
        </el-descriptions-item>


        <!-- IP -->
        <el-descriptions-item
          label="IP"
        >
          {{
            selectedLog.ip
            || '-'
          }}
        </el-descriptions-item>


        <!-- 结果 -->
        <el-descriptions-item
          label="结果"
        >

          <el-tag
            :type="
              selectedLog.success
                ? 'success'
                : 'danger'
            "
          >

            {{
              selectedLog.success
                ? '成功'
                : '失败'
            }}

          </el-tag>

        </el-descriptions-item>


        <!-- URI -->
        <el-descriptions-item
          label="请求地址"
          :span="2"
        >
          {{
            selectedLog.requestUri
            || '-'
          }}
        </el-descriptions-item>


        <!-- 参数 -->
        <el-descriptions-item
          label="请求参数"
          :span="2"
        >

          <pre class="detail-code">{{
            formatJson(
              selectedLog.requestParams
            )
          }}</pre>

        </el-descriptions-item>


        <!-- 失败原因 -->
        <el-descriptions-item
          v-if="
            !selectedLog.success
          "
          label="失败原因"
          :span="2"
        >

          <span class="error-text">

            {{
              selectedLog.errorMessage
              || '-'
            }}

          </span>

        </el-descriptions-item>


        <!-- User-Agent -->
        <el-descriptions-item
          label="User-Agent"
          :span="2"
        >

          {{
            selectedLog.userAgent
            || '-'
          }}

        </el-descriptions-item>


        <!-- 时间 -->
        <el-descriptions-item
          label="操作时间"
          :span="2"
        >

          {{
            formatTime(
              selectedLog.createdTime
            )
          }}

        </el-descriptions-item>

      </el-descriptions>


      <template #footer>

        <el-button
          @click="
            detailVisible = false
          "
        >
          关闭
        </el-button>

      </template>

    </el-dialog>

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
  ElMessage
} from 'element-plus'


import {
  getProjectDetailApi
} from '@/api/project'


import {
  getProjectOperationLogsApi
} from '@/api/operationLog'


import type {
  ProjectDetail
} from '@/types/project'


import type {
  OperationLog
} from '@/types/operationLog'


const route =
  useRoute()


const router =
  useRouter()


const projectId =
  Number(
    route.params.projectId
  )


/**
 * 项目。
 */
const project =
  ref<ProjectDetail | null>(
    null
  )


/**
 * 日志。
 */
const logs =
  ref<OperationLog[]>([])


const loading =
  ref(false)


/**
 * 搜索条件。
 */
const keyword =
  ref('')


const module =
  ref('')


const successFilter =
  ref<boolean | undefined>(
    undefined
  )


/**
 * =========================================================
 * 当前页面真正支持的模块
 * =========================================================
 *
 * 这个页面叫：
 *
 * 项目操作日志
 *
 * 因此只显示：
 *
 * PROJECT
 * TASK
 * COMMENT
 *
 *
 * TEAM：
 * 属于团队维度。
 *
 * USER：
 * 属于用户 / 系统维度。
 *
 * NOTIFICATION：
 * 属于用户通知维度。
 *
 * 不应该放在项目日志筛选器里。
 */
const projectModuleOptions = [

  {
    label:
      '项目',

    value:
      'PROJECT'
  },

  {
    label:
      '任务',

    value:
      'TASK'
  },

  {
    label:
      '评论',

    value:
      'COMMENT'
  }

]


/**
 * 日志详情。
 */
const detailVisible =
  ref(false)


const selectedLog =
  ref<OperationLog | null>(
    null
  )


/**
 * =========================================================
 * 初始化
 * =========================================================
 */
onMounted(
  async () => {

    await loadPage()

  }
)


/**
 * =========================================================
 * 加载页面
 * =========================================================
 */
async function loadPage() {

  try {

    const projectResult =
      await getProjectDetailApi(
        projectId
      )


    project.value =
      projectResult.data


    await loadLogs()

  }
  catch (error) {

    console.error(
      '项目操作日志页面加载失败',
      error
    )


    /**
     * request.ts如果已经统一提示业务异常，
     * 这里主要用于页面加载失败兜底。
     */
    ElMessage.error(
      '项目操作日志加载失败'
    )
  }
}


/**
 * =========================================================
 * 加载日志
 * =========================================================
 */
async function loadLogs() {

  loading.value =
    true


  try {

    const result =
      await getProjectOperationLogsApi(

        projectId,

        {

          keyword:
            keyword.value.trim()
            ||
            undefined,


          module:
            module.value
            ||
            undefined,


          success:
            successFilter.value

        }

      )


    logs.value =
      result.data

  }
  catch (error) {

    console.error(
      '操作日志查询失败',
      error
    )

  }
  finally {

    loading.value =
      false
  }
}


/**
 * =========================================================
 * 重置
 * =========================================================
 */
async function resetFilters() {

  keyword.value =
    ''


  module.value =
    ''


  successFilter.value =
    undefined


  await loadLogs()
}


/**
 * =========================================================
 * 打开日志详情
 * =========================================================
 */
function openDetail(
  log: OperationLog
) {

  selectedLog.value =
    log


  detailVisible.value =
    true
}


/**
 * =========================================================
 * 返回项目
 * =========================================================
 */
function goProject() {

  router.push(
    `/projects/${projectId}`
  )
}


/**
 * =========================================================
 * 模块中文名称
 * =========================================================
 *
 * 虽然筛选器现在只提供三个项目模块，
 * 这里仍然保留其他名称映射。
 *
 * 原因：
 *
 * 如果数据库里已有历史日志，
 * 页面仍然能够正常显示其模块名称。
 */
function getModuleName(
  value: string
) {

  const map:
    Record<string, string> = {

      PROJECT:
        '项目',

      TASK:
        '任务',

      COMMENT:
        '评论',

      TEAM:
        '团队',

      USER:
        '用户',

      NOTIFICATION:
        '通知',

      OTHER:
        '其他'

    }


  return map[value]
    || value
}


/**
 * =========================================================
 * 模块标签颜色
 * =========================================================
 */
function getModuleTagType(
  value: string
):
  'primary'
  | 'success'
  | 'warning'
  | 'danger'
  | 'info' {

  switch (value) {

    case 'TASK':

      return 'primary'


    case 'PROJECT':

      return 'success'


    case 'COMMENT':

      return 'info'


    case 'TEAM':

      return 'warning'


    default:

      return 'info'
  }
}


/**
 * =========================================================
 * 时间显示
 * =========================================================
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


/**
 * =========================================================
 * JSON参数美化
 * =========================================================
 */
function formatJson(
  value?: string
) {

  if (!value) {

    return '-'
  }


  try {

    return JSON.stringify(

      JSON.parse(
        value
      ),

      null,

      2

    )

  }
  catch {

    return value
  }
}

</script>


<style scoped>

.operation-log-page {

  width: 100%;

}


/* ========================================================
   页面头部
   ======================================================== */

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


/* ========================================================
   页面作用域提示
   ======================================================== */

.scope-alert {

  margin-bottom: 16px;

}


/* ========================================================
   筛选
   ======================================================== */

.filter-card {

  margin-bottom: 16px;

}


.filter-row {

  display: flex;

  flex-wrap: wrap;

  gap: 10px;

  align-items: center;

}


/* ========================================================
   日志详情参数
   ======================================================== */

.detail-code {

  max-height: 280px;

  margin: 0;

  padding: 12px;

  overflow: auto;

  background: #f5f7fa;

  border-radius: 6px;

  white-space: pre-wrap;

  word-break: break-all;

  font-family:
    Consolas,
    Monaco,
    monospace;

  font-size: 12px;

  line-height: 1.6;

}


/* ========================================================
   错误
   ======================================================== */

.error-text {

  color: #f56c6c;

  word-break: break-all;

}

</style>