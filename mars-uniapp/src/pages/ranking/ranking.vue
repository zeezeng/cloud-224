<script setup lang="ts">
import type { EphoneRankRecord } from '@/data/yun'
import { getAnchorGiftRanking, RANKING_PAGE_SIZE } from '@/api/ranking'
import type { RankingPeriod } from '@/api/ranking'
import BackTopButton from '@/components/yun/BackTopButton.vue'
import CapsuleTabs from '@/components/yun/CapsuleTabs.vue'
import PodiumBoard from '@/components/yun/PodiumBoard.vue'
import RankingList from '@/components/yun/RankingList.vue'
import YunListStatus from '@/components/yun/YunListStatus.vue'
import YunPage from '@/components/yun/YunPage.vue'
import { useRefreshLimit } from '@/hooks/useRefreshLimit'
import { formatClockTime } from '@/utils/yun'

defineOptions({
  name: 'Ranking',
})

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '排行',
    disableScroll: true,
    backgroundColor: '#000000',
  },
})

const periodTabs = ['今日', '昨日']
const periodValues: RankingPeriod[] = ['today', 'yesterday']

const activePeriodIndex = ref(0)
const records = ref<EphoneRankRecord[]>([])
const page = ref(1)
const total = ref(0)
const hasMore = ref(true)
const loading = ref(false)
const refreshing = ref(false)
const loadingMore = ref(false)
const loaded = ref(false)
const errorMessage = ref('')
const loadMoreError = ref('')
const latestSyncTime = ref('')
const periodLabel = ref('')
const periodKey = ref('')
const showBackTop = ref(false)
const listScrollTop = ref(0)

let loadGeneration = 0
let currentListScrollTop = 0

/** 下拉刷新 5 秒限流 */
const { tryRefresh } = useRefreshLimit(5000)

const currentPeriod = computed(() => periodValues[activePeriodIndex.value] || 'today')
const podiumRecords = computed(() => records.value.slice(0, 3))
const listRecords = computed(() => records.value.slice(3))
const summaryText = computed(() => {
  if (!loaded.value || loading.value || refreshing.value) {
    return '共 - 位'
  }
  return `共 ${total.value} 位 · ${periodLabel.value || periodTabs[activePeriodIndex.value]}`
})
const latestSyncTimeText = computed(() => `数据截止时间：${formatClockTime(latestSyncTime.value) || '--:--:--'}`)

async function loadRanking({ reset = false, isPullRefresh = false } = {}) {
  if ((loading.value || refreshing.value || loadingMore.value) && !reset) {
    return
  }

  const generation = ++loadGeneration
  const requestPage = reset ? 1 : page.value
  const isFirstPage = requestPage === 1

  if (reset) {
    page.value = 1
    hasMore.value = true
    loadMoreError.value = ''
  }

  if (isFirstPage) {
    // 下拉刷新时保持 refreshing，让 scroll-view 的 refresher 动画持续到加载完成，
    // 避免空数据时 refresher 提前收回导致列表位置错乱
    if (isPullRefresh) {
      refreshing.value = true
      loading.value = false
    }
    else {
      loading.value = !records.value.length
      refreshing.value = !!records.value.length
    }
    errorMessage.value = ''
  }
  else {
    loadingMore.value = true
    loadMoreError.value = ''
  }

  try {
    const result = await getAnchorGiftRanking({
      period: currentPeriod.value,
      page: requestPage,
      pageSize: RANKING_PAGE_SIZE,
    })
    if (generation !== loadGeneration) {
      return
    }
    records.value = isFirstPage ? result.records : records.value.concat(result.records)
    total.value = result.total
    page.value = result.page + 1
    hasMore.value = result.hasMore
    latestSyncTime.value = result.latestSyncTime
    periodLabel.value = result.periodLabel
    periodKey.value = result.periodKey
  }
  catch (error) {
    if (generation !== loadGeneration) {
      return
    }
    console.error('读取主播排行失败:', error)
    if (isFirstPage) {
      errorMessage.value = '暂时没能读取主播排行'
      records.value = []
      total.value = 0
    }
    else {
      loadMoreError.value = '加载更多失败'
    }
    uni.showToast({
      icon: 'none',
      title: '排行加载失败',
    })
  }
  finally {
    if (generation === loadGeneration) {
      loading.value = false
      refreshing.value = false
      loadingMore.value = false
      loaded.value = true
      uni.stopPullDownRefresh()
    }
  }
}

function handlePeriodSelect(index: number) {
  if (index === activePeriodIndex.value) {
    return
  }
  activePeriodIndex.value = index
  records.value = []
  loaded.value = false
  latestSyncTime.value = ''
  periodLabel.value = ''
  periodKey.value = ''
  loadRanking({ reset: true })
}

function handleRefresh() {
  if (!tryRefresh()) {
    // 被限流：先展开再收回，让 refresher 回到原位
    refreshing.value = true
    setTimeout(() => {
      refreshing.value = false
    }, 100)
    uni.showToast({
      icon: 'none',
      title: '刷新太频繁，请 5 秒后再试',
    })
    return
  }
  loadRanking({ reset: true, isPullRefresh: true })
}

function handleLoadMore() {
  if (!hasMore.value || loadingMore.value || loading.value || refreshing.value) {
    return
  }
  loadRanking()
}

function handleListScroll(event: { detail?: { scrollTop?: number } }) {
  currentListScrollTop = Number(event.detail?.scrollTop || 0)
  showBackTop.value = currentListScrollTop > 420
}

async function handleBackTop() {
  listScrollTop.value = currentListScrollTop
  await nextTick()
  listScrollTop.value = 0
}

onLoad(() => {
  loadRanking({ reset: true })
})
</script>

<template>
  <YunPage title="主播排行" subtitle="每一份热爱都值得被看见" scroll-locked>
    <view class="ranking-layout">
      <view class="ranking-fixed">
        <CapsuleTabs :items="periodTabs" :active="activePeriodIndex" compact @select="handlePeriodSelect" />
        <view class="ranking-summary">
          <text>{{ summaryText }}</text>
          <text class="ranking-sync-time">{{ latestSyncTimeText }}</text>
        </view>
      </view>

      <view class="ranking-fixed-space" />

      <scroll-view
        class="ranking-scroll"
        scroll-y
        :scroll-top="listScrollTop"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        :show-scrollbar="false"
        lower-threshold="120"
        scroll-with-animation
        @scroll="handleListScroll"
        @scrolltolower="handleLoadMore"
        @refresherrefresh="handleRefresh"
      >
        <view class="ranking-content">
          <YunListStatus
            :loading="loading"
            :refreshing="refreshing"
            :loading-more="loadingMore"
            :loaded="loaded"
            :has-more="hasMore"
            :has-items="!!records.length"
            :error-message="errorMessage"
            :load-more-error="loadMoreError"
            loading-text="正在读取主播排行..."
            empty-text="暂无主播礼物排行"
            @retry="loadMoreError ? loadRanking() : loadRanking({ reset: true })"
            @load-more="handleLoadMore"
          >
            <PodiumBoard :records="podiumRecords" show-guild />
            <RankingList
              v-if="listRecords.length"
              :records="listRecords"
              value-label="SR值"
              :start-rank="4"
              show-guild
            />
          </YunListStatus>
        </view>
      </scroll-view>
    </view>

    <BackTopButton :visible="showBackTop" :page-scroll="false" @back-top="handleBackTop" />
  </YunPage>
</template>

<style scoped lang="scss">
.ranking-layout {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.ranking-fixed {
  position: fixed;
  top: var(--ephone-page-content-top, 228rpx);
  left: 50%;
  z-index: 700;
  width: 100%;
  max-width: 960rpx;
  padding: 0 40rpx 12rpx;
  box-sizing: border-box;
  transform: translateX(-50%);
  background: var(--ephone-bg-scene);
  background-size: 100vw 100vh;
  background-position: 0 0;
  background-attachment: fixed;
}

.ranking-fixed :deep(.capsule-tabs) {
  margin-top: 0;
}

/* 与 .ranking-fixed 实际高度对齐：capsule(80) + summary(68) + padding-bottom(12) = 160rpx，避免统计栏遮挡列表 */
.ranking-fixed-space {
  flex: 0 0 160rpx;
  height: 160rpx;
}

.ranking-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
  min-height: 56rpx;
  padding-top: 12rpx;
  color: rgba(255, 255, 255, 0.56);
  font-size: 23rpx;
}

.ranking-sync-time {
  min-width: 0;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.52);
  font-size: 21rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ranking-scroll {
  flex: 1;
  width: 100%;
  min-height: 0;
}

.ranking-content {
  padding: 0 0 200rpx;
}
</style>
