<script setup lang="ts">
import { formatCompactNumber, formatMoney } from '@/utils/ephone'

const props = withDefaults(defineProps<{
  value: number
  label?: string
  money?: boolean
  size?: 'sm' | 'md' | 'lg'
}>(), {
  label: '',
  money: false,
  size: 'md',
})

const displayValue = computed(() => props.money ? formatMoney(props.value) : formatCompactNumber(props.value))
</script>

<template>
  <view class="stat-value" :class="`stat-value-${size}`">
    <view class="stat-main">
      <view v-if="!money" class="i-carbon-fire stat-icon" />
      <text>{{ displayValue }}</text>
    </view>
    <view v-if="label" class="stat-label">
      {{ label }}
    </view>
  </view>
</template>

<style scoped lang="scss">
.stat-value {
  text-align: right;
}

.stat-main {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
  color: var(--ephone-primary);
  font-weight: 900;
  line-height: 1;
  text-shadow: 0 0 18rpx rgba(255, 63, 151, 0.44);
}

.stat-icon {
  font-size: 32rpx;
}

.stat-label {
  margin-top: 10rpx;
  color: rgba(255, 255, 255, 0.62);
  font-size: 22rpx;
}

.stat-value-sm .stat-main {
  font-size: 26rpx;
}

.stat-value-md .stat-main {
  font-size: 34rpx;
}

.stat-value-lg .stat-main {
  font-size: 52rpx;
}
</style>
