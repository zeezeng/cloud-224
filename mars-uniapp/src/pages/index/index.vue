<script setup lang="ts">
import type { HomeBanner } from '@/api/banner'
import { getHomeBannerList, resolveBannerImageUrl } from '@/api/banner'
import { getAnchorGiftRanking } from '@/api/ranking'
import type { EphoneRankRecord } from '@/data/yun'
import { homeQuickActions } from '@/data/yun'
import { formatCompactNumber } from '@/utils/yun'
import YunPanel from '@/components/yun/YunPanel.vue'
import YunPage from '@/components/yun/YunPage.vue'
import QuickGrid from '@/components/yun/QuickGrid.vue'
import RankBadge from '@/components/yun/RankBadge.vue'
import AnchorAvatar from '@/components/yun/AnchorAvatar.vue'
import AddToDesktopTip from '@/components/yun/AddToDesktopTip.vue'
import { useRefreshLimit } from '@/hooks/useRefreshLimit'

defineOptions({
  name: 'Home',
})

definePage({
  type: 'home',
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '首页',
    backgroundColor: '#000000',
  },
})

const todayRanking = ref<EphoneRankRecord[]>([])
const todayRankingUpdatedAt = ref('')

const rankingUpdatedText = computed(() => {
  const value = todayRankingUpdatedAt.value
  if (!value) {
    return ''
  }
  const match = value.match(/^(\d{4}-\d{2}-\d{2}) (\d{2}:\d{2})/)
  if (!match) {
    return `更新于 ${value}`
  }
  const now = new Date()
  const pad = (num: number) => String(num).padStart(2, '0')
  const todayStr = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
  return match[1] === todayStr ? `更新于 ${match[2]}` : `更新于 ${match[1]} ${match[2]}`
})

/** 下拉刷新 5 秒限流 */
const { tryRefresh } = useRefreshLimit(5000)
const refreshing = ref(false)

function handleRefresh() {
  if (!tryRefresh()) {
    // 被限流：先展开再收回，让 refresher 回到原位
    refreshing.value = true
    setTimeout(() => {
      refreshing.value = false
    }, 100)
    uni.showToast({
      icon: 'none',
      title: '刷新太频繁，请 5 秒后再试',
    })
    return
  }
  refreshing.value = true
  Promise.all([loadHomeBanners(), loadTodayRanking()]).finally(() => {
    refreshing.value = false
  })
}
const fallbackBanner: HomeBanner = {
  id: 0,
  title: '闪耀舞台',
  description: '团结 · 奋进 · 梦想',
  imageUrl: '/static/ephone/home-stage.png',
  jumpType: 0,
}
const homeBanners = ref<HomeBanner[]>([fallbackBanner])
const tabbarPages = new Set([
  'pages/index/index',
  'pages/ranking/ranking',
  'pages/enjoy/enjoy',
  'pages/vault/vault',
])

onMounted(() => {
  loadHomeBanners()
  loadTodayRanking()
})

async function loadTodayRanking() {
  try {
    const result = await getAnchorGiftRanking({ period: 'today', page: 1, pageSize: 10 })
    todayRanking.value = Array.isArray(result.records) ? result.records : []
    todayRankingUpdatedAt.value = result.latestSyncTime || ''
  }
  catch (error) {
    console.error('首页今日排行加载失败', error)
    todayRanking.value = []
  }
}

async function loadHomeBanners() {
  try {
    const list = await getHomeBannerList()
    homeBanners.value = Array.isArray(list) && list.length > 0 ? list : [fallbackBanner]
  }
  catch (error) {
    console.error('首页轮播图加载失败', error)
    homeBanners.value = [fallbackBanner]
  }
}

function getBannerImageUrl(banner: HomeBanner) {
  return resolveBannerImageUrl(banner.imageUrl) || fallbackBanner.imageUrl || ''
}

function normalizePagePath(path?: string) {
  const value = String(path || '').trim()
  if (!value) {
    return ''
  }
  return value.startsWith('/') ? value : `/${value}`
}

function isTabbarPage(path: string) {
  return tabbarPages.has(path.startsWith('/') ? path.slice(1) : path)
}

function openWebUrl(url: string) {
  // #ifdef H5
  window.open(url, '_blank')
  // #endif

  // #ifndef H5
  uni.navigateTo({
    url: `/pages/webview/webview?url=${encodeURIComponent(url)}`,
  })
  // #endif
}

function handleBannerTap(banner: HomeBanner) {
  if (banner.jumpType === 1) {
    const path = normalizePagePath(banner.jumpTarget)
    if (!path) {
      return
    }
    const navigate = isTabbarPage(path) ? uni.switchTab : uni.navigateTo
    navigate({ url: path })
    return
  }

  if (banner.jumpType === 2) {
    const url = String(banner.jumpTarget || '').trim()
    if (/^https?:\/\//i.test(url)) {
      openWebUrl(url)
    }
  }
}
</script>

<template>
  <YunPage title="云224" subtitle="向阳而生 · 热爱同行 · 闪闪发光" scroll-locked>
    <view class="home-layout">
      <scroll-view
        class="home-scroll"
        scroll-y
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        :show-scrollbar="false"
        @refresherrefresh="handleRefresh"
      >
        <view class="home-content">
          <swiper
            class="home-banner-swiper"
            :indicator-dots="homeBanners.length > 1"
            circular
            autoplay
            :interval="4200"
            :duration="360"
          >
            <swiper-item v-for="banner in homeBanners" :key="banner.id || banner.imageUrl">
              <view class="home-banner-slide" @tap="handleBannerTap(banner)">
                <image
                  class="home-banner-image"
                  :src="getBannerImageUrl(banner)"
                  :alt="banner.title || '首页轮播图'"
                  mode="aspectFill"
                />
              </view>
            </swiper-item>
          </swiper>

          <view class="home-notice">
            <view class="i-carbon-volume-up notice-icon" />
            <text class="notice-title">公告</text>
            <text class="notice-copy">平台数据每 10 分钟更新一次</text>
          </view>

          <YunPanel title="主播今日排行" icon="i-carbon-trophy-filled" :action="rankingUpdatedText">
            <view class="home-rank-list">
              <view v-for="(anchor, index) in todayRanking" :key="anchor.id" class="home-rank-row">
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
                  {{ formatCompactNumber(anchor.value) }}
                </view>
              </view>
              <view v-if="todayRanking.length === 0" class="home-rank-empty">
                暂无排行数据，请先同步主播数据
              </view>
            </view>
          </YunPanel>

          <!-- <QuickGrid :items="homeQuickActions" /> -->
        </view>
      </scroll-view>
    </view>
  </YunPage>

  <AddToDesktopTip />
</template>

<style scoped lang="scss">
.home-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.home-scroll {
  flex: 1;
  height: 100%;
}

.home-content {
  padding-bottom: 40rpx;
}

.home-banner-swiper {
  height: 238rpx;
  margin-top: 26rpx;
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.04);
}

.home-banner-slide,
.home-banner-image {
  width: 100%;
  height: 100%;
}

.home-banner-slide {
  overflow: hidden;
  border-radius: 28rpx;
}

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

.home-rank-empty {
  padding: 40rpx 0;
  color: rgba(255, 255, 255, 0.5);
  font-size: 26rpx;
  text-align: center;
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
