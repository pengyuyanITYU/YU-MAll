import { del, get, post, put } from '@/utils/request';
import type { AjaxResult, TableData } from '@/types/http';
import type { ShopModel } from '@/types/domain';

export function listShops(params: Record<string, unknown>) {
  return get<TableData<ShopModel>>('/admin/shops/list', { params });
}

export function listShopSimple() {
  return get<AjaxResult<ShopModel[]>>('/admin/shops/simple');
}

export function getShopDetail(id: number | string) {
  return get<AjaxResult<ShopModel>>(`/admin/shops/${id}`);
}

export function createShop(data: Record<string, unknown>) {
  return post<AjaxResult<void>>('/admin/shops', data);
}

export function updateShop(data: Record<string, unknown>) {
  return put<AjaxResult<void>>('/admin/shops', data);
}

export function deleteShop(id: number | string) {
  return del<AjaxResult<void>>(`/admin/shops/${id}`);
}
