import { del, get } from '@/utils/request';
import type { AjaxResult, TableData } from '@/types/http';
import type { OrderModel, RecentOrder } from '@/types/domain';

export function listOrders(params: Record<string, unknown>) {
  return get<TableData<OrderModel>>('/admin/orders/list', { params });
}

export function getOrderDetail(id: number | string) {
  return get<AjaxResult<OrderModel>>(`/admin/orders/${id}`);
}

export function deleteOrder(id: number | string) {
  return del<AjaxResult<void>>(`/admin/orders/${id}`);
}

export function listRecentOrders() {
  return get<AjaxResult<RecentOrder[]>>('/admin/orders/recent');
}