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

export function formatFetchTime(date = new Date()) {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
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
