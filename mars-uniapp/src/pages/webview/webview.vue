<script setup lang="ts">
defineOptions({
  name: 'WebviewPage',
})

definePage({
  style: {
    navigationBarTitleText: '网页',
  },
})

const pageUrl = ref('')

onLoad((query) => {
  const url = decodeURIComponent(String(query?.url || '').trim())
  if (/^https?:\/\//i.test(url)) {
    pageUrl.value = url
    return
  }

  uni.showToast({
    icon: 'none',
    title: '网页地址无效',
  })
})
</script>

<template>
  <web-view v-if="pageUrl" :src="pageUrl" />
  <view v-else class="webview-empty">
    网页地址无效
  </view>
</template>

<style scoped lang="scss">
.webview-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40rpx;
  background: #000;
  color: rgba(255, 255, 255, 0.72);
  font-size: 28rpx;
}
</style>
