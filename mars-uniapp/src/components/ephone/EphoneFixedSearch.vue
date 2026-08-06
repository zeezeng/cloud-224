<script setup lang="ts">
import NeonSearch from './NeonSearch.vue'

const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder: string
  showButton?: boolean
  variant?: 'default' | 'enjoy' | 'vault'
  top?: string
  spacerHeight?: string
}>(), {
  modelValue: '',
  showButton: false,
  variant: 'default',
  top: 'var(--ephone-page-content-top, 228rpx)',
  spacerHeight: '122rpx',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'search': [value: string]
}>()

function handleUpdate(value: string) {
  emit('update:modelValue', value)
}

function handleSearch(value: string) {
  emit('search', value)
}
</script>

<template>
  <view class="ephone-fixed-search-space" :style="{ height: spacerHeight }" />
  <view
    class="ephone-fixed-search-mask"
    :style="{ height: `calc(${top} + ${spacerHeight})` }"
  />
  <view class="ephone-fixed-search" :style="{ top }">
    <NeonSearch
      :model-value="modelValue"
      :placeholder="placeholder"
      :show-button="showButton"
      :variant="variant"
      @update:modelValue="handleUpdate"
      @search="handleSearch"
    />
  </view>
</template>

<style scoped lang="scss">
.ephone-fixed-search-space {
  width: 100%;
}

.ephone-fixed-search-mask {
  position: fixed;
  top: 0;
  left: 50%;
  z-index: 750;
  width: 100%;
  max-width: 960rpx;
  background: var(--ephone-bg-solid, #000000);
  transform: translateX(-50%);
  pointer-events: none;
}

.ephone-fixed-search {
  position: fixed;
  left: 50%;
  z-index: 760;
  width: 100%;
  max-width: 960rpx;
  padding: 0 40rpx;
  box-sizing: border-box;
  transform: translateX(-50%);
}
</style>
