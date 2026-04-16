import axios, { type AxiosRequestConfig, type Method } from 'axios';
import { ElMessage } from 'element-plus';

interface StandardPayload<T = unknown> {
  code: number;
  msg: string;
  data?: T;
  rows?: unknown[];
  total?: number;
}

interface AdminErrorMeta {
  silent?: boolean;
  action?: string;
}

export interface AdminRequestConfig extends AxiosRequestConfig {
  errorMeta?: AdminErrorMeta;
}

export interface AdminRequestError<T = unknown> extends StandardPayload<T> {
  status: number;
  authExpired: boolean;
  handled: boolean;
  reason: string;
  displayMessage: string;
}

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
});

const RESOURCE_LABELS: Record<string, string> = {
  items: '商品',
  brands: '品牌',
  categories: '分类',
  comments: '评论',
  orders: '订单',
  shops: '店铺',
  users: '用户',
  admins: '管理员',
  dashboard: '看板',
  upload: '上传文件'
};

const TECHNICAL_MESSAGE_PATTERN =
  /exception|error|sql|mybatis|jdbc|redis|gateway|feign|trace|stack|org\.|java\.|nullpointer|syntax|<html|<!doctype/i;

let isRedirectingToLogin = false;

const unwrapStandardPayload = (payload: unknown): StandardPayload | null => {
  if (payload && typeof payload === 'object' && 'code' in payload && 'msg' in payload) {
    return payload as StandardPayload;
  }
  return null;
};

const isDownloadResponse = (response: { headers?: Record<string, unknown>; config?: AdminRequestConfig }) => {
  const contentType = String(response.headers?.['content-type'] || '');
  return contentType.includes('application/octet-stream') || response.config?.responseType === 'blob';
};

const sanitizeAdminReason = (message: unknown, fallback = '请稍后重试') => {
  if (typeof message !== 'string') {
    return fallback;
  }
  const normalized = message.replace(/\s+/g, ' ').trim();
  if (!normalized || TECHNICAL_MESSAGE_PATTERN.test(normalized)) {
    return fallback;
  }
  return normalized;
};

const splitPathSegments = (url?: string) =>
  (url || '')
    .split('?')[0]
    .split('/')
    .filter(Boolean);

const isNumericSegment = (value?: string) => Boolean(value) && /^\d+$/.test(value as string);

const resolveResourceLabel = (url?: string) => {
  const segments = splitPathSegments(url);
  const businessSegments = segments.filter((segment) => segment !== 'admin' && segment !== 'admins');
  const targetSegment = businessSegments.find(
    (segment) => !isNumericSegment(segment) && !['list', 'simple', 'dashboard', 'recent', 'approve', 'reject'].includes(segment)
  );
  return RESOURCE_LABELS[targetSegment || ''] || '数据';
};

const resolveAdminAction = (config: AdminRequestConfig) => {
  if (config.errorMeta?.action) {
    return config.errorMeta.action;
  }

  const method = ((config.method || 'get') as Method).toLowerCase();
  const url = config.url || '';
  const segments = splitPathSegments(url);
  const lastSegment = segments[segments.length - 1];
  const resourceLabel = resolveResourceLabel(url);

  if (url.includes('/admins/login')) {
    return '登录';
  }
  if (url.includes('/admins/register')) {
    return '注册';
  }
  if (url.includes('/upload')) {
    return '上传文件';
  }
  if (lastSegment === 'dashboard') {
    return '加载看板数据';
  }
  if (lastSegment === 'recent') {
    return '加载最近订单';
  }
  if (lastSegment === 'simple') {
    return `加载${resourceLabel}选项`;
  }
  if (lastSegment === 'approve') {
    return '审核评论';
  }
  if (lastSegment === 'reject') {
    return '驳回评论';
  }

  if (method === 'delete') {
    return `删除${resourceLabel}`;
  }
  if (method === 'post') {
    return `新增${resourceLabel}`;
  }
  if (method === 'put') {
    if (url.includes('/users/')) {
      return '更新用户状态';
    }
    return `更新${resourceLabel}`;
  }
  if (method === 'get') {
    if (lastSegment === 'list' || (!isNumericSegment(lastSegment) && segments.length <= 3)) {
      return `加载${resourceLabel}列表`;
    }
    if (isNumericSegment(lastSegment)) {
      return `加载${resourceLabel}详情`;
    }
    return `加载${resourceLabel}数据`;
  }
  return '请求数据';
};

const buildAdminDisplayMessage = (action: string, reason: string, authExpired: boolean) => {
  if (authExpired) {
    return '登录已过期，请重新登录';
  }
  if (action === '登录' || action === '注册') {
    return reason === '请稍后重试' ? `${action}失败` : reason;
  }
  return reason === '请稍后重试' ? `${action}失败，请稍后重试` : `${action}失败：${reason}`;
};

const normalizeAdminError = (config: AdminRequestConfig, payload?: StandardPayload | null, status?: number, rawMessage?: unknown) => {
  const code = Number(payload?.code || status || 500);
  const authExpired = code === 401 || status === 401;
  const action = resolveAdminAction(config);
  const reason = sanitizeAdminReason(payload?.msg ?? rawMessage, authExpired ? '登录已过期，请重新登录' : '请稍后重试');

  return {
    code,
    msg: payload?.msg || '',
    data: (payload?.data ?? null) as unknown,
    status: status || code || 500,
    authExpired,
    handled: false,
    reason,
    displayMessage: buildAdminDisplayMessage(action, reason, authExpired)
  } as AdminRequestError;
};

const handleAuthExpired = () => {
  localStorage.removeItem('ADMIN_AUTH_TOKEN');
  localStorage.removeItem('ADMIN_AUTH_USER');
  if (window.location.pathname.includes('/login') || isRedirectingToLogin) {
    return;
  }
  isRedirectingToLogin = true;
  ElMessage.error('登录已过期，请重新登录');
  window.location.href = '/login';
};

const notifyAdminError = (error: AdminRequestError, config: AdminRequestConfig) => {
  if (config.errorMeta?.silent) {
    return;
  }
  error.handled = true;
  if (error.authExpired) {
    handleAuthExpired();
    return;
  }
  ElMessage.error(error.displayMessage);
};

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
    const config = (response.config || {}) as AdminRequestConfig;
    if (isDownloadResponse(response)) {
      return response;
    }

    const payload = unwrapStandardPayload(response.data);
    if (payload && Number(payload.code) !== 200) {
      const normalized = normalizeAdminError(config, payload, response.status);
      notifyAdminError(normalized, config);
      return normalized;
    }

    return response.data;
  },
  (error) => {
    const config = ((error?.config || {}) as AdminRequestConfig);
    const payload = unwrapStandardPayload(error?.response?.data);
    const normalized = normalizeAdminError(config, payload, error?.response?.status, error?.message);
    notifyAdminError(normalized, config);
    return Promise.resolve(normalized);
  }
);

export const resolveAdminErrorReason = (error: unknown, fallback = '请稍后重试') =>
  sanitizeAdminReason((error as Partial<AdminRequestError>)?.reason || (error as Partial<AdminRequestError>)?.msg, fallback);

export function get<T>(url: string, config?: AdminRequestConfig) {
  return request.get<never, T>(url, config);
}

export function post<T>(url: string, data?: unknown, config?: AdminRequestConfig) {
  return request.post<never, T>(url, data, config);
}

export function put<T>(url: string, data?: unknown, config?: AdminRequestConfig) {
  return request.put<never, T>(url, data, config);
}

export function del<T>(url: string, config?: AdminRequestConfig) {
  return request.delete<never, T>(url, config);
}

export default request;
