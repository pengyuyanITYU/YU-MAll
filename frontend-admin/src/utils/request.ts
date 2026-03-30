import axios, { type AxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
});

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('ADMIN_AUTH_TOKEN');
  if (token) {
    const headers = (config.headers || {}) as Record<string, string>;
    headers.Authorization = token;
    config.headers = headers as any;
  }
  return config;
});

request.interceptors.response.use(
  (response) => {
    const contentType = response.headers['content-type'];
    if (contentType?.includes('application/octet-stream')) {
      return response;
    }
    return response.data;
  },
  (error) => {
    const status = error?.response?.status;
    if (status === 401) {
      localStorage.removeItem('ADMIN_AUTH_TOKEN');
      localStorage.removeItem('ADMIN_AUTH_USER');
      if (!window.location.pathname.includes('/login')) {
        ElMessage.error('登录已过期，请重新登录');
        window.location.href = '/login';
      }
    } else {
      const message = error?.response?.data?.msg || error.message || '请求失败';
      ElMessage.error(message);
    }
    return Promise.reject(error);
  }
);

export function get<T>(url: string, config?: AxiosRequestConfig) {
  return request.get<never, T>(url, config);
}

export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.post<never, T>(url, data, config);
}

export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.put<never, T>(url, data, config);
}

export function del<T>(url: string, config?: AxiosRequestConfig) {
  return request.delete<never, T>(url, config);
}

export default request;
