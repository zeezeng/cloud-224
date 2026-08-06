<script setup lang="ts">
defineProps<{
  title: string
  icon?: string
  action?: string
}>()
</script>

<template>
  <view class="section-header">
    <view class="section-title">
      <view v-if="icon" :class="icon" class="section-icon" />
      <text>{{ title }}</text>
    </view>
    <view v-if="action" class="section-action">
      {{ action }}
    </view>
  </view>
</template>

<style scoped lang="scss">
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22rpx;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  color: #fff;
  font-size: 32rpx;
  font-weight: 800;
}

.section-icon {
  color: var(--ephone-primary);
  font-size: 34rpx;
}

.section-action {
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.42);
  font-size: 24rpx;
}
</style>
