<script setup lang="ts">
import type { HomeBanner } from '@/api/banner'
import { getHomeBannerList, resolveBannerImageUrl } from '@/api/banner'
import type { HomeNotice } from '@/api/notice'
import { getHomeNoticeList } from '@/api/notice'
import { getAnchorGiftRanking } from '@/api/ranking'
import type { EphoneRankRecord } from '@/data/yun'
import { formatClockTime, formatCompactNumber } from '@/utils/yun'
import YunPanel from '@/components/yun/YunPanel.vue'
import YunPage from '@/components/yun/YunPage.vue'
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
const homeNotices = ref<HomeNotice[]>([])
const fallbackNoticeText = '平台数据每 10 分钟更新一次'

const rankingUpdatedText = computed(() => {
  return `数据截止时间：${formatClockTime(todayRankingUpdatedAt.value) || '--:--:--'}`
})

const homeNoticeTexts = computed(() => {
  const list = homeNotices.value
    .map(item => formatNoticeText(item))
    .filter(Boolean)
  return list.length > 0 ? list : [fallbackNoticeText]
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
  Promise.all([loadHomeBanners(), loadHomeNotices(), loadTodayRanking()]).finally(() => {
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
  loadHomeNotices()
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

async function loadHomeNotices() {
  try {
    const list = await getHomeNoticeList(5)
    homeNotices.value = Array.isArray(list) ? list : []
  }
  catch (error) {
    console.error('首页公告加载失败', error)
    homeNotices.value = []
  }
}

function getBannerImageUrl(banner: HomeBanner) {
  return resolveBannerImageUrl(banner.imageUrl) || fallbackBanner.imageUrl || ''
}

function formatNoticeText(notice: HomeNotice) {
  const title = String(notice.title || '').trim()
  if (title) {
    return title
  }
  const preview = String(notice.contentPreview || notice.content || '').trim()
  return preview
}

function shouldAnimateNotice(text: string) {
  return String(text || '').trim().length > 18
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
          <view class="home-banner-frame">
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
          </view>

          <view class="home-notice">
            <view class="i-carbon-volume-up notice-icon" />
            <text class="notice-title">公告</text>
            <view class="notice-body">
              <swiper
                v-if="homeNoticeTexts.length > 1"
                class="notice-swiper"
                vertical
                circular
                autoplay
                :interval="3600"
                :duration="320"
              >
                <swiper-item v-for="text in homeNoticeTexts" :key="text">
                  <view class="notice-line">
                    <view class="notice-marquee" :class="{ 'notice-marquee-animated': shouldAnimateNotice(text) }">
                      <text class="notice-copy">{{ text }}</text>
                      <text v-if="shouldAnimateNotice(text)" class="notice-copy notice-copy-clone">{{ text }}</text>
                    </view>
                  </view>
                </swiper-item>
              </swiper>
              <view v-else class="notice-line">
                <view class="notice-marquee" :class="{ 'notice-marquee-animated': shouldAnimateNotice(homeNoticeTexts[0]) }">
                  <text class="notice-copy">{{ homeNoticeTexts[0] }}</text>
                  <text v-if="shouldAnimateNotice(homeNoticeTexts[0])" class="notice-copy notice-copy-clone">{{ homeNoticeTexts[0] }}</text>
                </view>
              </view>
            </view>
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
  width: 100%;
  height: 100%;
  min-height: 0;
}

.home-scroll {
  flex: 1;
  width: 100%;
  height: 100%;
  min-height: 0;
}

.home-content {
  width: 100%;
  box-sizing: border-box;
  padding-bottom: 40rpx;
}

.home-banner-frame {
  position: relative;
  width: 100%;
  margin-top: 26rpx;
  padding-top: 50%;
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.04);
}

.home-banner-swiper {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
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
  width: 100%;
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
  color: rgba(255, 255, 255, 0.72);
  font-size: 26rpx;
  white-space: nowrap;
}

.notice-body {
  flex: 1;
  min-width: 0;
  height: 38rpx;
  margin-left: 26rpx;
  overflow: hidden;
}

.notice-swiper {
  width: 100%;
  height: 38rpx;
}

.notice-line {
  display: flex;
  align-items: center;
  width: 100%;
  height: 38rpx;
  overflow: hidden;
}

.notice-marquee {
  display: inline-flex;
  align-items: center;
  min-width: 0;
  max-width: 100%;
}

.notice-marquee-animated {
  min-width: max-content;
  animation: notice-marquee 10s linear infinite;
}

.notice-copy-clone {
  padding-left: 72rpx;
}

.home-rank-list {
  width: 100%;
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

@keyframes notice-marquee {
  0% {
    transform: translateX(0);
  }

  100% {
    transform: translateX(calc(-50% - 36rpx));
  }
}
</style>
