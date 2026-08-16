import { request } from '@/utils/request'

export type FeedbackType = 1 | 2 | 3 | 4
export type FeedbackStatus = 0 | 1 | 2 | 3

export interface AppFeedback {
  id?: number
  feedbackType?: FeedbackType
  content?: string
  contentPreview?: string
  status?: FeedbackStatus
  pagePath?: string
  clientInfo?: string
  handlerId?: number
  handlerName?: string
  handledAt?: string
  handleRemark?: string
  createTime?: string
  updateTime?: string
  createBy?: number
  updateBy?: number
  deleted?: number
}

export interface FeedbackContact {
  wechatId?: string
  qrcodeUrl?: string
  remark?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export const feedbackApi = {
  page(params: {
    page: number
    pageSize: number
    feedbackType?: FeedbackType
    status?: FeedbackStatus
    keyword?: string
    beginTime?: string
    endTime?: string
  }) {
    return request<PageResult<AppFeedback>>({ url: '/yun/feedback/page', method: 'get', params })
  },

  detail(id: number) {
    return request<AppFeedback>({ url: `/yun/feedback/${id}`, method: 'get' })
  },

  updateStatus(id: number, data: { status: FeedbackStatus; handleRemark?: string }) {
    return request({ url: `/yun/feedback/${id}/status`, method: 'put', data })
  },

  delete(ids: number[]) {
    return request({ url: `/yun/feedback/${ids.join(',')}`, method: 'delete' })
  },

  getContact() {
    return request<FeedbackContact>({ url: '/yun/feedback/contact', method: 'get' })
  },

  updateContact(data: FeedbackContact) {
    return request({ url: '/yun/feedback/contact', method: 'put', data })
  }
}
