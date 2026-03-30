import { get } from '@/utils/request';
import type { AjaxResult, TableData } from '@/types/http';
import type { ItemDashboard, ItemModel, RecentOrder, UserModel, OrderModel } from '@/types/domain';

export function fetchItemDashboard() {
  return get<AjaxResult<ItemDashboard>>('/admin/items/dashboard');
}

export function fetchRecentOrders() {
  return get<AjaxResult<RecentOrder[]>>('/admin/orders/recent');
}

export function fetchUserOverview() {
  return get<TableData<UserModel>>('/admin/users', {
    params: { pageNo: 1, pageSize: 1 }
  });
}

export function fetchItemOverview() {
  return get<TableData<ItemModel>>('/admin/items/list', {
    params: { pageNo: 1, pageSize: 1 }
  });
}

export function fetchOrderOverview() {
  return get<TableData<OrderModel>>('/admin/orders/list', {
    params: { pageNo: 1, pageSize: 1 }
  });
}