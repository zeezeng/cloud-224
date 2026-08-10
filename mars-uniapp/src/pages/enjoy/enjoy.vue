<script setup lang="ts">
import type { EphoneRankRecord } from '@/data/yun'
import { ENJOY_PAGE_SIZE, getEnjoyUserList, toEnjoyRankRecord } from '@/api/enjoy'
import BackTopButton from '@/components/yun/BackTopButton.vue'
import YunFixedSearch from '@/components/yun/YunFixedSearch.vue'
import YunPage from '@/components/yun/YunPage.vue'
import RankingList from '@/components/yun/RankingList.vue'
import { useRefreshLimit } from '@/hooks/useRefreshLimit'
import { formatFetchTime } from '@/utils/yun'

defineOptions({
  name: 'Enjoy',
})

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '乐享值',
    disableScroll: true,
    backgroundColor: '#000000',
  },
})

const searchKeyword = ref('')
const records = ref<EphoneRankRecord[]>([])
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
const fetchedAt = ref('')
const listScrollTop = ref(0)

let loadGeneration = 0
let currentListScrollTop = 0

const summaryText = computed(() => {
  if (!loaded.value || loading.value || refreshing.value) {
    return '共 - 位'
  }
  return `共 ${total.value} 位`
})

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

async function loadEnjoyUsers({ reset = false } = {}) {
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
    const data = await getEnjoyUserList({
      page: requestPage,
      size: ENJOY_PAGE_SIZE,
      keyword: searchKeyword.value,
    })
    if (generation !== loadGeneration) {
      return
    }
    const list = Array.isArray(data?.list) ? data.list : []
    const nextRecords = list.map((item, index) => toEnjoyRankRecord(item, (requestPage - 1) * ENJOY_PAGE_SIZE + index))
    records.value = isFirstPage ? nextRecords : [...records.value, ...nextRecords]
    total.value = Number(data?.total || records.value.length)
    hasMore.value = records.value.length < total.value && list.length >= ENJOY_PAGE_SIZE
    page.value = requestPage + 1
    fetchedAt.value = formatFetchTime()
  }
  catch (error) {
    if (generation !== loadGeneration) {
      return
    }
    console.error('读取乐享用户失败:', error)
    if (isFirstPage) {
      errorMessage.value = '暂时没能读取乐享用户'
      records.value = []
      total.value = 0
    }
    else {
      loadMoreError.value = '加载更多失败'
    }
    uni.showToast({
      title: '乐享用户加载失败',
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
  currentListScrollTop = 0
  listScrollTop.value = 0
  loadEnjoyUsers({ reset: true })
}

function handleLoadMore() {
  if (!hasMore.value || loadingMore.value || loading.value || refreshing.value) {
    return
  }
  loadEnjoyUsers()
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
  loadEnjoyUsers({ reset: true })
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
  loadEnjoyUsers({ reset: true })
})
</script>

<template>
  <YunPage title="乐享值" subtitle="善意发光 · 陪伴有声" scroll-locked>
    <view class="enjoy-layout">
      <YunFixedSearch
        v-model="searchKeyword"
        placeholder="搜索用户昵称/用户ID"
        show-button
        variant="enjoy"
        spacer-height="110rpx"
        @search="handleSearch"
      />

      <view class="enjoy-summary-space" />
      <view class="enjoy-summary">
        <text>{{ summaryText }}</text>
        <text class="enjoy-fetch-time">更新时间：{{ fetchedAt || '--:--:--' }}</text>
      </view>

      <scroll-view
        class="enjoy-list-scroll"
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
        <view class="enjoy-list-content">
          <view v-if="refreshing && loaded" class="enjoy-refreshing">
            <view class="i-carbon-circle-dash enjoy-refreshing-icon enjoy-spin" />
            <text>正在刷新...</text>
          </view>

          <view v-if="loading && !records.length" class="enjoy-state">
            <view class="i-carbon-circle-dash enjoy-state-icon enjoy-spin" />
            <text>正在读取乐享用户...</text>
          </view>

          <view v-else-if="errorMessage" class="enjoy-state enjoy-state-error">
            <view class="i-carbon-cloud-offline enjoy-state-icon" />
            <text>{{ errorMessage }}</text>
            <button class="enjoy-retry" @click="loadEnjoyUsers({ reset: true })">
              重试
            </button>
          </view>

          <template v-else-if="records.length">
            <RankingList
              :records="records"
              value-label="乐享值"
              :show-rank="false"
              variant="enjoy"
            />

            <view
              class="enjoy-list-load-more"
              :class="{ 'is-actionable': !loadingMore && (hasMore || loadMoreError) }"
              @tap="loadMoreError ? loadEnjoyUsers() : handleLoadMore()"
            >
              <view v-if="loadingMore" class="i-carbon-circle-dash enjoy-list-load-more-icon enjoy-spin" />
              <text>{{ bottomText }}</text>
            </view>
          </template>

          <view v-else class="enjoy-state">
            <view class="i-carbon-search-locate enjoy-state-icon" />
            <text>没有找到这个用户</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <BackTopButton :visible="showBackTop" :page-scroll="false" @back-top="handleBackTop" />
  </YunPage>
</template>

<style scoped lang="scss">
.enjoy-layout {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.enjoy-summary-space {
  flex: 0 0 56rpx;
  width: 100%;
  height: 56rpx;
}

.enjoy-summary {
  position: fixed;
  top: calc(var(--ephone-page-content-top, 228rpx) + 110rpx);
  left: 50%;
  z-index: 700;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
  width: 100%;
  max-width: 960rpx;
  min-height: 56rpx;
  padding: 8rpx 40rpx;
  box-sizing: border-box;
  transform: translateX(-50%);
  background: var(--ephone-bg-scene);
  background-size: 100vw 100vh;
  background-position: 0 0;
  background-attachment: fixed;
  color: rgba(255, 255, 255, 0.56);
  font-size: 23rpx;
}

.enjoy-summary .enjoy-fetch-time {
  color: rgba(255, 255, 255, 0.56);
  font-size: 21rpx;
}

.enjoy-list-scroll {
  flex: 1;
  width: 100%;
  min-height: 0;
}

.enjoy-list-content {
  padding: 0 0 24rpx;
}

.enjoy-refreshing {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  min-height: 58rpx;
  color: rgba(255, 255, 255, 0.62);
  font-size: 23rpx;
}

.enjoy-refreshing-icon {
  color: var(--ephone-primary-soft);
  font-size: 30rpx;
}

.enjoy-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 360rpx;
  margin-top: 20rpx;
  gap: 18rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.045);
  color: rgba(255, 255, 255, 0.62);
  font-size: 26rpx;
}

.enjoy-state-icon {
  color: var(--ephone-primary-soft);
  font-size: 56rpx;
}

.enjoy-state-error {
  color: rgba(255, 255, 255, 0.72);
}

.enjoy-retry {
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

.enjoy-retry::after {
  border: 0;
}

.enjoy-list-load-more {
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

.enjoy-list-load-more::before,
.enjoy-list-load-more::after {
  content: '';
  width: 64rpx;
  height: 1rpx;
  background: rgba(255, 255, 255, 0.12);
}

.enjoy-list-load-more.is-actionable {
  color: rgba(242, 182, 204, 0.72);
}

.enjoy-list-load-more.is-actionable::before,
.enjoy-list-load-more.is-actionable::after {
  background: rgba(242, 182, 204, 0.22);
}

.enjoy-list-load-more-icon {
  color: var(--ephone-primary-soft);
  font-size: 28rpx;
}

.enjoy-spin {
  animation: enjoy-spin 1.1s linear infinite;
}

@keyframes enjoy-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
