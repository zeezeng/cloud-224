<script setup lang="ts">
import { getCurrentSeason, getSeasonMemberList } from '@/api/season'
import type { AppSeason, AppSeasonMember } from '@/api/season'
import BackTopButton from '@/components/yun/BackTopButton.vue'
import SeasonAnchorCard from '@/components/yun/SeasonAnchorCard.vue'
import YunListStatus from '@/components/yun/YunListStatus.vue'
import YunPage from '@/components/yun/YunPage.vue'
import { useRefreshLimit } from '@/hooks/useRefreshLimit'

defineOptions({
  name: 'Season',
})

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '赛季',
    enablePullDownRefresh: true,
    backgroundTextStyle: 'dark',
    onReachBottomDistance: 120,
    backgroundColor: '#000000',
  },
})

const currentSeason = ref<AppSeason | null>(null)
const records = ref<AppSeasonMember[]>([])
const page = ref(1)
const total = ref(0)
const hasMore = ref(true)
const loading = ref(false)
const refreshing = ref(false)
const loadingMore = ref(false)
const loaded = ref(false)
const errorMessage = ref('')
const loadMoreError = ref('')
const showBackTop = ref(false)
const searchKeyword = ref('')
const searchInput = ref('')

let loadGeneration = 0

const { tryRefresh } = useRefreshLimit(5000)

const seasonTitle = computed(() => currentSeason.value?.seasonCode || '主播赛季')

const subtitleText = computed(() => {
  if (!currentSeason.value) {
    return '后台未设置当前赛季'
  }
  return currentSeason.value.seasonName || '当前展示赛季'
})

const eliminatedCount = computed(() => Number(currentSeason.value?.eliminatedCount ?? 0))

const activeCount = computed(() => Number(currentSeason.value?.activeCount ?? 0))

const memberCount = computed(() => Number(currentSeason.value?.memberCount ?? 0))

async function loadCurrentSeason({ isPullRefresh = false } = {}) {
  const generation = ++loadGeneration
  if (isPullRefresh) {
    refreshing.value = true
  }
  else {
    loading.value = !records.value.length
  }
  errorMessage.value = ''
  loadMoreError.value = ''

  try {
    const season = await getCurrentSeason()
    if (generation !== loadGeneration) {
      return
    }
    currentSeason.value = season
    records.value = []
    total.value = 0
    page.value = 1
    hasMore.value = true

    if (!season?.id) {
      loaded.value = true
      return
    }

    await loadSeasonMembers({ reset: true, isPullRefresh })
  }
  catch (error) {
    if (generation !== loadGeneration) {
      return
    }
    console.error('读取当前赛季失败:', error)
    currentSeason.value = null
    records.value = []
    total.value = 0
    errorMessage.value = '暂时没能读取当前赛季'
    uni.showToast({ icon: 'none', title: '赛季加载失败' })
  }
  finally {
    if (generation === loadGeneration && !currentSeason.value?.id) {
      loading.value = false
      refreshing.value = false
      loadingMore.value = false
      loaded.value = true
      uni.stopPullDownRefresh()
    }
  }
}

async function loadSeasonMembers({ reset = false, isPullRefresh = false } = {}) {
  const season = currentSeason.value
  if (!season?.id) {
    records.value = []
    total.value = 0
    return
  }
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
    const result = await getSeasonMemberList(season.id, {
      page: requestPage,
      pageSize: 12,
      keyword: searchKeyword.value,
    })
    if (generation !== loadGeneration) {
      return
    }
    records.value = isFirstPage ? result.list : records.value.concat(result.list)
    total.value = result.total
    page.value = result.page + 1
    hasMore.value = result.hasMore
  }
  catch (error) {
    if (generation !== loadGeneration) {
      return
    }
    console.error('读取赛季成员失败:', error)
    if (isFirstPage) {
      errorMessage.value = '暂时没能读取赛季成员'
      records.value = []
      total.value = 0
    }
    else {
      loadMoreError.value = '加载更多失败'
    }
    uni.showToast({ icon: 'none', title: '赛季成员加载失败' })
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

function handleRefresh() {
  if (!tryRefresh()) {
    uni.showToast({
      icon: 'none',
      title: '刷新太频繁，请 5 秒后再试',
    })
    uni.stopPullDownRefresh()
    return
  }
  loadCurrentSeason({ isPullRefresh: true })
}

function handleLoadMore() {
  if (!hasMore.value || loadingMore.value || loading.value || refreshing.value || !currentSeason.value?.id) {
    return
  }
  loadSeasonMembers()
}

function handleSearch() {
  const keyword = searchInput.value.trim()
  if (keyword === searchKeyword.value) {
    return
  }
  searchKeyword.value = keyword
  loadSeasonMembers({ reset: true })
}

function handleClearSearch() {
  searchInput.value = ''
  if (searchKeyword.value) {
    searchKeyword.value = ''
    loadSeasonMembers({ reset: true })
  }
}

onPullDownRefresh(() => {
  handleRefresh()
})

onReachBottom(() => {
  handleLoadMore()
})

onPageScroll((event: { scrollTop?: number }) => {
  showBackTop.value = Number(event.scrollTop || 0) > 420
})

onLoad(() => {
  void loadCurrentSeason()
})
</script>

<template>
  <YunPage :title="seasonTitle" :subtitle="subtitleText">
    <view class="season-panel">
      <view class="season-panel-num">
        <text class="season-panel-num-value">{{ memberCount }}</text>
        <text class="season-panel-num-label">总成员</text>
      </view>
      <view class="season-panel-num is-active">
        <text class="season-panel-num-value">{{ activeCount }}</text>
        <text class="season-panel-num-label">存活</text>
      </view>
      <view class="season-panel-num is-out">
        <text class="season-panel-num-value">{{ eliminatedCount }}</text>
        <text class="season-panel-num-label">已淘汰</text>
      </view>
    </view>

    <view class="season-search">
      <input
        v-model="searchInput"
        class="season-search-input"
        placeholder="搜索主播名 / 房间号 / 队伍"
        placeholder-class="season-search-placeholder"
        confirm-type="search"
        @confirm="handleSearch"
        @input="searchInput = $event.detail.value"
      >
      <view
        v-if="searchInput || searchKeyword"
        class="season-search-clear"
        @click="handleClearSearch"
      >
        清除
      </view>
      <view class="season-search-btn" :class="{ 'is-active': !!searchKeyword }" @click="handleSearch">
        搜索
      </view>
    </view>

    <YunListStatus
      :loading="loading"
      :refreshing="refreshing"
      :loading-more="loadingMore"
      :loaded="loaded"
      :has-more="hasMore"
      :has-items="!!records.length"
      :error-message="errorMessage"
      :load-more-error="loadMoreError"
      loading-text="正在读取当前赛季..."
      :empty-text="currentSeason ? '当前赛季暂无成员' : '后台暂未设置当前赛季'"
      empty-icon="i-carbon-user-avatar"
      @retry="loadMoreError ? loadSeasonMembers() : loadCurrentSeason()"
      @load-more="handleLoadMore"
    >
      <view class="season-list">
        <SeasonAnchorCard
          v-for="(record, index) in records"
          :key="record.id || `${record.anchorId}-${index}`"
          :record="record"
          :rank="index + 1"
        />
      </view>
    </YunListStatus>

    <BackTopButton :visible="showBackTop" />
  </YunPage>
</template>

<style scoped lang="scss">
.season-panel {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14rpx;
  margin-bottom: 24rpx;
  padding: 18rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.04);
}

.season-panel-num {
  padding: 18rpx 10rpx 16rpx;
  border-radius: 20rpx;
  background: rgba(0, 0, 0, 0.2);
  text-align: center;
}

.season-panel-num-value {
  display: block;
  color: #fff;
  font-size: 34rpx;
  font-weight: 800;
  line-height: 1.1;
}

.season-panel-num.is-active .season-panel-num-value {
  color: #d8ffef;
}

.season-panel-num.is-out .season-panel-num-value {
  color: rgba(255, 255, 255, 0.78);
}

.season-panel-num-label {
  display: block;
  margin-top: 9rpx;
  color: rgba(255, 255, 255, 0.5);
  font-size: 21rpx;
  line-height: 1.1;
}

.season-search {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin-bottom: 14rpx;
}

.season-search-input {
  flex: 1;
  min-width: 0;
  height: 68rpx;
  padding: 0 24rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  border-radius: 999rpx;
  background: rgba(0, 0, 0, 0.24);
  color: rgba(255, 255, 255, 0.92);
  font-size: 24rpx;
}

.season-search-placeholder {
  color: rgba(255, 255, 255, 0.35);
}

.season-search-clear {
  flex: 0 0 auto;
  height: 40rpx;
  padding: 0 8rpx;
  color: rgba(255, 255, 255, 0.4);
  font-size: 22rpx;
  line-height: 40rpx;
}

.season-search-btn {
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

.season-search-btn.is-active {
  background: #f2b6cc;
  color: #2a111b;
}

.season-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-top: 24rpx;
}
</style>
