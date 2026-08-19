<script setup lang="ts">
import type { FeedbackContact, FeedbackType } from '@/api/feedback'
import { getFeedbackContact, resolveFeedbackImageUrl, submitFeedback } from '@/api/feedback'
import YunTransparentNav from '@/components/yun/YunTransparentNav.vue'

defineOptions({
  name: 'Feedback',
})

definePage({
  style: {
    navigationStyle: 'custom',
    disableScroll: true,
    navigationBarTitleText: '意见反馈',
    backgroundColor: '#000000',
  },
})

const feedbackTypes: Array<{ label: string, value: FeedbackType }> = [
  { label: '想法建议', value: 1 },
  { label: 'Bug问题', value: 2 },
  { label: '内容错误', value: 3 },
  { label: '其他', value: 4 },
]

const form = reactive({
  feedbackType: 1 as FeedbackType,
  content: '',
})
const submitting = ref(false)
const contact = ref<FeedbackContact>({})
const navStyle = ref<Record<string, string>>({})

const contentLength = computed(() => form.content.trim().length)
const qrcodeUrl = computed(() => resolveFeedbackImageUrl(contact.value.qrcodeUrl))
const hasContact = computed(() => !!contact.value.wechatId || !!qrcodeUrl.value || !!contact.value.remark)

onMounted(() => {
  loadContact()
})

function handleNavLayout(style: Record<string, string>) {
  navStyle.value = style
}

async function loadContact() {
  try {
    contact.value = await getFeedbackContact()
  }
  catch (error) {
    console.error('反馈联系方式加载失败', error)
    contact.value = {}
  }
}

function getCurrentPagePath() {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  return current?.route ? `/${current.route}` : '/pages/feedback/feedback'
}

function getClientInfo() {
  try {
    const system = uni.getSystemInfoSync()
    return JSON.stringify({
      platform: system.platform,
      system: system.system,
      model: system.model,
      brand: system.brand,
      appVersion: system.appVersion,
    })
  }
  catch {
    return ''
  }
}

function validateForm() {
  const content = form.content.trim()
  if (content.length < 5) {
    uni.showToast({
      icon: 'none',
      title: '请至少输入5个字',
    })
    return false
  }
  if (content.length > 1000) {
    uni.showToast({
      icon: 'none',
      title: '反馈内容不能超过1000字',
    })
    return false
  }
  return true
}

async function handleSubmit() {
  if (submitting.value || !validateForm()) {
    return
  }
  submitting.value = true
  try {
    await submitFeedback({
      feedbackType: form.feedbackType,
      content: form.content.trim(),
      pagePath: getCurrentPagePath(),
      clientInfo: getClientInfo(),
    })
    uni.showToast({
      icon: 'success',
      title: '已收到反馈',
    })
    form.content = ''
  }
  finally {
    submitting.value = false
  }
}

function copyWechatId() {
  const wechatId = String(contact.value.wechatId || '').trim()
  if (!wechatId) {
    return
  }
  uni.setClipboardData({
    data: wechatId,
    success: () => {
      uni.showToast({
        icon: 'none',
        title: '微信号已复制',
      })
    },
  })
}

function previewQrcode() {
  if (!qrcodeUrl.value) {
    return
  }
  uni.previewImage({
    urls: [qrcodeUrl.value],
    current: qrcodeUrl.value,
  })
}
</script>

<template>
  <view class="feedback-page" :style="navStyle">
    <YunTransparentNav
      title="意见反馈"
      fallback-url="/pages/index/index"
      fallback-type="switchTab"
      @layout="handleNavLayout"
    />

    <scroll-view class="feedback-scroll" scroll-y :show-scrollbar="false">
      <view class="feedback-content">
        <view class="feedback-card">
          <view class="type-tabs">
            <view
              v-for="item in feedbackTypes"
              :key="item.value"
              class="type-tab"
              :class="{ 'type-tab-active': form.feedbackType === item.value }"
              @tap="form.feedbackType = item.value"
            >
              {{ item.label }}
            </view>
          </view>

          <textarea
            v-model="form.content"
            class="feedback-textarea"
            :maxlength="1000"
            auto-height
            placeholder="写下你遇到的问题，或想要的新功能"
            placeholder-class="feedback-placeholder"
          />

          <view class="form-foot">
            <text :class="{ 'content-count-warn': contentLength > 900 }">{{ contentLength }}/1000</text>
            <text>匿名提交</text>
          </view>
        </view>

        <view v-if="hasContact" class="contact-card">
          <view class="contact-main">
            <view class="contact-title">
              微信联系
            </view>
            <view v-if="contact.remark" class="contact-remark">
              {{ contact.remark }}
            </view>
            <view v-if="contact.wechatId" class="wechat-id" @tap="copyWechatId">
              {{ contact.wechatId }}
              <text>复制</text>
            </view>
          </view>
          <image
            v-if="qrcodeUrl"
            class="qrcode-image"
            :src="qrcodeUrl"
            mode="aspectFill"
            @tap="previewQrcode"
          />
        </view>
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <button class="submit-button" :disabled="submitting" @tap="handleSubmit">
        <view v-if="submitting" class="i-carbon-circle-dash submit-icon submit-icon-loading" />
        <view v-else class="i-carbon-send submit-icon" />
        {{ submitting ? '提交中' : '提交反馈' }}
      </button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.feedback-page {
  position: relative;
  height: 100vh;
  overflow: hidden;
  background: #0b0a0f;
  color: var(--ephone-text);
}

.feedback-scroll {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
}

.feedback-content {
  box-sizing: border-box;
  max-width: 960rpx;
  min-height: 100%;
  margin: 0 auto;
  padding: calc(
      var(--ephone-transparent-nav-top, env(safe-area-inset-top)) + var(--ephone-transparent-nav-height, 88rpx) + 34rpx
    )
    36rpx 190rpx;
}

.feedback-card {
  box-sizing: border-box;
  width: 100%;
  margin-top: 34rpx;
  padding: 18rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.045);
}

.type-tabs {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8rpx;
  padding: 6rpx;
  border-radius: 22rpx;
  background: rgba(0, 0, 0, 0.2);
}

.type-tab {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 64rpx;
  border-radius: 18rpx;
  color: rgba(255, 255, 255, 0.52);
  font-size: 23rpx;
  font-weight: 800;
  transition: opacity 0.2s ease, background 0.2s ease;
}

.type-tab-active {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.feedback-textarea {
  box-sizing: border-box;
  min-height: 330rpx;
  width: 100%;
  margin-top: 18rpx;
  padding: 24rpx 22rpx;
  border: 0;
  border-radius: 24rpx;
  background: rgba(0, 0, 0, 0.18);
  color: rgba(255, 255, 255, 0.92);
  font-size: 28rpx;
  line-height: 1.65;
}

.feedback-placeholder {
  color: rgba(255, 255, 255, 0.34);
}

.form-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14rpx 4rpx 2rpx;
  color: rgba(255, 255, 255, 0.42);
  font-size: 22rpx;
}

.content-count-warn {
  color: var(--ephone-primary-soft);
}

.contact-card {
  display: flex;
  align-items: center;
  gap: 18rpx;
  box-sizing: border-box;
  width: 100%;
  margin-top: 18rpx;
  padding: 18rpx 20rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.07);
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.035);
}

.contact-main {
  flex: 1;
  min-width: 0;
}

.contact-title {
  color: rgba(255, 255, 255, 0.86);
  font-size: 26rpx;
  font-weight: 850;
}

.contact-remark {
  margin-top: 6rpx;
  color: rgba(255, 255, 255, 0.5);
  font-size: 23rpx;
  line-height: 1.5;
  word-break: break-all;
}

.wechat-id {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  margin-top: 12rpx;
  color: var(--ephone-primary-soft);
  font-size: 26rpx;
  font-weight: 850;
  overflow-wrap: anywhere;
}

.wechat-id text {
  flex: 0 0 auto;
  margin-left: 14rpx;
  padding: 5rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(233, 138, 182, 0.12);
  font-size: 20rpx;
}

.qrcode-image {
  flex: 0 0 auto;
  width: 118rpx;
  height: 118rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.06);
}

.bottom-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 20;
  box-sizing: border-box;
  padding: 18rpx 36rpx calc(env(safe-area-inset-bottom) + 22rpx);
  background: linear-gradient(180deg, rgba(11, 10, 15, 0), rgba(11, 10, 15, 0.96) 32%);
}

.submit-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  width: 100%;
  height: 88rpx;
  border: 0;
  border-radius: 999rpx;
  background: var(--ephone-primary-soft);
  color: #1a1a1a;
  font-size: 29rpx;
  font-weight: 900;
}

.submit-button[disabled] {
  opacity: 0.62;
}

.submit-icon {
  font-size: 34rpx;
}

.submit-icon-loading {
  animation: spin 0.9s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
