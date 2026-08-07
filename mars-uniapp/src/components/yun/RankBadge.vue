<script setup lang="ts">
import { getRankTone } from '@/utils/yun'

const props = defineProps<{
  rank: number
}>()

const tone = computed(() => getRankTone(props.rank))
</script>

<template>
  <view class="rank-badge" :class="`rank-badge-${tone}`">
    {{ rank }}
  </view>
</template>

<style scoped lang="scss">
.rank-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 54rpx;
  height: 60rpx;
  clip-path: polygon(50% 0, 94% 22%, 94% 78%, 50% 100%, 6% 78%, 6% 22%);
  color: rgba(255, 255, 255, 0.9);
  font-size: 26rpx;
  font-weight: 900;
}

.rank-badge-gold {
  background: rgba(242, 182, 204, 0.22);
  box-shadow: none;
}

.rank-badge-purple {
  background: rgba(255, 255, 255, 0.14);
}

.rank-badge-coral {
  background: rgba(255, 255, 255, 0.11);
}

.rank-badge-dark {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.78);
}
</style>
