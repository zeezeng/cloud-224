import { httpGet } from '@/http/http'
import { getEnvBaseUrl } from '@/utils'

export type BannerJumpType = 0 | 1 | 2

export interface HomeBanner {
  id?: number
  title?: string
  description?: string
  imageUrl?: string
  jumpType?: BannerJumpType
  jumpTarget?: string
  sort?: number
  status?: number
}

export function getHomeBannerList() {
  return httpGet<HomeBanner[]>(
    '/app/banner/list',
    undefined,
    undefined,
    { skipAuth: true, hideErrorToast: true },
  )
}

export function resolveBannerImageUrl(url?: string) {
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
