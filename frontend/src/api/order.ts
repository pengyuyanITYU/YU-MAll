import { request } from '@/utils/request'

export interface OrderDetailDTO {
  itemId: string | number
  skuId?: string | number
  num: number
  price: string | number
  image: string
  specs: Record<string, string>
}

export interface OrderFormDTO {
  addressId: string | number
  paymentType?: number
  details: OrderDetailDTO[]
  token?: string
  totalFee: number
}

export interface OrderVO {
  id: string | number
  totalFee: number
  paymentType: number
  userId: string | number
  status: number
  createTime?: string
  payTime?: string
  consignTime?: string
  endTime?: string
  closeTime?: string
  receiverContact: string
  receiverMobile: string
  receiverAddress: string
  details?: OrderDetailVO[]
  commented: boolean
}

export interface OrderDetailVO {
  id: string | number
  itemId: string | number
  skuId?: string | number
  num: number
  name: string
  price: number
  image?: string
  spec: Record<string, string>
  commented?: boolean
}

export function getOrderList(id: number | string) {
  return request<OrderVO[]>({
    url: `/orders/${id}/list`,
    method: 'get'
  })
}

export function getOrderDetail(id: number | string) {
  return request({
    url: `/orders/${id}`,
    method: 'get'
  })
}

export function addOrder(data: OrderFormDTO) {
  return request({
    url: '/orders',
    method: 'post',
    data
  })
}

export function deleteOrder(id: number | string) {
  return request({
    url: `/orders/${id}`,
    method: 'delete'
  })
}

export function deleteOrderBatch(ids: (number | string)[]) {
  return request({
    url: '/orders',
    method: 'delete',
    params: {
      ids: ids.join(',')
    }
  })
}

export const cancelOrder = (id: number | string) => request.put(`/orders/cancel/${id}`)

export const confirmOrder = (id: number | string) => request.put(`/orders/confirm/${id}`)
