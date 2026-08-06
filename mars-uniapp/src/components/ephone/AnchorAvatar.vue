<script setup lang="ts">
withDefaults(defineProps<{
  src: string
  name: string
  crown?: boolean
  showPulse?: boolean
  size?: 'sm' | 'md' | 'lg'
}>(), {
  crown: false,
  showPulse: true,
  size: 'md',
})
</script>

<template>
  <view class="anchor-avatar" :class="`anchor-avatar-${size}`">
    <image class="anchor-avatar-img" :src="src" :alt="name" mode="aspectFill" />
    <view v-if="crown" class="anchor-crown">
      <view class="i-carbon-trophy-filled" />
    </view>
    <view v-if="showPulse" class="anchor-pulse">
      <view class="i-carbon-microphone-filled" />
    </view>
  </view>
</template>

<style scoped lang="scss">
.anchor-avatar {
  position: relative;
  flex: 0 0 auto;
  border-radius: 50%;
  box-shadow: 0 0 28rpx rgba(255, 67, 151, 0.42);
}

.anchor-avatar-sm {
  width: 76rpx;
  height: 76rpx;
}

.anchor-avatar-md {
  width: 126rpx;
  height: 126rpx;
}

.anchor-avatar-lg {
  width: 144rpx;
  height: 144rpx;
}

.anchor-avatar-img {
  box-sizing: border-box;
  display: block;
  width: 100%;
  height: 100%;
  border: 3rpx solid rgba(255, 101, 178, 0.62);
  border-radius: 50%;
}

.anchor-crown {
  position: absolute;
  top: -32rpx;
  left: 50%;
  color: #ffd35c;
  font-size: 52rpx;
  transform: translateX(-50%);
  text-shadow: 0 0 16rpx rgba(255, 211, 92, 0.7);
}

.anchor-pulse {
  position: absolute;
  right: -8rpx;
  bottom: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff79be, #f83d98);
  color: #fff;
  font-size: 26rpx;
}
</style>
