import { request, type UserRequestConfig } from '@/utils/request'

export interface ItemListQuery {
  name?: string
  pageNo?: number
  pageSize?: number
  category?: string
  brand?: string
  shopId?: number
  sold?: string
  sortBy?: string
  isAsc?: boolean
  minPrice?: number
  maxPrice?: number
}

export interface ItemListModel {
  id: number
  name: string
  subTitle?: string
  image?: string
  price: number
  originalPrice?: number
  sold?: number
  commentCount?: number | null
  positiveRate?: number | null
  brand?: string
  category?: string
  shopId?: number | null
  shopName?: string
  isSelf?: number | null
  shippingType?: string
  shippingFee?: number | null
  freeShippingThreshold?: number | null
  shippingDesc?: string
}

export interface ItemSpecTemplateItem {
  name: string
  values: string[]
}

export interface ItemSkuModel {
  id: number
  specs: Record<string, string>
  price: number
  stock: number
  image?: string
}

export interface ItemDetailModel extends ItemListModel {
  stock?: number
  avgScore?: number | null
  status?: number
  categoryId?: number | null
  bannerImages?: string[]
  videoUrl?: string
  detailHtml?: string
  specTemplate?: ItemSpecTemplateItem[]
  skuList?: ItemSkuModel[]
  tags?: string
}

export interface ShopModel {
  id: number
  name: string
  isSelf?: number | null
  shippingType?: string
  shippingFee?: number | null
  freeShippingThreshold?: number | null
  shippingDesc?: string
  status?: number | null
}

function normalizeListParams(params: ItemListQuery = {}): ItemListQuery {
  const normalized: ItemListQuery = { ...params }

  if (!normalized.sortBy) {
    delete normalized.isAsc
  } else if (typeof normalized.isAsc === 'undefined') {
    normalized.isAsc = true
  }

  if (!normalized.name) {
    delete normalized.name
  }
  if (!normalized.category) {
    delete normalized.category
  }
  if (!normalized.brand) {
    delete normalized.brand
  }
  if (normalized.shopId === undefined || normalized.shopId === null || Number.isNaN(Number(normalized.shopId))) {
    delete normalized.shopId
  }
  if (!normalized.sold) {
    delete normalized.sold
  }
  if (normalized.minPrice === undefined || normalized.minPrice === null || Number.isNaN(Number(normalized.minPrice))) {
    delete normalized.minPrice
  }
  if (normalized.maxPrice === undefined || normalized.maxPrice === null || Number.isNaN(Number(normalized.maxPrice))) {
    delete normalized.maxPrice
  }

  return normalized
}

export function list(params: ItemListQuery, config?: UserRequestConfig) {
  return request({
    ...(config || {}),
    url: '/items/list',
    method: 'get',
    params: normalizeListParams(params)
  } as UserRequestConfig)
}

export const itemApi = {
  getItemById(id: number | string) {
    return request({
      url: `/items/${id}`,
      method: 'get'
    })
  },
  getShopById(id: number | string) {
    return request({
      url: `/shops/${id}`,
      method: 'get'
    })
  }
}
