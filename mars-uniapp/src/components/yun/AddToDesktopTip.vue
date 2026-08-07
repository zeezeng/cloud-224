<script setup lang="ts">
import { ref } from 'vue'

const STORAGE_KEY = 'add-to-desktop-tipped'

const visible = ref(false)
const bubbleStyle = ref<Record<string, string>>({})

// #ifdef MP-WEIXIN
function readMenuRect() {
  try {
    const menu = uni.getMenuButtonBoundingClientRect()
    const system = uni.getSystemInfoSync()
    if (menu?.top && menu?.height && system.windowWidth) {
      // 气泡顶部紧贴胶囊按钮下方，右边缘贴近胶囊右侧（··· 所在方向）
      bubbleStyle.value = {
        top: `${Math.ceil(menu.top + menu.height + 10)}px`,
        right: `${Math.ceil(system.windowWidth - (menu.left + menu.width) + 8)}px`,
      }
      return
    }
  }
  catch {
    // 忽略，走兜底定位
  }
  bubbleStyle.value = {
    top: '132px',
    right: '16px',
  }
}
// #endif

onMounted(() => {
  // #ifdef MP-WEIXIN
  if (uni.getStorageSync(STORAGE_KEY))
    return
  readMenuRect()
  // 延迟显示，避免打断首屏渲染
  setTimeout(() => {
    visible.value = true
  }, 600)
  // #endif
})

function handleClose() {
  visible.value = false
  // #ifdef MP-WEIXIN
  uni.setStorageSync(STORAGE_KEY, '1')
  // #endif
}
</script>

<template>
  <!-- #ifdef MP-WEIXIN -->
  <view
    v-if="visible"
    class="add-tip-bubble"
    :style="bubbleStyle"
    @click="handleClose"
  >
    <view class="add-tip-arrow" />
    <view class="add-tip-copy">
      <view class="add-tip-text">
        点击右上角「···」
      </view>
      <view class="add-tip-sub">
        添加到桌面，下次打开更快
      </view>
    </view>
    <view class="i-carbon-close add-tip-close" />
  </view>
  <!-- #endif -->
</template>

<style scoped lang="scss">
.add-tip-bubble {
  position: fixed;
  z-index: 950;
  display: flex;
  align-items: center;
  gap: 12rpx;
  box-sizing: border-box;
  width: 392rpx;
  padding: 18rpx 20rpx;
  border: 1rpx solid rgba(242, 182, 204, 0.35);
  border-radius: 20rpx;
  background: rgba(24, 24, 30, 0.96);
  box-shadow:
    0 12rpx 32rpx rgba(0, 0, 0, 0.45),
    0 0 24rpx rgba(242, 182, 204, 0.12);
  animation: add-tip-pop 0.28s ease-out;
}

.add-tip-arrow {
  position: absolute;
  top: -7rpx;
  right: 58rpx;
  width: 22rpx;
  height: 22rpx;
  border-top: 1rpx solid rgba(242, 182, 204, 0.35);
  border-left: 1rpx solid rgba(242, 182, 204, 0.35);
  background: rgba(24, 24, 30, 0.96);
  transform: rotate(45deg);
}

.add-tip-copy {
  flex: 1;
  min-width: 0;
}

.add-tip-text {
  color: #fff;
  font-size: 25rpx;
  font-weight: 800;
}

.add-tip-sub {
  margin-top: 6rpx;
  color: rgba(255, 255, 255, 0.55);
  font-size: 22rpx;
}

.add-tip-close {
  flex: 0 0 auto;
  color: rgba(255, 255, 255, 0.4);
  font-size: 30rpx;
}

@keyframes add-tip-pop {
  from {
    opacity: 0;
    transform: translateY(-10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
