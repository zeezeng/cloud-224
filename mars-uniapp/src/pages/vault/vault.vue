<script setup lang="ts">
import type { EphoneVaultRecord } from '@/data/ephone'
import { getVaultRecordList, VAULT_PAGE_SIZE } from '@/api/vault'
import BackTopButton from '@/components/ephone/BackTopButton.vue'
import EphoneFixedSearch from '@/components/ephone/EphoneFixedSearch.vue'
import EphoneListStatus from '@/components/ephone/EphoneListStatus.vue'
import YunPage from '@/components/ephone/YunPage.vue'
import VaultCard from '@/components/ephone/VaultCard.vue'
import { formatFetchTime } from '@/utils/ephone'

defineOptions({
  name: 'Vault',
})

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '金库',
    enablePullDownRefresh: true,
    onReachBottomDistance: 120,
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

let loadGeneration = 0

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

onLoad(() => {
  loadVaultRecords({ reset: true })
})

onPullDownRefresh(() => {
  loadVaultRecords({ reset: true })
})

onReachBottom(() => {
  handleLoadMore()
})

onPageScroll((event) => {
  showBackTop.value = event.scrollTop > 420
})
</script>

<template>
  <YunPage title="主播金库" subtitle="稳稳积累 · 闪耀前行">
    <EphoneFixedSearch
      v-model="searchKeyword"
      placeholder="搜索主播昵称"
      show-button
      variant="vault"
      spacer-height="138rpx"
      @search="handleSearch"
    />

    <view class="vault-summary-space" />
    <view class="vault-summary">
      <text>{{ summaryText }}</text>
      <text class="vault-fetch-time">数据获取时间：{{ fetchedAt || '同步中...' }}</text>
    </view>

    <EphoneListStatus
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
    </EphoneListStatus>

    <BackTopButton :visible="showBackTop" />
  </YunPage>
</template>

<style scoped lang="scss">
.vault-summary-space {
  width: 100%;
  height: 56rpx;
}

.vault-summary {
  position: fixed;
  top: calc(var(--ephone-page-content-top, 228rpx) + 138rpx);
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
  color: rgba(255, 255, 255, 0.56);
  font-size: 21rpx;
}
</style>
