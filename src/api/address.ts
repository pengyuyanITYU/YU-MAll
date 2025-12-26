import { request } from '@/utils/request';

// 地址相关的API
const addressApi = {
  // 获取地址列表
  getAddressList: () => {
    return request.get('/addresses');
  },
  
  // 添加地址
  addAddress: (addressDTO: any) => {
    return request.post('/addresses', addressDTO);
  },
  
  // 修改地址
  updateAddress: (address: any) => {
    return request.put(`/addresses`, address);
  },
  
  // 删除地址
  deleteAddress: (id: number) => {
    return request.delete(`/addresses/${id}`);
  }
};

export default addressApi;
export const { getAddressList, addAddress, updateAddress, deleteAddress } = addressApi;