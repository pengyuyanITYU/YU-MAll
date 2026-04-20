const ORDER_FLOW_HISTORY_KEY = 'YU_MALL_ORDER_FLOW_HISTORY_GUARD'
const DEFAULT_ORDER_FLOW_REDIRECT = '/user?tab=orders'
const BLOCKED_BACK_PATHS = new Set(['/cart', '/checkout'])

interface OrderFlowHistoryState {
  redirect: string
  createdAt: number
}

const canUseSessionStorage = () => typeof window !== 'undefined'

const readOrderFlowHistoryState = (): OrderFlowHistoryState | null => {
  if (!canUseSessionStorage()) {
    return null
  }
  const raw = window.sessionStorage.getItem(ORDER_FLOW_HISTORY_KEY)
  if (!raw) {
    return null
  }
  try {
    const parsed = JSON.parse(raw) as OrderFlowHistoryState
    if (!parsed?.redirect) {
      return null
    }
    return parsed
  } catch (error) {
    window.sessionStorage.removeItem(ORDER_FLOW_HISTORY_KEY)
    return null
  }
}

export const markOrderFlowHistoryGuard = (redirect = DEFAULT_ORDER_FLOW_REDIRECT) => {
  if (!canUseSessionStorage()) {
    return
  }
  const state: OrderFlowHistoryState = {
    redirect,
    createdAt: Date.now()
  }
  window.sessionStorage.setItem(ORDER_FLOW_HISTORY_KEY, JSON.stringify(state))
}

export const clearOrderFlowHistoryGuard = () => {
  if (!canUseSessionStorage()) {
    return
  }
  window.sessionStorage.removeItem(ORDER_FLOW_HISTORY_KEY)
}

export const getOrderFlowHistoryRedirect = () => {
  return readOrderFlowHistoryState()?.redirect || DEFAULT_ORDER_FLOW_REDIRECT
}

export const shouldBlockOrderFlowBack = (path: string) => {
  if (!BLOCKED_BACK_PATHS.has(path)) {
    return false
  }
  return Boolean(readOrderFlowHistoryState())
}
