<script setup lang="ts">
import type { BubbleRankRecord, BubbleSortKey } from '@/api/bubble'
import { BUBBLE_PAGE_SIZE, getBubbleUserList, toBubbleRankRecord } from '@/api/bubble'
import BackTopButton from '@/components/yun/BackTopButton.vue'
import YunFixedSearch from '@/components/yun/YunFixedSearch.vue'
import RankingList from '@/components/yun/RankingList.vue'
import YunTransparentNav from '@/components/yun/YunTransparentNav.vue'
import { useRefreshLimit } from '@/hooks/useRefreshLimit'

defineOptions({
  name: 'Bubble',
})

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '泡吧',
    disableScroll: true,
    backgroundColor: '#000000',
  },
})

const searchKeyword = ref('')
const records = ref<BubbleRankRecord[]>([])
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const refreshing = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const loaded = ref(false)
const errorMessage = ref('')
const loadMoreError = ref('')
const showBackTop = ref(false)
const listScrollTop = ref(0)
const navStyle = ref<Record<string, string>>({})
const activeSort = ref<BubbleSortKey>('points')
// 顶部搜索固定在透明导航栏下方（H5 时 navStyle 为空、var 未定义，兜底 88rpx）
const bubbleNavTop = 'var(--ephone-transparent-nav-content-top, 88rpx)'
const bubbleSearchTop = `calc(${bubbleNavTop} + 12rpx)`
const bubbleTabsTop = `calc(${bubbleSearchTop} + 120rpx)`
const bubblePinnedSpace = '232rpx'

let loadGeneration = 0
let currentListScrollTop = 0

const sortTabs: { label: string, value: BubbleSortKey }[] = [
  { label: '泡点总数', value: 'points' },
  { label: '连续打卡', value: 'continuation' },
  { label: '总打卡', value: 'total' },
]

const currentSortTab = computed(() => sortTabs.find(item => item.value === activeSort.value) || sortTabs[0])

const bottomText = computed(() => {
  if (loadMoreError.value) {
    return '加载失败，点这里重试'
  }
  if (loadingMore.value) {
    return '正在加载更多...'
  }
  if (!records.value.length) {
    return ''
  }
  return hasMore.value ? '继续上拉加载更多' : '没有更多数据了！'
})

function handleNavLayout(style: Record<string, string>) {
  navStyle.value = style
}

async function loadBubbleUsers({ reset = false } = {}) {
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
    loading.value = !records.value.length
    refreshing.value = !!records.value.length
  }
  else {
    loadingMore.value = true
    loadMoreError.value = ''
  }

  if (isFirstPage) {
    errorMessage.value = ''
  }

  try {
    const data = await getBubbleUserList({
      page: requestPage,
      size: BUBBLE_PAGE_SIZE,
      keyword: searchKeyword.value,
      sort: activeSort.value,
      sortDirection: 'DESC',
    })
    if (generation !== loadGeneration) {
      return
    }
    const list = Array.isArray(data?.list) ? data.list : []
    const nextRecords = list.map((item, index) => toBubbleRankRecord(item, (requestPage - 1) * BUBBLE_PAGE_SIZE + index))
    records.value = isFirstPage ? nextRecords : [...records.value, ...nextRecords]
    total.value = Number(data?.total || records.value.length)
    hasMore.value = records.value.length < total.value && list.length >= BUBBLE_PAGE_SIZE
    page.value = requestPage + 1
  }
  catch (error) {
    if (generation !== loadGeneration) {
      return
    }
    console.error('读取泡吧用户失败:', error)
    if (isFirstPage) {
      errorMessage.value = '暂时没能读取泡点用户'
      records.value = []
      total.value = 0
    }
    else {
      loadMoreError.value = '加载更多失败'
    }
    uni.showToast({
      title: '泡点用户加载失败',
      icon: 'none',
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

function handleSearch(value: string) {
  searchKeyword.value = value
  // 清空列表并重置滚动位置，确保搜索结果从序号 1 开始显示
  records.value = []
  total.value = 0
  handleBackTop()
  loadBubbleUsers({ reset: true })
}

function handleSortChange(value: BubbleSortKey) {
  if (activeSort.value === value) {
    return
  }

  activeSort.value = value
  records.value = []
  total.value = 0
  handleBackTop()
  loadBubbleUsers({ reset: true })
}

function handleLoadMore() {
  if (!hasMore.value || loadingMore.value || loading.value || refreshing.value) {
    return
  }
  loadBubbleUsers()
}

const { tryRefresh } = useRefreshLimit(5000)

function handleRefresh() {
  if (!tryRefresh()) {
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
  loadBubbleUsers({ reset: true })
}

function handleListScroll(event: { detail?: { scrollTop?: number } }) {
  currentListScrollTop = Number(event.detail?.scrollTop || 0)
  showBackTop.value = currentListScrollTop > 420
}

async function handleBackTop() {
  // 必须先设置一个非零值才能触发滚动，否则值不变不会生效
  listScrollTop.value = 1
  await nextTick()
  listScrollTop.value = 0
}

onLoad(() => {
  loadBubbleUsers({ reset: true })
})
</script>

<template>
  <view class="bubble-page" :style="navStyle">
    <view class="bubble-aura bubble-aura-primary" />
    <view class="bubble-aura bubble-aura-secondary" />
    <YunTransparentNav
      title="泡吧"
      fallback-url="/pages/index/index"
      fallback-type="switchTab"
      @layout="handleNavLayout"
    />

    <YunFixedSearch
      v-model="searchKeyword"
      placeholder="搜索用户昵称"
      show-button
      variant="bubble"
      :top="bubbleSearchTop"
      :spacer-height="bubblePinnedSpace"
      @search="handleSearch"
    />

    <view class="bubble-sort-fixed" :style="{ top: bubbleTabsTop }">
      <view class="bubble-sort-tabs" role="tablist" aria-label="泡吧排序">
        <button
          v-for="item in sortTabs"
          :key="item.value"
          class="bubble-sort-tab"
          :class="{ 'is-active': activeSort === item.value }"
          hover-class="bubble-sort-tab-hover"
          :aria-selected="activeSort === item.value"
          role="tab"
          @tap="handleSortChange(item.value)"
        >
          <text class="bubble-sort-tab-text">{{ item.label }}</text>
        </button>
      </view>
    </view>

    <scroll-view
      class="bubble-scroll"
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
      <view class="bubble-content">
        <view class="bubble-search-spacer" />

        <view v-if="refreshing && loaded" class="bubble-refreshing">
          <view class="i-carbon-circle-dash bubble-refreshing-icon bubble-spin" />
          <text>正在刷新...</text>
        </view>

        <view v-if="loading && !records.length" class="bubble-state">
          <view class="i-carbon-circle-dash bubble-state-icon bubble-spin" />
          <text>正在读取泡点用户...</text>
        </view>

        <view v-else-if="errorMessage" class="bubble-state bubble-state-error">
          <view class="i-carbon-cloud-offline bubble-state-icon" />
          <text>{{ errorMessage }}</text>
          <button class="bubble-retry" @click="loadBubbleUsers({ reset: true })">
            重试
          </button>
        </view>

        <template v-else-if="records.length">
          <view class="bubble-section-head">
            <text class="bubble-section-title">成员列表</text>
            <text class="bubble-section-desc">按{{ currentSortTab.label }}从高到低</text>
          </view>

          <RankingList
            :records="records"
            value-label="泡点"
            :show-rank="false"
            variant="bubble"
          />

          <view
            class="bubble-list-load-more"
            :class="{ 'is-actionable': !loadingMore && (hasMore || loadMoreError) }"
            @tap="loadMoreError ? loadBubbleUsers() : handleLoadMore()"
          >
            <view v-if="loadingMore" class="i-carbon-circle-dash bubble-list-load-more-icon bubble-spin" />
            <text>{{ bottomText }}</text>
          </view>
        </template>

        <view v-else class="bubble-state">
          <view class="i-carbon-search-locate bubble-state-icon" />
          <text>没有找到这个用户</text>
        </view>
      </view>
    </scroll-view>

    <BackTopButton :visible="showBackTop" :page-scroll="false" @back-top="handleBackTop" />
  </view>
</template>

<style scoped lang="scss">
.bubble-page {
  --bubble-accent: var(--ephone-primary-soft);
  --bubble-cool: #7ec8e3;
  position: relative;
  height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 0%, rgba(242, 182, 204, 0.09) 0, rgba(242, 182, 204, 0) 36%),
    radial-gradient(circle at 88% 26%, rgba(126, 200, 227, 0.075) 0, rgba(126, 200, 227, 0) 36%), var(--ephone-bg-scene);
  background-size: 100vw 100vh;
  background-position: 0 0;
  background-attachment: fixed;
  color: var(--ephone-text);
}

.bubble-aura {
  position: fixed;
  z-index: 0;
  border-radius: 50%;
  filter: blur(34rpx);
  pointer-events: none;
}

.bubble-aura-primary {
  top: 146rpx;
  right: -180rpx;
  width: 460rpx;
  height: 460rpx;
  background: rgba(233, 138, 182, 0.115);
}

.bubble-aura-secondary {
  left: -220rpx;
  bottom: 110rpx;
  width: 440rpx;
  height: 440rpx;
  background: rgba(126, 200, 227, 0.07);
}

.bubble-scroll {
  position: absolute;
  top: var(--ephone-transparent-nav-content-top, 88rpx);
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1;
}

.bubble-content {
  box-sizing: border-box;
  max-width: 960rpx;
  min-height: 100%;
  margin: 0 auto;
  padding: 0 40rpx calc(156rpx + env(safe-area-inset-bottom));
}

.bubble-page :deep(.ephone-fixed-search-mask) {
  background:
    radial-gradient(circle at 18% 0%, rgba(242, 182, 204, 0.09) 0, rgba(242, 182, 204, 0) 36%),
    radial-gradient(circle at 88% 26%, rgba(126, 200, 227, 0.075) 0, rgba(126, 200, 227, 0) 36%), var(--ephone-bg-scene);
  background-size: 100vw 100vh;
  background-position: 0 0;
  background-attachment: fixed;
}

.bubble-search-spacer {
  height: 232rpx;
}

.bubble-sort-fixed {
  position: fixed;
  left: 50%;
  z-index: 755;
  box-sizing: border-box;
  width: 100%;
  max-width: 960rpx;
  padding: 0 40rpx;
  transform: translateX(-50%);
}

.bubble-sort-tabs {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0;
  box-sizing: border-box;
  min-height: 72rpx;
  overflow: hidden;
  padding: 0 12rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 28rpx;
  background: rgba(18, 15, 22, 0.88);
  box-shadow:
    0 10rpx 26rpx rgba(0, 0, 0, 0.18),
    0 1rpx 0 rgba(255, 255, 255, 0.05) inset;
  backdrop-filter: blur(18rpx);
  -webkit-backdrop-filter: blur(18rpx);
}

.bubble-sort-tab {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  min-height: 72rpx;
  overflow: hidden;
  margin: 0;
  padding: 0 8rpx;
  border-radius: 0;
  background: transparent;
  color: rgba(255, 255, 255, 0.56);
  font-size: 24rpx;
  font-weight: 750;
  line-height: 72rpx;
}

.bubble-sort-tab-text {
  position: relative;
  z-index: 1;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bubble-sort-tab::after {
  border: 0;
}

.bubble-sort-tab.is-active {
  background: transparent;
  color: #ffd4e4;
  font-weight: 900;
}

.bubble-sort-tab.is-active::before {
  position: absolute;
  left: 50%;
  bottom: 10rpx;
  width: 46rpx;
  height: 4rpx;
  border-radius: 999rpx;
  background: linear-gradient(90deg, rgba(242, 182, 204, 0), rgba(242, 182, 204, 0.98), rgba(242, 182, 204, 0));
  box-shadow: 0 0 12rpx rgba(242, 182, 204, 0.38);
  content: '';
  transform: translateX(-50%);
}

.bubble-sort-tab-hover {
  opacity: 0.82;
}

.bubble-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  margin: 16rpx 4rpx 0;
}

.bubble-section-title {
  color: rgba(255, 255, 255, 0.92);
  font-size: 30rpx;
  font-weight: 900;
}

.bubble-section-desc {
  color: rgba(255, 255, 255, 0.4);
  font-size: 22rpx;
}

.bubble-refreshing {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  min-height: 58rpx;
  color: rgba(255, 255, 255, 0.62);
  font-size: 23rpx;
}

.bubble-refreshing-icon {
  color: var(--bubble-accent);
  font-size: 30rpx;
}

.bubble-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 360rpx;
  margin-top: 20rpx;
  gap: 18rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 28rpx;
  background:
    radial-gradient(circle at 50% 0%, rgba(242, 182, 204, 0.09) 0, rgba(242, 182, 204, 0) 44%),
    rgba(255, 255, 255, 0.045);
  color: rgba(255, 255, 255, 0.62);
  font-size: 26rpx;
}

.bubble-state-icon {
  color: var(--bubble-accent);
  font-size: 56rpx;
}

.bubble-state-error {
  color: rgba(255, 255, 255, 0.72);
}

.bubble-retry {
  min-width: 148rpx;
  height: 62rpx;
  padding: 0 32rpx;
  border: 1rpx solid rgba(242, 182, 204, 0.22);
  border-radius: 999rpx;
  background: rgba(242, 182, 204, 0.1);
  color: #fff;
  font-size: 26rpx;
  line-height: 62rpx;
}

.bubble-retry::after {
  border: 0;
}

.bubble-list-load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 88rpx;
  margin-top: 28rpx;
  padding: 0 32rpx;
  gap: 16rpx;
  color: rgba(255, 255, 255, 0.32);
  font-size: 22rpx;
}

.bubble-list-load-more::before,
.bubble-list-load-more::after {
  content: '';
  width: 64rpx;
  height: 1rpx;
  background: rgba(255, 255, 255, 0.12);
}

.bubble-list-load-more.is-actionable {
  color: rgba(242, 182, 204, 0.72);
}

.bubble-list-load-more.is-actionable::before,
.bubble-list-load-more.is-actionable::after {
  background: rgba(242, 182, 204, 0.22);
}

.bubble-list-load-more-icon {
  color: var(--bubble-accent);
  font-size: 28rpx;
}

.bubble-spin {
  animation: bubble-spin 1.1s linear infinite;
}

@keyframes bubble-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
