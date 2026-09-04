<template>

  <el-card
    class="task-tag-panel"
    shadow="never"
  >

    <!-- ================================================= -->
    <!-- 标题 -->
    <!-- ================================================= -->
    <template #header>

      <div class="panel-header">

        <div>

          <div class="panel-title">
            任务标签
          </div>

          <div class="panel-description">
            可同时使用当前团队公共标签和当前项目标签
          </div>

        </div>


        <el-button
          v-if="context?.canEdit"
          type="primary"
          link
          @click="openManager"
        >
          管理标签
        </el-button>

      </div>

    </template>


    <!-- ================================================= -->
    <!-- 当前任务标签 -->
    <!-- ================================================= -->
    <div
      v-loading="loading"
      class="panel-content"
    >

      <div
        v-if="
          context
          &&
          context.selectedTags.length > 0
        "
        class="selected-tags"
      >

        <el-tag
          v-for="tag in context.selectedTags"
          :key="tag.id"
          effect="dark"
          :color="tag.color"
          class="tag-item"
        >

          {{ tag.name }}

        </el-tag>

      </div>


      <el-empty
        v-else-if="!loading"
        :image-size="60"
        description="当前任务暂无标签"
      />

    </div>


    <!-- ================================================= -->
    <!-- 管理任务标签弹窗 -->
    <!-- ================================================= -->
    <el-dialog
      v-model="dialogVisible"
      title="管理任务标签"
      width="620px"
      destroy-on-close
    >

      <!-- ================================================= -->
      <!-- 标签选择 -->
      <!-- ================================================= -->
      <div class="section">

        <div class="section-title">
          选择标签
        </div>


        <div class="section-description">
          团队公共标签和当前项目标签都可以绑定到当前任务。
        </div>


        <el-select
          v-model="selectedIds"
          multiple
          filterable
          clearable
          collapse-tags
          :max-collapse-tags="4"
          placeholder="请选择标签"
          class="tag-select"
        >

          <!-- ================================================= -->
          <!-- 团队标签 -->
          <!-- ================================================= -->
          <el-option-group
            v-if="teamTags.length > 0"
            label="团队标签"
          >

            <el-option
              v-for="tag in teamTags"
              :key="tag.id"
              :value="tag.id"
              :label="tag.name"
            >

              <div class="option-row">

                <span
                  class="color-dot"
                  :style="{
                    backgroundColor:
                      tag.color
                  }"
                />


                <span class="option-name">
                  {{ tag.name }}
                </span>


                <span class="option-scope">
                  团队
                </span>

              </div>

            </el-option>

          </el-option-group>


          <!-- ================================================= -->
          <!-- 项目标签 -->
          <!-- ================================================= -->
          <el-option-group
            v-if="projectTags.length > 0"
            label="项目标签"
          >

            <el-option
              v-for="tag in projectTags"
              :key="tag.id"
              :value="tag.id"
              :label="tag.name"
            >

              <div class="option-row">

                <span
                  class="color-dot"
                  :style="{
                    backgroundColor:
                      tag.color
                  }"
                />


                <span class="option-name">
                  {{ tag.name }}
                </span>


                <span class="option-scope">
                  项目
                </span>

              </div>

            </el-option>

          </el-option-group>

        </el-select>


        <div
          v-if="
            teamTags.length === 0
            &&
            projectTags.length === 0
          "
          class="no-tag-tip"
        >

          当前团队和项目还没有可用标签。

        </div>

      </div>


      <!-- ================================================= -->
      <!-- 快速创建项目标签 -->
      <!-- ================================================= -->
      <template
        v-if="
          context?.canManageProjectTags
        "
      >

        <el-divider>
          快速创建项目标签
        </el-divider>


        <div class="quick-create-section">

          <div class="quick-create-description">

            在这里创建的标签属于当前项目。
            创建成功后会自动加入上面的选择列表，并自动选中。

          </div>


          <div class="quick-create-form">

            <!-- 标签名称 -->
            <el-input
              v-model="newTagName"
              maxlength="50"
              show-word-limit
              clearable
              placeholder="请输入新标签名称"
              class="new-tag-name"
              @keyup.enter="quickCreateProjectTag"
            />


            <!-- 颜色选择 -->
            <el-color-picker
              v-model="newTagColor"
              :predefine="predefineColors"
            />


            <!-- 颜色文本 -->
            <el-input
              v-model="newTagColor"
              maxlength="7"
              class="new-tag-color"
              placeholder="#409EFF"
              @blur="normalizeNewTagColor"
            />


            <!-- 创建 -->
            <el-button
              type="primary"
              :loading="creatingTag"
              @click="quickCreateProjectTag"
            >

              创建

            </el-button>

          </div>


          <!-- ================================================= -->
          <!-- 实时预览 -->
          <!-- ================================================= -->
          <div class="quick-preview">

            <span class="preview-label">
              预览：
            </span>


            <el-tag
              effect="dark"
              :color="previewNewTagColor"
              class="preview-tag"
            >

              {{
                newTagName.trim()
                || '新标签'
              }}

            </el-tag>

          </div>


          <div
            v-if="
              newTagColor
              &&
              !isNewTagColorValid
            "
            class="color-error"
          >

            标签颜色格式应为 #RRGGBB，例如 #409EFF

          </div>

        </div>

      </template>


      <!-- ================================================= -->
      <!-- 没有创建项目标签权限 -->
      <!-- ================================================= -->
      <el-alert
        v-else
        type="info"
        :closable="false"
        show-icon
        class="permission-tip"
      >

        当前账号可以使用已有标签，但没有创建项目标签的权限。

      </el-alert>


      <!-- ================================================= -->
      <!-- 底部 -->
      <!-- ================================================= -->
      <template #footer>

        <el-button
          @click="
            dialogVisible = false
          "
        >

          取消

        </el-button>


        <el-button
          type="primary"
          :loading="saving"
          @click="save"
        >

          保存

        </el-button>

      </template>

    </el-dialog>

  </el-card>

</template>


<script setup lang="ts">

import {
  computed,
  onMounted,
  ref,
  watch
} from 'vue'


import {
  ElMessage
} from 'element-plus'


/**
 * =========================================================
 * 标签API
 * =========================================================
 */
import {
  createProjectTagApi,
  getTaskTagContextApi,
  updateTaskTagsApi
} from '@/api/tag'


/**
 * =========================================================
 * 任务详情API
 * =========================================================
 *
 * 这里用任务详情获取：
 *
 * projectId
 *
 * 这样父页面不需要额外传projectId，
 * TaskTagPanel组件自己就能正常工作。
 */
import {
  getTaskDetailApi
} from '@/api/task'


import type {
  Tag,
  TaskTagContext
} from '@/types/tag'


const props =
  defineProps<{

    taskId: number

  }>()


const emit =
  defineEmits<{

    (
      e:
        'changed'
    ): void

  }>()


/**
 * =========================================================
 * 当前任务所属项目ID
 * =========================================================
 */
const projectId =
  ref<number | null>(
    null
  )


/**
 * =========================================================
 * 标签上下文
 * =========================================================
 */
const context =
  ref<TaskTagContext | null>(
    null
  )


const loading =
  ref(false)


const saving =
  ref(false)


const creatingTag =
  ref(false)


const dialogVisible =
  ref(false)


/**
 * 当前弹窗中已经选择的标签ID。
 */
const selectedIds =
  ref<number[]>([])


/**
 * =========================================================
 * 快速创建项目标签
 * =========================================================
 */
const newTagName =
  ref('')


const newTagColor =
  ref('#409EFF')


/**
 * 常用颜色。
 */
const predefineColors = [

  '#409EFF',

  '#67C23A',

  '#E6A23C',

  '#F56C6C',

  '#909399',

  '#8E44AD',

  '#16A085',

  '#2C3E50',

  '#E67E22',

  '#E84393'

]


/**
 * =========================================================
 * 团队标签
 * =========================================================
 */
const teamTags =
  computed<Tag[]>(
    () => {

      return context.value
        ?.availableTags
        ?.filter(
          tag =>
            tag.scopeType
            === 'TEAM'
        )
        ?? []
    }
  )


/**
 * =========================================================
 * 项目标签
 * =========================================================
 */
const projectTags =
  computed<Tag[]>(
    () => {

      return context.value
        ?.availableTags
        ?.filter(
          tag =>
            tag.scopeType
            === 'PROJECT'
        )
        ?? []
    }
  )


/**
 * =========================================================
 * 新标签颜色是否合法
 * =========================================================
 */
const isNewTagColorValid =
  computed(
    () => {

      return /^#[0-9A-Fa-f]{6}$/
        .test(
          newTagColor.value
        )
    }
  )


/**
 * =========================================================
 * 新标签实时预览颜色
 * =========================================================
 */
const previewNewTagColor =
  computed(
    () => {

      return isNewTagColorValid.value

        ? newTagColor.value

        : '#409EFF'
    }
  )


/**
 * =========================================================
 * 初始化
 * =========================================================
 */
onMounted(
  async () => {

    await load()

  }
)


watch(
  () => props.taskId,
  async () => {

    await load()

  }
)


/**
 * =========================================================
 * 加载任务 + 标签
 * =========================================================
 */
async function load() {

  if (
    !props.taskId
  ) {

    return
  }


  loading.value =
    true


  try {

    /**
     * 并行获取：
     *
     * 1. 任务详情
     * 2. 标签上下文
     */
    const [
      taskResult,
      tagResult
    ] =
      await Promise.all([

        getTaskDetailApi(
          props.taskId
        ),

        getTaskTagContextApi(
          props.taskId
        )

      ])


    /**
     * Task本身已经有：
     *
     * projectId
     */
    projectId.value =
      Number(
        taskResult.data.projectId
      )


    context.value =
      tagResult.data

  }
  finally {

    loading.value =
      false
  }
}


/**
 * =========================================================
 * 只刷新标签上下文
 * =========================================================
 *
 * 快速创建标签时不需要再次请求任务详情。
 */
async function reloadTagContext() {

  const result =
    await getTaskTagContextApi(
      props.taskId
    )


  context.value =
    result.data
}


/**
 * =========================================================
 * 打开管理弹窗
 * =========================================================
 */
function openManager() {

  if (
    !context.value
    ||
    !context.value.canEdit
  ) {

    return
  }


  /**
   * 当前已有标签作为默认选择。
   */
  selectedIds.value =
    context.value
      .selectedTags
      .map(
        tag =>
          tag.id
      )


  /**
   * 每次打开都清空快速创建输入。
   */
  newTagName.value =
    ''


  newTagColor.value =
    '#409EFF'


  dialogVisible.value =
    true
}


/**
 * =========================================================
 * 格式化新标签颜色
 * =========================================================
 */
function normalizeNewTagColor() {

  let value =
    newTagColor.value
      .trim()


  if (!value) {

    newTagColor.value =
      '#409EFF'

    return
  }


  /**
   * 用户输入：
   *
   * F56C6C
   *
   * 自动转换：
   *
   * #F56C6C
   */
  if (
    !value.startsWith('#')
  ) {

    value =
      `#${value}`
  }


  newTagColor.value =
    value.toUpperCase()
}


/**
 * =========================================================
 * 快速创建项目标签
 * =========================================================
 */
async function quickCreateProjectTag() {

  /**
   * =====================================================
   * 权限
   * =====================================================
   */
  if (
    !context.value
    ||
    !context.value.canManageProjectTags
  ) {

    ElMessage.warning(
      '没有创建项目标签的权限'
    )

    return
  }


  /**
   * =====================================================
   * 项目ID
   * =====================================================
   */
  if (
    !projectId.value
  ) {

    ElMessage.error(
      '无法获取当前任务所属项目'
    )

    return
  }


  /**
   * =====================================================
   * 标签名称
   * =====================================================
   */
  const name =
    newTagName.value.trim()


  if (!name) {

    ElMessage.warning(
      '请输入标签名称'
    )

    return
  }


  /**
   * =====================================================
   * 颜色
   * =====================================================
   */
  normalizeNewTagColor()


  if (
    !isNewTagColorValid.value
  ) {

    ElMessage.warning(
      '请输入正确的标签颜色'
    )

    return
  }


  creatingTag.value =
    true


  try {

    /**
     * =====================================================
     * 真正创建项目标签
     * =====================================================
     *
     * 以前这里没有调用接口，
     * 所以“快速创建”实际上没有作用。
     *
     * 现在调用：
     *
     * POST
     * /projects/{projectId}/tags
     */
    const result =
      await createProjectTagApi(

        projectId.value,

        {
          name,

          color:
            newTagColor.value
        }

      )


    /**
     * 后端返回新建的Tag。
     */
    const createdTag =
      result.data


    /**
     * =====================================================
     * 刷新标签列表
     * =====================================================
     *
     * 创建以后：
     *
     * availableTags
     *
     * 立即包含刚才创建的新标签。
     */
    await reloadTagContext()


    /**
     * =====================================================
     * 自动选中新标签
     * =====================================================
     *
     * 用户不需要：
     *
     * 创建
     * ↓
     * 再到选择框搜索
     * ↓
     * 再选一次
     *
     * 创建成功以后直接加入当前待保存列表。
     */
    if (
      createdTag?.id
      &&
      !selectedIds.value
        .includes(
          createdTag.id
        )
    ) {

      selectedIds.value =
        [
          ...selectedIds.value,
          createdTag.id
        ]
    }


    /**
     * 清空名称，
     * 方便继续创建第二个。
     */
    newTagName.value =
      ''


    newTagColor.value =
      '#409EFF'


    ElMessage.success(
      '项目标签创建成功，已自动选中'
    )

  }
  finally {

    creatingTag.value =
      false
  }
}


/**
 * =========================================================
 * 保存任务标签
 * =========================================================
 */
async function save() {

  if (
    !context.value
    ||
    !context.value.canEdit
  ) {

    ElMessage.warning(
      '没有修改任务标签的权限'
    )

    return
  }


  saving.value =
    true


  try {

    /**
     * =====================================================
     * 完整覆盖当前任务标签
     * =====================================================
     */
    await updateTaskTagsApi(

      props.taskId,

      {
        tagIds:
          selectedIds.value
      }

    )


    ElMessage.success(
      '任务标签已更新'
    )


    dialogVisible.value =
      false


    /**
     * 刷新任务标签。
     */
    await reloadTagContext()


    /**
     * 通知父页面：
     *
     * 可以重新加载任务动态。
     */
    emit(
      'changed'
    )

  }
  finally {

    saving.value =
      false
  }
}

</script>


<style scoped>

/* ========================================================
   整体
   ======================================================== */

.task-tag-panel {

  margin-top: 16px;

}


/* ========================================================
   标题
   ======================================================== */

.panel-header {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  gap: 20px;

}


.panel-title {

  color: #303133;

  font-size: 16px;

  font-weight: 600;

}


.panel-description {

  margin-top: 5px;

  color: #909399;

  font-size: 12px;

}


/* ========================================================
   当前标签
   ======================================================== */

.panel-content {

  min-height: 50px;

}


.selected-tags {

  display: flex;

  flex-wrap: wrap;

  gap: 8px;

}


.tag-item {

  border: 0;

  color: #ffffff;

}


/* ========================================================
   弹窗区域
   ======================================================== */

.section {

  margin-bottom: 10px;

}


.section-title {

  margin-bottom: 5px;

  color: #303133;

  font-size: 15px;

  font-weight: 600;

}


.section-description {

  margin-bottom: 14px;

  color: #909399;

  font-size: 12px;

}


.tag-select {

  width: 100%;

}


/* ========================================================
   Select标签选项
   ======================================================== */

.option-row {

  display: flex;

  align-items: center;

  width: 100%;

  gap: 8px;

}


.color-dot {

  width: 10px;

  height: 10px;

  flex-shrink: 0;

  border-radius: 50%;

}


.option-name {

  flex: 1;

}


.option-scope {

  color: #909399;

  font-size: 12px;

}


.no-tag-tip {

  margin-top: 8px;

  color: #909399;

  font-size: 12px;

}


/* ========================================================
   快速创建
   ======================================================== */

.quick-create-section {

  padding:
    4px 0;

}


.quick-create-description {

  margin-bottom: 12px;

  color: #909399;

  font-size: 12px;

  line-height: 1.6;

}


.quick-create-form {

  display: grid;

  grid-template-columns:
    minmax(
      0,
      1fr
    )
    auto
    120px
    auto;

  gap: 10px;

  align-items: center;

}


.new-tag-name {

  min-width: 0;

}


.new-tag-color {

  width: 120px;

}


/* ========================================================
   快速创建预览
   ======================================================== */

.quick-preview {

  display: flex;

  align-items: center;

  gap: 8px;

  margin-top: 12px;

}


.preview-label {

  color: #909399;

  font-size: 12px;

}


.preview-tag {

  min-width: 65px;

  border: 0;

  color: #ffffff;

  text-align: center;

}


.color-error {

  margin-top: 7px;

  color: #f56c6c;

  font-size: 12px;

}


/* ========================================================
   权限提示
   ======================================================== */

.permission-tip {

  margin-top: 18px;

}


/* ========================================================
   小屏幕
   ======================================================== */

@media (
  max-width: 700px
) {

  .quick-create-form {

    grid-template-columns:
      1fr;

  }


  .new-tag-color {

    width: 100%;

  }

}

</style>