<script setup lang="ts">
import type { EphoneQuickAction } from '@/data/yun'

defineProps<{
  items: EphoneQuickAction[]
}>()

const emit = defineEmits<{
  tap: [item: EphoneQuickAction]
}>()

function handleTap(item: EphoneQuickAction) {
  emit('tap', item)
}
</script>

<template>
  <view class="quick-grid">
    <view
      v-for="item in items"
      :key="item.title"
      class="quick-item"
      hover-class="quick-item--active"
      :hover-stay-time="80"
      @tap="handleTap(item)"
    >
      <view
        class="quick-visual"
        :style="{ '--quick-color': item.accent, '--quick-glow': `${item.accent}66` }"
      >
        <view :class="item.icon" class="quick-icon" />
      </view>
      <view class="quick-title">
        {{ item.title }}
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 26rpx;
}

.quick-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  min-height: 164rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.038);
  transition:
    background-color 0.16s ease,
    transform 0.16s ease;
}

.quick-item--active {
  background: rgba(255, 255, 255, 0.085);
  transform: scale(0.96);
}

.quick-visual {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background:
    linear-gradient(160deg, rgba(255, 255, 255, 0.36) 0%, rgba(255, 255, 255, 0.08) 52%, rgba(255, 255, 255, 0) 100%),
    linear-gradient(135deg, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.26) 100%);
  background-color: var(--quick-color, #7c6ee0);
  box-shadow: 0 10rpx 24rpx var(--quick-glow, rgba(0, 0, 0, 0.2));
  font-size: 44rpx;
}

.quick-icon {
  color: #fff;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
}

.quick-title {
  width: 100%;
  margin-top: 12rpx;
  color: #fff;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.2;
  text-align: center;
}
</style>
