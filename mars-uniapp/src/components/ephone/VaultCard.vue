<script setup lang="ts">
import type { EphoneVaultRecord } from '@/data/ephone'
import { formatMoney, formatSignedMoney } from '@/utils/ephone'
import AnchorAvatar from './AnchorAvatar.vue'

const props = defineProps<{
  record: EphoneVaultRecord
}>()

const emit = defineEmits<{
  view: [record: EphoneVaultRecord]
}>()

function handleView() {
  emit('view', props.record)
}
</script>

<template>
  <view class="vault-card" @tap="handleView">
    <view class="vault-card-main">
      <AnchorAvatar :src="record.avatar" :name="record.name" :show-pulse="false" size="md" />
      <view class="vault-info">
        <view class="vault-name">
          {{ record.name }}
          <text>{{ record.group }}</text>
        </view>
        <view class="vault-meta">
          直播间号：{{ record.roomId }}
        </view>
        <view class="vault-meta">
          更新时间：{{ record.updatedAt }}
        </view>
      </view>
      <view class="vault-balance">
        <view class="vault-label">
          金币总额
        </view>
        <view class="vault-value">
          {{ formatMoney(record.balance) }}
        </view>
      </view>
    </view>

    <view class="vault-daily">
      <view class="vault-daily-item">
        <text>本日记录</text>
        <strong>{{ record.dailyRecordCount }} 笔</strong>
      </view>
      <view class="vault-daily-item">
        <text>本日增减</text>
        <strong :class="{ 'vault-delta-down': record.dailyDelta < 0 }">
          {{ formatSignedMoney(record.dailyDelta) }}
        </strong>
      </view>
      <button class="vault-button" @tap.stop="handleView">
        查看明细
      </button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.vault-card {
  position: relative;
  margin-top: 20rpx;
  padding: 24rpx;
  overflow: hidden;
  border: 1rpx solid rgba(255, 80, 166, 0.24);
  border-radius: 30rpx;
  background:
    radial-gradient(circle at 96% 14%, rgba(255, 82, 166, 0.2), transparent 34%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.08), rgba(255, 68, 158, 0.08) 42%, rgba(10, 10, 16, 0.92));
  box-shadow: inset 0 0 32rpx rgba(255, 80, 166, 0.07), 0 12rpx 34rpx rgba(0, 0, 0, 0.22);
}

.vault-card-main {
  display: grid;
  grid-template-columns: 108rpx minmax(0, 1fr) 210rpx;
  gap: 18rpx;
  align-items: center;
}

.vault-info {
  min-width: 0;
}

.vault-name {
  display: flex;
  align-items: center;
  gap: 14rpx;
  color: #fff;
  font-size: 30rpx;
  font-weight: 900;
}

.vault-name text {
  padding: 2rpx 12rpx;
  border: 1rpx solid rgba(255, 91, 174, 0.62);
  border-radius: 10rpx;
  color: var(--ephone-primary);
  font-size: 22rpx;
}

.vault-meta {
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.62);
  font-size: 23rpx;
}

.vault-balance {
  text-align: right;
}

.vault-label {
  color: rgba(255, 255, 255, 0.66);
  font-size: 22rpx;
}

.vault-value {
  margin-top: 10rpx;
  color: #fff;
  font-size: 36rpx;
  font-weight: 900;
  text-shadow: 0 0 18rpx rgba(255, 78, 160, 0.92);
}

.vault-daily {
  display: grid;
  grid-template-columns: 1fr 1fr 158rpx;
  gap: 14rpx;
  align-items: center;
  margin-top: 22rpx;
}

.vault-daily-item {
  min-height: 76rpx;
  padding: 12rpx 16rpx;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.06);
}

.vault-daily-item text {
  display: block;
  color: rgba(255, 255, 255, 0.5);
  font-size: 21rpx;
}

.vault-daily-item strong {
  display: block;
  margin-top: 8rpx;
  color: var(--ephone-primary);
  font-size: 28rpx;
  font-weight: 900;
}

.vault-daily-item .vault-delta-down {
  color: #36d889;
}

.vault-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 62rpx;
  padding: 0 26rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #ff69b4, #f83a94);
  color: #fff;
  font-size: 24rpx;
  font-weight: 800;
  line-height: 62rpx;
}

.vault-button::after {
  border: 0;
}
</style>
