import { httpGet } from '@/http/http'

export interface HomeNotice {
  id?: number
  title?: string
  content?: string
  contentPreview?: string
  sort?: number
  status?: number
  publishedAt?: string
}

export function getHomeNoticeList(limit = 5) {
  return httpGet<HomeNotice[]>(
    '/app/notice/list',
    { limit },
    undefined,
    { skipAuth: true, hideErrorToast: true },
  )
}
