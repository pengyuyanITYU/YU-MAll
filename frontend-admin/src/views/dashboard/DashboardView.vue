<template>
  <div class="dashboard-page" v-loading="loading">
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card gradient-a">
          <div class="label">用户总数</div>
          <div class="value">{{ overview.userTotal }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card gradient-b">
          <div class="label">商品总数</div>
          <div class="value">{{ overview.itemTotal }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card gradient-c">
          <div class="label">订单总数</div>
          <div class="value">{{ overview.orderTotal }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card gradient-d">
          <div class="label">低库存商品</div>
          <div class="value">{{ overview.itemDashboard.lowStockItems || 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="panel-row">
      <el-col :xs="24" :lg="12">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <span>分类分布</span>
          </template>
          <el-table :data="overview.itemDashboard.categoryDistribution || []" size="small" empty-text="暂无数据">
            <el-table-column prop="name" label="分类" min-width="180" />
            <el-table-column prop="value" label="数量" width="120" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <span>商品销量 Top</span>
          </template>
          <div v-if="(overview.itemDashboard.topSellingItems || []).length > 0">
            <div v-for="item in overview.itemDashboard.topSellingItems" :key="item.name" class="rank-item">
              <div class="name">{{ item.name }}</div>
              <el-progress :percentage="toPercent(item.value)" :show-text="false" :stroke-width="12" />
              <div class="count">{{ item.value }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无数据" />
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <span>最近订单</span>
      </template>
      <el-table :data="overview.recentOrders" stripe>
        <el-table-column prop="id" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column label="用户" min-width="140">
          <template #default="{ row }">{{ row.nickName || '-' }}</template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">￥{{ money(row.totalPrice) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import {
  fetchItemDashboard,
  fetchItemOverview,
  fetchOrderOverview,
  fetchRecentOrders,
  fetchUserOverview
} from '@/api/dashboard';
import type { ItemDashboard, RecentOrder } from '@/types/domain';

const loading = ref(false);

const overview = reactive<{
  userTotal: number;
  itemTotal: number;
  orderTotal: number;
  itemDashboard: ItemDashboard;
  recentOrders: RecentOrder[];
}>({
  userTotal: 0,
  itemTotal: 0,
  orderTotal: 0,
  itemDashboard: {
    totalItems: 0,
    onShelfItems: 0,
    offShelfItems: 0,
    lowStockItems: 0,
    totalCategories: 0,
    categoryDistribution: [],
    topSellingItems: []
  },
  recentOrders: []
});

function money(amount?: number) {
  if (amount === undefined || amount === null) return '0.00';
  return (Number(amount) / 100).toFixed(2);
}

function normalizeStatus(status: unknown): number {
  if (typeof status === 'number') return status;
  if (typeof status === 'string') return Number(status) || 0;
  if (status && typeof status === 'object' && 'value' in (status as any)) {
    return Number((status as any).value) || 0;
  }
  return 0;
}

function statusText(status: unknown) {
  const value = normalizeStatus(status);
  const map: Record<number, string> = {
    1: '待支付',
    2: '已支付',
    3: '已发货',
    4: '已完成',
    5: '已取消',
    6: '已评价'
  };
  return map[value] || '未知';
}

function statusType(status: unknown): 'info' | 'success' | 'warning' | 'danger' {
  const value = normalizeStatus(status);
  if (value === 4 || value === 6) return 'success';
  if (value === 5) return 'danger';
  if (value === 1) return 'warning';
  return 'info';
}

function toPercent(value: number) {
  const max = Math.max(...(overview.itemDashboard.topSellingItems || []).map((item) => Number(item.value)), 1);
  return Math.round((Number(value) / max) * 100);
}

async function load() {
  loading.value = true;
  try {
    const [itemDashboardRes, recentRes, userRes, itemRes, orderRes] = await Promise.all([
      fetchItemDashboard(),
      fetchRecentOrders(),
      fetchUserOverview(),
      fetchItemOverview(),
      fetchOrderOverview()
    ]);

    if (itemDashboardRes.code === 200 && itemDashboardRes.data) {
      overview.itemDashboard = itemDashboardRes.data;
    }
    if (recentRes.code === 200 && Array.isArray(recentRes.data)) {
      overview.recentOrders = recentRes.data;
    }
    overview.userTotal = userRes.total || 0;
    overview.itemTotal = itemRes.total || 0;
    overview.orderTotal = orderRes.total || 0;
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<style scoped lang="scss">
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-row,
.panel-row {
  margin-bottom: 0;
}

.stat-card {
  border-radius: 14px;
  color: #fff;
  padding: 18px;
  min-height: 104px;
  box-shadow: 0 10px 22px rgba(15, 42, 70, 0.12);
}

.gradient-a {
  background: linear-gradient(135deg, #1775f0, #4fa8ff);
}

.gradient-b {
  background: linear-gradient(135deg, #00a6a6, #33d3c8);
}

.gradient-c {
  background: linear-gradient(135deg, #4f46e5, #7f83ff);
}

.gradient-d {
  background: linear-gradient(135deg, #f97316, #f5b400);
}

.label {
  font-size: 14px;
  opacity: 0.95;
}

.value {
  font-size: 34px;
  margin-top: 6px;
  font-weight: 700;
}

.panel-card {
  border-radius: 14px;
  border: 1px solid rgba(15, 42, 70, 0.08);
  background: rgba(255, 255, 255, 0.9);
}

.rank-item {
  display: grid;
  grid-template-columns: 160px 1fr 60px;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.rank-item .name {
  color: #334155;
}

.rank-item .count {
  text-align: right;
  color: #0f172a;
  font-weight: 600;
}

@media (max-width: 900px) {
  .rank-item {
    grid-template-columns: 1fr;
  }

  .rank-item .count {
    text-align: left;
  }
}
</style>