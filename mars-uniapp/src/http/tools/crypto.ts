import CryptoJS from 'crypto-js'
import { getEnvBaseUrl } from '@/utils'

export interface CryptoConfig {
  enabled: boolean
  publicKey: string
  aesKey: string
}

interface DecryptResult<T = unknown> {
  data: T
  encrypted: boolean
  decrypted: boolean
}

const CRYPTO_CONFIG_PATH = '/crypto/config'

let cryptoConfigCache: CryptoConfig | null = null
let cryptoConfigPromise: Promise<CryptoConfig> | null = null

function joinUrl(baseUrl: string, path: string) {
  if (baseUrl.endsWith('/') && path.startsWith('/'))
    return `${baseUrl.slice(0, -1)}${path}`
  if (!baseUrl.endsWith('/') && !path.startsWith('/'))
    return `${baseUrl}/${path}`
  return `${baseUrl}${path}`
}

export function clearCryptoConfigCache() {
  cryptoConfigCache = null
}

export function isAesEncryptedData(data: unknown) {
  if (typeof data !== 'string')
    return false

  const parts = data.split('.')
  if (parts.length !== 2)
    return false

  const base64Regex = /^[A-Za-z0-9+/]+=*$/
  return parts[0].length === 16 && parts[1].length > 10 && base64Regex.test(parts[0]) && base64Regex.test(parts[1])
}

function parseDecryptedValue(value: string) {
  try {
    return JSON.parse(value)
  }
  catch {
    return value
  }
}

async function fetchCryptoConfig(forceRefresh = false): Promise<CryptoConfig> {
  if (!forceRefresh && cryptoConfigCache)
    return cryptoConfigCache

  if (!forceRefresh && cryptoConfigPromise)
    return cryptoConfigPromise

  const baseUrl = getEnvBaseUrl()
  cryptoConfigPromise = new Promise<CryptoConfig>((resolve) => {
    uni.request({
      url: joinUrl(baseUrl, CRYPTO_CONFIG_PATH),
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
      },
      success: (res) => {
        const data = res.data as any
        if (res.statusCode === 200 && data?.code === 200 && data?.data) {
          cryptoConfigCache = data.data as CryptoConfig
          resolve(cryptoConfigCache)
          return
        }

        const fallback = { enabled: false, publicKey: '', aesKey: '' }
        cryptoConfigCache = fallback
        resolve(fallback)
      },
      fail: () => {
        const fallback = { enabled: false, publicKey: '', aesKey: '' }
        cryptoConfigCache = fallback
        resolve(fallback)
      },
      complete: () => {
        cryptoConfigPromise = null
      },
    })
  })

  return cryptoConfigPromise
}

function decryptAesGcmByCtr(encryptedData: string, aesKeyBase64: string) {
  const parts = encryptedData.split('.')
  if (parts.length !== 2)
    throw new Error('加密数据格式错误')

  const iv = CryptoJS.enc.Base64.parse(parts[0])
  const data = CryptoJS.enc.Base64.parse(parts[1])
  const key = CryptoJS.enc.Base64.parse(aesKeyBase64)

  if (data.sigBytes <= 16)
    throw new Error('加密数据长度错误')

  const ciphertextSigBytes = data.sigBytes - 16
  const ciphertext = data.clone()
  ciphertext.sigBytes = ciphertextSigBytes
  ciphertext.clamp()

  const counterWords = iv.words.slice(0, 3)
  counterWords.push(2)
  const counter = CryptoJS.lib.WordArray.create(counterWords, 16)
  const cipherParams = CryptoJS.lib.CipherParams.create({ ciphertext })

  const decrypted = CryptoJS.AES.decrypt(cipherParams, key, {
    iv: counter,
    mode: CryptoJS.mode.CTR,
    padding: CryptoJS.pad.NoPadding,
  })

  return decrypted.toString(CryptoJS.enc.Utf8)
}

export async function decryptResponseData<T = unknown>(data: T): Promise<DecryptResult<T>> {
  if (!isAesEncryptedData(data))
    return { data, encrypted: false, decrypted: false }

  const tryDecrypt = async (forceRefresh = false) => {
    const config = await fetchCryptoConfig(forceRefresh)
    if (!config.enabled || !config.aesKey)
      return null

    const decryptedStr = decryptAesGcmByCtr(data as unknown as string, config.aesKey)
    return parseDecryptedValue(decryptedStr) as T
  }

  try {
    const decryptedData = await tryDecrypt(false)
    if (decryptedData !== null)
      return { data: decryptedData, encrypted: true, decrypted: true }
  }
  catch {
    clearCryptoConfigCache()
  }

  try {
    const decryptedData = await tryDecrypt(true)
    if (decryptedData !== null)
      return { data: decryptedData, encrypted: true, decrypted: true }
  }
  catch {
    clearCryptoConfigCache()
  }

  return { data, encrypted: true, decrypted: false }
}
