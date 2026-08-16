import { request } from '@/utils/request'
import type { AnchorStatus, YunAnchorPageRow } from './anchor'

export type SeasonStatus = 0 | 1
export type CaptainFlag = 0 | 1
export type EliminatedFlag = 0 | 1

export interface YunSeason {
  id?: number
  seasonCode: string
  seasonName?: string
  coverImageUrl?: string
  status: SeasonStatus
  appDisplay?: SeasonStatus
  startTime?: string
  endTime?: string
  sort: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface YunSeasonPageRow extends YunSeason {
  memberCount?: number
  activeCount?: number
  eliminatedCount?: number
  captainCount?: number
}

export interface YunSeasonAnchor {
  id?: number
  seasonId?: number
  anchorRefId?: number
  anchorId?: string
  platform?: string
  roomId?: string
  anchorName?: string
  avatarUrl?: string
  bigImageUrl?: string
  teamName?: string
  captainFlag?: CaptainFlag
  eliminated?: EliminatedFlag
  eliminationTimes?: number
  nextEliminationAmount?: number | string
  sort?: number
  remark?: string
  createTime?: string
  updateTime?: string
  anchorStatus?: number
  showRank?: number
  guildName?: string
  roomStatus?: number
  lastStartTime?: string
}

export interface YunSeasonAnchorPageRow extends YunSeasonAnchor {}

export interface YunSeasonMemberBatchRequest {
  anchorRefIds?: number[]
  anchorIds?: string[]
  teamName?: string
  captainFlag?: CaptainFlag
}

export interface YunSeasonMemberBatchResult {
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

export const seasonApi = {
  page(params: { page: number; pageSize: number; seasonCode?: string; seasonName?: string; status?: SeasonStatus }) {
    return request<PageResult<YunSeasonPageRow>>({ url: '/yun/season/page', method: 'get', params })
  },

  list() {
    return request<YunSeason[]>({ url: '/yun/season/list', method: 'get' })
  },

  detail(id: number) {
    return request<YunSeason>({ url: `/yun/season/${id}`, method: 'get' })
  },

  create(data: YunSeason) {
    return request({ url: '/yun/season', method: 'post', data })
  },

  update(data: YunSeason) {
    return request({ url: '/yun/season', method: 'put', data })
  },

  delete(ids: number[]) {
    return request({ url: `/yun/season/${ids.join(',')}`, method: 'delete' })
  },

  copy(id: number, data?: Partial<YunSeason> & { copyMembers?: boolean }) {
    return request<YunSeason>({ url: `/yun/season/${id}/copy`, method: 'post', data })
  },

  memberPage(params: { seasonId: number; page: number; pageSize: number; keyword?: string; eliminated?: EliminatedFlag; captainFlag?: CaptainFlag }) {
    return request<PageResult<YunSeasonAnchorPageRow>>({
      url: `/yun/season/${params.seasonId}/members`,
      method: 'get',
      params: {
        page: params.page,
        pageSize: params.pageSize,
        keyword: params.keyword,
        eliminated: params.eliminated,
        captainFlag: params.captainFlag,
      },
    })
  },

  candidateAnchors(seasonId: number, params: { page: number; pageSize: number; anchorId?: string; anchorName?: string; roomId?: string; guildName?: string; status?: AnchorStatus }) {
    return request<PageResult<YunAnchorPageRow>>({
      url: `/yun/season/${seasonId}/candidate-anchors`,
      method: 'get',
      params,
    })
  },

  addMembers(seasonId: number, data: YunSeasonMemberBatchRequest) {
    return request<YunSeasonMemberBatchResult>({ url: `/yun/season/${seasonId}/members`, method: 'post', data, timeout: 300000 })
  },

  updateMember(data: YunSeasonAnchor) {
    return request({ url: '/yun/season/members', method: 'put', data })
  },

  deleteMembers(seasonId: number, memberIds: number[]) {
    return request({ url: `/yun/season/${seasonId}/members/${memberIds.join(',')}`, method: 'delete' })
  },

  resetMembers(seasonId: number) {
    return request({ url: `/yun/season/${seasonId}/members/reset`, method: 'post' })
  },
}
