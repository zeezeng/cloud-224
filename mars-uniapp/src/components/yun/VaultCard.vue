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
  if (props.rank <= 3) {
    return String(props.rank)
  }
  return String(props.rank).padStart(2, '0')
})

const balanceText = computed(() => formatIntegerMoney(props.record.balance))

const avatarRing = computed(() => 'rgba(255, 255, 255, 0.14)')

const isMedalRank = computed(() => props.rank <= 3)

const cardClass = computed(() => ({
  'is-top-one': props.rank === 1,
  'is-top-two': props.rank === 2,
  'is-top-three': props.rank === 3,
}))

function handleView() {
  emit('view', props.record)
}
</script>

<template>
  <view class="vault-card" :class="cardClass" hover-class="vault-card-hover" @tap="handleView">
    <view class="vault-rank" :class="{ 'is-medal': isMedalRank, 'is-plain': !isMedalRank }">
      <view v-if="isMedalRank" class="vault-medal-ribbon" />
      <view v-if="isMedalRank" class="vault-medal-core">
        <text class="vault-badge-num">{{ rankLabel }}</text>
      </view>
      <text v-else class="vault-badge-num">{{ rankLabel }}</text>
    </view>

    <AnchorAvatar
      class="vault-avatar"
      :src="record.avatar"
      :name="record.name"
      :show-pulse="false"
      :ring-color="avatarRing"
      :scale="1.22"
      size="sm"
      style="width: 78rpx; height: 78rpx;"
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
      <view class="i-carbon-chevron-right" />
    </view>
  </view>
</template>

<style scoped lang="scss">
.vault-card {
  position: relative;
  display: grid;
  grid-template-columns: 64rpx 78rpx minmax(0, 1fr) minmax(128rpx, 174rpx) 24rpx;
  align-items: center;
  gap: 16rpx;
  min-height: 118rpx;
  margin-top: 14rpx;
  padding: 18rpx 24rpx;
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.045);
  border-radius: 26rpx;
  background:
    radial-gradient(circle at 88% 50%, rgba(118, 82, 126, 0.08) 0, rgba(118, 82, 126, 0) 42%),
    linear-gradient(135deg, rgba(23, 26, 36, 0.96), rgba(12, 15, 22, 0.98));
  box-sizing: border-box;
  box-shadow:
    inset 0 1rpx 0 rgba(255, 255, 255, 0.035),
    0 12rpx 32rpx rgba(0, 0, 0, 0.16);
}

.vault-card.is-top-one {
  border-color: rgba(242, 122, 188, 0.24);
  background:
    radial-gradient(circle at 86% 50%, rgba(242, 122, 188, 0.18) 0, rgba(242, 122, 188, 0) 45%),
    radial-gradient(circle at 12% 28%, rgba(255, 255, 255, 0.055) 0, rgba(255, 255, 255, 0) 30%),
    linear-gradient(135deg, rgba(23, 25, 35, 0.98), rgba(13, 15, 22, 0.98));
  box-shadow:
    inset 0 1rpx 0 rgba(255, 255, 255, 0.07),
    0 0 0 1rpx rgba(242, 122, 188, 0.05),
    0 18rpx 46rpx rgba(0, 0, 0, 0.24),
    0 0 36rpx rgba(242, 122, 188, 0.12);
}

.vault-card-hover {
  opacity: 0.9;
}

.vault-rank {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 78rpx;
  box-sizing: border-box;
}

.vault-badge-num {
  position: relative;
  z-index: 2;
  font-size: 25rpx;
  font-weight: 900;
  letter-spacing: 0;
  line-height: 1;
}

.vault-medal-core {
  position: absolute;
  top: 2rpx;
  left: 50%;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 54rpx;
  height: 54rpx;
  border-radius: 50%;
  transform: translateX(-50%);
  box-shadow:
    inset 0 4rpx 8rpx rgba(255, 255, 255, 0.38),
    inset 0 -8rpx 14rpx rgba(0, 0, 0, 0.1),
    0 8rpx 16rpx rgba(0, 0, 0, 0.18);
}

.vault-medal-ribbon {
  position: absolute;
  bottom: 4rpx;
  left: 50%;
  width: 44rpx;
  height: 34rpx;
  border-radius: 0 0 12rpx 12rpx;
  transform: translateX(-50%);
}

.vault-medal-ribbon::before,
.vault-medal-ribbon::after {
  position: absolute;
  bottom: -4rpx;
  width: 18rpx;
  height: 22rpx;
  border-radius: 0 0 8rpx 8rpx;
  background: inherit;
  content: '';
}

.vault-medal-ribbon::before {
  left: 5rpx;
  transform: rotate(12deg);
}

.vault-medal-ribbon::after {
  right: 5rpx;
  transform: rotate(-12deg);
}

.vault-card.is-top-one .vault-medal-core,
.vault-card.is-top-one .vault-medal-ribbon {
  background: linear-gradient(180deg, #ffedc4 0%, #f4bf71 100%);
  color: #674320;
}

.vault-card.is-top-two .vault-medal-core,
.vault-card.is-top-two .vault-medal-ribbon {
  background: linear-gradient(180deg, #e3efff 0%, #a7c8ed 100%);
  color: #37516e;
}

.vault-card.is-top-three .vault-medal-core,
.vault-card.is-top-three .vault-medal-ribbon {
  background: linear-gradient(180deg, #ffd7bd 0%, #f0a77c 100%);
  color: #69402d;
}

.vault-rank.is-plain {
  height: 58rpx;
  color: rgba(224, 230, 242, 0.58);
}

.vault-avatar {
  flex: 0 0 auto;
}

.vault-avatar :deep(.anchor-avatar-sm) {
  width: 78rpx;
  height: 78rpx;
}

.vault-avatar :deep(.anchor-avatar-frame) {
  border-width: 2rpx;
  box-shadow:
    0 0 0 1rpx rgba(255, 255, 255, 0.05),
    0 8rpx 18rpx rgba(0, 0, 0, 0.3);
}

.vault-main {
  min-width: 0;
}

.vault-name-line {
  display: flex;
  align-items: center;
  gap: 12rpx;
  min-width: 0;
}

.vault-name {
  flex: 0 1 auto;
  max-width: 68%;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.96);
  font-size: 31rpx;
  font-weight: 900;
  line-height: 1.18;
  text-shadow: 0 6rpx 16rpx rgba(255, 255, 255, 0.08);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  max-width: 32%;
  height: 32rpx;
  padding: 0 12rpx;
  overflow: hidden;
  border-radius: 999rpx;
  background: rgba(174, 120, 201, 0.2);
  color: rgba(238, 202, 255, 0.92);
  font-size: 20rpx;
  font-weight: 800;
  line-height: 32rpx;
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
  font-size: 33rpx;
  font-weight: 900;
  font-variant-numeric: tabular-nums;
  line-height: 1.05;
  text-align: right;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vault-card.is-top-one .vault-balance-value {
  color: #f58bc8;
  text-shadow: 0 0 20rpx rgba(245, 139, 200, 0.22);
}

.vault-card.is-top-two .vault-balance-value {
  color: #c9d7fa;
}

.vault-card.is-top-three .vault-balance-value {
  color: #ffbb9d;
}

.vault-go {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24rpx;
  height: 44rpx;
  border-radius: 50%;
  color: rgba(224, 230, 242, 0.34);
  font-size: 28rpx;
}
</style>
