import { ref, shallowRef, watch } from 'vue'
import {
  createSliderChallenge,
  type SliderCaptchaChallengeResponse,
  type SliderCaptchaScene,
  type SliderCaptchaVerifyRequest,
  verifySliderChallenge
} from '@/api/captcha'

type ResolveCaptcha = (ticket: string) => void
type RejectCaptcha = (reason?: unknown) => void

function createCancelledError() {
  return Object.assign(new Error('captcha cancelled'), { cancelled: true })
}

function resolveCaptchaErrorMessage(error: any) {
  if (error?.code === 429) {
    return '操作过于频繁，请稍后再试'
  }
  return error?.msg || error?.message || '验证失败，请重试'
}

export function useSliderCaptcha() {
  const captchaVisible = ref(false)
  const captchaLoading = ref(false)
  const captchaVerifying = ref(false)
  const captchaErrorMessage = ref('')
  const captchaChallenge = shallowRef<SliderCaptchaChallengeResponse | null>(null)
  const currentScene = ref<SliderCaptchaScene>('LOGIN')

  let resolveCaptcha: ResolveCaptcha | null = null
  let rejectCaptcha: RejectCaptcha | null = null

  const clearPending = () => {
    resolveCaptcha = null
    rejectCaptcha = null
  }

  const rejectPending = (reason: unknown) => {
    if (rejectCaptcha) {
      rejectCaptcha(reason)
    }
    clearPending()
  }

  const resetState = () => {
    captchaChallenge.value = null
    captchaLoading.value = false
    captchaVerifying.value = false
    captchaErrorMessage.value = ''
  }

  const loadChallenge = async (clearError = true) => {
    captchaLoading.value = true
    if (clearError) {
      captchaErrorMessage.value = ''
    }
    try {
      const response: any = await createSliderChallenge({ scene: currentScene.value })
      captchaChallenge.value = response?.data ?? response
      return captchaChallenge.value
    } finally {
      captchaLoading.value = false
    }
  }

  const openCaptcha = async (scene: SliderCaptchaScene) => {
    rejectPending(createCancelledError())
    currentScene.value = scene
    captchaVisible.value = true
    try {
      await loadChallenge()
    } catch (error) {
      captchaVisible.value = false
      throw error
    }

    return new Promise<string>((resolve, reject) => {
      resolveCaptcha = resolve
      rejectCaptcha = reject
    })
  }

  const closeCaptcha = () => {
    captchaVisible.value = false
  }

  const refreshCaptcha = async () => {
    captchaErrorMessage.value = ''
    await loadChallenge()
  }

  const submitCaptcha = async (payload: SliderCaptchaVerifyRequest) => {
    captchaVerifying.value = true
    captchaErrorMessage.value = ''
    try {
      const response: any = await verifySliderChallenge(payload)
      const result = response?.data ?? response
      const ticket = result?.captchaTicket

      if (!ticket) {
        throw new Error('验证码校验失败')
      }

      if (resolveCaptcha) {
        resolveCaptcha(ticket)
      }
      clearPending()
      captchaVisible.value = false
      resetState()
      return ticket
    } catch (error) {
      captchaErrorMessage.value = resolveCaptchaErrorMessage(error)
      await loadChallenge(false).catch(() => null)
      throw error
    } finally {
      captchaVerifying.value = false
    }
  }

  watch(captchaVisible, (visible, previous) => {
    if (!visible && previous) {
      resetState()
      rejectPending(createCancelledError())
    }
  })

  return {
    captchaVisible,
    captchaChallenge,
    captchaLoading,
    captchaVerifying,
    captchaErrorMessage,
    openCaptcha,
    closeCaptcha,
    refreshCaptcha,
    submitCaptcha
  }
}
