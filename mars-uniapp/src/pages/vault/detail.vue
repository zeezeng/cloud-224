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
    disableScroll: true,
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
const listScrollTop = ref(0)

let loadGeneration = 0
let currentListScrollTop = 0

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

function goVaultHistory() {
  uni.navigateTo({
    url: '/pages/vault/history',
  })
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
  loadVaultDetail({ reset: true })
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
</script>

<template>
  <view class="vault-detail-page" :style="navStyle">
    <view class="vault-detail-aura vault-detail-aura-primary" />
    <view class="vault-detail-aura vault-detail-aura-secondary" />
    <YunTransparentNav
      title="金库明细"
      fallback-url="/pages/vault/vault"
      fallback-type="switchTab"
      @layout="handleNavLayout"
    />

    <scroll-view
      class="vault-detail-scroll"
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
      <view class="vault-detail-content">
        <view v-if="record" class="vault-hero">
          <view class="vault-safe-visual">
            <view class="vault-safe-shadow" />
            <view class="vault-safe-body">
              <view class="vault-safe-door">
                <view class="vault-safe-dial">
                  <view class="vault-safe-dial-core" />
                </view>
                <view class="vault-safe-hinge vault-safe-hinge-top" />
                <view class="vault-safe-hinge vault-safe-hinge-bottom" />
              </view>
            </view>
          </view>

          <view class="vault-user">
            <AnchorAvatar
              :src="record.avatar"
              :name="record.name"
              :show-pulse="false"
              ring-color="rgba(255, 255, 255, 0.2)"
              size="md"
            />
            <view class="vault-user-main">
              <view class="vault-user-name">
                {{ record.name }}
                <text v-if="record.group">{{ record.group }}</text>
              </view>
              <view class="vault-user-meta">
                直播间号：{{ record.roomId }}
              </view>
              <view class="vault-user-meta">
                更新时间：{{ fetchedAt || '--:--:--' }}
              </view>
            </view>
          </view>

          <view class="vault-total">
            <view class="vault-total-label">
              金币总额
            </view>
            <view class="vault-total-value">
              {{ formatIntegerMoney(record.balance) }}
            </view>
          </view>

          <view class="vault-summary-stats">
            <view class="vault-summary-stat">
              <view class="vault-summary-icon is-count">
                <view class="i-carbon-recently-viewed" />
              </view>
              <view class="vault-summary-copy">
                <text>本日记录</text>
                <strong>{{ record.dailyRecordCount }} 笔</strong>
              </view>
            </view>
            <view class="vault-summary-stat">
              <view class="vault-summary-icon is-delta">
                <view class="i-carbon-money" />
              </view>
              <view class="vault-summary-copy">
                <text>本日增减</text>
                <strong :class="{ 'is-down': record.dailyDelta < 0 }">
                  {{ formatSignedIntegerMoney(record.dailyDelta) }}
                </strong>
              </view>
            </view>
          </view>
        </view>

        <view v-if="record" class="vault-record-head">
          <view class="vault-record-title">
            变动记录
          </view>
          <view class="vault-record-link" hover-class="vault-record-link-hover" @tap="goVaultHistory">
            <text>全部记录</text>
            <view class="i-carbon-chevron-right vault-record-link-icon" />
          </view>
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
          more-text="继续上拉查看更多记录"
          no-more-text="没有更多记录了"
          @retry="loadMoreError ? loadVaultDetail() : loadVaultDetail({ reset: true })"
          @load-more="handleLoadMore"
        >
          <view v-if="record" class="vault-change-list">
            <view
              v-for="change in record.changes"
              :key="change.id"
              class="vault-change-row"
              hover-class="vault-change-row-hover"
            >
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
    </scroll-view>

    <BackTopButton :visible="showBackTop" :page-scroll="false" @back-top="handleBackTop" />
  </view>
</template>

<style scoped lang="scss">
.vault-detail-page {
  position: relative;
  height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 0%, rgba(119, 124, 148, 0.16) 0, rgba(119, 124, 148, 0) 36%),
    radial-gradient(circle at 86% 28%, rgba(121, 93, 132, 0.16) 0, rgba(121, 93, 132, 0) 34%),
    linear-gradient(180deg, #060810 0%, #090b13 46%, #05080d 100%);
  color: var(--ephone-text);
}

.vault-detail-aura {
  position: fixed;
  z-index: 0;
  border-radius: 50%;
  filter: blur(34rpx);
  pointer-events: none;
}

.vault-detail-aura-primary {
  top: 146rpx;
  right: -180rpx;
  width: 460rpx;
  height: 460rpx;
  background: rgba(233, 138, 182, 0.12);
}

.vault-detail-aura-secondary {
  left: -220rpx;
  bottom: 110rpx;
  width: 440rpx;
  height: 440rpx;
  background: rgba(120, 104, 183, 0.1);
}

.vault-detail-scroll {
  position: absolute;
  top: var(--ephone-transparent-nav-content-top, calc(env(safe-area-inset-top) + 88rpx));
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1;
}

.vault-detail-content {
  box-sizing: border-box;
  max-width: 960rpx;
  min-height: 100%;
  margin: 0 auto;
  padding: 28rpx 36rpx calc(156rpx + env(safe-area-inset-bottom));
}

.vault-hero {
  position: relative;
  overflow: hidden;
  padding: 32rpx 30rpx 30rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  border-radius: 34rpx;
  background:
    radial-gradient(circle at 15% 0%, rgba(255, 255, 255, 0.085) 0, rgba(255, 255, 255, 0) 35%),
    radial-gradient(circle at 92% 62%, rgba(89, 77, 109, 0.26) 0, rgba(89, 77, 109, 0) 42%),
    linear-gradient(138deg, rgba(20, 24, 34, 0.94) 0%, rgba(13, 16, 24, 0.94) 54%, rgba(17, 17, 28, 0.9) 100%);
  box-shadow: 0 34rpx 86rpx rgba(0, 0, 0, 0.34);
}

.vault-safe-visual {
  position: absolute;
  top: 130rpx;
  right: -14rpx;
  z-index: 0;
  width: 278rpx;
  height: 206rpx;
  opacity: 0.76;
  pointer-events: none;
}

.vault-safe-shadow {
  position: absolute;
  right: -46rpx;
  bottom: -22rpx;
  width: 290rpx;
  height: 100rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.48);
  filter: blur(20rpx);
}

.vault-safe-body {
  position: absolute;
  top: 26rpx;
  right: 8rpx;
  width: 232rpx;
  height: 160rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 28rpx;
  background: linear-gradient(145deg, #363a47 0%, #171a25 48%, #070910 100%);
  box-shadow:
    inset 18rpx 20rpx 28rpx rgba(255, 255, 255, 0.06),
    inset -22rpx -20rpx 28rpx rgba(0, 0, 0, 0.52),
    24rpx 24rpx 48rpx rgba(0, 0, 0, 0.36);
  transform: perspective(520rpx) rotateY(-10deg) rotateZ(-1deg);
}

.vault-safe-door {
  position: absolute;
  top: 22rpx;
  left: 22rpx;
  width: 150rpx;
  height: 116rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 22rpx;
  background: linear-gradient(145deg, rgba(60, 64, 78, 0.92), rgba(17, 20, 30, 0.94));
  box-shadow:
    inset 8rpx 8rpx 18rpx rgba(255, 255, 255, 0.06),
    inset -12rpx -12rpx 22rpx rgba(0, 0, 0, 0.42);
}

.vault-safe-dial {
  position: absolute;
  top: 36rpx;
  left: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58rpx;
  height: 58rpx;
  border: 6rpx solid rgba(12, 14, 22, 0.72);
  border-radius: 50%;
  background:
    radial-gradient(circle at 34% 30%, rgba(255, 255, 255, 0.34), rgba(255, 255, 255, 0) 34%),
    linear-gradient(135deg, rgba(235, 239, 255, 0.34), rgba(32, 35, 47, 0.88) 48%, rgba(22, 25, 34, 0.94)), #242837;
  box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.38);
}

.vault-safe-dial-core {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.32), rgba(25, 29, 39, 0.9) 68%);
}

.vault-safe-hinge {
  position: absolute;
  right: -18rpx;
  width: 18rpx;
  height: 42rpx;
  border-radius: 0 10rpx 10rpx 0;
  background: linear-gradient(180deg, rgba(87, 91, 105, 0.9), rgba(14, 17, 25, 0.96));
  box-shadow: inset -4rpx 0 8rpx rgba(0, 0, 0, 0.38);
}

.vault-safe-hinge-top {
  top: 18rpx;
}

.vault-safe-hinge-bottom {
  bottom: 18rpx;
}

.vault-user {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.vault-user-main {
  min-width: 0;
}

.vault-user-name {
  display: flex;
  align-items: center;
  gap: 14rpx;
  min-width: 0;
  color: #fff;
  font-size: 32rpx;
  font-weight: 900;
  line-height: 1.18;
}

.vault-user-name text {
  flex: 0 0 auto;
  max-width: 136rpx;
  overflow: hidden;
  padding: 2rpx 12rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.12);
  border-radius: 10rpx;
  background: rgba(255, 255, 255, 0.055);
  color: rgba(255, 255, 255, 0.72);
  font-size: 22rpx;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-user-meta {
  margin-top: 10rpx;
  color: rgba(224, 230, 242, 0.64);
  font-size: 24rpx;
  line-height: 1.2;
}

.vault-total {
  position: relative;
  z-index: 1;
  max-width: 440rpx;
  margin-top: 54rpx;
}

.vault-total-label {
  color: rgba(224, 230, 242, 0.64);
  font-size: 24rpx;
}

.vault-total-value {
  margin-top: 14rpx;
  color: #fff;
  font-size: 72rpx;
  font-weight: 900;
  font-variant-numeric: tabular-nums;
  line-height: 0.98;
  text-shadow: 0 16rpx 34rpx rgba(255, 255, 255, 0.1);
  word-break: break-all;
}

.vault-summary-stats {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-top: 34rpx;
}

.vault-summary-stat {
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 18rpx;
  min-width: 0;
  min-height: 110rpx;
  padding: 18rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.07);
  border-radius: 20rpx;
  background: rgba(15, 20, 30, 0.72);
  box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.045);
  backdrop-filter: blur(16rpx);
  -webkit-backdrop-filter: blur(16rpx);
}

.vault-summary-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  font-size: 31rpx;
}

.vault-summary-icon.is-count {
  background: rgba(154, 127, 255, 0.16);
  color: #c6a9ff;
  box-shadow: 0 0 34rpx rgba(154, 127, 255, 0.18);
}

.vault-summary-icon.is-delta {
  background: rgba(87, 229, 151, 0.14);
  color: #69ebb1;
  box-shadow: 0 0 34rpx rgba(87, 229, 151, 0.14);
}

.vault-summary-copy {
  min-width: 0;
}

.vault-summary-copy text {
  display: block;
  color: rgba(224, 230, 242, 0.62);
  font-size: 22rpx;
  line-height: 1.2;
}

.vault-summary-copy strong {
  display: block;
  margin-top: 12rpx;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.96);
  font-size: 30rpx;
  font-weight: 900;
  line-height: 1.1;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-summary-copy strong.is-down,
.vault-change-mark.is-down,
.vault-change-amount.is-down {
  color: #63e6a3;
}

.vault-record-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
  margin: 40rpx 4rpx 20rpx;
}

.vault-record-title {
  color: #fff;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 1.2;
}

.vault-record-link {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex: 0 0 auto;
  min-width: 136rpx;
  min-height: 56rpx;
  color: rgba(224, 230, 242, 0.52);
  font-size: 24rpx;
}

.vault-record-link-hover {
  color: rgba(255, 255, 255, 0.86);
  opacity: 0.88;
}

.vault-record-link-icon {
  margin-left: 4rpx;
  font-size: 24rpx;
}

.vault-change-list {
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.075);
  border-radius: 30rpx;
  background:
    radial-gradient(circle at 0% 0%, rgba(255, 255, 255, 0.055) 0, rgba(255, 255, 255, 0) 36%), rgba(13, 18, 27, 0.88);
  box-shadow: 0 26rpx 64rpx rgba(0, 0, 0, 0.22);
}

.vault-change-row {
  display: grid;
  grid-template-columns: 62rpx minmax(0, 1fr) 150rpx;
  gap: 20rpx;
  align-items: center;
  min-height: 124rpx;
  padding: 24rpx 26rpx;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.065);
}

.vault-change-row:last-child {
  border-bottom: 0;
}

.vault-change-row-hover {
  background: rgba(255, 255, 255, 0.035);
}

.vault-change-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  align-self: center;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.12), rgba(255, 255, 255, 0.035));
  color: #bda0ff;
  font-size: 34rpx;
  font-weight: 900;
  box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.12);
}

.vault-change-main {
  min-width: 0;
}

.vault-change-name {
  overflow: hidden;
  color: #fff;
  font-size: 27rpx;
  font-weight: 850;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-change-desc {
  margin-top: 10rpx;
  color: rgba(224, 230, 242, 0.5);
  font-size: 22rpx;
  line-height: 1.45;
  overflow-wrap: anywhere;
  white-space: normal;
  word-break: break-word;
}

.vault-change-amount {
  padding-top: 2rpx;
  color: #f3a6cc;
  font-size: 34rpx;
  font-weight: 900;
  font-variant-numeric: tabular-nums;
  line-height: 1.1;
  text-align: right;
  white-space: nowrap;
}
</style>
