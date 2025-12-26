// src/api/upload.ts (新建或添加到现有文件)
import {request} from '@/utils/request' // 你的 axios 实例

// 上传接口
export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file) // 'file' 要和你后端 UploadController 里的参数名一致
  
  return request({
    url: '/upload', // 你的后端接口地址
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

