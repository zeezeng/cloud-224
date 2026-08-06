<script setup lang="ts">
withDefaults(defineProps<{
  items: string[]
  active?: number
  compact?: boolean
}>(), {
  active: 0,
  compact: false,
})
</script>

<template>
  <view class="capsule-tabs" :class="{ 'capsule-tabs-compact': compact }">
    <view
      v-for="(item, index) in items"
      :key="item"
      class="capsule-tab"
      :class="{ 'capsule-tab-active': index === active }"
    >
      {{ item }}
    </view>
  </view>
</template>

<style scoped lang="scss">
.capsule-tabs {
  display: flex;
  gap: 20rpx;
  margin-top: 28rpx;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  white-space: nowrap;
}

.capsule-tabs::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}

.capsule-tab {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 140rpx;
  min-height: 64rpx;
  padding: 0 28rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 999rpx;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.12), rgba(255, 255, 255, 0.06));
  color: rgba(255, 255, 255, 0.78);
  font-size: 28rpx;
  font-weight: 600;
}

.capsule-tab-active {
  border-color: rgba(255, 91, 174, 0.68);
  background: linear-gradient(135deg, #ff66b7, #f33491);
  box-shadow: 0 10rpx 28rpx rgba(255, 57, 151, 0.34);
  color: #fff;
}

.capsule-tabs-compact {
  gap: 0;
  padding: 8rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.08);
}

.capsule-tabs-compact .capsule-tab {
  flex: 1;
  min-width: 0;
  border-color: transparent;
  background: transparent;
}
</style>
