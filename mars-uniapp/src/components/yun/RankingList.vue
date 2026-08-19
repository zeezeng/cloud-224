<script setup lang="ts">
import type { EphoneRankRecord } from '@/data/yun'
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
  showGuild?: boolean
}>(), {
  showTrend: false,
  showRank: true,
  variant: 'default',
  startRank: 1,
  showGuild: false,
})
</script>

<template>
  <view
    class="ranking-list"
    :class="[`ranking-list-${variant}`, { 'ranking-list-no-rank': !showRank }]"
  >
    <view v-for="(record, index) in records" :key="record.id" class="ranking-row">
      <RankBadge v-if="showRank" :rank="record.rank ?? startRank + index" />
      <AnchorAvatar :src="record.avatar" :name="record.name" :show-pulse="false" size="sm" />
      <view class="ranking-main">
        <view class="ranking-name-line">
          <text class="ranking-name">{{ record.name }}</text>
          <text v-if="showGuild && record.guild" class="ranking-guild">· {{ record.guild }}</text>
        </view>
        <view class="ranking-subtitle">
          {{ record.subtitle }}
        </view>
      </view>
      <view class="ranking-stat">
        <StatValue :value="record.value" :label="valueLabel" size="md" />
        <view v-if="showTrend" class="ranking-trend" :class="`ranking-trend-${record.trend}`">
          <view :class="record.trend === 'down' ? 'i-carbon-arrow-down' : 'i-carbon-arrow-up'" />
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.ranking-list {
  width: 100%;
  margin-top: 18rpx;
  overflow: hidden;
  border-radius: 28rpx;
  background: rgba(18, 18, 22, 0.9);
  box-sizing: border-box;
}

.ranking-row {
  display: grid;
  grid-template-columns: 46rpx 76rpx minmax(0, 1fr) minmax(0, 156rpx);
  gap: 12rpx;
  align-items: center;
  min-height: 112rpx;
  padding: 22rpx 18rpx;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.06);
  box-sizing: border-box;
}

.ranking-list-no-rank .ranking-row {
  grid-template-columns: 76rpx minmax(0, 1fr) minmax(0, 156rpx);
}

.ranking-row:last-child {
  border-bottom: 0;
}

.ranking-main {
  min-width: 0;
}

.ranking-name-line {
  display: flex;
  align-items: center;
  min-width: 0;
  overflow: hidden;
}

.ranking-name {
  flex-shrink: 1;
  min-width: 0;
  overflow: hidden;
  color: #fff;
  font-size: 27rpx;
  font-weight: 850;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ranking-guild {
  flex-shrink: 0;
  margin-left: 6rpx;
  max-width: 50%;
  overflow: hidden;
  color: var(--ephone-primary-soft);
  font-size: 20rpx;
  font-weight: 600;
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
  grid-template-columns: 86rpx minmax(0, 1fr) minmax(0, 186rpx);
  min-height: 126rpx;
  padding: 18rpx 20rpx;
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
  text-shadow: none;
}

.ranking-list-enjoy :deep(.stat-label) {
  margin-top: 12rpx;
  color: rgba(255, 255, 255, 0.52);
  font-size: 22rpx;
}
</style>
