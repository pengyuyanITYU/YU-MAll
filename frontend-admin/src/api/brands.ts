import { del, get, post, put } from '@/utils/request';
import type { BrandModel } from '@/types/domain';
import type { AjaxResult, TableData } from '@/types/http';

export function listBrands(params: Record<string, unknown>) {
  return get<TableData<BrandModel>>('/admin/brands', { params });
}

export function listBrandsSimple() {
  return get<TableData<BrandModel>>('/admin/brands', {
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

export function createBrand(data: { name: string }) {
  return post<AjaxResult<void>>('/admin/brands', data);
}

export function updateBrand(data: { id: number; name: string }) {
  return put<AjaxResult<void>>('/admin/brands', data);
}

export function deleteBrand(id: number | string) {
  return del<AjaxResult<void>>(`/admin/brands/${id}`);
}
