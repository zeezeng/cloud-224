<script setup lang="ts">
import SectionHeader from './SectionHeader.vue'

defineProps<{
  title?: string
  icon?: string
  action?: string
}>()
</script>

<template>
  <view class="ephone-panel">
    <SectionHeader v-if="title" :title="title" :icon="icon" :action="action" />
    <view class="ephone-panel-body">
      <slot />
    </view>
  </view>
</template>

<style scoped lang="scss">
.ephone-panel {
  margin-top: 28rpx;
  padding: 22rpx 24rpx 18rpx;
  border: 1rpx solid rgba(255, 79, 163, 0.26);
  border-radius: 28rpx;
  background: linear-gradient(150deg, rgba(255, 64, 154, 0.12), rgba(12, 14, 20, 0.92));
  box-shadow: inset 0 0 32rpx rgba(255, 68, 158, 0.09), 0 0 26rpx rgba(255, 68, 158, 0.12);
}

.ephone-panel-body :deep(.capsule-tabs) {
  margin-top: 0;
}

.ephone-panel-body :deep(.capsule-tabs + .capsule-tabs) {
  margin-top: 16rpx;
}

.ephone-panel-body :deep(.ranking-list),
.ephone-panel-body :deep(.podium-board),
.ephone-panel-body :deep(.anchor-card),
.ephone-panel-body :deep(.vault-card) {
  margin-top: 18rpx;
}
</style>
