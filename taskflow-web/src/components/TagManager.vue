<template>

  <el-card
    class="tag-manager"
    shadow="never"
  >

    <!-- ================================================= -->
    <!-- 卡片头 -->
    <!-- ================================================= -->
    <template #header>

      <div class="manager-header">

        <div class="header-text">

          <div class="title">
            {{ title }}
          </div>


          <div class="subtitle">

            {{
              scopeType === 'TEAM'
                ? '团队公共标签可以在团队内各项目中复用'
                : '项目标签仅用于当前项目'
            }}

          </div>

        </div>


        <el-button
          v-if="canManage"
          type="primary"
          @click="openCreate"
        >

          新建标签

        </el-button>

      </div>

    </template>


    <!-- ================================================= -->
    <!-- 标签内容 -->
    <!-- ================================================= -->
    <div
      v-loading="loading"
      class="tag-list"
    >

      <div
        v-if="tags.length > 0"
        class="tag-grid"
      >

        <div
          v-for="tag in tags"
          :key="tag.id"
          class="tag-item"
        >

          <div class="tag-info">

            <!-- 颜色点 -->
            <span
              class="color-dot"
              :style="{
                backgroundColor:
                  tag.color
              }"
            />


            <!-- 标签 -->
            <el-tag
              effect="dark"
              :color="tag.color"
              class="tag"
            >

              {{ tag.name }}

            </el-tag>


            <!-- 标签来源 -->
            <span class="scope-name">

              {{
                tag.scopeType === 'TEAM'
                  ? '团队标签'
                  : '项目标签'
              }}

            </span>

          </div>


          <!-- 操作 -->
          <div
            v-if="canManage"
            class="operations"
          >

            <el-button
              link
              type="primary"
              @click="openEdit(tag)"
            >

              编辑

            </el-button>


            <el-button
              link
              type="danger"
              @click="removeTag(tag)"
            >

              删除

            </el-button>

          </div>

        </div>

      </div>


      <el-empty
        v-if="
          !loading
          &&
          tags.length === 0
        "
        :image-size="80"
        :description="
          scopeType === 'TEAM'
            ? '暂无团队标签'
            : '暂无项目标签'
        "
      />

    </div>


    <!-- ================================================= -->
    <!-- 新增 / 编辑标签 -->
    <!-- ================================================= -->
    <el-dialog
      v-model="dialogVisible"
      :title="
        editingTag
          ? '编辑标签'
          : (
              scopeType === 'TEAM'
                ? '新建团队标签'
                : '新建项目标签'
            )
      "
      width="480px"
      destroy-on-close
    >

      <el-form
        :model="form"
        label-width="80px"
      >

        <!-- 标签名称 -->
        <el-form-item
          label="名称"
          required
        >

          <el-input
            v-model="form.name"
            maxlength="50"
            show-word-limit
            clearable
            placeholder="请输入标签名称"
          />

        </el-form-item>


        <!-- 标签颜色 -->
        <el-form-item
          label="颜色"
          required
        >

          <div class="color-editor">

            <!-- Element Plus颜色选择器 -->
            <el-color-picker
              v-model="form.color"
              :predefine="predefineColors"
            />


            <!-- 手工HEX输入 -->
            <el-input
              v-model="form.color"
              maxlength="7"
              class="color-input"
              placeholder="#409EFF"
              @blur="normalizeInputColor"
            />


            <!--
              这里是真正的实时预览。

              不再使用“预览按钮”。

              名称、颜色发生改变时，
              这个标签会立即跟着变化。
            -->
            <div class="preview-area">

              <span class="preview-label">
                预览
              </span>


              <el-tag
                effect="dark"
                :color="
                  previewColor
                "
                class="preview-tag"
              >

                {{
                  form.name.trim()
                  || '标签预览'
                }}

              </el-tag>

            </div>

          </div>


          <div
            v-if="
              form.color
              &&
              !isColorValid
            "
            class="color-error"
          >

            请输入合法的十六进制颜色，例如 #409EFF

          </div>

        </el-form-item>


        <!-- 标签作用域提示 -->
        <el-form-item label="作用范围">

          <el-tag
            v-if="
              scopeType === 'TEAM'
            "
            type="success"
          >

            团队公共标签

          </el-tag>


          <el-tag
            v-else
            type="primary"
          >

            当前项目

          </el-tag>


          <div class="scope-tip">

            {{
              scopeType === 'TEAM'
                ? '团队标签创建后，团队内项目都可以在任务上使用该标签。'
                : '项目标签只能在当前项目的任务上使用。'
            }}

          </div>

        </el-form-item>

      </el-form>


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
          :loading="submitting"
          @click="submit"
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
  reactive,
  ref,
  watch
} from 'vue'


import {
  ElMessage,
  ElMessageBox
} from 'element-plus'


import {
  createProjectTagApi,
  createTeamTagApi,
  deleteTagApi,
  getProjectTagsApi,
  getTeamTagsApi,
  updateTagApi
} from '@/api/tag'


import type {
  Tag,
  TagScopeType
} from '@/types/tag'


const props =
  withDefaults(

    defineProps<{

      scopeType: TagScopeType

      scopeId: number

      title?: string

    }>(),

    {
      title:
        '标签管理'
    }

  )


/**
 * =========================================================
 * 预设颜色
 * =========================================================
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


const tags =
  ref<Tag[]>([])


const canManage =
  ref(false)


const loading =
  ref(false)


const submitting =
  ref(false)


const dialogVisible =
  ref(false)


const editingTag =
  ref<Tag | null>(
    null
  )


const form =
  reactive({

    name: '',

    color:
      '#409EFF'

  })


/**
 * =========================================================
 * 当前颜色是否合法
 * =========================================================
 */
const isColorValid =
  computed(
    () => {

      return /^#[0-9A-Fa-f]{6}$/
        .test(
          form.color
        )
    }
  )


/**
 * =========================================================
 * 预览颜色
 * =========================================================
 *
 * 输入合法：
 * 使用当前颜色。
 *
 * 输入非法：
 * 使用默认蓝色，避免组件得到非法背景色。
 */
const previewColor =
  computed(
    () => {

      return isColorValid.value

        ? form.color

        : '#409EFF'
    }
  )


onMounted(
  async () => {

    await load()

  }
)


watch(
  () => [
    props.scopeType,
    props.scopeId
  ],
  async () => {

    await load()

  }
)


/**
 * =========================================================
 * 加载标签
 * =========================================================
 */
async function load() {

  if (
    !props.scopeId
  ) {

    return
  }


  loading.value =
    true


  try {

    const result =
      props.scopeType === 'TEAM'

        ? await getTeamTagsApi(
            props.scopeId
          )

        : await getProjectTagsApi(
            props.scopeId
          )


    tags.value =
      result.data.tags
      ?? []


    canManage.value =
      Boolean(
        result.data.canManage
      )

  }
  finally {

    loading.value =
      false
  }
}


/**
 * =========================================================
 * 打开新建标签
 * =========================================================
 */
function openCreate() {

  editingTag.value =
    null


  form.name =
    ''


  form.color =
    '#409EFF'


  dialogVisible.value =
    true
}


/**
 * =========================================================
 * 打开编辑
 * =========================================================
 */
function openEdit(
  tag: Tag
) {

  editingTag.value =
    tag


  form.name =
    tag.name


  form.color =
    tag.color
    ||
    '#409EFF'


  dialogVisible.value =
    true
}


/**
 * =========================================================
 * 手工颜色输入格式化
 * =========================================================
 */
function normalizeInputColor() {

  let value =
    form.color
      .trim()


  if (!value) {

    form.color =
      '#409EFF'

    return
  }


  /**
   * 输入：
   *
   * 409EFF
   *
   * 自动转换：
   *
   * #409EFF
   */
  if (
    !value.startsWith('#')
  ) {

    value =
      `#${value}`
  }


  form.color =
    value.toUpperCase()
}


/**
 * =========================================================
 * 提交
 * =========================================================
 */
async function submit() {

  const name =
    form.name.trim()


  if (!name) {

    ElMessage.warning(
      '请输入标签名称'
    )

    return
  }


  normalizeInputColor()


  if (
    !isColorValid.value
  ) {

    ElMessage.warning(
      '请输入正确的标签颜色'
    )

    return
  }


  submitting.value =
    true


  try {

    /**
     * =====================================================
     * 编辑
     * =====================================================
     */
    if (
      editingTag.value
    ) {

      await updateTagApi(

        editingTag.value.id,

        {
          name,

          color:
            form.color
        }

      )


      ElMessage.success(
        '标签修改成功'
      )

    }

    /**
     * =====================================================
     * 团队标签
     * =====================================================
     */
    else if (
      props.scopeType === 'TEAM'
    ) {

      await createTeamTagApi(

        props.scopeId,

        {
          name,

          color:
            form.color
        }

      )


      ElMessage.success(
        '团队标签创建成功'
      )

    }

    /**
     * =====================================================
     * 项目标签
     * =====================================================
     */
    else {

      await createProjectTagApi(

        props.scopeId,

        {
          name,

          color:
            form.color
        }

      )


      ElMessage.success(
        '项目标签创建成功'
      )
    }


    dialogVisible.value =
      false


    await load()

  }
  finally {

    submitting.value =
      false
  }
}


/**
 * =========================================================
 * 删除标签
 * =========================================================
 */
async function removeTag(
  tag: Tag
) {

  try {

    await ElMessageBox.confirm(

      `确定删除标签“${tag.name}”吗？删除后会自动解除所有任务上的该标签。`,

      '删除标签',

      {
        type:
          'warning',

        confirmButtonText:
          '删除',

        cancelButtonText:
          '取消'
      }

    )

  }
  catch {

    return
  }


  await deleteTagApi(
    tag.id
  )


  ElMessage.success(
    '标签已删除'
  )


  await load()
}

</script>


<style scoped>

.tag-manager {

  width: 100%;

  margin: 0;

}


/* ========================================================
   标题
   ======================================================== */

.manager-header {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  gap: 20px;

}


.header-text {

  min-width: 0;

}


.title {

  font-size: 17px;

  font-weight: 600;

  color: #303133;

}


.subtitle {

  margin-top: 6px;

  color: #909399;

  font-size: 13px;

  line-height: 1.5;

}


/* ========================================================
   标签列表
   ======================================================== */

.tag-list {

  min-height: 100px;

}


.tag-grid {

  display: grid;

  grid-template-columns:
    repeat(
      auto-fill,
      minmax(
        260px,
        1fr
      )
    );

  gap: 12px;

}


.tag-item {

  display: flex;

  align-items: center;

  justify-content:
    space-between;

  gap: 14px;

  min-height: 56px;

  padding:
    10px 14px;

  border:
    1px solid #ebeef5;

  border-radius: 6px;

  background: #ffffff;

}


.tag-info {

  display: flex;

  align-items: center;

  gap: 9px;

  min-width: 0;

}


.color-dot {

  width: 10px;

  height: 10px;

  flex-shrink: 0;

  border-radius: 50%;

}


.tag {

  border: 0;

  color: #ffffff;

  max-width: 160px;

}


.scope-name {

  color: #909399;

  font-size: 12px;

  white-space: nowrap;

}


.operations {

  display: flex;

  align-items: center;

  flex-shrink: 0;

}


.operations :deep(
  .el-button + .el-button
) {

  margin-left: 4px;

}


/* ========================================================
   颜色编辑
   ======================================================== */

.color-editor {

  display: flex;

  align-items: center;

  flex-wrap: wrap;

  gap: 10px;

  width: 100%;

}


.color-input {

  width: 140px;

}


.preview-area {

  display: flex;

  align-items: center;

  gap: 8px;

}


.preview-label {

  color: #909399;

  font-size: 13px;

}


.preview-tag {

  min-width: 72px;

  border: 0;

  color: #ffffff;

  text-align: center;

}


.color-error {

  width: 100%;

  margin-top: 6px;

  color: #f56c6c;

  font-size: 12px;

}


/* ========================================================
   作用范围说明
   ======================================================== */

.scope-tip {

  width: 100%;

  margin-top: 7px;

  color: #909399;

  font-size: 12px;

  line-height: 1.5;

}


/* ========================================================
   响应式
   ======================================================== */

@media (
  max-width: 760px
) {

  .manager-header {

    align-items:
      flex-start;

    flex-direction:
      column;

  }


  .tag-grid {

    grid-template-columns:
      1fr;

  }


  .tag-item {

    align-items:
      flex-start;

  }

}

</style>