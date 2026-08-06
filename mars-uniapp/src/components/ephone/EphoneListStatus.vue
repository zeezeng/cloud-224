<script setup lang="ts">
const props = withDefaults(defineProps<{
  loading: boolean
  refreshing: boolean
  loadingMore: boolean
  loaded: boolean
  hasMore: boolean
  hasItems: boolean
  errorMessage?: string
  loadMoreError?: string
  loadingText?: string
  emptyText?: string
  emptyIcon?: string
  errorIcon?: string
  moreText?: string
  noMoreText?: string
}>(), {
  errorMessage: '',
  loadMoreError: '',
  loadingText: '正在加载...',
  emptyText: '暂无数据',
  emptyIcon: 'i-carbon-search-locate',
  errorIcon: 'i-carbon-cloud-offline',
  moreText: '继续上拉加载更多',
  noMoreText: '没有更多数据了！',
})

const emit = defineEmits<{
  retry: []
  loadMore: []
}>()

const bottomText = computed(() => {
  if (props.loadMoreError) {
    return '加载失败，点这里重试'
  }
  if (props.loadingMore) {
    return '正在加载更多...'
  }
  return props.hasMore ? props.moreText : props.noMoreText
})

function handleBottomTap() {
  if (props.loadingMore || (!props.hasMore && !props.loadMoreError)) {
    return
  }
  if (props.loadMoreError) {
    emit('retry')
    return
  }
  emit('loadMore')
}
</script>

<template>
  <view v-if="refreshing && loaded" class="ephone-list-refreshing">
    <view class="i-carbon-circle-dash ephone-list-refreshing-icon ephone-list-spin" />
    <text>正在刷新...</text>
  </view>

  <view v-if="loading && !hasItems" class="ephone-list-state">
    <view class="i-carbon-circle-dash ephone-list-state-icon ephone-list-spin" />
    <text>{{ loadingText }}</text>
  </view>

  <view v-else-if="errorMessage && !hasItems" class="ephone-list-state ephone-list-state-error">
    <view :class="errorIcon" class="ephone-list-state-icon" />
    <text>{{ errorMessage }}</text>
    <button class="ephone-list-retry" @tap="emit('retry')">
      重试
    </button>
  </view>

  <view v-else-if="loaded && !hasItems && !refreshing && !loading" class="ephone-list-state">
    <view :class="emptyIcon" class="ephone-list-state-icon" />
    <text>{{ emptyText }}</text>
  </view>

  <slot v-if="hasItems" />

  <button
    v-if="hasItems"
    class="ephone-list-load-more"
    :disabled="loadingMore || (!hasMore && !loadMoreError)"
    @tap="handleBottomTap"
  >
    <view v-if="loadingMore" class="i-carbon-circle-dash ephone-list-load-more-icon ephone-list-spin" />
    <text>{{ bottomText }}</text>
  </button>
</template>

<style scoped lang="scss">
.ephone-list-refreshing {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  min-height: 58rpx;
  color: rgba(255, 255, 255, 0.62);
  font-size: 23rpx;
}

.ephone-list-refreshing-icon,
.ephone-list-state-icon,
.ephone-list-load-more-icon {
  color: var(--ephone-primary-soft);
}

.ephone-list-refreshing-icon {
  font-size: 30rpx;
}

.ephone-list-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 360rpx;
  margin-top: 20rpx;
  gap: 18rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.045);
  color: rgba(255, 255, 255, 0.62);
  font-size: 26rpx;
  text-align: center;
}

.ephone-list-state-icon {
  font-size: 56rpx;
}

.ephone-list-state-error {
  color: rgba(255, 255, 255, 0.72);
}

.ephone-list-retry {
  min-width: 148rpx;
  height: 62rpx;
  padding: 0 32rpx;
  border: 1rpx solid rgba(242, 182, 204, 0.22);
  border-radius: 999rpx;
  background: rgba(242, 182, 204, 0.1);
  color: #fff;
  font-size: 26rpx;
  line-height: 62rpx;
}

.ephone-list-retry::after {
  border: 0;
}

.ephone-list-load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 88rpx;
  margin-top: 20rpx;
  gap: 12rpx;
  border: 0;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.045);
  color: rgba(255, 255, 255, 0.56);
  font-size: 24rpx;
  line-height: 88rpx;
}

.ephone-list-load-more::after {
  border: 0;
}

.ephone-list-load-more[disabled] {
  opacity: 1;
}

.ephone-list-load-more-icon {
  font-size: 30rpx;
}

.ephone-list-spin {
  animation: ephone-list-spin 1.1s linear infinite;
}

@keyframes ephone-list-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
