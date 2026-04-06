<template>
  <el-card class="page-card" shadow="never">
    <div class="toolbar">
      <el-form :inline="true" :model="query">
        <el-form-item label="商品名称">
          <el-input v-model="query.itemName" placeholder="输入商品名" clearable />
        </el-form-item>
        <el-form-item label="用户昵称">
          <el-input v-model="query.userNickname" placeholder="输入用户昵称" clearable />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="toolbar-right">
        <el-button @click="load">刷新</el-button>
      </div>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column type="index" label="序号" :index="rowIndex" width="80" />
      <el-table-column prop="itemName" label="商品" min-width="180" show-overflow-tooltip />
      <el-table-column prop="userNickname" label="用户" width="140" />
      <el-table-column label="评分" width="120">
        <template #default="{ row }">
          <el-rate :model-value="row.rating" disabled />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评论内容" min-width="240" show-overflow-tooltip />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusMeta(row.status).type">{{ statusMeta(row.status).text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rejectReason" label="驳回原因" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button v-if="row.status !== 1" link type="success" @click="handleApprove(row)">通过</el-button>
          <el-button link type="danger" @click="openReject(row)">驳回</el-button>
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

  <el-dialog v-model="detailVisible" title="评论详情" width="720px">
    <div v-if="detailItem" class="detail-wrap">
      <div class="detail-head">
        <el-image v-if="detailItem.itemImage" :src="detailItem.itemImage" class="detail-image" fit="cover" />
        <div class="detail-meta">
          <div class="detail-title">{{ detailItem.itemName || '-' }}</div>
          <div class="detail-sub">用户：{{ detailItem.userNickname || '-' }}</div>
          <div class="detail-sub">状态：{{ statusMeta(detailItem.status).text }}</div>
          <div class="detail-sub">规格：{{ formatSpecs(detailItem.skuSpecs) || '-' }}</div>
        </div>
      </div>
      <div class="detail-content">
        <el-rate :model-value="detailItem.rating" disabled />
        <p class="detail-text">{{ detailItem.content || '-' }}</p>
        <p v-if="detailItem.rejectReason" class="detail-reject">驳回原因：{{ detailItem.rejectReason }}</p>
        <div v-if="detailItem.images?.length" class="detail-images">
          <el-image
            v-for="image in detailItem.images"
            :key="image"
            :src="image"
            class="detail-image-item"
            :preview-src-list="detailItem.images"
            fit="cover"
          />
        </div>
      </div>
    </div>
  </el-dialog>

  <el-dialog v-model="rejectVisible" title="驳回评论" width="480px" destroy-on-close>
    <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="88px">
      <el-form-item label="驳回原因" prop="rejectReason">
        <el-input
          v-model="rejectForm.rejectReason"
          type="textarea"
          :rows="4"
          maxlength="255"
          show-word-limit
          placeholder="请输入驳回原因"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="rejectVisible = false">取消</el-button>
      <el-button type="danger" :loading="submitLoading" @click="submitReject">确认驳回</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { approveComment, getCommentDetail, listComments, rejectComment } from '@/api/comments';
import type { CommentReviewModel } from '@/types/domain';

const loading = ref(false);
const submitLoading = ref(false);
const rows = ref<CommentReviewModel[]>([]);
const total = ref(0);
const detailVisible = ref(false);
const rejectVisible = ref(false);
const detailItem = ref<CommentReviewModel | null>(null);
const currentRow = ref<CommentReviewModel | null>(null);
const rejectFormRef = ref<FormInstance>();

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  itemName: '',
  userNickname: '',
  status: undefined as number | undefined
});

const rejectForm = reactive({
  rejectReason: ''
});

const rejectRules: FormRules = {
  rejectReason: [{ required: true, message: '请输入驳回原因', trigger: 'blur' }]
};

function statusMeta(status?: number) {
  const realStatus = Number(status ?? 0);
  if (realStatus === 1) {
    return { text: '已通过', type: 'success' as const };
  }
  if (realStatus === 2) {
    return { text: '已驳回', type: 'danger' as const };
  }
  return { text: '待审核', type: 'warning' as const };
}

function formatSpecs(specs?: Record<string, string> | string) {
  if (!specs) return '';
  if (typeof specs === 'string') {
    return specs;
  }
  return Object.entries(specs).map(([key, value]) => `${key}: ${value}`).join(' / ');
}

function rowIndex(index: number) {
  return (query.pageNo - 1) * query.pageSize + index + 1;
}

async function load() {
  loading.value = true;
  try {
    const res = await listComments(query);
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
  query.itemName = '';
  query.userNickname = '';
  query.status = undefined;
  load();
}

async function openDetail(row: CommentReviewModel) {
  const res = await getCommentDetail(row.id);
  if (res.code !== 200 || !res.data) {
    return;
  }
  detailItem.value = { ...row, ...res.data };
  detailVisible.value = true;
}

async function handleApprove(row: CommentReviewModel) {
  await ElMessageBox.confirm(`确认通过评论 #${row.id} 吗？`, '审核通过', { type: 'warning' });
  const res = await approveComment(row.id);
  if (res.code === 200) {
    ElMessage.success('审核通过成功');
    load();
  }
}

function openReject(row: CommentReviewModel) {
  currentRow.value = row;
  rejectForm.rejectReason = row.rejectReason || '';
  rejectVisible.value = true;
}

async function submitReject() {
  if (!rejectFormRef.value || !currentRow.value) return;
  const valid = await rejectFormRef.value.validate().catch(() => false);
  if (!valid) return;

  submitLoading.value = true;
  try {
    const res = await rejectComment(currentRow.value.id, { rejectReason: rejectForm.rejectReason.trim() });
    if (res.code === 200) {
      ElMessage.success('驳回成功');
      rejectVisible.value = false;
      load();
    }
  } finally {
    submitLoading.value = false;
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

.detail-wrap {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-head {
  display: flex;
  gap: 16px;
}

.detail-image {
  width: 92px;
  height: 92px;
  border-radius: 12px;
  overflow: hidden;
}

.detail-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.detail-sub {
  color: #4b5563;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-text {
  margin: 0;
  color: #111827;
  line-height: 1.7;
  white-space: pre-wrap;
}

.detail-reject {
  margin: 0;
  color: #b42318;
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-image-item {
  width: 96px;
  height: 96px;
  border-radius: 10px;
}
</style>
