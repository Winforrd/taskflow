import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCountApi } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)

  async function fetchUnreadCount() {
    try {
      const res = await getUnreadCountApi()
      if (res.code === 200) {
        unreadCount.value = res.data || 0
      }
    } catch (e) {
      console.error('获取未读通知数失败', e)
    }
  }

  function setUnreadCount(count: number) {
    unreadCount.value = count
  }

  function decrementUnreadCount(amount: number = 1) {
    unreadCount.value = Math.max(0, unreadCount.value - amount)
  }

  return {
    unreadCount,
    fetchUnreadCount,
    setUnreadCount,
    decrementUnreadCount
  }
})