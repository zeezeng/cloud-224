import { httpGet } from '@/http/http'

export interface HomeNotice {
  id?: number
  title?: string
  content?: string
  contentPreview?: string
  noticeType?: number
  sort?: number
  status?: number
  publishedAt?: string
  validFrom?: string
  validTo?: string
}

/** 首页跑马灯公告列表 */
export function getHomeNoticeList(limit = 5) {
  return httpGet<HomeNotice[]>(
    '/app/notice/list',
    { limit },
    undefined,
    { skipAuth: true, hideErrorToast: true },
  )
}

/** 当前有效的弹窗公告列表 */
export function getPopupNoticeList() {
  return httpGet<HomeNotice[]>(
    '/app/notice/popup',
    undefined,
    undefined,
    { skipAuth: true, hideErrorToast: true },
  )
}
