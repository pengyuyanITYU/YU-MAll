export interface AjaxResult<T = unknown> {
  code: number;
  msg: string;
  data: T;
}

export interface TableData<T = unknown> {
  code: number;
  msg: string;
  rows: T[];
  total: number;
}