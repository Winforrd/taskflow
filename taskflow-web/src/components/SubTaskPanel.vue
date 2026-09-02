<template>

  <el-card class="subtask-card">

    <template #header>

      <div class="subtask-header">

        <div class="subtask-title">

          <span>
            子任务
          </span>

          <el-tag
            size="small"
            type="info"
          >

            {{ doneCount }}/{{ list.length }}

          </el-tag>

        </div>


        <el-button
          v-if="canCreate"
          type="primary"
          link
          @click="openCreate"
        >

          新增子任务

        </el-button>

      </div>

    </template>


    <div
      v-if="list.length > 0"
      class="subtask-list"
    >

      <div
        v-for="item in list"
        :key="item.id"
        class="subtask-item"
      >

        <div class="subtask-main">

          <el-checkbox
            :model-value="item.status === 'DONE'"
            :disabled="!item.canChangeStatus || statusLoadingId === item.id"
            @change="handleToggleStatus(item)"
          >

            <span
              class="subtask-name"
              :class="{
                done: item.status === 'DONE'
              }"
            >

              {{ item.title }}

            </span>

          </el-checkbox>


          <el-tag
            v-if="item.requiredFlag === 1"
            size="small"
            type="danger"
            effect="plain"
          >

            必需

          </el-tag>

        </div>


        <div class="subtask-actions">

          <el-button
            v-if="item.canEdit"
            link
            type="primary"
            @click="openEdit(item)"
          >

            编辑

          </el-button>


          <el-button
            v-if="item.canDelete"
            link
            type="danger"
            @click="handleDelete(item)"
          >

            删除

          </el-button>

        </div>

      </div>

    </div>


    <el-empty
      v-else
      :image-size="70"
      description="暂无子任务"
    />


    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑子任务' : '新增子任务'"
      width="520px"
      destroy-on-close
    >

      <el-form
        label-width="80px"
        @submit.prevent
      >

        <el-form-item label="标题">

          <el-input
            v-model="form.title"
            maxlength="200"
            show-word-limit
            placeholder="请输入子任务标题"
          />

        </el-form-item>


        <el-form-item label="描述">

          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="可选"
          />

        </el-form-item>


        <el-form-item label="必需">

          <el-checkbox
            v-model="form.requiredFlag"
            :true-value="1"
            :false-value="0"
          >

            完成任务前必须完成

          </el-checkbox>

        </el-form-item>

      </el-form>


      <template #footer>

        <el-button
          @click="dialogVisible = false"
        >

          取消

        </el-button>


        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >

          确定

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
  ElMessage,
  ElMessageBox
} from 'element-plus'

import {
  changeSubTaskStatus,
  createSubTask,
  deleteSubTask,
  getSubTasks,
  updateSubTask
} from '@/api/subTask'

import type {
  SubTask
} from '@/types/subTask'


const props =
  defineProps<{

    taskId: number

    canCreate: boolean

  }>()


const emit =
  defineEmits<{

    changed: []

  }>()


const list =
  ref<SubTask[]>([])


const dialogVisible =
  ref(false)


const submitting =
  ref(false)


const statusLoadingId =
  ref<number | null>(null)


const editingId =
  ref<number | null>(null)


const form =
  ref({

    title: '',

    description: '',

    requiredFlag: 1,

    sortOrder: 0

  })


const doneCount =
  computed(
    () =>
      list.value.filter(
        (item) =>
          item.status === 'DONE'
      ).length
  )


async function load() {

  const result =
    await getSubTasks(
      props.taskId
    )

  list.value =
    result.data
}


function resetForm() {

  form.value = {

    title: '',

    description: '',

    requiredFlag: 1,

    sortOrder: list.value.length

  }

  editingId.value =
    null
}


function openCreate() {

  resetForm()

  dialogVisible.value =
    true
}


function openEdit(
  item: SubTask
) {

  editingId.value =
    item.id

  form.value = {

    title: item.title,

    description:
      item.description || '',

    requiredFlag:
      item.requiredFlag ?? 1,

    sortOrder:
      item.sortOrder ?? 0

  }

  dialogVisible.value =
    true
}


async function handleSubmit() {

  const title =
    form.value.title.trim()

  if (!title) {

    ElMessage.warning(
      '请输入子任务标题'
    )

    return
  }

  submitting.value =
    true

  try {

    const payload = {

      title,

      description:
        form.value.description.trim()
        || undefined,

      requiredFlag:
        Number(
          form.value.requiredFlag
        ),

      sortOrder:
        form.value.sortOrder

    }

    if (editingId.value) {

      await updateSubTask(
        props.taskId,
        editingId.value,
        payload
      )

      ElMessage.success(
        '修改成功'
      )

    } else {

      await createSubTask(
        props.taskId,
        payload
      )

      ElMessage.success(
        '创建成功'
      )
    }

    dialogVisible.value =
      false

    await load()

    emit('changed')

  } finally {

    submitting.value =
      false
  }
}


async function handleToggleStatus(
  item: SubTask
) {

  if (!item.canChangeStatus) {

    return
  }

  statusLoadingId.value =
    item.id

  try {

    await changeSubTaskStatus(
      props.taskId,
      item.id
    )

    await load()

    emit('changed')

  } finally {

    statusLoadingId.value =
      null
  }
}


async function handleDelete(
  item: SubTask
) {

  try {

    await ElMessageBox.confirm(
      `确定删除子任务「${item.title}」吗？`,
      '删除确认',
      {

        type: 'warning',

        confirmButtonText: '删除',

        cancelButtonText: '取消'

      }
    )

  } catch {

    return
  }

  await deleteSubTask(
    props.taskId,
    item.id
  )

  ElMessage.success(
    '删除成功'
  )

  await load()

  emit('changed')
}


watch(
  () =>
    props.taskId,
  () => {

    load()
  }
)


onMounted(
  load
)

</script>



<style scoped>

.subtask-card {

  margin-top: 16px;

}


.subtask-header {

  display: flex;

  justify-content: space-between;

  align-items: center;

}


.subtask-title {

  display: flex;

  align-items: center;

  gap: 8px;

  font-weight: 600;

}


.subtask-list {

  display: flex;

  flex-direction: column;

  gap: 12px;

}


.subtask-item {

  display: flex;

  justify-content: space-between;

  align-items: flex-start;

  gap: 12px;

  padding: 10px 12px;

  border-radius: 8px;

  background: #fafafa;

}


.subtask-main {

  display: flex;

  align-items: flex-start;

  gap: 10px;

  flex: 1;

  min-width: 0;

}


.subtask-name {

  word-break: break-word;

}


.subtask-name.done {

  color: #909399;

  text-decoration: line-through;

}


.subtask-actions {

  display: flex;

  align-items: center;

  flex-shrink: 0;

}

</style>
