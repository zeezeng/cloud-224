<script setup lang="ts">
import type { EphoneVaultRecord } from '@/data/yun'
import { formatIntegerMoney } from '@/utils/yun'
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

const balanceText = computed(() => formatIntegerMoney(props.record.balance))

const avatarRing = computed(() => 'rgba(255, 255, 255, 0.14)')

function handleView() {
  emit('view', props.record)
}
</script>

<template>
  <view class="vault-card" hover-class="vault-card-hover" @tap="handleView">
    <view class="vault-rank is-plain">
      <text class="vault-badge-num">{{ rankLabel }}</text>
    </view>

    <AnchorAvatar
      class="vault-avatar"
      :src="record.avatar"
      :name="record.name"
      :show-pulse="false"
      :ring-color="avatarRing"
      :scale="1.22"
      size="sm"
      style="width: 88rpx; height: 88rpx;"
    />

    <view class="vault-main">
      <view class="vault-name-line">
        <text class="vault-name">{{ record.name }}</text>
        <text v-if="record.group" class="vault-tag">{{ record.group }}</text>
      </view>
    </view>

    <view class="vault-balance">
      <text class="vault-balance-value">{{ balanceText }}</text>
    </view>

    <view class="vault-go">
      <view class="i-carbon-arrow-right" />
    </view>
  </view>
</template>

<style scoped lang="scss">
.vault-card {
  position: relative;
  display: grid;
  grid-template-columns: 58rpx 88rpx minmax(0, 1fr) minmax(126rpx, 176rpx) 30rpx;
  align-items: center;
  gap: 14rpx;
  min-height: 132rpx;
  margin-top: 14rpx;
  padding: 20rpx 18rpx;
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.045);
  box-sizing: border-box;
}

.vault-card::before {
  position: absolute;
  top: 24rpx;
  bottom: 24rpx;
  left: 0;
  width: 4rpx;
  border-radius: 999rpx;
  background: var(--ephone-primary-soft);
  content: '';
  opacity: 0;
}

.vault-card-hover {
  background: rgba(255, 255, 255, 0.075);
  opacity: 0.94;
}

.vault-rank {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58rpx;
  height: 58rpx;
  border-radius: 18rpx;
  box-sizing: border-box;
}

.vault-badge-num {
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 0;
  line-height: 1;
}

.vault-rank.is-plain {
  border: 1rpx solid rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.035);
  color: rgba(255, 255, 255, 0.62);
}

.vault-avatar {
  flex: 0 0 auto;
}

.vault-avatar :deep(.anchor-avatar-sm) {
  width: 88rpx;
  height: 88rpx;
}

.vault-avatar :deep(.anchor-avatar-frame) {
  border-width: 3rpx;
  box-shadow:
    0 0 0 1rpx rgba(255, 255, 255, 0.05),
    0 8rpx 18rpx rgba(0, 0, 0, 0.28);
}

.vault-main {
  min-width: 0;
}

.vault-name-line {
  display: flex;
  align-items: center;
  gap: 10rpx;
  min-width: 0;
}

.vault-name {
  flex: 0 1 auto;
  max-width: 64%;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.96);
  font-size: 29rpx;
  font-weight: 850;
  line-height: 1.18;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  max-width: 36%;
  height: 30rpx;
  padding: 0 11rpx;
  overflow: hidden;
  border-radius: 999rpx;
  background: rgba(242, 182, 204, 0.1);
  color: var(--ephone-primary-soft);
  font-size: 18rpx;
  font-weight: 700;
  line-height: 30rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-balance {
  display: flex;
  justify-content: flex-end;
  min-width: 0;
}

.vault-balance-value {
  display: block;
  max-width: 100%;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.96);
  font-size: 34rpx;
  font-weight: 900;
  font-variant-numeric: tabular-nums;
  line-height: 1.05;
  text-align: right;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-go {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30rpx;
  height: 44rpx;
  border-radius: 50%;
  color: rgba(255, 255, 255, 0.34);
  font-size: 24rpx;
}
</style>
