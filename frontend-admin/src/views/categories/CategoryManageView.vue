<template>
  <el-card class="page-card" shadow="never">
    <div class="toolbar">
      <el-form :inline="true" :model="query">
        <el-form-item label="分类名称">
          <el-input v-model="query.name" clearable placeholder="输入分类关键词" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="120" />
      <el-table-column prop="name" label="分类名称" min-width="220" />
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column prop="updateTime" label="更新时间" min-width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="420px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="form.name" maxlength="20" show-word-limit placeholder="请输入分类名称" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submit">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { createCategory, deleteCategory, listCategories, updateCategory } from '@/api/categories';
import type { CategoryModel } from '@/types/domain';

const loading = ref(false);
const rows = ref<CategoryModel[]>([]);
const total = ref(0);

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  name: ''
});

const dialogVisible = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();
const form = reactive({
  id: undefined as number | undefined,
  name: ''
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
};

async function load() {
  loading.value = true;
  try {
    const res = await listCategories(query);
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
  load();
}

function openCreate() {
  form.id = undefined;
  form.name = '';
  dialogVisible.value = true;
}

function openEdit(row: CategoryModel) {
  form.id = row.id;
  form.name = row.name;
  dialogVisible.value = true;
}

async function submit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  submitLoading.value = true;
  try {
    const res = form.id
      ? await updateCategory({ id: form.id, name: form.name })
      : await createCategory({ name: form.name });
    if (res.code === 200) {
      ElMessage.success(form.id ? '更新成功' : '新增成功');
      dialogVisible.value = false;
      load();
    }
  } finally {
    submitLoading.value = false;
  }
}

async function remove(row: CategoryModel) {
  await ElMessageBox.confirm(`确认删除分类「${row.name}」吗？`, '提示', {
    type: 'warning'
  });
  const res = await deleteCategory(row.id);
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

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 12px;
  flex-wrap: wrap;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>