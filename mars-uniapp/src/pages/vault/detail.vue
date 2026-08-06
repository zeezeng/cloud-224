<script setup lang="ts">
import { vaultRecords } from '@/data/ephone'
import { formatMoney, formatSignedMoney } from '@/utils/ephone'
import AnchorAvatar from '@/components/ephone/AnchorAvatar.vue'
import EphoneTransparentNav from '@/components/ephone/EphoneTransparentNav.vue'

defineOptions({
  name: 'VaultDetail',
})

definePage({
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#000000',
  },
})

const vaultId = ref(vaultRecords[0]?.id || 1)
const navStyle = ref<Record<string, string>>({})

onLoad((query) => {
  const id = Number(query?.id)
  if (Number.isFinite(id)) {
    vaultId.value = id
  }
})

const record = computed(() => {
  return vaultRecords.find(item => item.id === vaultId.value) || vaultRecords[0]
})

function handleNavLayout(style: Record<string, string>) {
  navStyle.value = style
}
</script>

<template>
  <view class="vault-detail-page" :style="navStyle">
    <view class="vault-detail-glow" />
    <EphoneTransparentNav
      title="金库明细"
      fallback-url="/pages/vault/vault"
      fallback-type="switchTab"
      @layout="handleNavLayout"
    />

    <view class="vault-detail-content">
      <view class="vault-summary">
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
              更新时间：{{ record.updatedAt }}
            </view>
          </view>
        </view>

        <view class="vault-total-label">
          金币总额
        </view>
        <view class="vault-total-value">
          {{ formatMoney(record.balance) }}
        </view>

        <view class="vault-summary-stats">
          <view class="vault-summary-stat">
            <text>本日记录</text>
            <strong>{{ record.dailyRecordCount }} 笔</strong>
          </view>
          <view class="vault-summary-stat">
            <text>本日增减</text>
            <strong :class="{ 'is-down': record.dailyDelta < 0 }">
              {{ formatSignedMoney(record.dailyDelta) }}
            </strong>
          </view>
        </view>
      </view>

      <view class="vault-record-title">
        变动记录
      </view>

      <view class="vault-change-list">
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
            {{ formatSignedMoney(change.amount) }}
          </view>
        </view>
      </view>
    </view>
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
  background: rgba(255, 76, 166, 0.08);
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
  border: 1rpx solid rgba(255, 88, 167, 0.3);
  border-radius: 32rpx;
  background:
    radial-gradient(circle at 96% 10%, rgba(255, 82, 166, 0.24), transparent 38%),
    linear-gradient(145deg, rgba(255, 70, 158, 0.16), rgba(12, 13, 20, 0.92));
  box-shadow: inset 0 0 34rpx rgba(255, 70, 158, 0.08), 0 18rpx 44rpx rgba(0, 0, 0, 0.24);
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
  border: 1rpx solid rgba(255, 91, 174, 0.62);
  border-radius: 10rpx;
  color: var(--ephone-primary);
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
  text-shadow: 0 0 24rpx rgba(255, 78, 160, 0.82);
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
  color: var(--ephone-primary);
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
  align-items: center;
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
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: rgba(255, 82, 166, 0.16);
  color: var(--ephone-primary);
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
  overflow: hidden;
  color: rgba(255, 255, 255, 0.5);
  font-size: 22rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-change-amount {
  color: var(--ephone-primary);
  font-size: 30rpx;
  font-weight: 900;
  text-align: right;
}
</style>
