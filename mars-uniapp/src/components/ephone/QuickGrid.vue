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
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.038);
}

.quick-visual {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  color: rgba(255, 255, 255, 0.72);
  font-size: 58rpx;
  opacity: 0.86;
  text-shadow: none;
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
