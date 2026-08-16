<script setup lang="ts">
import type { EphoneVaultRecord } from '@/data/yun'
import { getVaultRecordList, VAULT_PAGE_SIZE } from '@/api/vault'
import BackTopButton from '@/components/yun/BackTopButton.vue'
import YunFixedSearch from '@/components/yun/YunFixedSearch.vue'
import YunListStatus from '@/components/yun/YunListStatus.vue'
import YunPage from '@/components/yun/YunPage.vue'
import VaultCard from '@/components/yun/VaultCard.vue'
import { useRefreshLimit } from '@/hooks/useRefreshLimit'
import { formatFetchTime } from '@/utils/yun'

defineOptions({
  name: 'Vault',
})

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '金库',
    disableScroll: true,
    backgroundColor: '#000000',
  },
})

const searchKeyword = ref('')
const records = ref<EphoneVaultRecord[]>([])
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
const fetchedAt = ref('')
const listScrollTop = ref(0)

let loadGeneration = 0
let currentListScrollTop = 0

function goHistoryPage() {
  uni.navigateTo({
    url: '/pages/vault/history',
  })
}

const summaryText = computed(() => {
  if (!loaded.value || loading.value || refreshing.value) {
    return '共 - 位'
  }
  return `共 ${total.value} 位`
})

async function loadVaultRecords({ reset = false } = {}) {
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
    errorMessage.value = ''
  }
  else {
    loadingMore.value = true
    loadMoreError.value = ''
  }

  try {
    const result = await getVaultRecordList({
      page: requestPage,
      size: VAULT_PAGE_SIZE,
      keyword: searchKeyword.value,
    })
    if (generation !== loadGeneration) {
      return
    }
    records.value = isFirstPage ? result.list : records.value.concat(result.list)
    total.value = result.total
    page.value = result.page + 1
    hasMore.value = result.hasMore
    fetchedAt.value = formatFetchTime()
  }
  catch (error) {
    if (generation !== loadGeneration) {
      return
    }
    console.error(error)
    if (isFirstPage) {
      errorMessage.value = '暂时没能读取金库数据'
      records.value = []
      total.value = 0
    }
    else {
      loadMoreError.value = '加载更多失败'
    }
    uni.showToast({
      icon: 'none',
      title: '金库数据加载失败',
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
  searchKeyword.value = value.trim()
  // 清空列表并重置滚动位置，确保搜索结果从序号 1 开始显示
  records.value = []
  total.value = 0
  currentListScrollTop = 0
  listScrollTop.value = 0
  loadVaultRecords({ reset: true })
}

function handleLoadMore() {
  if (!hasMore.value || loadingMore.value || loading.value || refreshing.value) {
    return
  }
  loadVaultRecords()
}

function goVaultDetail(record: EphoneVaultRecord) {
  uni.navigateTo({
    url: `/pages/vault/detail?id=${record.id}`,
  })
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
  loadVaultRecords({ reset: true })
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
  loadVaultRecords({ reset: true })
})
</script>

<template>
  <YunPage title="主播金库" subtitle="稳稳积累 · 闪耀前行" scroll-locked>
    <view class="vault-layout">
      <YunFixedSearch
        v-model="searchKeyword"
        placeholder="搜索主播昵称"
        show-button
        variant="vault"
        spacer-height="110rpx"
        @search="handleSearch"
      />

      <view class="vault-summary-space" />
      <view class="vault-summary">
        <view class="vault-summary-left">
          <text>{{ summaryText }}</text>
          <text class="vault-fetch-time">· 更新 {{ fetchedAt || '--:--:--' }}</text>
        </view>
        <view
          class="vault-history-link"
          hover-class="vault-history-link-hover"
          @tap="goHistoryPage"
        >
          <text>最近变动</text>
          <view class="i-carbon-chevron-right vault-history-link-arrow" />
        </view>
      </view>

      <scroll-view
        class="vault-list-scroll"
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
        <view class="vault-list-content">
          <YunListStatus
            :loading="loading"
            :refreshing="refreshing"
            :loading-more="loadingMore"
            :loaded="loaded"
            :has-more="hasMore"
            :has-items="!!records.length"
            :error-message="errorMessage"
            :load-more-error="loadMoreError"
            loading-text="正在读取金库数据..."
            empty-text="没有找到金库团员"
            @retry="loadMoreError ? loadVaultRecords() : loadVaultRecords({ reset: true })"
            @load-more="handleLoadMore"
          >
            <VaultCard
              v-for="(record, index) in records"
              :key="record.id"
              :record="record"
              :rank="index + 1"
              @view="goVaultDetail"
            />
          </YunListStatus>
        </view>
      </scroll-view>
    </view>

    <BackTopButton :visible="showBackTop" :page-scroll="false" @back-top="handleBackTop" />
  </YunPage>
</template>

<style scoped lang="scss">
.vault-layout {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.vault-summary-space {
  flex: 0 0 56rpx;
  width: 100%;
  height: 56rpx;
}

.vault-summary {
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

.vault-summary .vault-fetch-time {
  color: rgba(255, 255, 255, 0.4);
  font-size: 21rpx;
}

.vault-summary-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
  min-width: 0;
  overflow: hidden;
}

.vault-summary-left .vault-fetch-time {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-list-scroll {
  flex: 1;
  width: 100%;
  min-height: 0;
}

.vault-list-content {
  max-width: 960rpx;
  margin: 0 auto;
  padding: 8rpx 40rpx 20rpx;
}

.vault-history-link {
  display: inline-flex;
  align-items: center;
  gap: 2rpx;
  color: rgba(255, 255, 255, 0.4);
  font-size: 21rpx;
}

.vault-history-link-hover {
  color: rgba(255, 255, 255, 0.7);
}

.vault-history-link-arrow {
  font-size: 20rpx;
}
</style>
