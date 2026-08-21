import type { EphoneRankRecord } from '@/data/yun'
import { httpPost } from '@/http/http'

// 「泡吧」数据源：云团一簇站内泡点用户榜单
// 反推自 https://dongdongne.com/#bubbles 的主包（api/live/user/clock/list?pid=182102）
const BUBBLE_API = 'https://api.dongdongne.com/api/live/user/clock/list'
const DONGDONGNE_PROJECT_ID = '182102'
export const BUBBLE_PAGE_SIZE = 20
export type BubbleSortKey = 'points' | 'continuation' | 'total'
export type BubbleSortDirection = 'ASC' | 'DESC'

const requestHeader = {
  'Content-Type': 'application/json',
  'X-Project': DONGDONGNE_PROJECT_ID,
}

export interface BubbleUser {
  id?: number | string
  uid?: number | string
  name?: string
  nickname?: string
  avatar?: string
  points?: number | string
  bubblePoints?: number | string
  continuation?: number | string
  total?: number | string
  userData?: {
    name?: string
    avatar?: string
  }
}

export interface BubbleUserPage {
  total: number
  size: number
  page: number
  list: BubbleUser[]
}

export interface BubbleRankRecord extends EphoneRankRecord {
  points: number
  continuation: number
  total: number
}

export interface BubbleUserListParams {
  page?: number
  size?: number
  keyword?: string
  sort?: BubbleSortKey
  sortDirection?: BubbleSortDirection
}

function toNumber(value: unknown) {
  const num = Number(String(value ?? '').replace(/,/g, ''))
  return Number.isFinite(num) ? num : 0
}

function getDisplayName(user: BubbleUser) {
  return String(user.userData?.name || user.nickname || user.name || user.uid || '').trim()
}

function normalizeAvatar(user: BubbleUser) {
  const avatar = String(user.userData?.avatar || user.avatar || '').trim()
  if (!avatar) {
    return ''
  }
  if (/^https?:\/\//i.test(avatar)) {
    return avatar
  }
  if (avatar.startsWith('//')) {
    return `https:${avatar}`
  }

  let path = avatar.startsWith('/') ? avatar : `/${avatar}`
  if (/\.[a-z0-9]+(?:[?#].*)?$/i.test(path)) {
    return `https://apic.douyucdn.cn/upload${path}`
  }
  if (!path.endsWith('_middle')) {
    path += '_middle'
  }
  return `https://apic.douyucdn.cn/upload${path}.jpg`
}

function buildSearch(keyword: string) {
  const value = keyword.trim()
  if (!value) {
    return undefined
  }
  // 站点按 name 关键字 LIKE 过滤
  return [
    { key: 'name', value: `%${value}%`, condition: 'LIKE', relationship: 'OR', type: 'CONDITION' },
  ]
}

export function getBubbleUserList(params: BubbleUserListParams = {}) {
  const sort = params.sort || 'points'
  const payload: Record<string, any> = {
    page: params.page || 1,
    size: params.size || BUBBLE_PAGE_SIZE,
    sort: [{ content: sort, condition: params.sortDirection || 'DESC' }],
  }
  const search = buildSearch(params.keyword || '')
  if (search) {
    payload.search = search
  }

  return httpPost<BubbleUserPage>(
    BUBBLE_API,
    payload,
    { pid: DONGDONGNE_PROJECT_ID },
    requestHeader,
    { hideErrorToast: true, skipAuth: true },
  )
}

export function toBubbleRankRecord(user: BubbleUser, index: number): BubbleRankRecord {
  const uid = String(user.id ?? user.uid ?? '')
  const name = getDisplayName(user) || `泡吧用户 ${uid || index + 1}`.trim()
  const continuation = toNumber(user.continuation)
  const total = toNumber(user.total ?? user.bubblePoints)
  const points = toNumber(user.points ?? user.bubblePoints)
  const avatar = normalizeAvatar(user)

  return {
    id: Number(user.id ?? user.uid ?? index + 1),
    name,
    roomId: uid,
    avatar,
    value: points,
    points,
    continuation,
    total,
    subtitle: `连续打卡 ${continuation} 天 · 总打卡 ${total} 天`,
  }
}
