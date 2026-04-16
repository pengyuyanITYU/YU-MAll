import { post } from '@/utils/request';
import type { AjaxResult } from '@/types/http';

interface UploadFileInfo {
  url?: string;
  [key: string]: unknown;
}

export function uploadFile(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return post<AjaxResult<UploadFileInfo>>('/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    errorMeta: {
      silent: true,
      action: '上传文件'
    }
  });
}
