<script setup lang="ts">
import type { HomeBanner } from '@/api/banner'
import { getHomeBannerList, resolveBannerImageUrl } from '@/api/banner'
import type { HomeNotice } from '@/api/notice'
import { getHomeNoticeList, getPopupNoticeList } from '@/api/notice'
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
    disableScroll: true,
    navigationBarTitleText: '首页',
    backgroundColor: '#000000',
  },
})

const todayRanking = ref<EphoneRankRecord[]>([])
const todayRankingUpdatedAt = ref('')
const homeNotices = ref<HomeNotice[]>([])
const fallbackNoticeText = '您好，欢迎来到224！'
const popupVisible = ref(false)
const popupNotice = ref<HomeNotice | null>(null)

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
  'pages/season/season',
])

onMounted(() => {
  loadHomeBanners()
  loadHomeNotices()
  loadTodayRanking()
  checkPopupNotice()
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

/** 弹窗公告：本次启动最多检查一次，本地存储已读 id 避免重复弹 */
const POPUP_READ_KEY = 'popup_notice_read_ids'
let hasCheckedPopup = false

async function checkPopupNotice() {
  if (hasCheckedPopup) {
    return
  }
  hasCheckedPopup = true
  try {
    const list = await getPopupNoticeList()
    if (!Array.isArray(list) || list.length === 0) {
      return
    }
    let readIds: number[] = []
    try {
      readIds = JSON.parse(uni.getStorageSync(POPUP_READ_KEY) || '[]')
    }
    catch {
      readIds = []
    }
    const unread = list.filter(n => n.id != null && !readIds.includes(n.id))
    if (unread.length === 0) {
      return
    }
    popupNotice.value = unread[0]
    popupVisible.value = true
  }
  catch (error) {
    console.error('弹窗公告加载失败', error)
  }
}

function handlePopupConfirm() {
  const notice = popupNotice.value
  if (notice?.id != null) {
    let readIds: number[] = []
    try {
      readIds = JSON.parse(uni.getStorageSync(POPUP_READ_KEY) || '[]')
    }
    catch {
      readIds = []
    }
    if (!readIds.includes(notice.id)) {
      readIds.push(notice.id)
      uni.setStorageSync(POPUP_READ_KEY, JSON.stringify(readIds))
    }
  }
  popupVisible.value = false
  popupNotice.value = null
}

function getBannerImageUrl(banner: HomeBanner) {
  return resolveBannerImageUrl(banner.imageUrl) || fallbackBanner.imageUrl || ''
}

function formatNoticeText(notice: HomeNotice) {
  // 优先使用公告内容，无内容时回退到标题
  const content = String(notice.contentPreview || notice.content || '').trim()
  if (content) {
    return content
  }
  return String(notice.title || '').trim()
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

function openFeedback() {
  uni.navigateTo({
    url: '/pages/feedback/feedback',
  })
}
</script>

<template>
  <YunPage title="云224" subtitle="向阳而生 · 闪闪发光" scroll-locked>
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

  <view v-if="!popupVisible" class="home-feedback-fab" @tap="openFeedback">
    <view class="i-carbon-help feedback-fab-icon" />
  </view>

  <AddToDesktopTip />

  <view v-if="popupVisible" class="popup-mask" @tap="handlePopupConfirm">
    <view class="popup-card" @tap.stop>
      <view class="popup-header">
        <view class="i-carbon-notification popup-header-icon" />
        <text class="popup-header-label">系统公告</text>
      </view>
      <text class="popup-title">{{ popupNotice?.title || '系统公告' }}</text>
      <text class="popup-content">{{ popupNotice?.content }}</text>
      <view class="popup-btn" @tap="handlePopupConfirm">
        我知道了
      </view>
    </view>
  </view>
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
  max-width: 960rpx;
  margin: 0 auto;
  padding: 0 40rpx 150rpx;
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

.home-feedback-fab {
  position: fixed;
  right: 28rpx;
  bottom: calc(156rpx + env(safe-area-inset-bottom));
  z-index: 1100;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 76rpx;
  height: 76rpx;
  padding: 0;
  border: 1rpx solid rgba(255, 255, 255, 0.14);
  border-radius: 999rpx;
  background: rgba(24, 20, 28, 0.86);
  box-shadow: 0 12rpx 34rpx rgba(0, 0, 0, 0.34), 0 0 0 1rpx rgba(233, 138, 182, 0.08) inset;
  backdrop-filter: blur(18rpx);
  -webkit-backdrop-filter: blur(18rpx);
}

.feedback-fab-icon {
  color: var(--ephone-primary-soft);
  font-size: 36rpx;
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
  gap: 8rpx;
  color: var(--ephone-primary-soft);
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1;
}

.home-rank-value .i-carbon-fire {
  flex-shrink: 0;
  font-size: 32rpx;
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

.popup-mask {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 60rpx;
  background: rgba(0, 0, 0, 0.65);
  backdrop-filter: blur(12rpx);
  -webkit-backdrop-filter: blur(12rpx);
}

.popup-card {
  position: relative;
  width: 100%;
  max-width: 600rpx;
  padding: 48rpx 40rpx 36rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.12);
  border-radius: 32rpx;
  background: linear-gradient(180deg, rgba(34, 28, 38, 0.97), rgba(16, 14, 20, 0.98));
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.6);
  box-sizing: border-box;
  text-align: center;
  animation: popup-in 0.28s ease both;
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 22rpx;
  color: var(--ephone-primary-soft);
}

.popup-header-icon {
  font-size: 36rpx;
}

.popup-header-label {
  margin-left: 10rpx;
  font-size: 26rpx;
  font-weight: 700;
  letter-spacing: 2rpx;
}

.popup-title {
  display: block;
  margin-bottom: 20rpx;
  color: #fff;
  font-size: 36rpx;
  font-weight: 800;
}

.popup-content {
  display: block;
  margin-bottom: 40rpx;
  color: rgba(255, 255, 255, 0.78);
  font-size: 28rpx;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.popup-btn {
  width: 100%;
  height: 84rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 42rpx;
  background: linear-gradient(135deg, var(--ephone-primary), var(--ephone-primary-soft));
  color: #1a1a1a;
  font-size: 30rpx;
  font-weight: 800;
}

@keyframes popup-in {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(20rpx);
  }

  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
</style>
