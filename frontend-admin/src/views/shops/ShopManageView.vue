<template>
  <el-card class="page-card" shadow="never">
    <div class="toolbar">
      <el-form :inline="true" :model="query">
        <el-form-item label="店铺名">
          <el-input v-model="query.name" placeholder="输入店铺名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="toolbar-right">
        <el-button @click="load">刷新</el-button>
        <el-button type="primary" @click="openCreate">新增店铺</el-button>
      </div>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column type="index" label="序号" :index="rowIndex" width="80" />
      <el-table-column prop="name" label="店铺名" min-width="180" show-overflow-tooltip />
      <el-table-column label="店铺类型" width="110">
        <template #default="{ row }">
          <el-tag :type="row.isSelf === 1 ? 'danger' : 'info'">{{ row.isSelf === 1 ? '自营' : '商家' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="运费规则" min-width="220">
        <template #default="{ row }">
          {{ formatShipping(row) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" min-width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="warning" @click="openEdit(row)">编辑</el-button>
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

  <el-dialog v-model="formVisible" :title="form.id ? '编辑店铺' : '新增店铺'" width="640px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="店铺名" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="店铺类型" prop="isSelf">
        <el-radio-group v-model="form.isSelf">
          <el-radio :value="1">自营</el-radio>
          <el-radio :value="0">商家</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="运费模式" prop="shippingType">
        <el-select v-model="form.shippingType" style="width: 100%" @change="handleShippingTypeChange">
          <el-option label="包邮" value="FREE" />
          <el-option label="固定运费" value="FIXED" />
          <el-option label="满额包邮" value="THRESHOLD_FREE" />
        </el-select>
      </el-form-item>
      <el-form-item label="固定运费(分)" prop="shippingFee">
        <el-input-number v-model="form.shippingFee" :min="0" :controls="false" style="width: 100%" />
      </el-form-item>
      <el-form-item v-if="form.shippingType === 'THRESHOLD_FREE'" label="包邮门槛(分)" prop="freeShippingThreshold">
        <el-input-number v-model="form.freeShippingThreshold" :min="0" :controls="false" style="width: 100%" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status" style="width: 100%">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { createShop, deleteShop, getShopDetail, listShops, updateShop } from '@/api/shops';
import type { ShopModel } from '@/types/domain';

const loading = ref(false);
const rows = ref<ShopModel[]>([]);
const total = ref(0);
const formVisible = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  status: undefined as number | undefined
});

const form = reactive({
  id: undefined as number | string | undefined,
  name: '',
  isSelf: 1,
  shippingType: 'FREE',
  shippingFee: 0,
  freeShippingThreshold: 0,
  status: 1
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入店铺名', trigger: 'blur' }],
  isSelf: [{ required: true, message: '请选择店铺类型', trigger: 'change' }],
  shippingType: [{ required: true, message: '请选择运费模式', trigger: 'change' }],
  shippingFee: [{ required: true, message: '请输入固定运费', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  freeShippingThreshold: [{
    validator: (_rule, value, callback) => {
      if (form.shippingType === 'THRESHOLD_FREE' && (value === undefined || value === null || Number(value) <= 0)) {
        callback(new Error('请输入满额包邮门槛'));
        return;
      }
      callback();
    },
    trigger: 'change'
  }]
};

function money(amount?: number) {
  if (amount === undefined || amount === null) return '0.00';
  return (Number(amount) / 100).toFixed(2);
}

function formatShipping(row: ShopModel) {
  if (row.shippingType === 'FREE') {
    return '包邮';
  }
  if (row.shippingType === 'FIXED') {
    return `运费 ${money(row.shippingFee)} 元`;
  }
  return `满 ${money(row.freeShippingThreshold)} 元包邮，未满运费 ${money(row.shippingFee)} 元`;
}

function rowIndex(index: number) {
  return (query.pageNo - 1) * query.pageSize + index + 1;
}

function resetForm() {
  form.id = undefined;
  form.name = '';
  form.isSelf = 1;
  form.shippingType = 'FREE';
  form.shippingFee = 0;
  form.freeShippingThreshold = 0;
  form.status = 1;
}

function handleShippingTypeChange(value: string) {
  if (value === 'FREE') {
    form.shippingFee = 0;
    form.freeShippingThreshold = 0;
    return;
  }
  if (value === 'FIXED') {
    form.freeShippingThreshold = 0;
  }
}

async function load() {
  loading.value = true;
  try {
    const res = await listShops(query);
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
  query.name = '';
  query.status = undefined;
  load();
}

function openCreate() {
  resetForm();
  formVisible.value = true;
}

async function openEdit(row: ShopModel) {
  const res = await getShopDetail(row.id);
  if (res.code !== 200 || !res.data) {
    return;
  }
  resetForm();
  form.id = res.data.id;
  form.name = res.data.name || '';
  form.isSelf = res.data.isSelf ?? 1;
  form.shippingType = res.data.shippingType || 'FREE';
  form.shippingFee = res.data.shippingFee ?? 0;
  form.freeShippingThreshold = res.data.freeShippingThreshold ?? 0;
  form.status = res.data.status ?? 1;
  formVisible.value = true;
}

function buildPayload() {
  return {
    id: form.id,
    name: form.name,
    isSelf: form.isSelf,
    shippingType: form.shippingType,
    shippingFee: Number(form.shippingFee ?? 0),
    freeShippingThreshold: Number(form.freeShippingThreshold ?? 0),
    status: form.status
  };
}

async function submit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  submitLoading.value = true;
  try {
    const payload = buildPayload();
    const res = form.id ? await updateShop(payload) : await createShop(payload);
    if (res.code === 200) {
      ElMessage.success(form.id ? '更新成功' : '新增成功');
      formVisible.value = false;
      load();
    }
  } finally {
    submitLoading.value = false;
  }
}

async function remove(row: ShopModel) {
  await ElMessageBox.confirm(`确认删除店铺「${row.name}」吗？`, '提示', { type: 'warning' });
  const res = await deleteShop(row.id);
  if (res.code === 200) {
    ElMessage.success('删除成功');
    load();
  }
}

onMounted(() => {
  load();
});
</script>

<style scoped>
.page-card {
  border-radius: 14px;
  border: 1px solid rgba(15, 42, 70, 0.08);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
