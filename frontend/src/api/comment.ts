import { request } from '@/utils/request'

// ==========================================
// 类型定义 (对应 Java 的 DTO 和 VO)
// ==========================================

/**
 * 新增评价表单 DTO
 */
export interface CommentDTO {
  /** 商品ID */
  itemId: number | string;
  /** SKU ID */
  skuId?: number | string;
  /** 订单ID */
  orderId: number | string;
  /** 订单明细ID */
  orderDetailId: number | string;
  /** 评分(1-5) */
  rating: number;
  /** 评价内容 (max 500) */
  content: string;
  /** 图片列表(URL数组) */
  images?: string[];
  /** 是否匿名 */
  isAnonymous?: boolean;

  userNickname: string;
  userAvatar: string;
}

/**
 * 评价列表展示 VO
 */
export interface CommentVO {
  /** 评价ID */
  id: number | string;
  /** 用户ID */
  userId: number | string;
  /** 用户昵称 */
  userNickname: string;
  /** 用户头像 */
  userAvatar: string;
  /** 评分 */
  rating: number;
  /** 评价内容 */
  content: string;
  /** 图片列表 */
  images?: string[];
  /** SKU信息(如: 红色, 64G) */
  skuSpecs: string;
  /** 点赞数 */
  likeCount: number;
  /** 是否当前用户已点赞 */
  isLiked: boolean;
  /** 商家回复内容 */
  merchantReplyContent?: string;
  /** 商家回复时间 */
  merchantReplyTime?: string;
  /** 评价时间 */
  createTime: string;
  itemName: string;
  itemImage?: string;

}

// ==========================================
// API 方法
// ==========================================

/**
 * 查询所有评论
 * GET /comments
 */
export function listComments(id: number | string) {
  return request<CommentVO[]>({
    url: `/comments/all/${id}`,
    method: 'get'
  })
}

/**
 * 发表评论
 * POST /comments
 * @param data 评价表单数据
 */
export function addComment(data: CommentDTO) {
  return request<void>({
    url: '/comments',
    method: 'post',
    data: data
  })
}

/**
 * 删除评论
 * DELETE /comments/{id}
 * @param id 评论ID
 */
export function deleteMyComment(id: number | string) {
  return request<void>({
    url: `/comments/${id}`,
    method: 'delete'
  })
}

/**
 * 查询用户自己的评论 (根据订单详情ID或评论ID，视后端具体实现而定)
 * GET /comments/{id}
 * @param id 订单详情ID (对应后端注释)
 */
export function getMyComment() {
  return request<CommentVO>({
    url: `/comments/user`,
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
  return request({
    url: `/comments/detail/${id}`,
    method: 'get'
  })
}