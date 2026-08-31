<template>

  <div class="extensions-page">

    <!-- ================================================= -->
    <!-- 页面头部 -->
    <!-- ================================================= -->
    <div class="page-header">

      <div>

        <h2>
          项目扩展能力
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
    <!-- 页面说明 -->
    <!-- ================================================= -->
    <el-alert
      v-if="project"
      type="info"
      :closable="false"
      show-icon
      class="description-alert"
    >

      <template #title>
        当前页面展示 TaskFlow 已预留的扩展能力。
      </template>


      <template #default>

        审批流、项目日历、工时管理和智能任务分派当前尚未启用，
        系统已经通过独立 Provider 接口完成架构预留，
        后续可在不修改任务核心业务的情况下增加具体实现。

      </template>

    </el-alert>


    <!-- ================================================= -->
    <!-- 页面主体 -->
    <!-- ================================================= -->
    <div
      v-loading="loading"
      class="page-content"
    >

      <!-- ================================================= -->
      <!-- 扩展能力 -->
      <!-- ================================================= -->
      <div
        v-if="
          project
          &&
          capabilities.length > 0
        "
        class="extension-grid"
      >

        <el-card
          v-for="item in capabilities"
          :key="item.code"
          class="extension-card"
          shadow="hover"
        >

          <!-- 卡片头 -->
          <div class="extension-header">

            <div class="extension-icon">

              {{
                getExtensionIcon(
                  item.code
                )
              }}

            </div>


            <div class="extension-title-area">

              <div class="extension-title">
                {{ item.name }}
              </div>


              <div class="extension-code">
                {{ item.code }}
              </div>

            </div>


            <el-tag
              v-if="item.enabled"
              type="success"
            >
              已启用
            </el-tag>


            <el-tag
              v-else
              type="info"
            >
              已预留
            </el-tag>

          </div>


          <!-- 功能说明 -->
          <div class="extension-description">

            {{ item.description }}

          </div>


          <!-- 当前状态 -->
          <div class="extension-footer">

            <span class="status-label">
              当前状态
            </span>


            <span
              :class="[
                'status-value',
                {
                  enabled:
                    item.enabled
                }
              ]"
            >

              {{
                item.enabled
                  ? '功能已启用'
                  : '接口已预留，功能未启用'
              }}

            </span>

          </div>

        </el-card>

      </div>


      <!-- ================================================= -->
      <!-- 有权限但没有扩展能力 -->
      <!-- ================================================= -->
      <el-empty
        v-else-if="
          !loading
          &&
          project
          &&
          capabilities.length === 0
        "
        description="暂无扩展能力"
      />

    </div>

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
  getProjectDetailApi
} from '@/api/project'


import {
  getProjectExtensionCapabilitiesApi
} from '@/api/extension'


import type {
  ProjectDetail
} from '@/types/project'


import type {
  ExtensionCapability,
  ExtensionCode
} from '@/types/extension'


const route =
  useRoute()


const router =
  useRouter()


/**
 * =========================================================
 * 当前项目ID
 * =========================================================
 */
const projectId =
  Number(
    route.params.projectId
  )


/**
 * =========================================================
 * 项目信息
 * =========================================================
 */
const project =
  ref<ProjectDetail | null>(
    null
  )


/**
 * =========================================================
 * 扩展能力
 * =========================================================
 */
const capabilities =
  ref<ExtensionCapability[]>([])


/**
 * =========================================================
 * 页面加载状态
 * =========================================================
 */
const loading =
  ref(false)


/**
 * =========================================================
 * 页面初始化
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
 *
 * 这里不能使用：
 *
 * Promise.all([
 *   getProjectDetailApi(),
 *   getProjectExtensionCapabilitiesApi()
 * ])
 *
 *
 * 原因：
 *
 * 如果当前用户没有项目访问权限，
 *
 * 两个请求都会同时到达后端，
 * 然后两个接口都会返回：
 *
 * “没有该项目操作权限”
 *
 * request.ts 会分别显示两次错误提示。
 *
 *
 * 正确流程：
 *
 * 第一步：
 * 先加载项目详情。
 *
 * 第二步：
 * 项目详情成功说明用户有访问权限。
 *
 * 第三步：
 * 再加载扩展能力。
 *
 *
 * 如果第一步失败：
 *
 * request.ts显示一次后端真实错误，
 * 然后直接结束，
 * 不再请求扩展能力接口。
 */
async function loadPage() {

  loading.value =
    true


  /**
   * 每次重新加载页面时，
   * 先清空旧数据。
   */
  project.value =
    null


  capabilities.value =
    []


  try {

    /**
     * =====================================================
     * 第一步：
     * 获取项目详情
     * =====================================================
     *
     * 这个接口本身已经具备项目数据权限校验。
     *
     * 如果用户无权限：
     *
     * await会直接抛异常，
     * 下面的扩展能力请求不会继续执行。
     */
    const projectResult =
      await getProjectDetailApi(
        projectId
      )


    project.value =
      projectResult.data


    /**
     * =====================================================
     * 第二步：
     * 项目访问成功以后再获取扩展能力
     * =====================================================
     */
    const capabilityResult =
      await getProjectExtensionCapabilitiesApi(
        projectId
      )


    capabilities.value =
      capabilityResult.data
      ?? []

  }
  catch (error) {

    /**
     * =====================================================
     * 这里故意不调用：
     *
     * ElMessage.error(...)
     * =====================================================
     *
     * 因为你的 request.ts 已经负责统一显示：
     *
     * 没有该项目操作权限
     *
     * 或其他后端业务异常。
     *
     *
     * 如果这里再次弹出：
     *
     * “扩展能力加载失败”
     *
     * 就会产生重复提示。
     *
     *
     * 当前catch只负责：
     *
     * 1. 阻止后续页面逻辑
     * 2. 清理页面数据
     * 3. 输出控制台日志方便开发排查
     */
    console.warn(
      '项目扩展能力页面加载失败',
      error
    )


    project.value =
      null


    capabilities.value =
      []

  }
  finally {

    loading.value =
      false
  }
}


/**
 * =========================================================
 * 返回项目
 * =========================================================
 */
function goProject() {

  /**
   * 如果当前用户没有项目权限，
   *
   * 点击“返回项目”仍然会进入：
   *
   * /projects/{projectId}
   *
   * 然后项目详情接口会再次进行后端权限判断。
   *
   * 为了避免无权限用户再次进入无权项目，
   * 当项目详情本身没有成功加载时，
   * 直接返回项目列表。
   */
  if (!project.value) {

    router.push(
      '/projects'
    )

    return
  }


  router.push(
    `/projects/${projectId}`
  )
}


/**
 * =========================================================
 * 扩展能力图标
 * =========================================================
 *
 * 当前不额外安装图标依赖，
 * 使用简单中文字符进行展示。
 */
function getExtensionIcon(
  code: ExtensionCode
) {

  switch (code) {

    case 'APPROVAL':

      return '审'


    case 'CALENDAR':

      return '历'


    case 'WORKTIME':

      return '时'


    case 'SMART_ASSIGNMENT':

      return '智'


    default:

      return '扩'
  }
}

</script>


<style scoped>

/* ========================================================
   页面
   ======================================================== */

.extensions-page {

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
   说明
   ======================================================== */

.description-alert {

  margin-bottom: 20px;

}


/* ========================================================
   页面主体
   ======================================================== */

.page-content {

  min-height: 260px;

}


/* ========================================================
   扩展能力卡片
   ======================================================== */

.extension-grid {

  display: grid;

  grid-template-columns:
    repeat(
      2,
      minmax(
        0,
        1fr
      )
    );

  gap: 18px;

}


.extension-card {

  min-height: 190px;

}


/* ========================================================
   扩展卡片头
   ======================================================== */

.extension-header {

  display: flex;

  align-items: center;

  gap: 12px;

}


.extension-icon {

  display: flex;

  align-items: center;

  justify-content: center;

  width: 44px;

  height: 44px;

  flex-shrink: 0;

  border-radius: 10px;

  background: #ecf5ff;

  color: #409eff;

  font-size: 20px;

  font-weight: 600;

}


.extension-title-area {

  min-width: 0;

  flex: 1;

}


.extension-title {

  color: #303133;

  font-size: 17px;

  font-weight: 600;

}


.extension-code {

  margin-top: 4px;

  color: #a8abb2;

  font-size: 11px;

}


/* ========================================================
   描述
   ======================================================== */

.extension-description {

  min-height: 48px;

  margin-top: 20px;

  color: #606266;

  font-size: 14px;

  line-height: 1.7;

}


/* ========================================================
   状态
   ======================================================== */

.extension-footer {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  margin-top: 22px;

  padding-top: 14px;

  border-top:
    1px solid #ebeef5;

}


.status-label {

  color: #909399;

  font-size: 13px;

}


.status-value {

  color: #909399;

  font-size: 13px;

}


.status-value.enabled {

  color: #67c23a;

}


/* ========================================================
   响应式
   ======================================================== */

@media (
  max-width: 900px
) {

  .extension-grid {

    grid-template-columns:
      1fr;

  }

}

</style>