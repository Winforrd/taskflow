import request from '@/utils/request'

import type {
  ApiResponse
} from '@/types/api'

import type {
  Notification
} from '@/types/notification'


export function getNotificationsApi() {

  return request.get<
    any,
    ApiResponse<Notification[]>
  >(
    '/notifications'
  )
}


export function getUnreadCountApi() {

  return request.get<
    any,
    ApiResponse<number>
  >(
    '/notifications/unread-count'
  )
}


export function markNotificationReadApi(
  notificationId: number
) {

  return request.put<
    any,
    ApiResponse<null>
  >(
    `/notifications/${notificationId}/read`
  )
}


export function markAllNotificationsReadApi() {

  return request.put<
    any,
    ApiResponse<null>
  >(
    '/notifications/read-all'
  )
}