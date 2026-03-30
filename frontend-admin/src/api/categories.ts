import { del, get, post, put } from '@/utils/request';
import type { AjaxResult, TableData } from '@/types/http';
import type { CategoryModel } from '@/types/domain';

export function listCategories(params: Record<string, unknown>) {
  return get<TableData<CategoryModel>>('/admin/categories', { params });
}

export function listCategoriesSimple() {
  return get<TableData<CategoryModel>>('/admin/categories', {
    params: {
      pageNo: 1,
      pageSize: 1000
    }
  }).then((res) => ({
    code: res.code,
    msg: res.msg,
    data: res.rows || []
  }));
}

export function createCategory(data: { name: string }) {
  return post<AjaxResult<void>>('/admin/categories', data);
}

export function updateCategory(data: { id: number; name: string }) {
  return put<AjaxResult<void>>('/admin/categories', data);
}

export function deleteCategory(id: number | string) {
  return del<AjaxResult<void>>(`/admin/categories/${id}`);
}
