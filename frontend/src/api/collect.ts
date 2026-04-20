import { request, type UserRequestConfig } from '@/utils/request'

export interface CollectItem {
  itemId: number
  name: string
  image: string
  price: number | string
  tags: string
}

export function addCollect(data: CollectItem) {
  return request({
    url: '/collects',
    method: 'post',
    data
  })
}

export function getCollectList(params?: unknown, config?: UserRequestConfig) {
  return request({
    ...(config || {}),
    url: '/collects',
    method: 'get',
    params
  } as UserRequestConfig)
}

export const deleteById = (id: number) =>
  request({
    url: `/collects/${id}`,
    method: 'delete'
  })
