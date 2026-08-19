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
const searchKeyword = ref('')
const searchInput = ref('')

let loadGeneration = 0
let currentListScrollTop = 0

/** 下拉刷新 5 秒限流 */
const { tryRefresh } = useRefreshLimit(5000)

const currentPeriod = computed(() => periodValues[activePeriodIndex.value] || 'today')
const isSearching = computed(() => !!searchKeyword.value)
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
      keyword: searchKeyword.value,
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

function handleSearch() {
  const keyword = searchInput.value.trim()
  if (keyword === searchKeyword.value) {
    return
  }
  searchKeyword.value = keyword
  records.value = []
  loadRanking({ reset: true })
}

function handleClearSearch() {
  searchInput.value = ''
  if (searchKeyword.value) {
    searchKeyword.value = ''
    records.value = []
    loadRanking({ reset: true })
  }
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
        <view class="ranking-search">
          <input
            v-model="searchInput"
            class="ranking-search-input"
            placeholder="搜索主播名 / 房间号 / 公会"
            placeholder-class="ranking-search-placeholder"
            confirm-type="search"
            @confirm="handleSearch"
            @input="searchInput = $event.detail.value"
          >
          <view
            v-if="searchInput || searchKeyword"
            class="ranking-search-clear"
            @click="handleClearSearch"
          >
            清除
          </view>
          <view class="ranking-search-btn" :class="{ 'is-active': !!searchKeyword }" @click="handleSearch">
            搜索
          </view>
        </view>
        <view class="ranking-summary">
          <text>{{ summaryText }}</text>
          <!-- 昨日榜单为终值数据，不再显示“数据截止时间”，避免误导 -->
          <text v-if="currentPeriod !== 'yesterday'" class="ranking-sync-time">{{ latestSyncTimeText }}</text>
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
            <template v-if="isSearching">
              <!-- 搜索态：不展示前三领奖台，用普通列表展示命中结果，并显示其真实全局排名 -->
              <RankingList
                v-if="records.length"
                :records="records"
                value-label="SR值"
                show-guild
              />
            </template>
            <template v-else>
              <PodiumBoard :records="podiumRecords" show-guild />
              <RankingList
                v-if="listRecords.length"
                :records="listRecords"
                value-label="SR值"
                :start-rank="4"
                show-guild
              />
            </template>
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

/* 与 .ranking-fixed 实际高度对齐：capsule(80) + search(60) + summary(68) + padding-bottom(12) = 220rpx，避免搜索栏遮挡列表 */
.ranking-fixed-space {
  flex: 0 0 220rpx;
  height: 220rpx;
}

.ranking-search {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding-top: 12rpx;
}

.ranking-search-input {
  flex: 1;
  min-width: 0;
  height: 60rpx;
  padding: 0 24rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  border-radius: 999rpx;
  background: rgba(0, 0, 0, 0.24);
  color: rgba(255, 255, 255, 0.92);
  font-size: 24rpx;
}

.ranking-search-placeholder {
  color: rgba(255, 255, 255, 0.35);
}

.ranking-search-clear {
  flex: 0 0 auto;
  height: 40rpx;
  padding: 0 8rpx;
  color: rgba(255, 255, 255, 0.4);
  font-size: 22rpx;
  line-height: 40rpx;
}

.ranking-search-btn {
  flex: 0 0 auto;
  min-width: 104rpx;
  height: 60rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.7);
  font-size: 23rpx;
  font-weight: 700;
  line-height: 60rpx;
  text-align: center;
}

.ranking-search-btn.is-active {
  background: #f2b6cc;
  color: #2a111b;
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
  max-width: 960rpx;
  margin: 0 auto;
  padding: 0 40rpx 200rpx;
}
</style>
