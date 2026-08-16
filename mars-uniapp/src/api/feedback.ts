import { httpGet, httpPost } from '@/http/http'
import { getEnvBaseUrl } from '@/utils'

export type FeedbackType = 1 | 2 | 3 | 4

export interface FeedbackSubmitPayload {
  feedbackType: FeedbackType
  content: string
  pagePath?: string
  clientInfo?: string
}

export interface FeedbackContact {
  wechatId?: string
  qrcodeUrl?: string
  remark?: string
}

export function submitFeedback(data: FeedbackSubmitPayload) {
  return httpPost<number>(
    '/app/feedback',
    data,
    undefined,
    undefined,
    { skipAuth: true },
  )
}

export function getFeedbackContact() {
  return httpGet<FeedbackContact>(
    '/app/feedback/contact',
    undefined,
    undefined,
    { skipAuth: true, hideErrorToast: true },
  )
}

export function resolveFeedbackImageUrl(url?: string) {
  const value = String(url || '').trim()
  if (!value) {
    return ''
  }
  if (/^(https?:)?\/\//i.test(value) || /^(data|blob):/i.test(value)) {
    return value.startsWith('//') ? `https:${value}` : value
  }
  if (/^\/?static\//.test(value)) {
    return value.startsWith('/') ? value : `/${value}`
  }

  const baseUrl = getEnvBaseUrl()
  if (!baseUrl) {
    return value
  }

  const base = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
  const path = value.startsWith('/') ? value : `/${value}`
  if (/\/api$/i.test(base) && /^\/api\//i.test(path)) {
    return `${base.slice(0, -4)}${path}`
  }

  return `${base}${path}`
}
