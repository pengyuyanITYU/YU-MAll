import { request, type UserRequestConfig } from '@/utils/request'

export interface LoginForm {
  username: string
  password: string
  rememberMe: boolean
  captchaTicket?: string
}

export const login = (loginForm: LoginForm) =>
  request.post('/users/login', loginForm, {
    errorMeta: {
      silent: true,
      preserveBusinessMessage: true
    }
  } as UserRequestConfig)
