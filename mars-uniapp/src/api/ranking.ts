import type { EphoneRankRecord } from '@/data/yun'
import { httpGet } from '@/http/http'

export type RankingPeriod = 'today' | 'yesterday' | 'month'

export const RANKING_PAGE_SIZE = 20

export interface AnchorGiftRankingRecord {
  rankNo?: number
  anchorId?: string
  roomId?: string
  name?: string
  avatar?: string
  guildName?: string
  giftTotalValue?: number | string
  gift_total_value?: number | string
  value?: number | string
  score?: number | string
  paidGiftValue?: number | string
  paid_gift_value?: number | string
  giftUserCount?: number
  syncedAt?: string
}

export interface AnchorGiftRankingResponse {
  period: RankingPeriod
  periodKey: string
  periodLabel: string
  latestSyncTime?: string
  total: number
  page: number
  pageSize: number
  list: AnchorGiftRankingRecord[]
}

export interface RankingListResult {
  records: EphoneRankRecord[]
  period: RankingPeriod
  periodKey: string
  periodLabel: string
  latestSyncTime: string
  page: number
  pageSize: number
  total: number
  hasMore: boolean
}

function toNumber(value: unknown) {
  const num = Number(String(value ?? '').replace(/,/g, '').trim())
  return Number.isFinite(num) ? num : 0
}

function toRecord(item: AnchorGiftRankingRecord, index: number): EphoneRankRecord {
  const anchorId = String(item.anchorId || item.roomId || '')
  const srValue = toNumber(item.paidGiftValue ?? item.paid_gift_value)
  const giftUsers = Number(item.giftUserCount || 0)
  const guildName = String(item.guildName || '').trim()
  const subtitleParts = [
    item.roomId ? `房间号：${item.roomId}` : anchorId ? `主播ID：${anchorId}` : '',
    giftUsers ? `送礼人数：${giftUsers}` : '',
  ].filter(Boolean)

  return {
    id: Number(anchorId || item.rankNo || index + 1) || index + 1,
    name: String(item.name || `主播 ${anchorId}`).trim(),
    roomId: String(item.roomId || anchorId || '').trim(),
    avatar: String(item.avatar || '').trim(),
    value: srValue > 0 && srValue < 1 ? 1 : Math.round(srValue),
    // 仅保留有意义的中文公会名，过滤 UUID 等纯标识符
    guild: /^[\w-]+$/.test(guildName) ? '' : guildName,
    // 透出后端真实全局排名，供搜索/榜单按真实名次展示
    rank: Number(item.rankNo) || undefined,

    subtitle: subtitleParts[0] || '主播礼物数据',
  }
}

export async function getAnchorGiftRanking(params: {
  period: RankingPeriod
  keyword?: string
  page?: number
  pageSize?: number
}): Promise<RankingListResult> {
  const page = params.page || 1
  const pageSize = params.pageSize || RANKING_PAGE_SIZE
  const data = await httpGet<AnchorGiftRankingResponse>(
    '/app/ranking/anchor-gifts',
    {
      period: params.period,
      keyword: params.keyword || undefined,
      page,
      pageSize,
    },
    undefined,
    { hideErrorToast: true, skipAuth: true },
  )
  const list = Array.isArray(data.list) ? data.list : []
  const records = list.map((item, index) => toRecord(item, (page - 1) * pageSize + index))
  const total = Number(data.total || 0)

  return {
    records,
    period: data.period || params.period,
    periodKey: data.periodKey || '',
    periodLabel: data.periodLabel || '',
    latestSyncTime: data.latestSyncTime || '',
    page: Number(data.page || page),
    pageSize: Number(data.pageSize || pageSize),
    total,
    hasMore: page * pageSize < total,
  }
}
