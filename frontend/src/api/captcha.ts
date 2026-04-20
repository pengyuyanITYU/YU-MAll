import { request } from '@/utils/request'

export type SliderCaptchaScene = 'LOGIN' | 'REGISTER'

export interface SliderCaptchaChallengeRequest {
  scene: SliderCaptchaScene
}

export interface SliderCaptchaChallengeResponse {
  captchaId: string
  renderMode: 'track'
  trackWidth: number
  handleWidth: number
  targetLeft: number
  targetWidth: number
  backgroundImage?: string | null
  sliderImage?: string | null
  expiresAt: number
}

export interface SliderCaptchaTrackPoint {
  x: number
  y: number
  t: number
}

export interface SliderCaptchaVerifyRequest {
  captchaId: string
  offsetX: number
  dragTrack: SliderCaptchaTrackPoint[]
}

export interface SliderCaptchaVerifyResponse {
  captchaTicket: string
  expiresAt: number
}

export const createSliderChallenge = (payload: SliderCaptchaChallengeRequest) =>
  request.post('/users/captcha/slider/challenge', payload)

export const verifySliderChallenge = (payload: SliderCaptchaVerifyRequest) =>
  request.post('/users/captcha/slider/verify', payload)
