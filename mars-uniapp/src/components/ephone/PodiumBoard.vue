<script setup lang="ts">
import type { EphoneRankRecord } from '@/data/ephone'
import AnchorAvatar from './AnchorAvatar.vue'
import RankBadge from './RankBadge.vue'
import StatValue from './StatValue.vue'

const props = defineProps<{
  records: EphoneRankRecord[]
}>()

const podiumItems = computed(() => {
  const topRecords = props.records.slice(0, 3)
  return [topRecords[1], topRecords[0], topRecords[2]]
    .filter(Boolean)
    .map(record => ({
      record,
      rank: topRecords.findIndex(item => item.id === record.id) + 1,
    }))
})
</script>

<template>
  <view class="podium-board">
    <view
      v-for="item in podiumItems"
      :key="item.record.id"
      class="podium-card"
      :class="{ 'podium-card-first': item.rank === 1 }"
    >
      <RankBadge :rank="item.rank" />
      <AnchorAvatar
        :src="item.record.avatar"
        :name="item.record.name"
        :crown="item.rank === 1"
        :show-pulse="false"
        :size="item.rank === 1 ? 'lg' : 'md'"
      />
      <view class="podium-name">
        {{ item.record.name }}
      </view>
      <view class="podium-room">
        房间号：{{ item.record.roomId }}
      </view>
      <StatValue :value="item.record.value" :size="item.rank === 1 ? 'md' : 'sm'" />
    </view>
  </view>
</template>

<style scoped lang="scss">
.podium-board {
  display: grid;
  grid-template-columns: 1fr 1.18fr 1fr;
  gap: 16rpx;
  align-items: end;
  margin-top: 18rpx;
  padding-top: 28rpx;
}

.podium-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 268rpx;
  padding: 24rpx 14rpx 22rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.09);
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.045);
  box-shadow: none;
}

.podium-card-first {
  min-height: 332rpx;
  border-color: rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.06);
  box-shadow: none;
  transform: translateY(-18rpx);
}

.podium-card :deep(.rank-badge) {
  position: absolute;
  top: -24rpx;
  left: 50%;
  transform: translateX(-50%);
}

.podium-name {
  width: 100%;
  margin-top: 18rpx;
  overflow: hidden;
  color: #fff;
  font-size: 26rpx;
  font-weight: 900;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.podium-card-first .podium-name {
  font-size: 30rpx;
}

.podium-room {
  width: 100%;
  margin: 10rpx 0 14rpx;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.58);
  font-size: 20rpx;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
