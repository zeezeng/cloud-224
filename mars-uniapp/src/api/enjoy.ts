import type { EphoneRankRecord } from '@/data/ephone'
import { httpPost } from '@/http/http'

const DONGDONGNE_LIVE_API = 'https://api.dongdongne.com/api/live'
const DONGDONGNE_PROJECT_ID = '182102'
export const ENJOY_PAGE_SIZE = 20

const requestHeader = {
  'Content-Type': 'application/json',
  'X-Project': DONGDONGNE_PROJECT_ID,
}

const enjoyValueKeys = ['pointsNum2', 'points_num2', 'pointsNum', 'points_num', 'value', 'score', 'num'] as const

interface EnjoySearchCondition {
  key: string
  value: string
  condition: 'LIKE'
  relationship: 'OR'
  type: 'CONDITION'
}

export interface EnjoyUser {
  id?: number | string
  uid?: number | string
  room?: number | string
  avatar?: string
  avatarUrl?: string
  avatar_url?: string
  headImg?: string
  head_img?: string
  name?: string
  nickname?: string
  pointsNum2?: number | string
  points_num2?: number | string
  pointsNum?: number | string
  points_num?: number | string
  pointsTotal?: number | string
  value?: number | string
  score?: number | string
  num?: number | string
  modificationDateTime?: string
  creationDateTime?: string
}

export interface EnjoyUserPage {
  total: number
  size: number
  count?: Record<string, unknown>
  page: number
  list: EnjoyUser[]
}

export interface EnjoyUserListParams {
  page?: number
  size?: number
  keyword?: string
}

function toNumber(value: unknown) {
  const num = Number(String(value ?? '').replace(/,/g, ''))
  return Number.isFinite(num) ? num : null
}

function normalizeEnjoyValue(value: unknown) {
  const num = toNumber(value)
  if (num === null) {
    return 0
  }
  return Math.trunc(Math.abs(num) > 0 && Math.abs(num) < 100 ? num : num / 100)
}

function getEnjoyValue(user: EnjoyUser) {
  for (const key of enjoyValueKeys) {
    const value = user[key]
    if (value !== undefined && value !== null && value !== '') {
      return normalizeEnjoyValue(value)
    }
  }
  return 0
}

function getDisplayName(user: EnjoyUser) {
  return String(user.name || '').trim()
    || String(user.nickname || '').trim()
    || `乐享用户 ${user.uid || user.id || ''}`.trim()
}

function normalizeAvatar(user: EnjoyUser) {
  const avatar = String(user.avatar || user.avatarUrl || user.avatar_url || user.headImg || user.head_img || '').trim()
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

function buildSearch(keyword: string): EnjoySearchCondition[] | undefined {
  const value = keyword.trim()
  if (!value) {
    return undefined
  }

  const likeValue = `%${value}%`
  const search: EnjoySearchCondition[] = [
    { key: 'nickname', value: likeValue, condition: 'LIKE', relationship: 'OR', type: 'CONDITION' },
    { key: 'name', value: likeValue, condition: 'LIKE', relationship: 'OR', type: 'CONDITION' },
  ]

  if (/^\d+$/.test(value)) {
    search.push({ key: 'uid', value: likeValue, condition: 'LIKE', relationship: 'OR', type: 'CONDITION' })
  }

  return search
}

export function getEnjoyUserList(params: EnjoyUserListParams = {}) {
  const payload: Record<string, any> = {
    page: params.page || 1,
    size: params.size || ENJOY_PAGE_SIZE,
  }
  const search = buildSearch(params.keyword || '')
  if (search) {
    payload.search = search
  }

  return httpPost<EnjoyUserPage>(
    `${DONGDONGNE_LIVE_API}/user/list`,
    payload,
    undefined,
    requestHeader,
    { hideErrorToast: true, skipAuth: true },
  )
}

export function toEnjoyRankRecord(user: EnjoyUser, index: number): EphoneRankRecord {
  const uid = String(user.uid || user.id || '')
  const nickname = String(user.nickname || '').trim()
  const updatedAt = String(user.modificationDateTime || user.creationDateTime || '').trim()

  return {
    id: Number(user.id || user.uid || index + 1),
    name: getDisplayName(user),
    roomId: uid,
    avatar: normalizeAvatar(user),
    value: getEnjoyValue(user),
    subtitle: nickname
      ? `昵称：${nickname}`
      : uid
        ? `用户ID：${uid}`
        : updatedAt
          ? `更新：${updatedAt}`
          : '乐享用户',
  }
}
