import { request } from '@/utils/request'

export interface CommentDTO {
  itemId: number | string
  skuId?: number | string
  orderId: number | string
  orderDetailId: number | string
  rating: number
  content: string
  images?: string[]
  isAnonymous?: boolean
  userNickname: string
  userAvatar: string
}

export interface CommentVO {
  id: number | string
  userId: number | string
  userNickname: string
  userAvatar: string
  itemId?: number | string
  skuId?: number | string
  orderId?: number | string
  orderDetailId?: number | string
  rating: number
  content: string
  images?: string[]
  isAnonymous?: boolean
  status?: number
  rejectReason?: string
  skuSpecs?: string | Record<string, string>
  likeCount: number
  isLiked?: boolean
  merchantReplyContent?: string
  merchantReplyTime?: string
  createTime: string
  updateTime?: string
  itemName?: string
  itemImage?: string
}

export function listComments(id: number | string) {
  return request<CommentVO[]>({
    url: `/comments/all/${id}`,
    method: 'get'
  })
}

export function addComment(data: CommentDTO) {
  return request<void>({
    url: '/comments',
    method: 'post',
    data
  })
}

export function updateComment(id: number | string, data: CommentDTO) {
  return request<void>({
    url: `/comments/${id}`,
    method: 'put',
    data
  })
}

export function deleteMyComment(id: number | string) {
  return request<void>({
    url: `/comments/${id}`,
    method: 'delete'
  })
}

export function getMyComment() {
  return request<CommentVO[]>({
    url: '/comments/user',
    method: 'get'
  })
}

export const likeCommentAndCancelLike = (id: number | string) => {
  return request({
    url: `/comments/like/${id}`,
    method: 'post'
  })
}

export const getUserCommentDetail = (id: number | string) => {
  return request<CommentVO>({
    url: `/comments/detail/${id}`,
    method: 'get'
  })
}
