<script setup lang="ts">
const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder: string
  showButton?: boolean
  variant?: 'default' | 'enjoy' | 'vault' | 'bubble'
}>(), {
  modelValue: '',
  showButton: false,
  variant: 'default',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'search': [value: string]
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
.neon-search-vault,
.neon-search-bubble {
  min-height: 88rpx;
  margin-top: 12rpx;
  padding: 0 12rpx 0 28rpx;
  border-color: rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.06);
  box-shadow: none;
}

.neon-search-enjoy .neon-search-icon,
.neon-search-vault .neon-search-icon,
.neon-search-bubble .neon-search-icon {
  color: rgba(255, 255, 255, 0.62);
  font-size: 40rpx;
}

.neon-search-enjoy .neon-search-input,
.neon-search-vault .neon-search-input,
.neon-search-bubble .neon-search-input {
  height: 86rpx;
  font-size: 27rpx;
}

.neon-search-enjoy .neon-search-button,
.neon-search-vault .neon-search-button,
.neon-search-bubble .neon-search-button {
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

.neon-search-bubble {
  border-color: rgba(242, 182, 204, 0.18);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.105), rgba(255, 255, 255, 0.045)),
    rgba(12, 9, 15, 0.9);
  box-shadow:
    0 18rpx 42rpx rgba(0, 0, 0, 0.28),
    0 0 0 1rpx rgba(255, 255, 255, 0.045) inset;
  backdrop-filter: blur(18rpx);
  -webkit-backdrop-filter: blur(18rpx);
}

.neon-search-bubble .neon-search-icon {
  color: var(--ephone-primary-soft);
}

.neon-search-bubble .neon-search-button {
  border-color: rgba(255, 255, 255, 0.18);
  background: linear-gradient(135deg, var(--ephone-primary), var(--ephone-primary-soft));
  color: #241019;
  font-weight: 850;
}
</style>
