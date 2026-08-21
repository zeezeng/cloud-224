export interface EphoneAnchor {
  id: number
  name: string
  roomId: string
  group: '女团' | '男团'
  avatar: string
  isLive: boolean
  monthlyFlow: number
  enjoyValue: number
  vaultBalance: number
  updatedAt: string
  trend: 'up' | 'down'
}

export interface EphoneQuickAction {
  title: string
  icon: string
  accent: string
  /** 跳转目标：页面路径（如 /pages/enjoy/enjoy）或 http(s) 外链 */
  target?: string
}

export interface EphoneRankRecord {
  id: number
  name: string
  roomId: string
  avatar: string
  value: number
  subtitle: string
  guild?: string
  trend?: 'up' | 'down'
  /** 全局真实排名（来自后端 rankNo，搜索/榜单一致），未提供时前端按列表位置推算 */
  rank?: number
}

export interface EphoneVaultChange {
  id: number
  title: string
  amount: number
  time: string
  remark: string
}

export interface EphoneVaultRecord {
  id: number
  name: string
  roomId: string
  group: string
  avatar: string
  balance: number
  dailyRecordCount: number
  dailyDelta: number
  updatedAt: string
  changes: EphoneVaultChange[]
}

export const ephoneAnchors: EphoneAnchor[] = [
  {
    id: 1,
    name: '小鹿酱',
    roomId: '224001',
    group: '女团',
    avatar: '/static/ephone/avatar-xiaolu.png',
    isLive: true,
    monthlyFlow: 1234567,
    enjoyValue: 2345678,
    vaultBalance: 88765.43,
    updatedAt: '2025-05-23 20:30',
    trend: 'up',
  },
  {
    id: 2,
    name: '甜心小野',
    roomId: '224002',
    group: '女团',
    avatar: '/static/ephone/avatar-tianxin.png',
    isLive: true,
    monthlyFlow: 987654,
    enjoyValue: 1987654,
    vaultBalance: 56432.1,
    updatedAt: '2025-05-23 19:58',
    trend: 'down',
  },
  {
    id: 3,
    name: '奶糖月',
    roomId: '224003',
    group: '女团',
    avatar: '/static/ephone/avatar-naitang.png',
    isLive: true,
    monthlyFlow: 765432,
    enjoyValue: 1456789,
    vaultBalance: 32456.78,
    updatedAt: '2025-05-23 18:45',
    trend: 'up',
  },
  {
    id: 4,
    name: '星眠',
    roomId: '224568',
    group: '女团',
    avatar: '/static/ephone/avatar-xiaolu.png',
    isLive: false,
    monthlyFlow: 654321,
    enjoyValue: 986520,
    vaultBalance: 22876.12,
    updatedAt: '2025-05-23 17:20',
    trend: 'up',
  },
  {
    id: 5,
    name: '软软',
    roomId: '224689',
    group: '女团',
    avatar: '/static/ephone/avatar-tianxin.png',
    isLive: true,
    monthlyFlow: 543210,
    enjoyValue: 876430,
    vaultBalance: 18930.55,
    updatedAt: '2025-05-23 16:12',
    trend: 'down',
  },
  {
    id: 6,
    name: '小七',
    roomId: '224721',
    group: '女团',
    avatar: '/static/ephone/avatar-naitang.png',
    isLive: false,
    monthlyFlow: 432109,
    enjoyValue: 765210,
    vaultBalance: 15672.4,
    updatedAt: '2025-05-23 15:32',
    trend: 'up',
  },
]

export const homeQuickActions: EphoneQuickAction[] = [
  { title: '乐享值查询', icon: 'i-carbon-star-filled', accent: '#f26ba8', target: '/pages/enjoy/enjoy' },
  { title: '主播金库', icon: 'i-carbon-box', accent: '#4aa8f0', target: '/pages/vault/vault' },
  { title: '泡吧', icon: 'i-carbon-bottles-container', accent: '#9a7cf0', target: '/pages/bubble/bubble' },
  { title: '赛季', icon: 'i-carbon-calendar', accent: '#ff9e54', target: '/pages/season/season' },
]

export const anchorFilters = ['全部', '直播中', '女团', '男团', '本月Top']

export const periodTabs = ['今日', '本周', '本月']

export const enjoyPeriodTabs = ['今日', '本周', '本月', '全部']

export const enjoyRanks: EphoneRankRecord[] = ephoneAnchors.map((anchor, index) => ({
  id: anchor.id,
  name: ['星光守护者', '粉色心愿', '月光同频', '暖心陪伴', '闪耀同盟', '甜蜜守护'][index] || anchor.name,
  roomId: anchor.roomId,
  avatar: anchor.avatar,
  value: anchor.enjoyValue,
  subtitle: `${10 + index * 3} 分钟前更新`,
}))

export const rankingRecords: EphoneRankRecord[] = ephoneAnchors.map(anchor => ({
  id: anchor.id,
  name: anchor.name,
  roomId: anchor.roomId,
  avatar: anchor.avatar,
  value: anchor.monthlyFlow,
  subtitle: `房间号：${anchor.roomId}`,
  trend: anchor.trend,
}))

const vaultChangeGroups: Omit<EphoneVaultChange, 'id'>[][] = [
  [
    { title: '直播礼物结算', amount: 2580.6, time: '20:18', remark: '甜心守护 · 玫瑰雨' },
    { title: '金库转出', amount: -620, time: '16:42', remark: '主播提现申请' },
    { title: '任务奖励入库', amount: 360, time: '12:20', remark: '本日活跃奖励' },
  ],
  [
    { title: '直播礼物结算', amount: 1880, time: '19:58', remark: '星光票房结算' },
    { title: '活动奖励入库', amount: 520, time: '14:36', remark: '人气冲榜奖励' },
  ],
  [
    { title: '直播礼物结算', amount: 1320.88, time: '18:45', remark: '晚间场流水入库' },
    { title: '金库转出', amount: -300, time: '11:08', remark: '日常提现' },
  ],
  [
    { title: '直播礼物结算', amount: 860, time: '17:20', remark: '午后场流水入库' },
    { title: '任务奖励入库', amount: 180, time: '10:32', remark: '连续开播奖励' },
  ],
  [
    { title: '直播礼物结算', amount: 620.5, time: '16:12', remark: '粉丝团礼物入库' },
    { title: '金库转出', amount: -200, time: '09:48', remark: '主播提现申请' },
  ],
]

export const vaultRecords: EphoneVaultRecord[] = ephoneAnchors.slice(0, 5).map((anchor, index) => {
  const changes = (vaultChangeGroups[index] || []).map((change, changeIndex) => ({
    id: Number(`${anchor.id}${changeIndex + 1}`),
    ...change,
  }))
  const dailyDelta = changes.reduce((sum, change) => sum + change.amount, 0)

  return {
    id: anchor.id,
    name: anchor.name,
    roomId: anchor.roomId.replace('224', '100'),
    group: anchor.group,
    avatar: anchor.avatar,
    balance: anchor.vaultBalance,
    dailyRecordCount: changes.length,
    dailyDelta,
    updatedAt: anchor.updatedAt,
    changes,
  }
})
