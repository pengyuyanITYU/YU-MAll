export interface LoginForm {
  username: string;
  password: string;
  rememberMe?: boolean;
}

export interface LoginResult {
  token: string;
  userId: number;
  username?: string;
  nickName?: string;
  avatar?: string;
}

export interface UserModel {
  id: number | string;
  username: string;
  nickName?: string;
  phone?: string;
  email?: string;
  balance?: number;
  currentPoints?: number;
  levelName?: string;
  status: number | string | { value?: number };
  createTime?: string;
}

export interface ItemModel {
  id: number | string;
  name: string;
  subTitle?: string;
  image?: string;
  price: number;
  originalPrice?: number;
  stock?: number;
  sold?: number;
  status?: number;
  category?: string;
  categoryId?: number;
  brand?: string;
  tags?: string;
  updateTime?: string;
}

export interface CategoryModel {
  id: number;
  name: string;
  createTime?: string;
  updateTime?: string;
}

export interface OrderDetailModel {
  itemId: number;
  num: number;
  price: number;
  name?: string;
  image?: string;
}

export interface OrderModel {
  id: number | string;
  userId: number | string;
  nickName?: string;
  totalFee: number;
  paymentType?: number;
  status?: number | string;
  createTime?: string;
  receiverContact?: string;
  receiverMobile?: string;
  receiverAddress?: string;
  details?: OrderDetailModel[];
}

export interface ItemDashboard {
  totalItems: number;
  onShelfItems: number;
  offShelfItems: number;
  lowStockItems: number;
  totalCategories: number;
  categoryDistribution: Array<{ name: string; value: number }>;
  topSellingItems: Array<{ name: string; value: number }>;
}

export interface RecentOrder {
  id: number | string;
  avatar?: string;
  nickName?: string;
  totalPrice: number;
  status?: number | string;
  createTime?: string;
}