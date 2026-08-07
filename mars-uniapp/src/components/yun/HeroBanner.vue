<script setup lang="ts">
withDefaults(defineProps<{
  title: string
  subtitle: string
  image: string
  alt?: string
  imageRight?: boolean
  imageOnly?: boolean
}>(), {
  imageRight: true,
  imageOnly: false,
})
</script>

<template>
  <view class="hero-banner" :class="{ 'hero-banner-image-only': imageOnly }">
    <view v-if="!imageOnly" class="hero-copy">
      <view class="hero-title">
        {{ title }}
      </view>
      <view class="hero-subtitle">
        {{ subtitle }}
      </view>
    </view>
    <image class="hero-image" :class="{ 'hero-image-right': imageRight }" :src="image" :alt="alt || title" mode="aspectFill" />
  </view>
</template>

<style scoped lang="scss">
.hero-banner {
  position: relative;
  min-height: 246rpx;
  margin-top: 26rpx;
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.04);
  box-shadow: none;
}

.hero-banner-image-only {
  min-height: 238rpx;
}

.hero-copy {
  position: relative;
  z-index: 2;
  width: 52%;
  padding: 56rpx 34rpx;
}

.hero-title {
  color: #fff;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1.2;
  text-shadow: none;
}

.hero-subtitle {
  margin-top: 20rpx;
  color: rgba(255, 255, 255, 0.72);
  font-size: 24rpx;
}

.hero-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  opacity: 0.96;
}

.hero-image-right {
  left: auto;
}
</style>
