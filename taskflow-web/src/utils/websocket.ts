import { getToken } from '@/utils/auth'

let socket: WebSocket | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let shouldReconnect = false
let reconnectAttempts = 0

/**
 * 基于当前页面协议与 host 构建 WS 地址（开发环境走 Vite /ws 代理）
 */
function buildWsUrl(): string {
  const token = getToken() || ''
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/ws/notification?token=${encodeURIComponent(token)}`
}

function clearReconnectTimer() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

function scheduleReconnect() {
  if (!shouldReconnect) {
    return
  }
  clearReconnectTimer()
  const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000)
  reconnectAttempts += 1
  reconnectTimer = setTimeout(() => {
    if (shouldReconnect) {
      openSocket()
    }
  }, delay)
}

function openSocket() {
  if (
    socket &&
    (socket.readyState === WebSocket.OPEN ||
      socket.readyState === WebSocket.CONNECTING)
  ) {
    return
  }

  const token = getToken()
  if (!token) {
    return
  }

  socket = new WebSocket(buildWsUrl())

  socket.onopen = () => {
    reconnectAttempts = 0
  }

  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      window.dispatchEvent(
        new CustomEvent('notification', {
          detail: data
        })
      )
    } catch (e) {
      console.error('通知消息解析失败', e)
    }
  }

  socket.onclose = () => {
    socket = null
    scheduleReconnect()
  }

  socket.onerror = () => {
    socket?.close()
  }
}

/**
 * 建立 WebSocket 实时通知连接
 * @param _userId 当前登录用户ID（兼容旧调用；实际身份由 token 决定）
 */
export function connectWebSocket(_userId: number) {
  shouldReconnect = true
  reconnectAttempts = 0
  clearReconnectTimer()
  openSocket()
}

/**
 * 断开 WebSocket 连接
 */
export function disconnectWebSocket() {
  shouldReconnect = false
  clearReconnectTimer()
  reconnectAttempts = 0
  if (socket) {
    socket.close()
    socket = null
  }
}
