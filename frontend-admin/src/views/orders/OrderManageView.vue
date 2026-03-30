<template>
  <el-card class="page-card" shadow="never">
    <el-form :inline="true" :model="query" class="query-form">
      <el-form-item label="用户ID">
        <el-input v-model="query.userId" placeholder="输入用户ID" clearable />
      </el-form-item>
      <el-form-item label="订单状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 130px">
          <el-option label="待支付" :value="1" />
          <el-option label="已支付" :value="2" />
          <el-option label="已发货" :value="3" />
          <el-option label="已完成" :value="4" />
          <el-option label="已取消" :value="5" />
          <el-option label="已评价" :value="6" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付方式">
        <el-select v-model="query.paymentType" clearable placeholder="全部" style="width: 130px">
          <el-option label="支付宝" :value="1" />
          <el-option label="微信" :value="2" />
          <el-option label="余额" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="id" label="订单号" min-width="180" show-overflow-tooltip />
      <el-table-column prop="userId" label="用户ID" width="110" />
      <el-table-column prop="nickName" label="用户昵称" min-width="130" />
      <el-table-column label="金额" width="120">
        <template #default="{ row }">￥{{ money(row.totalFee) }}</template>
      </el-table-column>
      <el-table-column label="支付方式" width="110">
        <template #default="{ row }">{{ paymentText(row.paymentType) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showDetail(row)">详情</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.pageNo"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="load"
        @size-change="load"
      />
    </div>
  </el-card>

  <el-drawer v-model="detailVisible" title="订单详情" size="760px">
    <template v-if="detailRow">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detailRow.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detailRow.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户昵称">{{ detailRow.nickName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ paymentText(detailRow.paymentType) }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">{{ statusText(detailRow.status) }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">￥{{ money(detailRow.totalFee) }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ detailRow.receiverContact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailRow.receiverMobile || '-' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="收货地址">{{ detailRow.receiverAddress || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>商品明细</el-divider>
      <el-table :data="detailRow.details || []" size="small">
        <el-table-column prop="itemId" label="商品ID" width="130" />
        <el-table-column prop="name" label="商品名称" min-width="220" />
        <el-table-column prop="num" label="数量" width="90" />
        <el-table-column label="单价" width="120">
          <template #default="{ row }">￥{{ money(row.price) }}</template>
        </el-table-column>
      </el-table>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { deleteOrder, getOrderDetail, listOrders } from '@/api/orders';
import type { OrderModel } from '@/types/domain';

const loading = ref(false);
const rows = ref<OrderModel[]>([]);
const total = ref(0);
const detailVisible = ref(false);
const detailRow = ref<OrderModel | null>(null);

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: '',
  status: undefined as number | undefined,
  paymentType: undefined as number | undefined
});

function toStatus(status: unknown) {
  if (typeof status === 'number') return status;
  if (typeof status === 'string') return Number(status) || 0;
  if (status && typeof status === 'object' && 'value' in (status as any)) {
    return Number((status as any).value) || 0;
  }
  return 0;
}

function statusText(status: unknown) {
  const value = toStatus(status);
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

function statusType(status: unknown): 'success' | 'warning' | 'info' | 'danger' {
  const value = toStatus(status);
  if (value === 4 || value === 6) return 'success';
  if (value === 5) return 'danger';
  if (value === 1) return 'warning';
  return 'info';
}

function paymentText(type?: number) {
  const map: Record<number, string> = {
    1: '支付宝',
    2: '微信',
    3: '余额'
  };
  return map[type || 0] || '-';
}

function money(amount?: number) {
  if (amount === undefined || amount === null) return '0.00';
  return (Number(amount) / 100).toFixed(2);
}

async function load() {
  loading.value = true;
  try {
    const params = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      paymentType: query.paymentType,
      userId: query.userId ? Number(query.userId) : undefined
    };
    const res = await listOrders(params);
    rows.value = res.rows || [];
    total.value = res.total || 0;
  } finally {
    loading.value = false;
  }
}

function search() {
  query.pageNo = 1;
  load();
}

function reset() {
  query.pageNo = 1;
  query.pageSize = 10;
  query.userId = '';
  query.status = undefined;
  query.paymentType = undefined;
  load();
}

async function showDetail(row: OrderModel) {
  const res = await getOrderDetail(row.id);
  if (res.code === 200 && res.data) {
    detailRow.value = res.data;
    detailVisible.value = true;
  }
}

async function remove(row: OrderModel) {
  await ElMessageBox.confirm(`确认删除订单 ${row.id} 吗？`, '提示', { type: 'warning' });
  const res = await deleteOrder(row.id);
  if (res.code === 200) {
    ElMessage.success('删除成功');
    load();
  }
}

onMounted(load);
</script>

<style scoped>
.page-card {
  border-radius: 14px;
  border: 1px solid rgba(15, 42, 70, 0.08);
}

.query-form {
  margin-bottom: 8px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>