import { request } from '@/utils/request';

/**
 * 支付确认表单参数接口
 * 对应后端: com.yu.pay.domain.dto.PayOrderFormDTO
 */
export interface PayOrderFormDTO {
  /**
   * 业务订单id
   * 注意：Java Long 类型在 JS 中建议使用 string 传输以防精度丢失
   */
  bizOrderNo: string;

  /**
   * 支付密码
   */
  payPassword: string;

  /**
   * 支付方式
   * 1、支付宝，2、微信，3、扣减余额
   */
  paymentType: number;

  /**
   * 支付金额
   */
  amount: number;
}

/**
 * 支付订单接口
 * 对应后端: PayController.payOrder
 * 路径: PUT /pay-orders
 */
export function payOrder(data: PayOrderFormDTO) {
  return request({
    url: '/pay-orders',
    method: 'put',
    data: data
  });
}