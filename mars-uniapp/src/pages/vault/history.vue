<script setup lang="ts">
import type { VaultRecentChange } from '@/api/vault'
import { clearVaultPlayerCache, getRecentVaultChanges, VAULT_PAGE_SIZE } from '@/api/vault'
import BackTopButton from '@/components/yun/BackTopButton.vue'
import YunListStatus from '@/components/yun/YunListStatus.vue'
import YunTransparentNav from '@/components/yun/YunTransparentNav.vue'
import { useRefreshLimit } from '@/hooks/useRefreshLimit'
import { formatFetchTime, formatSignedIntegerMoney } from '@/utils/yun'

defineOptions({
  name: 'VaultHistory',
})

definePage({
  style: {
    navigationStyle: 'custom',
    disableScroll: true,
    backgroundColor: '#000000',
  },
})

const navStyle = ref<Record<string, string>>({})
const records = ref<VaultRecentChange[]>([])
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

const summaryText = computed(() => {
  if (!loaded.value || loading.value || refreshing.value) {
    return '共 - 条'
  }
  return `共 ${total.value} 条`
})

function handleNavLayout(style: Record<string, string>) {
  navStyle.value = style
}

async function loadRecentChanges({ reset = false } = {}) {
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
    const result = await getRecentVaultChanges({ page: requestPage, size: VAULT_PAGE_SIZE })
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
      errorMessage.value = '暂时没能读取最近变动'
      records.value = []
      total.value = 0
    }
    else {
      loadMoreError.value = '加载更多失败'
    }
    uni.showToast({
      icon: 'none',
      title: isFirstPage ? '最近变动加载失败' : '记录加载失败',
    })
  }
  finally {
    if (generation === loadGeneration) {
      loading.value = false
      refreshing.value = false
      loadingMore.value = false
      loaded.value = true
    }
  }
}

function handleLoadMore() {
  if (!hasMore.value || loadingMore.value || loading.value || refreshing.value) {
    return
  }
  loadRecentChanges()
}

function goVaultDetail(item: VaultRecentChange) {
  if (!item.playerId) {
    return
  }
  uni.navigateTo({
    url: `/pages/vault/detail?id=${item.playerId}`,
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
  clearVaultPlayerCache()
  loadRecentChanges({ reset: true })
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
  loadRecentChanges({ reset: true })
})
</script>

<template>
  <view class="vault-history-page" :style="navStyle">
    <view class="vault-history-glow" />
    <YunTransparentNav
      title="最近变动"
      fallback-url="/pages/vault/vault"
      fallback-type="switchTab"
      @layout="handleNavLayout"
    />

    <scroll-view
      class="vault-history-scroll"
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
      <view class="vault-history-content">
        <view class="vault-history-summary">
          <text>{{ summaryText }}</text>
          <text class="vault-history-summary-time">· 更新 {{ fetchedAt || '--:--:--' }}</text>
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
          loading-text="正在读取最近变动..."
          empty-text="暂无变动记录"
          empty-icon="i-carbon-recently-viewed"
          @retry="loadMoreError ? loadRecentChanges() : loadRecentChanges({ reset: true })"
          @load-more="handleLoadMore"
        >
          <view class="vault-history-list">
            <view
              v-for="item in records"
              :key="`${item.card}-${item.id}`"
              class="vault-history-row"
              :class="{ 'is-clickable': item.playerId }"
              hover-class="vault-history-row-hover"
              @tap="goVaultDetail(item)"
            >
              <view class="vault-history-mark" :class="{ 'is-down': item.amount < 0 }">
                {{ item.amount >= 0 ? '+' : '-' }}
              </view>
              <view class="vault-history-main">
                <view class="vault-history-row-head">
                  <view class="vault-history-row-title">
                    {{ item.title }}
                  </view>
                  <view class="vault-history-row-anchor">
                    {{ item.playerName }}
                  </view>
                </view>
                <view class="vault-history-row-desc">
                  {{ item.time }} · {{ item.remark }}
                </view>
              </view>
              <view class="vault-history-amount" :class="{ 'is-down': item.amount < 0 }">
                {{ formatSignedIntegerMoney(item.amount) }}
              </view>
            </view>
          </view>
        </YunListStatus>
      </view>
    </scroll-view>

    <BackTopButton :visible="showBackTop" :page-scroll="false" @back-top="handleBackTop" />
  </view>
</template>

<style scoped lang="scss">
.vault-history-page {
  position: relative;
  height: 100vh;
  overflow: hidden;
  background: var(--ephone-bg-scene);
  color: var(--ephone-text);
}

.vault-history-glow {
  position: fixed;
  right: -180rpx;
  bottom: 120rpx;
  width: 420rpx;
  height: 420rpx;
  border-radius: 50%;
  background: rgba(233, 138, 182, 0.055);
  filter: blur(30rpx);
  pointer-events: none;
}

.vault-history-scroll {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
}

.vault-history-content {
  box-sizing: border-box;
  max-width: 960rpx;
  min-height: 100%;
  margin: 0 auto;
  padding: calc(var(--ephone-transparent-nav-top, env(safe-area-inset-top)) + var(--ephone-transparent-nav-height, 88rpx) + 36rpx) 40rpx 160rpx;
}

.vault-history-summary {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 18rpx;
  color: rgba(255, 255, 255, 0.56);
  font-size: 23rpx;
}

.vault-history-summary-time {
  color: rgba(255, 255, 255, 0.4);
  font-size: 21rpx;
}

.vault-history-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.vault-history-row {
  display: grid;
  grid-template-columns: 58rpx minmax(0, 1fr) 190rpx;
  gap: 16rpx;
  align-items: flex-start;
  min-height: 112rpx;
  padding: 18rpx 20rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 24rpx;
  background: rgba(18, 18, 24, 0.86);
}

.vault-history-row.is-clickable {
  cursor: pointer;
}

.vault-history-row-hover {
  opacity: 0.86;
}

.vault-history-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  align-self: center;
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  color: var(--ephone-primary-soft);
  font-size: 32rpx;
  font-weight: 900;
}

.vault-history-mark.is-down,
.vault-history-amount.is-down {
  color: #36d889;
}

.vault-history-main {
  min-width: 0;
}

.vault-history-row-head {
  display: flex;
  align-items: center;
  gap: 12rpx;
  min-width: 0;
}

.vault-history-row-title {
  overflow: hidden;
  color: #fff;
  font-size: 28rpx;
  font-weight: 850;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-history-row-anchor {
  flex: 0 0 auto;
  max-width: 180rpx;
  overflow: hidden;
  padding: 2rpx 12rpx;
  border: 1rpx solid rgba(233, 138, 182, 0.32);
  border-radius: 10rpx;
  background: rgba(233, 138, 182, 0.1);
  color: var(--ephone-primary-soft);
  font-size: 20rpx;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-history-row-desc {
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.5);
  font-size: 22rpx;
  line-height: 1.45;
  overflow-wrap: anywhere;
  white-space: normal;
  word-break: break-word;
}

.vault-history-amount {
  padding-top: 2rpx;
  color: var(--ephone-primary-soft);
  font-size: 30rpx;
  font-weight: 900;
  text-align: right;
}
</style>
