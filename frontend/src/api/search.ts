import { request, type UserRequestConfig } from '@/utils/request'
import type { ItemListModel } from '@/api/item'

export interface SearchItemQuery {
  keyword?: string
  pageNo?: number
  pageSize?: number
  category?: string
  brand?: string
  sortBy?: string
  isAsc?: boolean
  minPrice?: number
  maxPrice?: number
}

function normalizeSearchParams(params: SearchItemQuery = {}): SearchItemQuery {
  const normalized: SearchItemQuery = { ...params }

  if (!normalized.keyword) {
    delete normalized.keyword
  }
  if (!normalized.category) {
    delete normalized.category
  }
  if (!normalized.brand) {
    delete normalized.brand
  }
  if (!normalized.sortBy) {
    delete normalized.isAsc
  } else if (typeof normalized.isAsc === 'undefined') {
    normalized.isAsc = true
  }
  if (normalized.minPrice === undefined || normalized.minPrice === null || Number.isNaN(Number(normalized.minPrice))) {
    delete normalized.minPrice
  }
  if (normalized.maxPrice === undefined || normalized.maxPrice === null || Number.isNaN(Number(normalized.maxPrice))) {
    delete normalized.maxPrice
  }

  return normalized
}

export function searchItems(params: SearchItemQuery, config?: UserRequestConfig) {
  return request({
    ...(config || {}),
    url: '/search/items',
    method: 'get',
    params: normalizeSearchParams(params)
  } as UserRequestConfig)
}

export type SearchItemModel = ItemListModel
