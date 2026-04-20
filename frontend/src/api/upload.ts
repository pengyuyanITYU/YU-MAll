import { request, type UserRequestConfig } from '@/utils/request'

export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    errorMeta: {
      silent: true
    }
  } as UserRequestConfig)
}
