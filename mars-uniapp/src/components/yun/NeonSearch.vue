<script setup lang="ts">
const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder: string
  showButton?: boolean
  variant?: 'default' | 'enjoy' | 'vault'
}>(), {
  modelValue: '',
  showButton: false,
  variant: 'default',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  search: [value: string]
}>()

function handleInput(event: { detail?: { value?: string } }) {
  emit('update:modelValue', event.detail?.value || '')
}

function handleSearch() {
  emit('search', props.modelValue.trim())
}
</script>

<template>
  <view class="neon-search" :class="`neon-search-${variant}`">
    <view class="i-carbon-search neon-search-icon" />
    <input
      class="neon-search-input"
      :value="modelValue"
      :placeholder="placeholder"
      confirm-type="search"
      @input="handleInput"
      @confirm="handleSearch"
    >
    <button v-if="showButton" class="neon-search-button" @click="handleSearch">
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
  background: rgba(255, 255, 255, 0.06);
  box-shadow: none;
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
  border: 1rpx solid rgba(242, 182, 204, 0.2);
  background: rgba(242, 182, 204, 0.1);
  box-shadow: none;
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
  color: rgba(255, 255, 255, 0.62);
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
  border: 1rpx solid rgba(242, 182, 204, 0.22);
  background: rgba(242, 182, 204, 0.1);
  font-size: 26rpx;
  line-height: 66rpx;
}

.neon-search-vault {
  border-color: rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.06);
}
</style>
