import { post } from '@/utils/request';
import type { AjaxResult } from '@/types/http';
import type { LoginForm, LoginResult } from '@/types/domain';

export function login(data: LoginForm) {
  return post<AjaxResult<LoginResult>>('/users/login', data);
}