<script setup lang="ts">
import type { EphoneRankRecord } from '@/data/yun'
import AnchorAvatar from './AnchorAvatar.vue'
import RankBadge from './RankBadge.vue'
import StatValue from './StatValue.vue'

withDefaults(defineProps<{
  records: EphoneRankRecord[]
  valueLabel?: string
  showTrend?: boolean
  showRank?: boolean
  variant?: 'default' | 'enjoy' | 'bubble'
  startRank?: number
  showGuild?: boolean
  showSubtitle?: boolean
}>(), {
  valueLabel: '',
  showTrend: false,
  showRank: true,
  variant: 'default',
  startRank: 1,
  showGuild: false,
  showSubtitle: true,
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
        <view v-if="showSubtitle && record.subtitle" class="ranking-subtitle">
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

.ranking-list-bubble {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 18rpx;
  overflow: visible;
  border-radius: 0;
  background: transparent;
}

.ranking-list-bubble .ranking-row {
  position: relative;
  grid-template-columns: 46rpx 84rpx minmax(0, 1fr) minmax(0, 172rpx);
  min-height: 132rpx;
  overflow: hidden;
  padding: 20rpx 20rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  border-radius: 30rpx;
  background:
    radial-gradient(circle at 14% 0%, rgba(242, 182, 204, 0.14) 0, rgba(242, 182, 204, 0) 38%),
    radial-gradient(circle at 100% 92%, rgba(126, 200, 227, 0.11) 0, rgba(126, 200, 227, 0) 42%),
    linear-gradient(145deg, rgba(30, 23, 32, 0.92), rgba(15, 14, 20, 0.92));
  box-shadow:
    0 22rpx 54rpx rgba(0, 0, 0, 0.26),
    0 1rpx 0 rgba(255, 255, 255, 0.06) inset;
}

.ranking-list-bubble.ranking-list-no-rank .ranking-row {
  grid-template-columns: 84rpx minmax(0, 1fr) minmax(0, 172rpx);
}

.ranking-list-bubble .ranking-row:last-child {
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);
}

.ranking-list-bubble .ranking-row::after {
  position: absolute;
  top: 26rpx;
  right: 18rpx;
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: rgba(242, 182, 204, 0.72);
  box-shadow: 0 0 18rpx rgba(242, 182, 204, 0.5);
  content: '';
}

.ranking-list-bubble .ranking-main {
  position: relative;
  z-index: 1;
}

.ranking-list-bubble .ranking-name {
  color: rgba(255, 255, 255, 0.96);
  font-size: 30rpx;
  font-weight: 900;
}

.ranking-list-bubble .ranking-subtitle {
  display: inline-flex;
  max-width: 100%;
  margin-top: 12rpx;
  overflow: hidden;
  padding: 6rpx 14rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.055);
  color: rgba(255, 255, 255, 0.56);
  font-size: 22rpx;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ranking-list-bubble .ranking-stat {
  position: relative;
  z-index: 1;
  min-width: 0;
  padding-right: 2rpx;
}

.ranking-list-bubble :deep(.anchor-avatar-sm) {
  width: 84rpx;
  height: 84rpx;
}

.ranking-list-bubble :deep(.anchor-avatar-frame) {
  border-color: rgba(242, 182, 204, 0.22);
  background: rgba(255, 255, 255, 0.065);
  box-shadow: 0 0 0 6rpx rgba(242, 182, 204, 0.045);
}

.ranking-list-bubble :deep(.stat-main) {
  color: #ffd2e2;
  font-size: 32rpx;
  font-variant-numeric: tabular-nums;
}

.ranking-list-bubble :deep(.stat-icon) {
  color: #7ec8e3;
  font-size: 28rpx;
}

.ranking-list-bubble :deep(.stat-label) {
  margin-top: 12rpx;
  color: rgba(255, 255, 255, 0.48);
  font-size: 21rpx;
}
</style>
