<script setup lang="ts">
withDefaults(defineProps<{
  placeholder: string
  showButton?: boolean
  variant?: 'default' | 'enjoy' | 'vault'
}>(), {
  showButton: false,
  variant: 'default',
})
</script>

<template>
  <view class="neon-search" :class="`neon-search-${variant}`">
    <view class="i-carbon-search neon-search-icon" />
    <input class="neon-search-input" :placeholder="placeholder" confirm-type="search">
    <button v-if="showButton" class="neon-search-button">
      搜索
    </button>
  </view>
</template>

<style scoped lang="scss">
.neon-search {
  display: flex;
  align-items: center;
  box-sizing: border-box;
  min-height: 76rpx;
  margin-top: 22rpx;
  padding: 0 10rpx 0 26rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.18);
  border-radius: 999rpx;
  background: linear-gradient(100deg, rgba(255, 255, 255, 0.1), rgba(255, 78, 159, 0.14));
  box-shadow: inset 0 0 20rpx rgba(255, 255, 255, 0.04), 0 0 30rpx rgba(255, 55, 151, 0.16);
}

.neon-search-icon {
  flex: 0 0 auto;
  color: rgba(255, 255, 255, 0.56);
  font-size: 38rpx;
}

.neon-search-input {
  flex: 1;
  min-width: 0;
  height: 74rpx;
  padding: 0 20rpx;
  color: #fff;
  font-size: 28rpx;
}

.neon-search-button {
  min-width: 116rpx;
  height: 60rpx;
  padding: 0 28rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #ff69b4, #f92f8f);
  box-shadow: 0 0 22rpx rgba(255, 66, 156, 0.52);
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
  line-height: 60rpx;
}

.neon-search-button::after {
  border: 0;
}

.neon-search-enjoy,
.neon-search-vault {
  min-height: 88rpx;
  margin-top: 12rpx;
  padding: 0 12rpx 0 28rpx;
  border-color: rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.06);
  box-shadow: none;
}

.neon-search-enjoy .neon-search-icon,
.neon-search-vault .neon-search-icon {
  color: var(--ephone-primary-soft);
  font-size: 40rpx;
}

.neon-search-enjoy .neon-search-input,
.neon-search-vault .neon-search-input {
  height: 86rpx;
  font-size: 27rpx;
}

.neon-search-enjoy .neon-search-button,
.neon-search-vault .neon-search-button {
  min-width: 128rpx;
  height: 66rpx;
  border: 1rpx solid rgba(255, 91, 174, 0.36);
  background: rgba(255, 79, 163, 0.18);
  font-size: 26rpx;
  line-height: 66rpx;
}

.neon-search-vault {
  border-color: rgba(255, 112, 181, 0.34);
  background:
    radial-gradient(circle at 90% 50%, rgba(255, 79, 163, 0.14), transparent 34%),
    rgba(3, 3, 6, 0.48);
}
</style>
