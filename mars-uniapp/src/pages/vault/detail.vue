<script setup lang="ts">
import type { EphoneVaultRecord } from '@/data/yun'
import { getVaultRecordDetail, getVaultRecordLogPage, VAULT_DETAIL_LOG_SIZE } from '@/api/vault'
import BackTopButton from '@/components/yun/BackTopButton.vue'
import AnchorAvatar from '@/components/yun/AnchorAvatar.vue'
import YunListStatus from '@/components/yun/YunListStatus.vue'
import YunTransparentNav from '@/components/yun/YunTransparentNav.vue'
import { useRefreshLimit } from '@/hooks/useRefreshLimit'
import { formatFetchTime, formatIntegerMoney, formatSignedIntegerMoney } from '@/utils/yun'

defineOptions({
  name: 'VaultDetail',
})

definePage({
  style: {
    navigationStyle: 'custom',
    enablePullDownRefresh: true,
    onReachBottomDistance: 120,
    backgroundColor: '#000000',
  },
})

const vaultId = ref('')
const navStyle = ref<Record<string, string>>({})
const record = ref<EphoneVaultRecord | null>(null)
const vaultCard = ref('')
const logsPage = ref(1)
const logsTotal = ref(0)
const logsHasMore = ref(true)
const loading = ref(false)
const refreshing = ref(false)
const loadingMore = ref(false)
const loaded = ref(false)
const loadFailed = ref(false)
const loadMoreError = ref('')
const showBackTop = ref(false)
const fetchedAt = ref('')

let loadGeneration = 0

onLoad((query) => {
  const id = String(query?.id || '').trim()
  if (id) {
    vaultId.value = id
    loadVaultDetail({ reset: true })
  }
})

function handleNavLayout(style: Record<string, string>) {
  navStyle.value = style
}

async function loadVaultDetail({ reset = false } = {}) {
  if (!vaultId.value) {
    loadFailed.value = true
    return
  }

  if ((loading.value || refreshing.value || loadingMore.value) && !reset) {
    return
  }

  const generation = ++loadGeneration
  const requestPage = reset ? 1 : logsPage.value
  const isFirstPage = requestPage === 1

  if (reset) {
    logsPage.value = 1
    logsTotal.value = 0
    logsHasMore.value = true
    loadMoreError.value = ''
  }

  if (isFirstPage) {
    loading.value = !record.value
    refreshing.value = !!record.value
    loadFailed.value = false
  }
  else {
    loadingMore.value = true
    loadMoreError.value = ''
  }

  try {
    if (isFirstPage) {
      const result = await getVaultRecordDetail(vaultId.value)
      if (generation !== loadGeneration) {
        return
      }
      record.value = result.record
      vaultCard.value = result.card
      logsTotal.value = result.logsTotal
      logsPage.value = result.logsPage + 1
      logsHasMore.value = result.logsPage * VAULT_DETAIL_LOG_SIZE < result.logsTotal
    }
    else {
      if (!record.value || !vaultCard.value) {
        logsHasMore.value = false
        return
      }
      const result = await getVaultRecordLogPage(vaultCard.value, {
        page: requestPage,
        size: VAULT_DETAIL_LOG_SIZE,
      })
      if (generation !== loadGeneration) {
        return
      }
      record.value = {
        ...record.value,
        changes: record.value.changes.concat(result.list),
      }
      logsTotal.value = result.total
      logsPage.value = result.page + 1
      logsHasMore.value = result.hasMore
    }
    fetchedAt.value = formatFetchTime()
  }
  catch (error) {
    if (generation !== loadGeneration) {
      return
    }
    console.error(error)
    if (isFirstPage) {
      loadFailed.value = true
      record.value = null
    }
    else {
      loadMoreError.value = '加载更多失败'
    }
    uni.showToast({
      icon: 'none',
      title: isFirstPage ? '金库详情加载失败' : '记录加载失败',
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

function handleLoadMore() {
  if (!logsHasMore.value || loadingMore.value || loading.value || refreshing.value) {
    return
  }
  loadVaultDetail()
}

const { tryRefresh } = useRefreshLimit(5000)

onPullDownRefresh(() => {
  if (!tryRefresh()) {
    uni.showToast({
      icon: 'none',
      title: '刷新太频繁，请 5 秒后再试',
    })
    uni.stopPullDownRefresh()
    return
  }
  loadVaultDetail({ reset: true })
})

onReachBottom(() => {
  handleLoadMore()
})

onPageScroll((event) => {
  showBackTop.value = event.scrollTop > 420
})
</script>

<template>
  <view class="vault-detail-page" :style="navStyle">
    <view class="vault-detail-glow" />
    <YunTransparentNav
      title="金库明细"
      fallback-url="/pages/vault/vault"
      fallback-type="switchTab"
      @layout="handleNavLayout"
    />

    <view class="vault-detail-content">
      <view v-if="record" class="vault-summary">
        <view class="vault-user">
          <AnchorAvatar :src="record.avatar" :name="record.name" :show-pulse="false" size="md" />
          <view class="vault-user-main">
            <view class="vault-user-name">
              {{ record.name }}
              <text>{{ record.group }}</text>
            </view>
            <view class="vault-user-meta">
              直播间号：{{ record.roomId }}
            </view>
            <view class="vault-user-meta">
              更新时间：{{ fetchedAt || '--:--:--' }}
            </view>
          </view>
        </view>

        <view class="vault-total-label">
          金币总额
        </view>
        <view class="vault-total-value">
          {{ formatIntegerMoney(record.balance) }}
        </view>

        <view class="vault-summary-stats">
          <view class="vault-summary-stat">
            <text>本日记录</text>
            <strong>{{ record.dailyRecordCount }} 笔</strong>
          </view>
          <view class="vault-summary-stat">
            <text>本日增减</text>
            <strong :class="{ 'is-down': record.dailyDelta < 0 }">
              {{ formatSignedIntegerMoney(record.dailyDelta) }}
            </strong>
          </view>
        </view>
      </view>

      <view v-if="record" class="vault-record-title">
        变动记录
      </view>

      <YunListStatus
        :loading="loading"
        :refreshing="refreshing"
        :loading-more="loadingMore"
        :loaded="loaded"
        :has-more="logsHasMore"
        :has-items="!!record?.changes.length"
        :error-message="loadFailed ? '金库详情加载失败' : ''"
        :load-more-error="loadMoreError"
        loading-text="正在读取金库详情..."
        empty-text="暂无乐享币增减记录"
        @retry="loadMoreError ? loadVaultDetail() : loadVaultDetail({ reset: true })"
        @load-more="handleLoadMore"
      >
        <view v-if="record" class="vault-change-list">
          <view v-for="change in record.changes" :key="change.id" class="vault-change-row">
            <view class="vault-change-mark" :class="{ 'is-down': change.amount < 0 }">
              {{ change.amount >= 0 ? '+' : '-' }}
            </view>
            <view class="vault-change-main">
              <view class="vault-change-name">
                {{ change.title }}
              </view>
              <view class="vault-change-desc">
                {{ change.time }} · {{ change.remark }}
              </view>
            </view>
            <view class="vault-change-amount" :class="{ 'is-down': change.amount < 0 }">
              {{ formatSignedIntegerMoney(change.amount) }}
            </view>
          </view>
        </view>
      </YunListStatus>
    </view>

    <BackTopButton :visible="showBackTop" />
  </view>
</template>

<style scoped lang="scss">
.vault-detail-page {
  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  background: var(--ephone-bg-scene);
  color: var(--ephone-text);
}

.vault-detail-glow {
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

.vault-detail-content {
  position: relative;
  z-index: 1;
  box-sizing: border-box;
  max-width: 960rpx;
  min-height: 100vh;
  margin: 0 auto;
  padding: calc(var(--ephone-transparent-nav-top, env(safe-area-inset-top)) + var(--ephone-transparent-nav-height, 88rpx) + 36rpx) 40rpx 160rpx;
}

.vault-summary {
  padding: 28rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.045);
  box-shadow: none;
}

.vault-user {
  display: flex;
  align-items: center;
  gap: 22rpx;
}

.vault-user-main {
  min-width: 0;
}

.vault-user-name {
  display: flex;
  align-items: center;
  gap: 14rpx;
  color: #fff;
  font-size: 34rpx;
  font-weight: 900;
}

.vault-user-name text {
  padding: 2rpx 12rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.12);
  border-radius: 10rpx;
  color: rgba(255, 255, 255, 0.78);
  font-size: 22rpx;
}

.vault-user-meta {
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.58);
  font-size: 24rpx;
}

.vault-total-label {
  margin-top: 34rpx;
  color: rgba(255, 255, 255, 0.58);
  font-size: 24rpx;
}

.vault-total-value {
  margin-top: 10rpx;
  color: #fff;
  font-size: 58rpx;
  font-weight: 900;
  line-height: 1;
  text-shadow: none;
}

.vault-summary-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-top: 28rpx;
}

.vault-summary-stat {
  min-height: 88rpx;
  padding: 16rpx 18rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.07);
}

.vault-summary-stat text {
  display: block;
  color: rgba(255, 255, 255, 0.5);
  font-size: 22rpx;
}

.vault-summary-stat strong {
  display: block;
  margin-top: 10rpx;
  color: var(--ephone-primary-soft);
  font-size: 32rpx;
  font-weight: 900;
}

.vault-summary-stat .is-down,
.vault-change-mark.is-down,
.vault-change-amount.is-down {
  color: #36d889;
}

.vault-record-title {
  margin: 34rpx 0 18rpx;
  color: #fff;
  font-size: 34rpx;
  font-weight: 900;
}

.vault-change-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.vault-change-row {
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

.vault-change-mark {
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

.vault-change-main {
  min-width: 0;
}

.vault-change-name {
  overflow: hidden;
  color: #fff;
  font-size: 28rpx;
  font-weight: 850;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-change-desc {
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.5);
  font-size: 22rpx;
  line-height: 1.45;
  overflow-wrap: anywhere;
  white-space: normal;
  word-break: break-word;
}

.vault-change-amount {
  padding-top: 2rpx;
  color: var(--ephone-primary-soft);
  font-size: 30rpx;
  font-weight: 900;
  text-align: right;
}
</style>
