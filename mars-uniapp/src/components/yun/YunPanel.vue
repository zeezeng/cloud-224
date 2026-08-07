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
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.045);
  box-shadow: none;
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
