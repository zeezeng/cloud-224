import type { EphoneVaultChange, EphoneVaultRecord } from '@/data/ephone'
import { httpGet, httpPost } from '@/http/http'

const DONGDONGNE_VAULT_API = 'https://api.dongdongne.com/api/live/player'
const DONGDONGNE_PROJECT_ID = '182102'
export const VAULT_PAGE_SIZE = 20
export const VAULT_DETAIL_LOG_SIZE = 20

const requestHeader = {
  'Content-Type': 'application/json',
  'X-Project': DONGDONGNE_PROJECT_ID,
}

interface VaultSearchCondition {
  key: string
  value: string
  condition: '=' | '>=' | '<' | 'LIKE'
  relationship: 'AND' | 'OR'
  type: 'CONDITION'
}

export interface VaultPlayer {
  id?: number | string
  pid?: number | string
  oid?: number | string
  rid?: number | string
  avatar?: string
  avatarUrl?: string
  avatar_url?: string
  headImg?: string
  head_img?: string
  name?: string
  nickname?: string
  nickName?: string
  nick_name?: string
  alias?: string
  platform?: string
  num?: number | string
  balance?: number | string
  playerNum?: number | string
  player_num?: number | string
  pointsNum?: number | string
  points_num?: number | string
  value?: number | string
  redemption?: number | string
  enable?: number | string
  online?: number | string
  card?: string
  attribute?: string | Record<string, unknown>
  modificationDateTime?: string
  creationDateTime?: string
}

export interface VaultLog {
  id?: number | string
  num?: number | string
  balance?: number | string
  type?: string
  changeType?: string
  action?: string
  content?: string
  card?: string
  rid?: number | string
  platform?: string
  uid?: number | string
  modificationDateTime?: string
  creationDateTime?: string
  createTime?: string
  create_time?: string
}

interface VaultPage<T> {
  total: number
  size: number
  page: number
  list: T[]
}

export interface VaultRecordListParams {
  page?: number
  size?: number
  keyword?: string
  currentSeasonOnly?: boolean
}

export interface VaultRecordListResult {
  list: EphoneVaultRecord[]
  page: number
  size: number
  total: number
  hasMore: boolean
}

export interface VaultRecordDetailResult {
  record: EphoneVaultRecord
  card: string
  logsTotal: number
  logsPage: number
}

export interface VaultLogPageResult {
  list: EphoneVaultChange[]
  page: number
  size: number
  total: number
  hasMore: boolean
}

interface VaultDailyStat {
  dailyDelta: number
  dailyRecordCount: number
}

function toNumber(value: unknown) {
  const num = Number(String(value ?? '').replace(/,/g, '').trim())
  return Number.isFinite(num) ? num : null
}

function normalizeAmount(value: unknown, divisor = 100) {
  const num = toNumber(value)
  if (num === null) {
    return 0
  }
  return Math.trunc(Math.abs(num) > 0 && Math.abs(num) < divisor ? num : num / divisor)
}

function normalizePositiveAmount(value: unknown, divisor = 100) {
  return Math.abs(normalizeAmount(value, divisor))
}

function trimText(value: unknown, maxLength = 40) {
  return String(value ?? '').replace(/\s+/g, ' ').trim().slice(0, maxLength)
}

function getDisplayName(player: VaultPlayer) {
  return trimText(player.name) || trimText(player.nickname) || `金库团员 ${player.id || ''}`.trim()
}

function getShortName(player: VaultPlayer) {
  return [
    player.nickname,
    player.nickName,
    player.nick_name,
    player.alias,
  ].map(value => trimText(value)).find(Boolean) || '云团成员'
}

function normalizeAvatar(player: VaultPlayer) {
  const avatar = trimText(player.avatar || player.avatarUrl || player.avatar_url || player.headImg || player.head_img, 200)
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

function parseAttribute(player: VaultPlayer) {
  const attribute = player.attribute
  if (!attribute) {
    return {}
  }
  if (typeof attribute === 'object') {
    return attribute
  }
  try {
    return JSON.parse(attribute) as Record<string, unknown>
  }
  catch {
    return {}
  }
}

function isCurrentSeasonMember(player: VaultPlayer) {
  return String(parseAttribute(player).type || '') === '1'
}

function getVaultBalance(player: VaultPlayer) {
  const keys = ['balance', 'playerNum', 'player_num', 'pointsNum', 'points_num', 'value', 'num'] as const
  for (const key of keys) {
    const value = player[key]
    if (value !== undefined && value !== null && value !== '') {
      const amount = normalizeAmount(value)
      if (amount !== 0) {
        return amount
      }
    }
  }
  return 0
}

function getUpdatedAt(player: VaultPlayer) {
  const updatedAt = trimText(player.modificationDateTime || player.creationDateTime, 30)
  return updatedAt.startsWith('0001-') ? '暂无更新' : updatedAt
}

function buildPlayerSearch(keyword = '') {
  const search: VaultSearchCondition[] = [
    { key: 'enable', value: '1', condition: '=', relationship: 'AND', type: 'CONDITION' },
  ]
  const value = keyword.trim()
  if (value) {
    const likeValue = `%${value}%`
    search.unshift(
      { key: 'nickname', value: likeValue, condition: 'LIKE', relationship: 'OR', type: 'CONDITION' },
      { key: 'name', value: likeValue, condition: 'LIKE', relationship: 'OR', type: 'CONDITION' },
    )
  }
  return search
}

function buildIdSearch(id: string | number) {
  return [
    { key: 'id', value: String(id), condition: '=', relationship: 'AND', type: 'CONDITION' },
    { key: 'enable', value: '1', condition: '=', relationship: 'AND', type: 'CONDITION' },
  ] satisfies VaultSearchCondition[]
}

function getDayRange(date = new Date()) {
  const start = new Date(date)
  start.setHours(0, 0, 0, 0)
  const end = new Date(start)
  end.setDate(end.getDate() + 1)
  return {
    start: formatDateTime(start),
    end: formatDateTime(end),
  }
}

function formatDateTime(date: Date) {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function getLogContent(log: VaultLog) {
  const content = String(log.content || '').trim()
  if (!content) {
    return '金库金额变动'
  }
  try {
    const parsed = JSON.parse(content)
    return trimText(parsed?.content || parsed?.title || parsed?.remark || parsed?.message || parsed?.name || content, 80)
  }
  catch {
    return trimText(content, 80)
  }
}

function getLogDirection(log: VaultLog) {
  const type = String(log.type || log.changeType || log.action || '').toLowerCase()
  const rawNum = toNumber(log.num)
  if (type === 'sys_operation' && rawNum !== null) {
    if (rawNum > 0) {
      return 'plus'
    }
    if (rawNum < 0) {
      return 'minus'
    }
  }
  if (type === 'user_meal_eat') {
    return 'flat'
  }
  if (['increase', 'recharge', 'add', 'income', 'user_increase_player_num', 'sys_operation_increase', 'team_increase'].includes(type)) {
    return 'plus'
  }
  if (['decrease', 'consumption', 'consume', 'reduce', 'deduct', 'user_decrease_player_num', 'sys_operation_decrease', 'team_decrease'].includes(type)) {
    return 'minus'
  }

  const content = getLogContent(log)
  if (/(给|增加|获得|收入)/.test(content)) {
    return 'plus'
  }
  if (/(扣除|消费|减少|支出|炸)/.test(content)) {
    return 'minus'
  }
  return 'flat'
}

function getSignedLogAmount(log: VaultLog) {
  const amount = normalizePositiveAmount(log.num ?? log.balance)
  const direction = getLogDirection(log)
  if (direction === 'minus') {
    return -amount
  }
  if (direction === 'plus') {
    return amount
  }
  return 0
}

function toVaultChange(log: VaultLog, index: number): EphoneVaultChange {
  const direction = getLogDirection(log)
  return {
    id: Number(log.id || index + 1),
    title: direction === 'plus' ? '乐享币增加' : direction === 'minus' ? '乐享币减少' : '金库变动',
    amount: getSignedLogAmount(log),
    time: trimText(log.creationDateTime || log.createTime || log.create_time || log.modificationDateTime, 30) || '时间未知',
    remark: getLogContent(log),
  }
}

function toVaultRecord(player: VaultPlayer, dailyStat?: VaultDailyStat, changes: EphoneVaultChange[] = []): EphoneVaultRecord {
  return {
    id: Number(player.id || player.rid || 0),
    name: getDisplayName(player),
    roomId: trimText(player.rid, 24),
    group: getShortName(player),
    avatar: normalizeAvatar(player),
    balance: getVaultBalance(player),
    dailyRecordCount: dailyStat?.dailyRecordCount || 0,
    dailyDelta: dailyStat?.dailyDelta || 0,
    updatedAt: getUpdatedAt(player),
    changes,
  }
}

async function getVaultPlayerPage(page: number, size: number, keyword = '') {
  return httpPost<VaultPage<VaultPlayer>>(
    `${DONGDONGNE_VAULT_API}/list`,
    {
      page,
      size,
      search: buildPlayerSearch(keyword),
    },
    undefined,
    requestHeader,
    { hideErrorToast: true, skipAuth: true },
  )
}

async function getAllVaultPlayers(keyword = '') {
  const players: VaultPlayer[] = []
  let page = 1
  let loaded = 0
  let total = Number.POSITIVE_INFINITY

  while (loaded < total && page <= 20) {
    const result = await getVaultPlayerPage(page, 500, keyword)
    const list = Array.isArray(result.list) ? result.list : []
    players.push(...list)
    total = Number(result.total || 0)
    loaded += list.length
    if (!list.length) {
      break
    }
    page += 1
  }

  return players
}

export async function getVaultLogList(params: { page?: number, size?: number, card?: string }) {
  const search = params.card
    ? [{ key: 'card', value: params.card, condition: '=', relationship: 'AND', type: 'CONDITION' } satisfies VaultSearchCondition]
    : []

  return httpPost<VaultPage<VaultLog>>(
    `${DONGDONGNE_VAULT_API}/log/list`,
    {
      page: params.page || 1,
      size: params.size || VAULT_DETAIL_LOG_SIZE,
      search,
    },
    undefined,
    requestHeader,
    { hideErrorToast: true, skipAuth: true },
  )
}

export async function getVaultRecordLogPage(card: string, params: { page?: number, size?: number } = {}): Promise<VaultLogPageResult> {
  const page = params.page || 1
  const size = params.size || VAULT_DETAIL_LOG_SIZE
  const result = await getVaultLogList({ page, size, card })
  const total = Number(result.total || 0)
  const list = (Array.isArray(result.list) ? result.list : []).map((log, index) =>
    toVaultChange(log, (page - 1) * size + index),
  )

  return {
    list,
    page,
    size,
    total,
    hasMore: page * size < total,
  }
}

async function getTodayVaultStats() {
  const { start, end } = getDayRange()
  const stats = new Map<string, VaultDailyStat>()
  let page = 1
  let loaded = 0
  let total = Number.POSITIVE_INFINITY

  while (loaded < total && page <= 20) {
    const result = await httpPost<VaultPage<VaultLog>>(
      `${DONGDONGNE_VAULT_API}/log/list`,
      {
        page,
        size: 500,
        search: [
          { key: 'creationDateTime', value: start, condition: '>=', relationship: 'AND', type: 'CONDITION' },
          { key: 'creationDateTime', value: end, condition: '<', relationship: 'AND', type: 'CONDITION' },
        ] satisfies VaultSearchCondition[],
      },
      undefined,
      requestHeader,
      { hideErrorToast: true, skipAuth: true },
    )

    total = Number(result.total || 0)
    const list = Array.isArray(result.list) ? result.list : []
    loaded += list.length

    for (const log of list) {
      const card = trimText(log.card, 40)
      if (!card) {
        continue
      }
      const stat = stats.get(card) || { dailyDelta: 0, dailyRecordCount: 0 }
      stat.dailyDelta += getSignedLogAmount(log)
      stat.dailyRecordCount += 1
      stats.set(card, stat)
    }

    if (!list.length) {
      break
    }
    page += 1
  }

  return stats
}

export async function getVaultRecordList(params: VaultRecordListParams = {}): Promise<VaultRecordListResult> {
  const page = params.page || 1
  const size = params.size || VAULT_PAGE_SIZE
  const [players, dailyStats] = await Promise.all([
    getAllVaultPlayers(params.keyword || ''),
    getTodayVaultStats().catch(() => new Map<string, VaultDailyStat>()),
  ])

  const sortedRecords = players
    .filter(player => params.currentSeasonOnly === false || isCurrentSeasonMember(player))
    .map(player => toVaultRecord(player, dailyStats.get(trimText(player.card, 40))))
    .sort((a, b) => b.balance - a.balance || a.name.localeCompare(b.name, 'zh-CN'))
  const start = (page - 1) * size
  const list = sortedRecords.slice(start, start + size)

  return {
    list,
    page,
    size,
    total: sortedRecords.length,
    hasMore: start + size < sortedRecords.length,
  }
}

export async function getVaultPlayerDetail(id: string | number) {
  return httpGet<VaultPlayer>(
    `${DONGDONGNE_VAULT_API}/${encodeURIComponent(String(id))}`,
    { id },
    requestHeader,
    { hideErrorToast: true, skipAuth: true },
  )
}

export async function getVaultRecordDetail(id: string | number): Promise<VaultRecordDetailResult> {
  const player = await getVaultPlayerDetail(id)
  const dailyStats = await getTodayVaultStats().catch(() => new Map<string, VaultDailyStat>())
  const card = trimText(player.card, 40)
  const logs = card
    ? await getVaultLogList({ page: 1, size: VAULT_DETAIL_LOG_SIZE, card })
    : { total: 0, page: 1, size: VAULT_DETAIL_LOG_SIZE, list: [] }
  const changes = (Array.isArray(logs.list) ? logs.list : []).map(toVaultChange)

  return {
    record: toVaultRecord(player, dailyStats.get(card), changes),
    card,
    logsTotal: Number(logs.total || 0),
    logsPage: Number(logs.page || 1),
  }
}

export function getVaultRecordFromPlayer(player: VaultPlayer) {
  return toVaultRecord(player)
}
