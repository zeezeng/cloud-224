<script setup lang="ts">
type FallbackType = 'navigateTo' | 'redirectTo' | 'switchTab'

interface WindowMetrics {
  statusBarHeight?: number
  windowWidth?: number
  screenWidth?: number
}

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

function getWindowMetrics(): WindowMetrics {
  const uniApi = uni as unknown as {
    getWindowInfo?: () => WindowMetrics
    getSystemInfoSync: () => WindowMetrics
  }

  if (typeof uniApi.getWindowInfo === 'function') {
    return uniApi.getWindowInfo()
  }

  return uniApi.getSystemInfoSync()
}

function syncNavLayout() {
  // #ifdef MP-WEIXIN
  try {
    const menu = uni.getMenuButtonBoundingClientRect()
    const windowMetrics = getWindowMetrics()
    const statusBarHeight = Math.max(0, Number(windowMetrics.statusBarHeight || 0))
    const windowWidth = Number(windowMetrics.windowWidth || windowMetrics.screenWidth || 0)
    const menuTop = Number(menu?.top || 0)
    const menuHeight = Number(menu?.height || 0)
    const menuLeft = Number(menu?.left || 0)

    if (menuTop && menuHeight && windowWidth) {
      const menuGap = Math.max(0, menuTop - statusBarHeight)
      const navBarHeight = menuGap * 2 + menuHeight
      const totalHeight = statusBarHeight + navBarHeight
      const rightSpace = menuLeft > 0 ? Math.ceil(windowWidth - menuLeft + 12) : 40

      navStyle.value = {
        '--ephone-transparent-nav-status-height': `${statusBarHeight}px`,
        '--ephone-transparent-nav-menu-top': `${menuTop}px`,
        '--ephone-transparent-nav-menu-height': `${menuHeight}px`,
        '--ephone-transparent-nav-bar-height': `${navBarHeight}px`,
        '--ephone-transparent-nav-total-height': `${totalHeight}px`,
        '--ephone-transparent-nav-content-top': `${totalHeight}px`,
        '--ephone-transparent-nav-right-space': `${rightSpace}px`,
        '--ephone-transparent-nav-top': '0px',
        '--ephone-transparent-nav-height': `${totalHeight}px`,
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
    <view class="ephone-nav-row">
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
  </view>
</template>

<style scoped lang="scss">
.ephone-transparent-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 99;
  box-sizing: border-box;
  height: var(--ephone-transparent-nav-height, calc(env(safe-area-inset-top) + 88rpx));
  background: transparent;
  pointer-events: none;
}

.ephone-nav-row {
  position: absolute;
  top: var(--ephone-transparent-nav-menu-top, env(safe-area-inset-top));
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  height: var(--ephone-transparent-nav-menu-height, 88rpx);
  padding: 0 var(--ephone-transparent-nav-right-space, 40rpx) 0 32rpx;
  pointer-events: none;
}

.ephone-nav-back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: var(--ephone-transparent-nav-menu-height, 74rpx);
  height: var(--ephone-transparent-nav-menu-height, 74rpx);
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
  width: 100%;
  height: 100%;
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
