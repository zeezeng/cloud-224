<script setup lang="ts">
import type { EphoneRankRecord } from '@/data/ephone'
import AnchorAvatar from './AnchorAvatar.vue'
import RankBadge from './RankBadge.vue'
import StatValue from './StatValue.vue'

withDefaults(defineProps<{
  records: EphoneRankRecord[]
  valueLabel: string
  showTrend?: boolean
  showRank?: boolean
  variant?: 'default' | 'enjoy'
  startRank?: number
}>(), {
  showTrend: false,
  showRank: true,
  variant: 'default',
  startRank: 1,
})
</script>

<template>
  <view class="ranking-list" :class="[`ranking-list-${variant}`, { 'ranking-list-no-rank': !showRank }]">
    <view v-for="(record, index) in records" :key="record.id" class="ranking-row">
      <RankBadge v-if="showRank" :rank="startRank + index" />
      <AnchorAvatar :src="record.avatar" :name="record.name" :show-pulse="false" size="sm" />
      <view class="ranking-main">
        <view class="ranking-name">
          {{ record.name }}
        </view>
        <view class="ranking-subtitle">
          {{ record.subtitle }}
        </view>
      </view>
      <view class="ranking-stat">
        <StatValue :value="record.value" :label="valueLabel" :size="variant === 'enjoy' ? 'lg' : 'md'" />
        <view v-if="showTrend" class="ranking-trend" :class="`ranking-trend-${record.trend}`">
          <view :class="record.trend === 'down' ? 'i-carbon-arrow-down' : 'i-carbon-arrow-up'" />
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.ranking-list {
  margin-top: 18rpx;
  overflow: hidden;
  border-radius: 28rpx;
  background: rgba(18, 18, 22, 0.9);
}

.ranking-row {
  display: grid;
  grid-template-columns: 50rpx 70rpx minmax(0, 1fr) 190rpx;
  gap: 14rpx;
  align-items: center;
  min-height: 98rpx;
  padding: 14rpx 18rpx;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.06);
}

.ranking-list-no-rank .ranking-row {
  grid-template-columns: 70rpx minmax(0, 1fr) 190rpx;
}

.ranking-row:last-child {
  border-bottom: 0;
}

.ranking-main {
  min-width: 0;
}

.ranking-name {
  overflow: hidden;
  color: #fff;
  font-size: 27rpx;
  font-weight: 850;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ranking-subtitle {
  margin-top: 6rpx;
  color: rgba(255, 255, 255, 0.56);
  font-size: 21rpx;
}

.ranking-stat {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16rpx;
}

.ranking-trend {
  color: var(--ephone-primary-soft);
  font-size: 28rpx;
}

.ranking-trend-down {
  color: #30d27b;
}

.ranking-list-enjoy {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 22rpx;
  overflow: visible;
  border-radius: 0;
  background: transparent;
}

.ranking-list-enjoy .ranking-row {
  grid-template-columns: 92rpx minmax(0, 1fr) 216rpx;
  min-height: 134rpx;
  padding: 20rpx 22rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 26rpx;
  background: rgba(255, 255, 255, 0.045);
  box-shadow: none;
}

.ranking-list-enjoy .ranking-row:last-child {
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.08);
}

.ranking-list-enjoy .ranking-name {
  font-size: 29rpx;
  font-weight: 850;
}

.ranking-list-enjoy .ranking-subtitle {
  margin-top: 12rpx;
  color: rgba(255, 255, 255, 0.5);
  font-size: 23rpx;
}

.ranking-list-enjoy .ranking-stat {
  padding-right: 4rpx;
}

.ranking-list-enjoy :deep(.anchor-avatar-sm) {
  width: 86rpx;
  height: 86rpx;
}

.ranking-list-enjoy :deep(.stat-main) {
  color: var(--ephone-primary-soft);
  font-size: 36rpx;
  text-shadow: none;
}

.ranking-list-enjoy :deep(.stat-label) {
  margin-top: 12rpx;
  color: rgba(255, 255, 255, 0.52);
  font-size: 22rpx;
}
</style>
