import { post } from '@/utils/request';
import type { AjaxResult } from '@/types/http';
import type { LoginForm, LoginResult, RegisterForm } from '@/types/domain';

export function login(data: LoginForm) {
  return post<AjaxResult<LoginResult>>('/admins/login', data, {
    errorMeta: {
      silent: true,
      action: '登录'
    }
  });
}

export function register(data: RegisterForm) {
  return post<AjaxResult<LoginResult>>('/admins/register', data, {
    errorMeta: {
      silent: true,
      action: '注册'
    }
  });
}
