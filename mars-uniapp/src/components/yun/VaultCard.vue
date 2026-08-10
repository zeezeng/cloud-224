<script setup lang="ts">
import type { EphoneVaultRecord } from '@/data/yun'
import { formatIntegerMoney, formatSignedIntegerMoney } from '@/utils/yun'
import AnchorAvatar from './AnchorAvatar.vue'

const props = withDefaults(defineProps<{
  record: EphoneVaultRecord
  rank?: number
}>(), {
  rank: 1,
})

const emit = defineEmits<{
  view: [record: EphoneVaultRecord]
}>()

const rankLabel = computed(() => {
  return String(props.rank).padStart(2, '0')
})

function handleView() {
  emit('view', props.record)
}
</script>

<template>
  <view class="vault-card" hover-class="vault-card-hover" @tap="handleView">
    <view class="vault-rank">
      {{ rankLabel }}
    </view>

    <view class="vault-avatar-zone">
      <AnchorAvatar
        class="vault-card-avatar"
        :src="record.avatar"
        :name="record.name"
        :crown="rank === 1"
        :show-pulse="false"
        ring-color="rgba(233, 138, 182, 0.88)"
        size="lg"
      />
    </view>

    <view class="vault-content">
      <view class="vault-card-head">
        <view class="vault-name-row">
          <view class="vault-name">
            {{ record.name }}
          </view>
          <view class="vault-tag">
            {{ record.group }}
          </view>
        </view>

        <button class="vault-button" aria-label="查看金库详情" @tap.stop="handleView">
          <text>详情</text>
          <view class="i-carbon-chevron-right vault-button-icon" />
        </button>
      </view>

      <view class="vault-label">
        金币余额
      </view>
      <view class="vault-value">
        {{ formatIntegerMoney(record.balance) }}
      </view>

      <view class="vault-divider" />

      <view class="vault-daily">
        <view class="vault-daily-item">
          <view class="vault-stat-icon vault-stat-record">
            <view class="i-carbon-calendar" />
          </view>
          <view class="vault-stat-copy">
            <view class="vault-daily-label">
              本日记录
            </view>
            <view class="vault-daily-value">
              {{ record.dailyRecordCount }} 笔
            </view>
          </view>
        </view>
        <view class="vault-daily-item">
          <view class="vault-stat-icon vault-stat-delta">
            <view class="i-carbon-money" />
          </view>
          <view class="vault-stat-copy">
            <view class="vault-daily-label">
              本日增减
            </view>
            <view class="vault-daily-value vault-delta" :class="{ 'vault-delta-down': record.dailyDelta < 0 }">
              {{ formatSignedIntegerMoney(record.dailyDelta) }}
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.vault-card {
  position: relative;
  display: grid;
  grid-template-columns: 156rpx minmax(0, 1fr);
  gap: 18rpx;
  align-items: center;
  min-height: 226rpx;
  margin-top: 20rpx;
  padding: 26rpx 22rpx 24rpx 28rpx;
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 26rpx;
  background: rgba(255, 255, 255, 0.045);
  box-shadow: none;
}

.vault-card-hover {
  opacity: 0.86;
}

.vault-rank {
  position: absolute;
  top: 18rpx;
  left: 18rpx;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 46rpx;
  height: 34rpx;
  padding: 0 10rpx;
  border: 1rpx solid rgba(233, 138, 182, 0.38);
  border-radius: 999rpx;
  background: rgba(20, 12, 16, 0.86);
  color: var(--ephone-primary-soft);
  font-size: 20rpx;
  font-weight: 900;
  letter-spacing: 0;
  line-height: 34rpx;
}

.vault-avatar-zone {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  align-self: stretch;
  min-height: 174rpx;
}

.vault-card-avatar {
  width: 142rpx;
  height: 142rpx;
  z-index: 2;
}

.vault-card-avatar :deep(.anchor-avatar-frame) {
  border-width: 4rpx;
  box-shadow:
    0 0 0 7rpx rgba(233, 138, 182, 0.08),
    0 0 0 1rpx rgba(255, 255, 255, 0.05),
    0 10rpx 22rpx rgba(0, 0, 0, 0.32);
}

.vault-card-avatar :deep(.anchor-crown) {
  top: -24rpx;
  color: #f0c86f;
  font-size: 42rpx;
  filter: drop-shadow(0 6rpx 8rpx rgba(0, 0, 0, 0.28));
}

.vault-content {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.vault-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14rpx;
}

.vault-name-row {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  gap: 10rpx;
}

.vault-name {
  overflow: hidden;
  color: rgba(255, 255, 255, 0.97);
  font-size: 31rpx;
  font-weight: 900;
  line-height: 1.16;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  min-height: 32rpx;
  padding: 0 13rpx;
  border-radius: 999rpx;
  background: rgba(233, 138, 182, 0.16);
  color: var(--ephone-primary-soft);
  font-size: 20rpx;
  font-weight: 700;
  line-height: 32rpx;
}

.vault-label {
  margin-top: 13rpx;
  color: rgba(255, 255, 255, 0.58);
  font-size: 22rpx;
  font-weight: 800;
  line-height: 1.1;
}

.vault-value {
  margin-top: 9rpx;
  color: var(--ephone-primary-soft);
  font-size: 45rpx;
  font-weight: 900;
  letter-spacing: 0;
  line-height: 1.04;
  text-shadow: 0 7rpx 16rpx rgba(0, 0, 0, 0.32);
}

.vault-divider {
  height: 1rpx;
  margin: 20rpx 0 17rpx;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.11), rgba(233, 138, 182, 0.16), rgba(255, 255, 255, 0.04));
}

.vault-daily {
  display: grid;
  grid-template-columns: 0.68fr 1.32fr;
  gap: 0;
}

.vault-daily-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10rpx;
  min-width: 0;
  min-height: 58rpx;
}

.vault-daily-item + .vault-daily-item {
  padding-left: 18rpx;
}

.vault-daily-item + .vault-daily-item::before {
  position: absolute;
  top: 8rpx;
  bottom: 8rpx;
  left: 0;
  width: 1rpx;
  background: rgba(255, 255, 255, 0.14);
  content: '';
}

.vault-stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 46rpx;
  height: 46rpx;
  border-radius: 50%;
  font-size: 24rpx;
}

.vault-stat-record {
  background: rgba(233, 138, 182, 0.15);
  color: var(--ephone-primary-soft);
}

.vault-stat-delta {
  background: rgba(255, 128, 167, 0.15);
  color: #ff94ac;
}

.vault-stat-copy {
  min-width: 0;
}

.vault-daily-label {
  overflow: hidden;
  color: rgba(255, 255, 255, 0.58);
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.1;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-daily-value {
  margin-top: 6rpx;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.96);
  font-size: 25rpx;
  font-weight: 900;
  line-height: 1.15;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-delta-down {
  color: #7bd9a3;
}

.vault-delta {
  overflow: visible;
  color: #ff93a9;
  font-size: 24rpx;
  text-overflow: clip;
}

.vault-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 2rpx;
  flex: 0 0 auto;
  min-width: 94rpx;
  height: 48rpx;
  padding: 0 12rpx 0 18rpx;
  border-radius: 999rpx;
  border: 1rpx solid rgba(233, 138, 182, 0.56);
  background: rgba(22, 14, 18, 0.52);
  color: var(--ephone-primary-soft);
  font-size: 22rpx;
  font-weight: 800;
  line-height: 48rpx;
}

.vault-button-icon {
  font-size: 26rpx;
}

.vault-button::after {
  border: 0;
}
</style>
