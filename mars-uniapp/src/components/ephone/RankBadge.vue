<script setup lang="ts">
import { getRankTone } from '@/utils/ephone'

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
  color: #fff;
  font-size: 26rpx;
  font-weight: 900;
}

.rank-badge-gold {
  background: linear-gradient(160deg, #ffd86a, #ff3b9a);
  box-shadow: 0 0 24rpx rgba(255, 204, 86, 0.42);
}

.rank-badge-purple {
  background: linear-gradient(160deg, #b08cff, #7a5cff);
}

.rank-badge-coral {
  background: linear-gradient(160deg, #ff9d78, #ff6048);
}

.rank-badge-dark {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.78);
}
</style>
