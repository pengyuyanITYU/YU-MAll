import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

interface AjaxPayload<T = unknown> {
  code: number
  msg: string
  data?: T
  rows?: unknown[]
  total?: number
}

interface UserErrorMeta {
  silent?: boolean
  preserveBusinessMessage?: boolean
}

export interface UserRequestConfig extends AxiosRequestConfig {
  errorMeta?: UserErrorMeta
}

export interface UserRequestError<T = unknown> extends AjaxPayload<T> {
  status: number
  handled: boolean
  authExpired: boolean
  serviceKey: string
  userMessage: string
}

let isRelogging = false
let lastErrorMessage = ''
let lastErrorAt = 0

const MESSAGE_DEDUP_WINDOW = 1200
const TECHNICAL_MESSAGE_PATTERN =
  /exception|error|sql|mybatis|jdbc|redis|gateway|feign|trace|stack|org\.|java\.|nullpointer|syntax|<html|<!doctype/i

const SERVICE_MESSAGE_MAP: Record<string, string> = {
  items: '当前内容加载失败，请稍后再试',
  shops: '当前内容加载失败，请稍后再试',
  search: '当前内容加载失败，请稍后再试',
  carts: '购物车服务开小差了，请稍后再试',
  orders: '订单服务开小差了，请稍后再试',
  'pay-orders': '支付服务暂时不可用，请稍后再试',
  collects: '收藏服务开小差了，请稍后再试',
  comments: '评论服务开小差了，请稍后再试',
  upload: '上传失败，请稍后重试',
  ai: 'AI 服务暂时不可用，请稍后再试',
  users: '账户服务开小差了，请稍后再试',
  addresses: '账户服务开小差了，请稍后再试',
  members: '账户服务开小差了，请稍后再试'
}

const unwrapAjaxResult = (payload: unknown): AjaxPayload | null => {
  if (payload && typeof payload === 'object' && 'code' in payload && 'msg' in payload) {
    return payload as AjaxPayload
  }
  return null
}

const sanitizeSimpleMessage = (message: unknown, fallback: string) => {
  if (typeof message !== 'string') {
    return fallback
  }
  const normalized = message.replace(/\s+/g, ' ').trim()
  if (!normalized || TECHNICAL_MESSAGE_PATTERN.test(normalized)) {
    return fallback
  }
  return normalized
}

const resolveServiceKey = (url?: string) => {
  const segments = (url || '')
    .split('?')[0]
    .split('/')
    .filter(Boolean)
  return segments[0] || 'unknown'
}

const shouldPreserveBusinessMessage = (config: UserRequestConfig) => Boolean(config.errorMeta?.preserveBusinessMessage)

const resolveFriendlyMessage = (config: UserRequestConfig, payload?: AjaxPayload | null, status?: number, rawMessage?: unknown) => {
  const authExpired = (payload?.code || status) === 401 || status === 401
  if (authExpired) {
    return '登录已过期，请重新登录'
  }

  if (shouldPreserveBusinessMessage(config)) {
    const fallback = config.url?.includes('/register') ? '注册失败' : '登录失败'
    return sanitizeSimpleMessage(payload?.msg ?? rawMessage, fallback)
  }

  const serviceKey = resolveServiceKey(config.url)
  return SERVICE_MESSAGE_MAP[serviceKey] || '页面出了点问题，请稍后再试'
}

const normalizeUserError = (config: UserRequestConfig, payload?: AjaxPayload | null, status?: number, rawMessage?: unknown) => {
  const code = Number(payload?.code || status || 500)
  const authExpired = code === 401 || status === 401
  return {
    code,
    msg: payload?.msg || '',
    data: (payload?.data ?? null) as unknown,
    status: status || code || 500,
    handled: false,
    authExpired,
    serviceKey: resolveServiceKey(config.url),
    userMessage: resolveFriendlyMessage(config, payload, status, rawMessage)
  } as UserRequestError
}

const showDedupedMessage = (message: string) => {
  const now = Date.now()
  if (message === lastErrorMessage && now - lastErrorAt < MESSAGE_DEDUP_WINDOW) {
    return
  }
  lastErrorMessage = message
  lastErrorAt = now
  ElMessage.error(message)
}

const handleAuthExpired = () => {
  if (window.location.pathname.includes('/login') || isRelogging) {
    return
  }

  isRelogging = true
  localStorage.removeItem('Authorization')
  showDedupedMessage('登录已过期，请重新登录')

  setTimeout(() => {
    window.location.href = '/login'
  }, 800)
}

const notifyUserError = (error: UserRequestError, config: UserRequestConfig) => {
  if (config.errorMeta?.silent) {
    return
  }
  error.handled = true
  if (error.authExpired) {
    handleAuthExpired()
    return
  }
  showDedupedMessage(error.userMessage)
}

export const request = axios.create({
  baseURL: '/api',
  timeout: 60000
})

request.interceptors.request.use(
  (config) => {
    const loginUser = localStorage.getItem('Authorization')
    if (loginUser) {
      config.headers.Authorization = loginUser
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const config = (response.config || {}) as UserRequestConfig
    const data = unwrapAjaxResult(response.data)
    if (data && Number(data.code) !== 200) {
      const normalized = normalizeUserError(config, data, response.status)
      notifyUserError(normalized, config)
      return Promise.reject(normalized)
    }
    return response.data
  },
  (error) => {
    const config = ((error?.config || {}) as UserRequestConfig)
    const payload = unwrapAjaxResult(error?.response?.data)
    const normalized = normalizeUserError(config, payload, error?.response?.status, error?.message)
    notifyUserError(normalized, config)
    return Promise.reject(normalized)
  }
)

export const isHandledRequestError = (error: unknown) => Boolean((error as Partial<UserRequestError>)?.handled)

export const resolveSimpleAuthErrorMessage = (error: unknown, fallback: string) =>
  sanitizeSimpleMessage((error as Partial<UserRequestError>)?.msg || (error as Error)?.message, fallback)
