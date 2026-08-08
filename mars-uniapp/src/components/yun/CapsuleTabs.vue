<script setup lang="ts">
withDefaults(defineProps<{
  items: string[]
  active?: number
  compact?: boolean
}>(), {
  active: 0,
  compact: false,
})

const emit = defineEmits<{
  select: [index: number]
}>()
</script>

<template>
  <view class="capsule-tabs" :class="{ 'capsule-tabs-compact': compact }">
    <view
      v-for="(item, index) in items"
      :key="item"
      class="capsule-tab"
      :class="{ 'capsule-tab-active': index === active }"
      @tap="emit('select', index)"
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
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.055);
  color: rgba(255, 255, 255, 0.78);
  font-size: 28rpx;
  font-weight: 600;
}

.capsule-tab-active {
  border-color: rgba(242, 182, 204, 0.24);
  background: rgba(242, 182, 204, 0.1);
  box-shadow: none;
  color: rgba(255, 255, 255, 0.94);
}

.capsule-tabs-compact {
  gap: 0;
  padding: 8rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.045);
}

.capsule-tabs-compact .capsule-tab {
  flex: 1;
  min-width: 0;
  border-color: transparent;
  background: transparent;
}

.capsule-tabs-compact .capsule-tab-active {
  border-color: rgba(242, 182, 204, 0.3);
  background: rgba(242, 182, 204, 0.85);
  color: #241022;
  font-weight: 700;
}
</style>
