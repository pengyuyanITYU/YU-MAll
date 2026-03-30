import { request } from '@/utils/request';

// ==========================================
// DTO 定义 (对应后端 com.yu.order.domain.dto 和 com.yu.api.dto)
// ==========================================

/**
 * 订单明细传输对象 (对应 com.yu.api.dto.OrderDetailDTO)
 */
export interface OrderDetailDTO {
  itemId: string | number; // 商品ID
  num: number;             // 购买数量
  price: string | number;
  image: string;
  specs: Record<string, string>;
}

/**
 * 交易下单表单实体 (对应 com.yu.order.domain.dto.OrderFormDTO)
 */
export interface OrderFormDTO {
  addressId: string | number; // 收货地址id
  paymentType?: number;        // 支付类型：1、支付宝，2、微信，3、扣减余额
  details: OrderDetailDTO[];  // 下单商品列表
  token?: string;             // 防重令牌
  totalFee: number;           // 订单总金额
}

// ==========================================
// VO 定义 (对应后端返回的视图对象)
// ==========================================

/**
 * 订单列表视图对象 (对应 OrderVO)
 */
export interface OrderVO {
  id: string | number;
  totalFee: number;
  paymentType: number;
  userId: string | number;
  status: number;
  createTime?: string;
  payTime?: string;
  consignTime?: string;
  endTime?: string;
  closeTime?: string;
  // ... 其他 OrderVO 中的字段
  receiverContact: string;
    receiverMobile: string;
    receiverAddress: string;
    details?: OrderDetailVO[]; // 订单详情列表
    commented: boolean; // 是否已评价
}

/**
 * 订单详情视图对象 (对应 OrderDetailVO)
 */
export interface OrderDetailVO {
  id: string | number;
  itemId: string | number;
  num: number;
  name: string;
  price: number;
  image?: string;
  spec: Record<string, string>;

  // ... 其他 OrderDetailVO 中的字段
}

// ==========================================
// API 请求函数
// ==========================================

/**
 * 查询用户订单列表
 * 对应后端: @GetMapping("/{id}/list")
 * @param id 用户ID
 */
export function getOrderList(id: number | string) {
  return request<OrderVO[]>({
    url: `/orders/${id}/list`,
    method: 'get'
  });
}

/**
 * 查询用户订单详情
 * 对应后端: @GetMapping("/{id}")
 * 注意：后端返回的是 List<OrderDetailVO>，即一个订单包含多个商品详情
 * @param id 订单ID
 */
export function getOrderDetail(id: number  | string) {
  return request({
    url: `/orders/${id}`, // 修正：去掉了 /detail
    method: 'get'
  });
}

/**
 * 添加订单
 * 对应后端: @PostMapping
 * @param data 下单表单数据
 */
export function addOrder(data: OrderFormDTO) {
  return request({
    url: '/orders',
    method: 'post',
    data: data
  });
}

/**
 * 删除订单
 * 对应后端: @DeleteMapping("/{id}")
 * @param id 订单ID
 */
export function deleteOrder(id: number | string) {
  return request({
    url: `/orders/${id}`,
    method: 'delete'
  });
}

/**
 * 批量删除订单
 * 对应后端: @DeleteMapping (Param: ids)
 * @param ids 订单ID集合
 */
export function deleteOrderBatch(ids: (number | string)[]) {
  return request({
    url: '/orders',
    method: 'delete',
    params: {
      // Spring MVC 默认支持逗号分隔的字符串作为 List<Long> 参数
      ids: ids.join(',')
    }
  });
}

export const cancelOrder = (id: number | string) => request.put(`/orders/cancel/${id}`);

export const confirmOrder = (id: number | string) => request.put(`/orders/confirm/${id}`);