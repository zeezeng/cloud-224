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
    <view class="ephone-content" :class="{ 'ephone-content-with-image': heroImage }">
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
  background-size: 100vw 100vh;
  background-position: 0 0;
  background-attachment: fixed;
  color: var(--ephone-text);
}

.ephone-content {
  position: relative;
  z-index: 1;
  box-sizing: border-box;
  max-width: 960rpx;
  min-height: 100vh;
  margin: 0 auto;
  --ephone-page-content-top: 228rpx;
  padding: var(--ephone-page-content-top) 40rpx 180rpx;
}

.ephone-content-with-image {
  --ephone-page-content-top: 372rpx;
  padding-top: 372rpx;
}

.ephone-glow {
  position: fixed;
  pointer-events: none;
  filter: blur(34rpx);
  opacity: 0.5;
}

.ephone-glow-left {
  top: 680rpx;
  left: -160rpx;
  width: 260rpx;
  height: 520rpx;
  background: rgba(233, 138, 182, 0.055);
}

.ephone-glow-right {
  top: 120rpx;
  right: -240rpx;
  width: 440rpx;
  height: 440rpx;
  background: rgba(233, 138, 182, 0.065);
}

.ephone-heading {
  position: fixed;
  top: 0;
  left: 50%;
  z-index: 800;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  width: 100%;
  max-width: 960rpx;
  min-height: 204rpx;
  padding-left: 40rpx;
  padding-top: var(--ephone-nav-top, calc(env(safe-area-inset-top) + 64rpx));
  padding-right: calc(var(--ephone-nav-right-space, 0rpx) + 40rpx);
  padding-bottom: 28rpx;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.96) 0%, rgba(0, 0, 0, 0.86) 76%, rgba(0, 0, 0, 0) 100%);
  transform: translateX(-50%);
  backdrop-filter: blur(18rpx);
}

.ephone-heading-copy {
  flex: 1 1 auto;
  min-width: 0;
}

.ephone-heading-with-image {
  gap: 22rpx;
  padding-top: var(--ephone-hero-top, calc(env(safe-area-inset-top) + 104rpx));
  padding-right: 40rpx;
}

.ephone-heading-with-image .ephone-heading-copy {
  flex: 0 1 300rpx;
}

.ephone-title {
  color: rgba(255, 255, 255, 0.96);
  font-size: 46rpx;
  font-weight: 900;
  line-height: 1.12;
  text-shadow: none;
}

.ephone-title::first-letter {
  color: inherit;
}

.ephone-subtitle {
  margin-top: 10rpx;
  color: var(--ephone-muted);
  font-size: 24rpx;
  line-height: 1.35;
}

.ephone-heading-with-image .ephone-title {
  white-space: nowrap;
}

.ephone-heading-with-image .ephone-subtitle {
  font-size: 23rpx;
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
