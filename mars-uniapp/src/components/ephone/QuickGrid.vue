<script setup lang="ts">
import type { EphoneQuickAction } from '@/data/ephone'

defineProps<{
  items: EphoneQuickAction[]
}>()
</script>

<template>
  <view class="quick-grid">
    <view v-for="item in items" :key="item.title" class="quick-item">
      <view class="quick-visual" :style="{ '--quick-color': item.accent }">
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
  border: 1rpx solid rgba(255, 67, 151, 0.16);
  border-radius: 22rpx;
  background: linear-gradient(180deg, rgba(255, 72, 162, 0.16), rgba(255, 72, 162, 0.05));
}

.quick-visual {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  color: var(--quick-color);
  font-size: 58rpx;
  text-shadow: 0 0 24rpx var(--quick-color);
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
