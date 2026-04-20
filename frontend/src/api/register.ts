import { request, type UserRequestConfig } from '@/utils/request'

export interface RegisterForm {
  username: string
  phone: string
  password: string
  avatar: string
  nickName: string
  captchaTicket: string
}

export const register = (registerForm: RegisterForm) =>
  request.post('/users/register', registerForm, {
    errorMeta: {
      silent: true,
      preserveBusinessMessage: true
    }
  } as UserRequestConfig)
