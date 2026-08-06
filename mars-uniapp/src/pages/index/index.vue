<script setup lang="ts">
import { ephoneAnchors, homeQuickActions, rankingTypeTabs } from '@/data/ephone'
import { formatCompactNumber } from '@/utils/ephone'
import CapsuleTabs from '@/components/ephone/CapsuleTabs.vue'
import YunPanel from '@/components/ephone/YunPanel.vue'
import YunPage from '@/components/ephone/YunPage.vue'
import HeroBanner from '@/components/ephone/HeroBanner.vue'
import QuickGrid from '@/components/ephone/QuickGrid.vue'
import RankBadge from '@/components/ephone/RankBadge.vue'
import AnchorAvatar from '@/components/ephone/AnchorAvatar.vue'

defineOptions({
  name: 'Home',
})

definePage({
  type: 'home',
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '首页',
  },
})

const topAnchors = ephoneAnchors.slice(0, 3)
</script>

<template>
  <YunPage title="云224" subtitle="向阳而生 · 热爱同行 · 闪闪发光">
    <HeroBanner
      title="闪耀舞台"
      subtitle="团结 · 奋进 · 梦想"
      image="/static/ephone/home-stage.png"
      alt="闪耀舞台皇冠横幅"
      image-only
    />

    <view class="home-notice">
      <view class="i-carbon-volume-up notice-icon" />
      <text class="notice-title">公告</text>
      <text class="notice-copy">平台数据每 10 分钟更新一次</text>
    </view>

    <YunPanel title="主播排行" icon="i-carbon-trophy-filled" action="更多排行">
      <CapsuleTabs :items="rankingTypeTabs" compact />
      <view class="home-rank-list">
        <view v-for="(anchor, index) in topAnchors" :key="anchor.id" class="home-rank-row">
          <RankBadge :rank="index + 1" />
          <AnchorAvatar
            class="home-rank-avatar"
            :src="anchor.avatar"
            :name="anchor.name"
            :show-pulse="false"
            size="sm"
          />
          <view class="home-rank-name">
            {{ anchor.name }}
          </view>
          <view class="home-rank-value">
            <view class="i-carbon-fire" />
            {{ formatCompactNumber(anchor.monthlyFlow) }}
          </view>
        </view>
      </view>
    </YunPanel>

    <QuickGrid :items="homeQuickActions" />
  </YunPage>
</template>

<style scoped lang="scss">
.home-notice {
  display: flex;
  align-items: center;
  min-height: 70rpx;
  margin-top: 24rpx;
  padding: 0 24rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.05);
}

.notice-icon,
.notice-title {
  color: var(--ephone-primary-soft);
}

.notice-icon {
  font-size: 34rpx;
}

.notice-title {
  margin-left: 10rpx;
  font-size: 26rpx;
  font-weight: 800;
}

.notice-copy {
  flex: 1;
  margin-left: 26rpx;
  color: rgba(255, 255, 255, 0.72);
  font-size: 26rpx;
}

.home-rank-list {
  margin-top: 14rpx;
  overflow: hidden;
  border-radius: 24rpx;
  background: rgba(0, 0, 0, 0.18);
}

.home-rank-row {
  display: grid;
  grid-template-columns: 52rpx 66rpx minmax(0, 1fr) 190rpx;
  gap: 14rpx;
  align-items: center;
  min-height: 92rpx;
  padding: 14rpx 18rpx;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.06);
}

.home-rank-row:last-child {
  border-bottom: 0;
}

.home-rank-name {
  overflow: hidden;
  color: #fff;
  font-size: 28rpx;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home-rank-value {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6rpx;
  color: var(--ephone-primary-soft);
  font-size: 26rpx;
  font-weight: 900;
}

.home-rank-avatar {
  width: 62rpx;
  height: 62rpx;
}
</style>
