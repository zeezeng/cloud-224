import { httpGet } from '@/http/http'

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface AppSeason {
  id?: number
  seasonCode: string
  seasonName?: string
  coverImageUrl?: string
  status?: number
  appDisplay?: number
  startTime?: string
  endTime?: string
  sort?: number
  memberCount?: number
  activeCount?: number
  eliminatedCount?: number
  captainCount?: number
}

export interface AppSeasonMember {
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
  captainFlag?: 0 | 1
  eliminated?: 0 | 1
  eliminationTimes?: number
  nextEliminationAmount?: number | string
  sort?: number
}

export interface SeasonListResult {
  list: AppSeason[]
}

export interface SeasonMemberListResult {
  list: AppSeasonMember[]
  page: number
  pageSize: number
  total: number
  hasMore: boolean
}

function toNumber(value: unknown) {
  const num = Number(String(value ?? '').replace(/,/g, '').trim())
  return Number.isFinite(num) ? num : 0
}

function normalizeSeasonList(items: AppSeason[]) {
  return items
    .filter(item => item.id)
    .sort((a, b) => (a.sort || 0) - (b.sort || 0) || (b.id || 0) - (a.id || 0))
}

function normalizeMemberList(items: AppSeasonMember[]) {
  return items
    .map(item => ({
      ...item,
      eliminationTimes: Number(item.eliminationTimes || 0),
      nextEliminationAmount: toNumber(item.nextEliminationAmount),
    }))
    .sort((a, b) => (a.eliminated || 0) - (b.eliminated || 0) || (b.captainFlag || 0) - (a.captainFlag || 0) || (a.sort || 0) - (b.sort || 0) || (b.id || 0) - (a.id || 0))
}

export async function getSeasonList() {
  const data = await httpGet<AppSeason[]>('/app/season/list', undefined, undefined, { hideErrorToast: true, skipAuth: true })
  return normalizeSeasonList(Array.isArray(data) ? data : [])
}

export async function getCurrentSeason() {
  const data = await httpGet<AppSeason | null>('/app/season/current', undefined, undefined, { hideErrorToast: true, skipAuth: true })
  return data?.id ? data : null
}

export async function getSeasonMemberList(seasonId: number, params: { page?: number, pageSize?: number, keyword?: string } = {}): Promise<SeasonMemberListResult> {
  const page = params.page || 1
  const pageSize = params.pageSize || 20
  const data = await httpGet<PageResult<AppSeasonMember>>(
    `/app/season/${seasonId}/members`,
    { page, pageSize, keyword: params.keyword || undefined },
    undefined,
    { hideErrorToast: true, skipAuth: true },
  )
  const list = normalizeMemberList(Array.isArray(data.list) ? data.list : [])
  const total = Number(data.total || 0)

  return {
    list,
    page: Number(data.page || page),
    pageSize: Number(data.pageSize || pageSize),
    total,
    hasMore: page * pageSize < total,
  }
}
