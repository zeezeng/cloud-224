<script setup lang="ts">
defineProps<{
  title: string
  subtitle: string
  heroImage?: string
  heroAlt?: string
}>()

const navbarStyle = ref<Record<string, string>>({})

onMounted(() => {
  // #ifdef MP-WEIXIN
  try {
    const menu = uni.getMenuButtonBoundingClientRect()
    const system = uni.getSystemInfoSync()
    if (menu?.top && menu.height && system.windowWidth) {
      navbarStyle.value = {
        '--ephone-nav-top': `${menu.top}px`,
        '--ephone-hero-top': `${Math.ceil(menu.top + menu.height + 16)}px`,
        '--ephone-nav-right-space': `${Math.ceil(system.windowWidth - menu.left + 12)}px`,
      }
    }
  }
  catch {
    navbarStyle.value = {}
  }
  // #endif
})
</script>

<template>
  <view class="ephone-page">
    <view class="ephone-glow ephone-glow-left" />
    <view class="ephone-glow ephone-glow-right" />
    <view class="ephone-content">
      <view class="ephone-heading" :class="{ 'ephone-heading-with-image': heroImage }" :style="navbarStyle">
        <view class="ephone-heading-copy">
          <view class="ephone-title">
            {{ title }}
          </view>
          <view class="ephone-subtitle">
            {{ subtitle }}
          </view>
        </view>
        <image
          v-if="heroImage"
          class="ephone-heading-image"
          :src="heroImage"
          :alt="heroAlt || title"
          mode="aspectFit"
        />
      </view>

      <slot />
    </view>
  </view>
</template>

<style scoped lang="scss">
.ephone-page {
  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  background: var(--ephone-bg-scene);
  color: var(--ephone-text);
}

.ephone-content {
  position: relative;
  z-index: 1;
  box-sizing: border-box;
  max-width: 960rpx;
  min-height: 100vh;
  margin: 0 auto;
  padding: 0 40rpx 180rpx;
}

.ephone-glow {
  position: fixed;
  pointer-events: none;
  filter: blur(28rpx);
  opacity: 0.72;
}

.ephone-glow-left {
  top: 680rpx;
  left: -160rpx;
  width: 260rpx;
  height: 520rpx;
  background: rgba(255, 76, 166, 0.08);
}

.ephone-glow-right {
  top: 120rpx;
  right: -240rpx;
  width: 440rpx;
  height: 440rpx;
  background: rgba(255, 76, 166, 0.1);
}

.ephone-heading {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  min-height: 204rpx;
  padding-top: var(--ephone-nav-top, calc(env(safe-area-inset-top) + 64rpx));
  padding-right: var(--ephone-nav-right-space, 0);
  padding-bottom: 28rpx;
}

.ephone-heading-copy {
  flex: 1 1 auto;
  min-width: 0;
}

.ephone-heading-with-image {
  gap: 22rpx;
  padding-top: var(--ephone-hero-top, calc(env(safe-area-inset-top) + 104rpx));
  padding-right: 0;
}

.ephone-heading-with-image .ephone-heading-copy {
  flex: 0 1 300rpx;
}

.ephone-title {
  color: #fff;
  font-size: 58rpx;
  font-weight: 900;
  line-height: 1.08;
  text-shadow: 0 0 20rpx rgba(255, 61, 153, 0.62);
}

.ephone-title::first-letter {
  color: var(--ephone-primary-soft);
}

.ephone-subtitle {
  margin-top: 14rpx;
  color: var(--ephone-muted);
  font-size: 28rpx;
  line-height: 1.35;
}

.ephone-heading-with-image .ephone-title {
  white-space: nowrap;
}

.ephone-heading-with-image .ephone-subtitle {
  font-size: 26rpx;
}

.ephone-heading-image {
  flex: 0 0 auto;
  width: 360rpx;
  height: 220rpx;
  margin-right: -28rpx;
}

.ephone-heading-with-image .ephone-heading-image {
  width: 330rpx;
  height: 204rpx;
  margin-right: 0;
}
</style>
