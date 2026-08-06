<script setup lang="ts">
type FallbackType = 'navigateTo' | 'redirectTo' | 'switchTab'

const props = withDefaults(defineProps<{
  title?: string
  fallbackUrl?: string
  fallbackType?: FallbackType
}>(), {
  title: '',
  fallbackUrl: '',
  fallbackType: 'navigateTo',
})

const emit = defineEmits<{
  layout: [style: Record<string, string>]
}>()

const navStyle = ref<Record<string, string>>({})

function syncNavLayout() {
  // #ifdef MP-WEIXIN
  try {
    const menu = uni.getMenuButtonBoundingClientRect()
    const system = uni.getSystemInfoSync()
    if (menu?.top && menu.height && system.windowWidth) {
      navStyle.value = {
        '--ephone-transparent-nav-top': `${menu.top}px`,
        '--ephone-transparent-nav-height': `${menu.height}px`,
        '--ephone-transparent-nav-right-space': `${Math.ceil(system.windowWidth - menu.left + 12)}px`,
      }
      emit('layout', navStyle.value)
      return
    }
  }
  catch {
    navStyle.value = {}
  }
  // #endif

  navStyle.value = {}
  emit('layout', {})
}

function goFallback() {
  if (!props.fallbackUrl) {
    return
  }

  if (props.fallbackType === 'switchTab') {
    uni.switchTab({ url: props.fallbackUrl })
    return
  }

  if (props.fallbackType === 'redirectTo') {
    uni.redirectTo({ url: props.fallbackUrl })
    return
  }

  uni.navigateTo({ url: props.fallbackUrl })
}

function handleBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
    return
  }

  goFallback()
}

onMounted(syncNavLayout)
</script>

<template>
  <view class="ephone-transparent-nav" :style="navStyle">
    <button class="ephone-nav-back" hover-class="ephone-nav-back-hover" @tap="handleBack">
      <view class="ephone-nav-back-inner">
        <view class="ephone-nav-back-icon" />
      </view>
    </button>
    <view v-if="title" class="ephone-nav-title">
      {{ title }}
    </view>
    <slot name="right" />
  </view>
</template>

<style scoped lang="scss">
.ephone-transparent-nav {
  position: fixed;
  top: var(--ephone-transparent-nav-top, env(safe-area-inset-top));
  left: 0;
  right: 0;
  z-index: 99;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  height: var(--ephone-transparent-nav-height, 88rpx);
  padding: 0 var(--ephone-transparent-nav-right-space, 40rpx) 0 32rpx;
  background: transparent;
  pointer-events: none;
}

.ephone-nav-back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 74rpx;
  height: 74rpx;
  padding: 0;
  border: 1rpx solid rgba(255, 255, 255, 0.18);
  border-radius: 50%;
  background: rgba(10, 9, 14, 0.34);
  box-shadow: none;
  pointer-events: auto;
}

.ephone-nav-back::after {
  border: 0;
}

.ephone-nav-back-hover {
  opacity: 0.82;
  transform: scale(0.96);
}

.ephone-nav-back-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 54rpx;
  height: 54rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  box-shadow: none;
}

.ephone-nav-back-icon {
  width: 18rpx;
  height: 18rpx;
  margin-left: 6rpx;
  border-bottom: 4rpx solid rgba(255, 255, 255, 0.94);
  border-left: 4rpx solid rgba(255, 255, 255, 0.94);
  border-radius: 2rpx;
  transform: rotate(45deg);
}

.ephone-nav-title {
  flex: 1;
  min-width: 0;
  margin-left: 18rpx;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.94);
  font-size: 30rpx;
  font-weight: 850;
  letter-spacing: 0;
  text-overflow: ellipsis;
  text-shadow: none;
  white-space: nowrap;
  pointer-events: none;
}
</style>
