import { request } from '@/utils/request'

export type NoticeStatus = 0 | 1

export interface AppNotice {
  id?: number
  title?: string
  content?: string
  contentPreview?: string
  sort?: number
  status?: NoticeStatus
  publishedAt?: string
  remark?: string
  createTime?: string
  updateTime?: string
  createBy?: number
  updateBy?: number
  deleted?: number
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export const noticeApi = {
  page(params: {
    page: number
    pageSize: number
    title?: string
    status?: NoticeStatus
  }) {
    return request<PageResult<AppNotice>>({ url: '/yun/notice/page', method: 'get', params })
  },

  detail(id: number) {
    return request<AppNotice>({ url: `/yun/notice/${id}`, method: 'get' })
  },

  create(data: AppNotice) {
    return request({ url: '/yun/notice', method: 'post', data })
  },

  update(data: AppNotice) {
    return request({ url: '/yun/notice', method: 'put', data })
  },

  publish(id: number) {
    return request({ url: `/yun/notice/${id}/publish`, method: 'put' })
  },

  offline(id: number) {
    return request({ url: `/yun/notice/${id}/offline`, method: 'put' })
  },

  delete(ids: number[]) {
    return request({ url: `/yun/notice/${ids.join(',')}`, method: 'delete' })
  }
}
