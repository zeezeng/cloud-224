import { request } from '@/utils/request'

export type AnchorStatus = 0 | 1
export type AnchorFlag = 0 | 1
export type AnchorDataSource = 'MANUAL' | 'DOSEEING' | 'DOSEEING_HUYA' | string

export interface YunAnchor {
  id?: number
  anchorId?: string
  platform?: string
  roomId?: string
  anchorName?: string
  avatarUrl?: string
  bigImageUrl?: string
  roomTitle?: string
  categoryId?: string
  categoryName?: string
  guildNo?: string
  guildName?: string
  bio?: string
  roomStatus?: number
  lastStartTime?: string
  status?: AnchorStatus
  showRank?: AnchorFlag
  sort?: number
  dataSource?: AnchorDataSource
  autoUpdateProfile?: AnchorFlag
  lastProfileSyncTime?: string
  lastGiftSyncTime?: string
  remark?: string
  createTime?: string
  updateTime?: string
  createBy?: number
  updateBy?: number
  deleted?: number
}

export interface YunAnchorPageRow extends YunAnchor {
  // 今日
  todaySrValue?: number | string
  todaySrUserCount?: number
  todayLwValue?: number | string
  todayLwUserCount?: number
  todayStreamHours?: number | string
  // 昨日
  yesterdaySrValue?: number | string
  yesterdaySrUserCount?: number
  yesterdayLwValue?: number | string
  yesterdayLwUserCount?: number
  yesterdayStreamHours?: number | string
  // 本月
  monthSrValue?: number | string
  monthSrUserCount?: number
  monthLwValue?: number | string
  monthLwUserCount?: number
  monthStreamHours?: number | string
  todaySyncedAt?: string
  yesterdaySyncedAt?: string
  monthSyncedAt?: string
}

export interface YunSyncResult {
  totalCount: number
  successCount: number
  failCount: number
  errors: string[]
  startedAt?: string
  endedAt?: string
}

export interface YunSyncProgress {
  taskId?: string
  running?: boolean
  totalCount?: number
  completedCount?: number
  successCount?: number
  failCount?: number
  currentAnchorId?: string
  currentPeriodKey?: string
  errors?: string[]
  startedAt?: string
  endedAt?: string
}

export type YunCookieStatusValue = 'NOT_CONFIGURED' | 'OK' | 'EXPIRED' | 'ERROR'

export interface YunCookieStatus {
  configured: boolean
  status: YunCookieStatusValue
  message: string
}

export interface YunAnchorBatchCreateResult {
  totalCount: number
  successCount: number
  failCount: number
  successAnchorIds: string[]
  errors: string[]
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export const anchorApi = {
  page(params: {
    page: number
    pageSize: number
    anchorId?: string
    anchorName?: string
    roomId?: string
    guildName?: string
    status?: AnchorStatus
  }) {
    return request<PageResult<YunAnchorPageRow>>({ url: '/yun/anchor/page', method: 'get', params })
  },

  detail(id: number) {
    return request<YunAnchor>({ url: `/yun/anchor/${id}`, method: 'get' })
  },

  fetchPreview(anchorId: string, dataSource?: string) {
    return request<YunAnchor>({ url: '/yun/anchor/fetch-preview', method: 'get', params: { anchorId, dataSource } })
  },

  create(data: YunAnchor) {
    return request({ url: '/yun/anchor', method: 'post', data })
  },

  batchCreate(anchorIds: string[], dataSource?: string) {
    return request<YunAnchorBatchCreateResult>({
      url: '/yun/anchor/batch',
      method: 'post',
      data: { anchorIds, dataSource },
      timeout: 300000
    })
  },

  update(data: YunAnchor) {
    return request({ url: '/yun/anchor', method: 'put', data })
  },

  updateStatus(id: number, status: AnchorStatus) {
    return request({ url: `/yun/anchor/${id}/status`, method: 'put', params: { status } })
  },

  updateShowRank(id: number, showRank: AnchorFlag) {
    return request({ url: `/yun/anchor/${id}/show-rank`, method: 'put', params: { showRank } })
  },

  delete(ids: number[]) {
    return request({ url: `/yun/anchor/${ids.join(',')}`, method: 'delete' })
  },

  sync(id: number, dataSource?: string) {
    return request<YunSyncResult>({ url: `/yun/anchor/${id}/sync`, method: 'post', params: { dataSource } })
  },

  syncAll(dataSource?: string) {
    return request<YunSyncProgress>({ url: '/yun/anchor/sync-all', method: 'post', params: { dataSource } })
  },

  syncAllProgress(taskId: string) {
    return request<YunSyncProgress>({ url: '/yun/anchor/sync-all/progress', method: 'get', params: { taskId } })
  },

  cookieStatus() {
    return request<YunCookieStatus>({ url: '/yun/anchor/cookie-status', method: 'get' })
  }
}
