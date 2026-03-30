import { del, get, post, put } from '@/utils/request';
import type { AjaxResult, TableData } from '@/types/http';
import type { ItemDashboard, ItemModel } from '@/types/domain';

export function listItems(params: Record<string, unknown>) {
  return get<TableData<ItemModel>>('/admin/items/list', { params });
}

export function getItemDetail(id: number | string) {
  return get<AjaxResult<any>>(`/admin/items/${id}`);
}

export function createItem(data: any) {
  return post<AjaxResult<void>>('/admin/items', data);
}

export function updateItem(data: any) {
  return put<AjaxResult<void>>('/admin/items', data);
}

export function deleteItem(id: number | string) {
  return del<AjaxResult<void>>(`/admin/items/${id}`);
}

export function getItemDashboard() {
  return get<AjaxResult<ItemDashboard>>('/admin/items/dashboard');
}