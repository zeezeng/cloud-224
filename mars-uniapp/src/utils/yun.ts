export function formatCompactNumber(value: number) {
  return value.toLocaleString('zh-CN')
}

export function formatMoney(value: number) {
  return value.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

export function formatSignedMoney(value: number) {
  if (value === 0) {
    return formatMoney(0)
  }

  const sign = value > 0 ? '+' : '-'
  return `${sign}${formatMoney(Math.abs(value))}`
}

export function formatIntegerMoney(value: number) {
  return Math.trunc(value).toLocaleString('zh-CN')
}

export function formatSignedIntegerMoney(value: number) {
  if (value === 0) {
    return formatIntegerMoney(0)
  }

  const sign = value > 0 ? '+' : '-'
  return `${sign}${formatIntegerMoney(Math.abs(value))}`
}

export function formatClockTime(value?: string | Date) {
  const pad = (num: number) => String(num).padStart(2, '0')

  if (!value) {
    return ''
  }

  if (value instanceof Date) {
    return `${pad(value.getHours())}:${pad(value.getMinutes())}:${pad(value.getSeconds())}`
  }

  const text = String(value).trim()
  if (!text) {
    return ''
  }

  const match = text.match(/(\d{2}):(\d{2})(?::(\d{2}))?/)
  if (match) {
    return `${match[1]}:${match[2]}:${match[3] || '00'}`
  }

  const parsed = new Date(text.replace(/-/g, '/'))
  if (!Number.isNaN(parsed.getTime())) {
    return `${pad(parsed.getHours())}:${pad(parsed.getMinutes())}:${pad(parsed.getSeconds())}`
  }

  return text
}

export function formatFetchTime(date = new Date()) {
  return formatClockTime(date)
}

export function getRankTone(rank: number) {
  if (rank === 1) {
    return 'gold'
  }
  if (rank === 2) {
    return 'purple'
  }
  if (rank === 3) {
    return 'coral'
  }
  return 'dark'
}
