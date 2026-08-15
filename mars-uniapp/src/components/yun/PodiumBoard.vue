<script setup lang="ts">
import type { EphoneRankRecord } from '@/data/yun'
import AnchorAvatar from './AnchorAvatar.vue'
import StatValue from './StatValue.vue'

const props = defineProps<{
  records: EphoneRankRecord[]
  showGuild?: boolean
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
      :class="`podium-card-${item.rank}`"
    >
      <view class="podium-rank" :class="`podium-rank-${item.rank}`">
        <text class="podium-rank-num">{{ item.rank }}</text>
      </view>

      <AnchorAvatar
        :src="item.record.avatar"
        :name="item.record.name"
        :show-pulse="false"
        size="md"
      />

      <view class="podium-name-block">
        <text class="podium-name">{{ item.record.name }}</text>
        <text v-if="showGuild && item.record.guild" class="podium-guild">{{ item.record.guild }}</text>
      </view>

      <view class="podium-room">
        房间号：{{ item.record.roomId }}
      </view>

      <view class="podium-stat">
        <StatValue :value="item.record.value" :size="item.rank === 1 ? 'md' : 'sm'" />
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.podium-board {
  position: relative;
  display: grid;
  /* 三卡均分宽度，第一名只在高度上突出，避免整体超出屏幕 */
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12rpx;
  align-items: end;
  margin-top: 28rpx;
  padding-top: 42rpx;
  width: 100%;
  box-sizing: border-box;
  overflow: visible;
}

/* ===== Card Base ===== */
.podium-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 0;
  min-height: 282rpx;
  padding: 44rpx 8rpx 20rpx;
  border-radius: 26rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.06) 0%, rgba(255, 255, 255, 0.02) 100%);
  box-sizing: border-box;
  overflow: visible;
}

/* ===== 1st — Gold ===== */
.podium-card-1 {
  min-height: 360rpx;
  border-color: rgba(242, 199, 100, 0.58);
  background:
    radial-gradient(120% 62% at 50% 0%, rgba(242, 199, 100, 0.26) 0%, rgba(242, 199, 100, 0.06) 62%, transparent 100%),
    linear-gradient(180deg, rgba(64, 48, 24, 0.72) 0%, rgba(28, 24, 20, 0.86) 100%);
  box-shadow: 0 12rpx 38rpx rgba(242, 190, 100, 0.13);
  transform: translateY(-18rpx);
}

/* ===== 2nd — Silver ===== */
.podium-card-2 {
  border-color: rgba(210, 222, 232, 0.34);
  background:
    radial-gradient(120% 52% at 50% 0%, rgba(210, 222, 232, 0.14) 0%, transparent 64%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0.02) 100%);
}

/* ===== 3rd — Bronze ===== */
.podium-card-3 {
  border-color: rgba(220, 180, 140, 0.34);
  background:
    radial-gradient(120% 52% at 50% 0%, rgba(220, 180, 140, 0.14) 0%, transparent 64%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0.02) 100%);
}

/* ===== Rank Medal (圆形奖牌，浮出卡片顶部) ===== */
.podium-rank {
  position: absolute;
  top: -30rpx;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  width: 58rpx;
  height: 58rpx;
  border-radius: 50%;
  border: 4rpx solid;
  font-weight: 900;
  font-size: 29rpx;
}

.podium-rank-1 {
  top: -36rpx;
  width: 70rpx;
  height: 70rpx;
  border-color: #ffe7a3;
  background: linear-gradient(135deg, #ffe8a8, #f2c764 45%, #d98924);
  color: #1a1200;
  font-size: 34rpx;
  box-shadow: 0 8rpx 20rpx rgba(242, 190, 100, 0.35);
}

.podium-rank-2 {
  border-color: #eef6ff;
  background: linear-gradient(135deg, #f8fbff, #c8d2dc);
  color: #3a4045;
  box-shadow: 0 4rpx 10rpx rgba(200, 210, 220, 0.28);
}

.podium-rank-3 {
  border-color: #f0d4b9;
  background: linear-gradient(135deg, #f3dac3, #dcb48c);
  color: #4a3020;
  box-shadow: 0 4rpx 10rpx rgba(220, 180, 140, 0.28);
}

/* ===== Name ===== */
.podium-name-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  margin-top: 16rpx;
  min-width: 0;
  overflow: hidden;
}

.podium-name {
  display: block;
  width: 100%;
  overflow: hidden;
  color: #fff;
  font-size: 24rpx;
  font-weight: 900;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.podium-card-1 .podium-name {
  font-size: 28rpx;
}

.podium-guild {
  display: block;
  width: 100%;
  margin-top: 8rpx;
  overflow: hidden;
  color: var(--ephone-primary-soft);
  font-size: 19rpx;
  font-weight: 600;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== Room ===== */
.podium-room {
  width: 100%;
  margin: 8rpx 0 0;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.5);
  font-size: 18rpx;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== Stat ===== */
.podium-stat {
  margin-top: 8rpx;
}
</style>
