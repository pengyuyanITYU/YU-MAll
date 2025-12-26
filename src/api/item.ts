import {request} from '@/utils/request';

interface PageQueryForm {
  name: string
  category?: string
  brand?: boolean
  pageNo: number
  pageSize: number
}
export function list(params:any) {
  return request({ url: '/items/list', method: 'get', params }) // ✅ 必须加 return
}

  export const itemApi = {
    // 根据ID获取商品详情
    getItemById: (id: number) => {
      return request.get(`/items/${id}`);
    }}