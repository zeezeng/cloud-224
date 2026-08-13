/**
 * 下拉刷新限流 Hook。
 * 在 intervalMs 时间窗口内只允许触发一次刷新，防止频繁下拉请求。
 */
export interface RefreshLimit {
  /**
   * 尝试刷新：允许则返回 true 并记录时间；被限流返回 false。
   */
  tryRefresh: () => boolean
}

export function useRefreshLimit(intervalMs = 5000): RefreshLimit {
  let lastRefreshAt = 0

  return {
    tryRefresh() {
      const now = Date.now()
      if (now - lastRefreshAt < intervalMs) {
        return false
      }
      lastRefreshAt = now
      return true
    },
  }
}
