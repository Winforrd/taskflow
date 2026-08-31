<template>

  <el-card
    class="avatar-card"
    shadow="never"
  >

    <template #header>

      <div class="card-header">

        <span>
          个人头像
        </span>

      </div>

    </template>


    <div class="avatar-content">

      <!-- ============================================= -->
      <!-- 当前头像 -->
      <!-- ============================================= -->
      <div class="avatar-preview">

        <el-avatar
          :size="100"
          :src="userStore.userInfo?.avatar"
        >

          {{
            getAvatarText()
          }}

        </el-avatar>

      </div>


      <!-- ============================================= -->
      <!-- 操作区域 -->
      <!-- ============================================= -->
      <div class="avatar-actions">

        <div class="avatar-tip">

          支持 JPG、PNG、WEBP 格式，
          图片大小不能超过 2 MB。

        </div>


        <div class="button-row">

          <!--
            原生input隐藏。

            不直接使用el-upload自动上传，
            可以完全控制上传流程和错误提示。
          -->
          <input
            ref="fileInputRef"
            class="file-input"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            @change="handleFileChange"
          />


          <el-button
            type="primary"
            :loading="uploading"
            @click="chooseFile"
          >
            {{
              userStore.userInfo?.avatar
                ? '更换头像'
                : '上传头像'
            }}
          </el-button>


          <el-button
            v-if="userStore.userInfo?.avatar"
            type="danger"
            plain
            :loading="removing"
            @click="handleRemoveAvatar"
          >
            删除头像
          </el-button>

        </div>

      </div>

    </div>

  </el-card>

</template>


<script setup lang="ts">

import {
  ref
} from 'vue'


import {
  ElMessage,
  ElMessageBox
} from 'element-plus'


import {
  removeAvatarApi,
  uploadAvatarApi
} from '@/api/avatar'


import {
  useUserStore
} from '@/stores/user'


const userStore =
  useUserStore()


const fileInputRef =
  ref<HTMLInputElement | null>(
    null
  )


const uploading =
  ref(false)


const removing =
  ref(false)


/**
 * 2MB。
 */
const MAX_FILE_SIZE =
  2 * 1024 * 1024


/**
 * 前端允许类型。
 */
const ALLOWED_TYPES =
  new Set([

    'image/jpeg',

    'image/png',

    'image/webp'

  ])


/**
 * =========================================================
 * 点击选择文件
 * =========================================================
 */
function chooseFile() {


  /**
   * 清空旧值。
   *
   * 否则连续选择同一张图片时，
   * change事件可能不触发。
   */
  if (
    fileInputRef.value
  ) {

    fileInputRef.value.value =
      ''


    fileInputRef.value.click()
  }
}


/**
 * =========================================================
 * 文件变化
 * =========================================================
 */
async function handleFileChange(
  event: Event
) {


  const input = event.target as HTMLInputElement


  const file =
    input.files?.[0]


  if (!file) {

    return
  }


  /**
   * 前端第一层类型校验。
   *
   * 后端还会再次校验，
   * 前端不是安全边界。
   */
  if (
    !ALLOWED_TYPES.has(
      file.type
    )
  ) {

    ElMessage.warning(
      '头像仅支持 JPG、PNG、WEBP 格式'
    )

    return
  }


  if (
    file.size
    >
    MAX_FILE_SIZE
  ) {

    ElMessage.warning(
      '头像图片不能超过 2 MB'
    )

    return
  }


  await uploadAvatar(
    file
  )
}


/**
 * =========================================================
 * 上传
 * =========================================================
 */
async function uploadAvatar(
  file: File
) {


  uploading.value =
    true


  try {


    await uploadAvatarApi(
      file
    )


    /**
     * =====================================================
     * 重新读取用户信息
     * =====================================================
     *
     * 不只手工：
     *
     * userInfo.avatar = xxx
     *
     * 而是重新请求服务器。
     *
     * 这样：
     *
     * Pinia
     * BasicLayout
     * Profile
     *
     * 全部使用数据库最新状态。
     */
    await userStore
      .getUserInfo()


    ElMessage.success(
      '头像上传成功'
    )


  }
  catch (error) {


    /**
     * request.ts已经负责后端业务错误提示。
     */
    console.error(
      '头像上传失败',
      error
    )


  }
  finally {


    uploading.value =
      false
  }
}


/**
 * =========================================================
 * 删除头像
 * =========================================================
 */
async function handleRemoveAvatar() {


  try {


    await ElMessageBox.confirm(

      '确定要删除当前头像吗？',

      '删除头像',

      {
        confirmButtonText:
          '确定',

        cancelButtonText:
          '取消',

        type:
          'warning'
      }

    )


  }
  catch {


    return
  }


  removing.value =
    true


  try {


    await removeAvatarApi()


    await userStore
      .getUserInfo()


    ElMessage.success(
      '头像已删除'
    )


  }
  catch (error) {


    console.error(
      '删除头像失败',
      error
    )


  }
  finally {


    removing.value =
      false
  }
}


/**
 * =========================================================
 * 默认头像文字
 * =========================================================
 */
function getAvatarText() {


  const nickname =
    userStore.userInfo
      ?.nickname


  if (nickname) {

    return nickname
      .charAt(0)
  }


  const username =
    userStore.userInfo
      ?.username


  if (username) {

    return username
      .charAt(0)
  }


  return 'U'
}

</script>


<style scoped>

.avatar-card {

  margin-bottom: 20px;

}


.card-header {

  font-size: 16px;

  font-weight: 600;

}


.avatar-content {

  display: flex;

  align-items: center;

  gap: 30px;

}


.avatar-preview {

  flex-shrink: 0;

}


.avatar-actions {

  display: flex;

  flex-direction: column;

  gap: 16px;

}


.avatar-tip {

  color: #909399;

  font-size: 13px;

  line-height: 1.6;

}


.button-row {

  display: flex;

  align-items: center;

  gap: 10px;

}


.file-input {

  display: none;

}


@media (
  max-width: 600px
) {

  .avatar-content {

    flex-direction: column;

    align-items: flex-start;

  }

}

</style>