import { get, put } from '@/utils/request';
import type { AjaxResult, TableData } from '@/types/http';
import type { CommentReviewModel } from '@/types/domain';

export function listComments(params: Record<string, unknown>) {
  return get<TableData<CommentReviewModel>>('/admin/comments/list', { params });
}

export function getCommentDetail(id: number | string) {
  return get<AjaxResult<CommentReviewModel>>(`/admin/comments/${id}`);
}

export function approveComment(id: number | string) {
  return put<AjaxResult<void>>(`/admin/comments/${id}/approve`);
}

export function rejectComment(id: number | string, data: { rejectReason: string }) {
  return put<AjaxResult<void>>(`/admin/comments/${id}/reject`, data);
}
