<script setup lang="ts">
// i-carbon-code
import { customTabbarEnable, needHideNativeTabbar, tabbarCacheEnable } from './config'
import { tabbarList, tabbarStore } from './store'
import TabbarItem from './TabbarItem.vue'

// #ifdef MP-WEIXIN
// 将自定义节点设置成虚拟的（去掉自定义组件包裹层），更加接近Vue组件的表现，能更好的使用flex属性
defineOptions({
  virtualHost: true,
})
// #endif

/**
 * 中间的鼓包tabbarItem的点击事件
 */
function handleClickBulge() {
  uni.showToast({
    title: '点击了中间的鼓包tabbarItem',
    icon: 'none',
  })
}

function handleClick(index: number) {
  // 当前高亮和真实页面都已经是目标 tab 时，不重复跳转
  if (index === tabbarStore.curIdx && tabbarStore.isCurrentRouteTabbarItem(index)) {
    return
  }
  const list = tabbarList.value
  if (!list[index]) {
    return
  }
  if (list[index].isBulge) {
    handleClickBulge()
    return
  }
  const url = list[index].pagePath
  const prevIdx = tabbarStore.curIdx
  tabbarStore.setCurIdx(index)
  const syncTabbarAfterNavigation = () => {
    tabbarStore.syncCurIdxByCurrentPageAsync()
  }
  const restoreTabbarWhenNavigationFailed = () => {
    tabbarStore.setCurIdx(prevIdx)
  }
  if (tabbarCacheEnable) {
    uni.switchTab({
      url,
      success: syncTabbarAfterNavigation,
      fail: restoreTabbarWhenNavigationFailed,
    })
  }
  else {
    uni.navigateTo({
      url,
      success: syncTabbarAfterNavigation,
      fail: restoreTabbarWhenNavigationFailed,
    })
  }
}
// #ifndef MP-WEIXIN || MP-ALIPAY
// 因为有了 custom:true， 微信里面不需要多余的hide操作
onLoad(() => {
  // 解决原生 tabBar 未隐藏导致有2个 tabBar 的问题
  needHideNativeTabbar
  && uni.hideTabBar({
    fail(err) {
      console.log('hideTabBar fail: ', err)
    },
    success(res) {
      // console.log('hideTabBar success: ', res)
    },
  })
})
// #endif

// #ifdef MP-ALIPAY
onMounted(() => {
  // 解决支付宝自定义tabbar 未隐藏导致有2个 tabBar 的问题; 注意支付宝很特别，需要在 onMounted 钩子调用
  customTabbarEnable // 另外，支付宝里面，只要是 customTabbar 都需要隐藏
  && uni.hideTabBar({
    fail(err) {
      console.log('hideTabBar fail: ', err)
    },
    success(res) {
      // console.log('hideTabBar success: ', res)
    },
  })
})
// #endif
const activeColor = 'var(--ephone-primary-soft, #f2b6cc)'
const inactiveColor = 'rgba(255,255,255,0.42)'
function getColorByIndex(index: number) {
  return tabbarStore.curIdx === index ? activeColor : inactiveColor
}
</script>

<template>
  <view v-if="customTabbarEnable" class="h-62px pb-safe">
    <view class="border-and-fixed" @touchmove.stop.prevent>
      <view class="tabbar-inner">
        <view
          v-for="(item, index) in tabbarList" :key="index"
          class="tabbar-cell"
          :class="{ 'tabbar-cell-active': tabbarStore.curIdx === index }"
          :style="{ color: getColorByIndex(index) }"
          @click="handleClick(index)"
        >
          <view v-if="item.isBulge" class="relative">
            <!-- 中间一个鼓包tabbarItem的处理 -->
            <view class="bulge">
              <TabbarItem :item="item" :index="index" class="text-center" is-bulge />
            </view>
          </view>
          <TabbarItem v-else :item="item" :index="index" class="relative text-center" />
        </view>
      </view>

      <view class="pb-safe" />
    </view>
  </view>
</template>

<style scoped lang="scss">
.border-and-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  box-sizing: border-box;
  overflow: hidden;
  border-top: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 30rpx 30rpx 0 0;
  background: rgba(0, 0, 0, 0.55);
  -webkit-backdrop-filter: blur(24rpx) saturate(1.2);
  backdrop-filter: blur(24rpx) saturate(1.2);
  box-shadow: 0 -8rpx 30rpx rgba(0, 0, 0, 0.5);
}

.tabbar-inner {
  display: flex;
  align-items: center;
  min-height: 124rpx;
  padding: 6rpx 20rpx 0;
}

.tabbar-cell {
  position: relative;
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 96rpx;
  transition:
    color 0.2s ease,
    transform 0.2s ease;
}

.tabbar-cell-active {
  transform: translateY(-2rpx);
  text-shadow: none;
}

// 中间鼓包的样式
.bulge {
  position: absolute;
  top: -20px;
  left: 50%;
  transform-origin: top center;
  transform: translateX(-50%) scale(0.5) translateY(-33%);
  display: flex;
  justify-content: center;
  align-items: center;
  width: 250rpx;
  height: 250rpx;
  border-radius: 50%;
  background-color: #fff;
  box-shadow: inset 0 0 0 1px #fefefe;

  &:active {
    // opacity: 0.8;
  }
}
</style>
