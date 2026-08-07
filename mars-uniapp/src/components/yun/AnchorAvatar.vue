<script setup lang="ts">
const props = withDefaults(defineProps<{
  src: string
  name: string
  crown?: boolean
  showPulse?: boolean
  size?: 'sm' | 'md' | 'lg'
  ringColor?: string
  scale?: number
}>(), {
  crown: false,
  showPulse: true,
  size: 'md',
  ringColor: 'rgba(255, 255, 255, 0.16)',
  scale: 1.32,
})

const avatarStyle = computed<Record<string, string>>(() => ({
  '--anchor-avatar-ring': props.ringColor,
}))

const imageStyle = computed<Record<string, string>>(() => ({
  transform: `scale(${props.scale})`,
}))
</script>

<template>
  <view class="anchor-avatar" :class="`anchor-avatar-${size}`" :style="avatarStyle">
    <view class="anchor-avatar-frame">
      <image class="anchor-avatar-img" :src="src" :alt="name" mode="aspectFill" :style="imageStyle" />
    </view>
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
  --anchor-avatar-ring: rgba(255, 255, 255, 0.16);
  box-shadow: none;
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

.anchor-avatar-frame {
  box-sizing: border-box;
  display: block;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border: 3rpx solid var(--anchor-avatar-ring);
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.28);
}

.anchor-avatar-img {
  display: block;
  width: 100%;
  height: 100%;
  transform-origin: center center;
}

.anchor-crown {
  position: absolute;
  top: -32rpx;
  left: 50%;
  color: #e8c46a;
  font-size: 52rpx;
  transform: translateX(-50%);
  text-shadow: none;
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
  border: 1rpx solid rgba(255, 255, 255, 0.16);
  background: rgba(242, 182, 204, 0.2);
  color: rgba(255, 255, 255, 0.92);
  font-size: 26rpx;
}
</style>
