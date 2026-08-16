<script setup lang="ts">
import type { AppSeasonMember } from '@/api/season'
import { formatMoney } from '@/utils/yun'

const props = withDefaults(defineProps<{
  record: AppSeasonMember
  rank?: number
}>(), {
  rank: 1,
})

const isEliminated = computed(() => props.record.eliminated === 1)

const avatarSrc = computed(() => props.record.avatarUrl || '/static/images/default-avatar.png')

const rankLabel = computed(() => String(props.rank).padStart(2, '0'))

const anchorName = computed(() => props.record.anchorName || '未命名主播')

const isCaptain = computed(() => props.record.captainFlag === 1)

const teamName = computed(() => props.record.teamName || '')

const eliminationTimes = computed(() => Number(props.record.eliminationTimes || 0))

const nextEliminationAmountText = computed(() => formatMoney(Number(props.record.nextEliminationAmount || 0)).replace(/\.00$/, ''))
</script>

<template>
  <view class="row">
    <view class="row-rank" :class="{ 'is-top': rank <= 3 }">
      {{ rankLabel }}
    </view>

    <image
      class="row-avatar"
      :class="{ 'is-eliminated': isEliminated }"
      :src="avatarSrc"
      mode="aspectFill"
    />

    <view class="row-main">
      <view class="row-name-line">
        <text class="row-name" :class="{ 'is-eliminated': isEliminated }">
          {{ anchorName }}
        </text>
        <view class="row-status" :class="{ 'is-eliminated': isEliminated }">
          {{ isEliminated ? '已淘汰' : '存活' }}
        </view>
      </view>
      <view class="row-meta">
        <text v-if="isCaptain" class="row-captain">队长</text>
        <text v-if="teamName" class="row-team">{{ teamName }}</text>
        <text class="row-times">淘汰 {{ eliminationTimes }} 次</text>
      </view>
    </view>

    <view class="row-amount">
      <text class="row-amount-value">{{ nextEliminationAmountText }}</text>
      <text class="row-amount-label">下次金额</text>
    </view>
  </view>
</template>

<style scoped lang="scss">
.row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  min-height: 112rpx;
  padding: 16rpx 22rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.04);
}

.row-rank {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 52rpx;
  height: 40rpx;
  border-radius: 14rpx;
  background: rgba(255, 255, 255, 0.07);
  color: rgba(255, 255, 255, 0.78);
  font-size: 21rpx;
  font-weight: 900;
  line-height: 40rpx;
}

.row-rank.is-top {
  background: linear-gradient(135deg, rgba(233, 138, 182, 0.32), rgba(233, 138, 182, 0.08));
  color: #ffdff0;
}

.row-avatar {
  flex: 0 0 auto;
  box-sizing: border-box;
  width: 72rpx;
  height: 72rpx;
  border: 3rpx solid rgba(233, 138, 182, 0.45);
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.28);
}

.row-avatar.is-eliminated {
  border-color: rgba(255, 255, 255, 0.16);
  filter: grayscale(1);
}

.row-main {
  flex: 1;
  min-width: 0;
}

.row-name-line {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.row-name {
  flex: 0 1 auto;
  max-width: 60%;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.96);
  font-size: 27rpx;
  font-weight: 800;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row-name.is-eliminated {
  color: rgba(255, 255, 255, 0.55);
}

.row-status {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  height: 32rpx;
  padding: 0 12rpx;
  border-radius: 999rpx;
  background: rgba(38, 197, 146, 0.14);
  color: #b8ffdf;
  font-size: 18rpx;
  font-weight: 800;
  line-height: 32rpx;
}

.row-status::before {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #45d9a4;
  content: '';
}

.row-status.is-eliminated {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.55);
}

.row-status.is-eliminated::before {
  background: rgba(255, 255, 255, 0.35);
}

.row-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 8rpx;
}

.row-captain {
  flex: 0 0 auto;
  height: 28rpx;
  padding: 0 9rpx;
  border-radius: 8rpx;
  background: #f2b6cc;
  color: #2a111b;
  font-size: 16rpx;
  font-weight: 800;
  line-height: 28rpx;
}

.row-team {
  max-width: 200rpx;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.46);
  font-size: 19rpx;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row-times {
  flex: 0 0 auto;
  color: rgba(255, 255, 255, 0.4);
  font-size: 18rpx;
  line-height: 1.2;
}

.row-amount {
  display: flex;
  align-items: flex-end;
  flex-direction: column;
  flex: 0 0 auto;
  margin-left: 8rpx;
}

.row-amount-value {
  color: var(--ephone-primary-soft, #f2b6cc);
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1;
}

.row-amount-label {
  margin-top: 7rpx;
  color: rgba(255, 255, 255, 0.4);
  font-size: 16rpx;
  line-height: 1.1;
}
</style>
