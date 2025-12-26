import {request} from '@/utils/request';

export function addItem2Cart(data:any) {
  return request({
    url: '/carts',
    method: 'post',
    data: data
  });
}


export function updateCart(data: any) {
  return request({
    url: '/carts',
    method: 'put',
    data: data
  });
}


export function deleteCartItem(id: any) {
  return request({
    url: `/carts/${id}`,
    method: 'delete'
  });
}


export function queryMyCarts() {
  return request({
    url: '/carts',
    method: 'get'
  });
}

/**
 * 批量删除购物车中商品
 * Path: DELETE /carts
 * Param: ids (List<Long>)
 */
export function deleteCartItemByIds(ids: number[]) {
  return request({
    url: '/carts',
    method: 'delete',
    // SpringMVC 默认接收逗号分隔的字符串 ids=1,2,3 或重复参数 ids=1&ids=2
    // axios 默认会将数组序列化为 ids[]=1&ids[]=2，后端可能需要配置。
    // 这里建议将数组转为逗号分隔字符串，或者依赖 qs 库的序列化配置。
    params: {
      ids: ids.join(',')
    }
  });
}