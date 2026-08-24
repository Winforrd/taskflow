let socket: WebSocket | null = null

/**
 * 建立 WebSocket 实时通知连接
 * @param userId 当前登录用户ID
 */
export function connectWebSocket(userId: number) {
  // 防止重复连接
  if (socket && socket.readyState === WebSocket.OPEN) {
    return
  }

  socket = new WebSocket(`ws://localhost:8080/ws/notification?userId=${userId}`)

  // 收到后端推送的消息，派发全局事件
  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      console.log('收到实时通知', data)
      window.dispatchEvent(
        new CustomEvent('notification', {
          detail: data
        })
      )
    } catch (e) {
      console.error('通知消息解析失败', e)
    }
  }

  // 连接关闭：重置状态
  socket.onclose = () => {
    socket = null
  }

  // 连接异常：关闭并重置
  socket.onerror = () => {
    socket?.close()
    socket = null
  }
}

/**
 * 断开 WebSocket 连接
 */
export function disconnectWebSocket() {
  if (socket) {
    socket.close()
    socket = null
  }
}
