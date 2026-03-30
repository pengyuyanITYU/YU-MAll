<template>
  <el-card class="page-card" shadow="never">
    <el-form :inline="true" :model="query" class="query-form">
      <el-form-item label="会员等级">
        <el-input v-model="query.levelName" placeholder="输入等级名称" clearable />
      </el-form-item>
      <el-form-item label="用户状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="正常" :value="1" />
          <el-option label="冻结" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="110" />
      <el-table-column prop="username" label="用户名" min-width="130" />
      <el-table-column prop="nickName" label="昵称" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="140" />
      <el-table-column prop="levelName" label="会员等级" width="120" />
      <el-table-column label="余额(元)" width="110">
        <template #default="{ row }">{{ money(row.balance) }}</template>
      </el-table-column>
      <el-table-column prop="currentPoints" label="积分" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="userStatus(row.status) === 1 ? 'success' : 'danger'">
            {{ userStatus(row.status) === 1 ? '正常' : '冻结' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="165" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showDetail(row)">详情</el-button>
          <el-button
            link
            :type="userStatus(row.status) === 1 ? 'danger' : 'success'"
            @click="switchStatus(row)"
          >
            {{ userStatus(row.status) === 1 ? '冻结' : '解冻' }}
          </el-button>
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

  <el-drawer v-model="detailVisible" title="用户详情" size="420px">
    <el-descriptions :column="1" border v-if="detailRow">
      <el-descriptions-item label="ID">{{ detailRow.id }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ detailRow.username }}</el-descriptions-item>
      <el-descriptions-item label="昵称">{{ detailRow.nickName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ detailRow.phone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ detailRow.email || '-' }}</el-descriptions-item>
      <el-descriptions-item label="会员等级">{{ detailRow.levelName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="余额">￥{{ money(detailRow.balance) }}</el-descriptions-item>
      <el-descriptions-item label="积分">{{ detailRow.currentPoints || 0 }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        {{ userStatus(detailRow.status) === 1 ? '正常' : '冻结' }}
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { getUserDetail, listUsers, updateUserStatus } from '@/api/users';
import type { UserModel } from '@/types/domain';

const loading = ref(false);
const rows = ref<UserModel[]>([]);
const total = ref(0);
const detailVisible = ref(false);
const detailRow = ref<UserModel | null>(null);

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  levelName: '',
  status: undefined as number | undefined
});

function userStatus(status: unknown): number {
  if (typeof status === 'number') return status;
  if (typeof status === 'string') {
    if (status === 'NORMAL') return 1;
    if (status === 'FROZEN') return 0;
    return Number(status) || 0;
  }
  if (status && typeof status === 'object' && 'value' in (status as any)) {
    return Number((status as any).value) || 0;
  }
  return 0;
}

function money(amount?: number) {
  if (amount === undefined || amount === null) return '0.00';
  return (Number(amount) / 100).toFixed(2);
}

async function load() {
  loading.value = true;
  try {
    const res = await listUsers(query);
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
  query.levelName = '';
  query.status = undefined;
  load();
}

async function switchStatus(row: UserModel) {
  const current = userStatus(row.status);
  const target = current === 1 ? 0 : 1;
  const res = await updateUserStatus(row.id, target);
  if (res.code === 200) {
    ElMessage.success(target === 1 ? '已解冻' : '已冻结');
    load();
  }
}

async function showDetail(row: UserModel) {
  const res = await getUserDetail(row.id);
  if (res.code === 200 && res.data) {
    detailRow.value = res.data;
    detailVisible.value = true;
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