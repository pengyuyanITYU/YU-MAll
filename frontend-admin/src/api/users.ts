import { get, put } from '@/utils/request';
import type { AjaxResult, TableData } from '@/types/http';
import type { UserModel } from '@/types/domain';

export function listUsers(params: Record<string, unknown>) {
  return get<TableData<UserModel>>('/admin/users', { params });
}

export function getUserDetail(id: number | string) {
  return get<AjaxResult<UserModel>>(`/admin/users/${id}`);
}

export function updateUserStatus(id: number | string, status: number) {
  return put<AjaxResult<void>>(`/admin/users/${id}/${status}`);
}