<script setup lang="ts">
const props = withDefaults(defineProps<{
  visible: boolean
  bottom?: string
  right?: string
  duration?: number
  pageScroll?: boolean
}>(), {
  bottom: 'calc(env(safe-area-inset-bottom) + 156rpx)',
  right: '32rpx',
  duration: 260,
  pageScroll: true,
})

const emit = defineEmits<{
  backTop: []
}>()

function handleBackTop() {
  if (props.pageScroll) {
    uni.pageScrollTo({
      scrollTop: 0,
      duration: props.duration,
    })
  }
  emit('backTop')
}
</script>

<template>
  <button
    v-show="visible"
    class="back-top-button"
    :style="{ right, bottom }"
    aria-label="返回顶部"
    @click.stop="handleBackTop"
  >
    <view class="i-carbon-chevron-up back-top-icon" />
  </button>
</template>

<style scoped lang="scss">
.back-top-button {
  position: fixed;
  z-index: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 86rpx;
  height: 86rpx;
  padding: 0;
  border: 1rpx solid rgba(242, 182, 204, 0.22);
  border-radius: 50%;
  background: rgba(18, 18, 24, 0.88);
  box-shadow: 0 16rpx 36rpx rgba(0, 0, 0, 0.26);
  color: #fff;
  line-height: 1;
}

.back-top-button::after {
  border: 0;
}

.back-top-icon {
  color: var(--ephone-primary-soft);
  font-size: 44rpx;
}
</style>
