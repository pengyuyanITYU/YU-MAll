import { request } from '@/utils/request'

// 定义类型接口 (可选)
export interface CollectItem {
  itemId: number;
  name: string;
  image: string;
  price: number | string; //这里要能处理Long处理
  tags:string
  // ...其他字段
}

// 1. 查询收藏列表


// 2. 添加收藏
export function addCollect(data: CollectItem) {
  return request({
    url: '/collects',
    method: 'post',
    data
  })
}
export function getCollectList(params?: any) {
  return request({
    url: '/collects',
    method: 'get',
    params // 通常查询会带分页或筛选参数
  })
}


// 4. 单个删除 (建议只走 URL)
export const deleteById = (id: number) => {
  return request({
    url: `/collects/${id}`,
    method: 'delete'
    // 通常单删不需要 data: id，因为 ID 已经在 URL 里了
  })
}