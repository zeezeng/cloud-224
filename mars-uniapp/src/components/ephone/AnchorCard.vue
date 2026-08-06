<script setup lang="ts">
import type { EphoneAnchor } from '@/data/ephone'
import AnchorAvatar from './AnchorAvatar.vue'
import StatValue from './StatValue.vue'

defineProps<{
  anchor: EphoneAnchor
  rank?: number
}>()
</script>

<template>
  <view class="anchor-card">
    <view class="anchor-left">
      <AnchorAvatar :src="anchor.avatar" :name="anchor.name" :crown="rank === 1" :show-pulse="false" size="md" />
      <view class="anchor-live" :class="{ 'anchor-live-off': !anchor.isLive }">
        <view class="anchor-live-dot" />
        {{ anchor.isLive ? '直播中' : '待开播' }}
      </view>
    </view>
    <view class="anchor-middle">
      <view class="anchor-name">
        {{ anchor.name }}
      </view>
      <view class="anchor-room">
        房间号 <text>{{ anchor.roomId }}</text>
      </view>
      <view class="anchor-tag">
        {{ anchor.group }}
      </view>
    </view>
    <view class="anchor-side">
      <view class="anchor-side-label">
        本月流水
      </view>
      <StatValue :value="anchor.monthlyFlow" size="md" />
      <button class="anchor-button">
        进入金库
      </button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.anchor-card {
  display: grid;
  grid-template-columns: 132rpx minmax(0, 1fr) 220rpx;
  gap: 16rpx;
  align-items: center;
  min-height: 176rpx;
  margin-top: 18rpx;
  padding: 20rpx 22rpx;
  border: 1rpx solid rgba(255, 71, 151, 0.42);
  border-radius: 28rpx;
  background: linear-gradient(120deg, rgba(255, 58, 151, 0.16), rgba(14, 14, 24, 0.94));
  box-shadow: inset 0 0 34rpx rgba(255, 69, 158, 0.08), 0 0 30rpx rgba(255, 58, 151, 0.12);
}

.anchor-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.anchor-live {
  display: flex;
  align-items: center;
  gap: 10rpx;
  color: var(--ephone-primary);
  font-size: 24rpx;
  font-weight: 700;
}

.anchor-live-off {
  color: rgba(255, 255, 255, 0.56);
}

.anchor-live-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 16rpx currentColor;
}

.anchor-middle {
  min-width: 0;
}

.anchor-name {
  color: #fff;
  font-size: 32rpx;
  font-weight: 900;
}

.anchor-room {
  margin-top: 10rpx;
  color: rgba(255, 255, 255, 0.68);
  font-size: 24rpx;
}

.anchor-room text {
  color: var(--ephone-primary-soft);
}

.anchor-tag {
  display: inline-flex;
  align-items: center;
  min-height: 38rpx;
  margin-top: 12rpx;
  padding: 0 18rpx;
  border-radius: 12rpx;
  background: rgba(255, 105, 180, 0.54);
  color: #08080c;
  font-size: 22rpx;
  font-weight: 800;
}

.anchor-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10rpx;
}

.anchor-side-label {
  color: rgba(255, 255, 255, 0.58);
  font-size: 24rpx;
}

.anchor-button {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 160rpx;
  height: 54rpx;
  padding: 0 22rpx;
  border: 1rpx solid rgba(255, 83, 170, 0.64);
  border-radius: 999rpx;
  background: rgba(255, 70, 158, 0.18);
  color: var(--ephone-primary-soft);
  font-size: 24rpx;
  font-weight: 800;
  line-height: 54rpx;
}

.anchor-button::after {
  border: 0;
}
</style>
