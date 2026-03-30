import { request } from '@/utils/request';

/**
 * 商品分页查询参数
 * - 移除：category、brand
 * - 新增：sortBy、isAsc（排序字段与方向）
 * - 兼容：允许透传其他筛选字段（如 minPrice、maxPrice、hasStock 等）
 */
export interface PageQueryForm {
  name: string;
  pageNo: number;
  pageSize: number;
  sold?: string | number;
  sortBy?: string;
  isAsc?: boolean;
  category?: string;
  minPrice?: number;
  maxPrice?: number;
  [key: string]: any;
}

const ALLOWED_SORT_KEYS = ['price', 'sales', 'rating', 'new'];

function normalizeListParams(params: any): PageQueryForm {
  const p: PageQueryForm = { ...params };

  // 移除旧字段，保持向后兼容（调用方可仍存在，但不再上送）
  // if ('category' in p) delete p.category;
  if ('brand' in p) delete p.brand;

  // 兼容旧的 sort 字段（若存在则映射为 sortBy/isAsc）
  if (typeof p.sort === 'string' && !p.sortBy) {
    switch (p.sort) {
      case 'price_asc':
        p.sortBy = 'price';
        p.isAsc = true;
        break;
      case 'price_desc':
        p.sortBy = 'price';
        p.isAsc = false;
        break;
      default:
        // 其他值不处理
        break;
    }
    delete p.sort;
  }

  // // 校验 sortBy
  // if (p.sortBy && !ALLOWED_SORT_KEYS.includes(p.sortBy)) {
  //   delete p.sortBy;
  //   delete p.isAsc;
  // }

  // 仅传 isAsc 时无意义，移除
  if (!p.sortBy && typeof p.isAsc !== 'undefined') {
    delete p.isAsc;
  }

  // 仅传 sortBy 时，默认升序
  if (p.sortBy && typeof p.isAsc === 'undefined') {
    p.isAsc = true;
  }

  return p;
}

/**
 * 商品分页列表
 * - 支持排序：sortBy（price|sales|rating|new），isAsc（默认 true）
 * - 其余筛选字段透传
 */
export function list(params: any) {
  const normalized = normalizeListParams(params);
  return request({ url: '/items/list', method: 'get', params: normalized });
}

export const itemApi = {
  getItemById: (id: number) => {
    return request.get(`/items/${id}`);
  },
};
