import { request } from '@/utils/request'

export type BannerJumpType = 0 | 1 | 2
export type BannerStatus = 0 | 1

export interface AppBanner {
  id?: number
  title?: string
  description?: string
  imageUrl?: string
  jumpType?: BannerJumpType
  jumpTarget?: string
  sort?: number
  status?: BannerStatus
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

export const bannerApi = {
  page(params: {
    page: number
    pageSize: number
    title?: string
    jumpType?: BannerJumpType
    status?: BannerStatus
  }) {
    return request<PageResult<AppBanner>>({ url: '/yun/banner/page', method: 'get', params })
  },

  detail(id: number) {
    return request<AppBanner>({ url: `/yun/banner/${id}`, method: 'get' })
  },

  create(data: AppBanner) {
    return request({ url: '/yun/banner', method: 'post', data })
  },

  update(data: AppBanner) {
    return request({ url: '/yun/banner', method: 'put', data })
  },

  delete(ids: number[]) {
    return request({ url: `/yun/banner/${ids.join(',')}`, method: 'delete' })
  }
}
