<template>
  <el-card class="page-card" shadow="never">
    <div class="toolbar">
      <el-form :inline="true" :model="query">
        <el-form-item label="商品名">
          <el-input v-model="query.name" placeholder="输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="query.category" placeholder="输入分类" clearable />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="query.brand" placeholder="输入品牌" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="toolbar-right">
        <el-button @click="load">刷新</el-button>
        <el-button type="primary" @click="openCreate">新增商品</el-button>
      </div>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="110" />
      <el-table-column prop="name" label="商品名" min-width="180" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" min-width="120" />
      <el-table-column prop="brand" label="品牌" width="120" />
      <el-table-column label="价格" width="120">
        <template #default="{ row }">￥{{ money(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column prop="sold" label="销量" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" min-width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showDetail(row)">详情</el-button>
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

  <el-dialog v-model="formVisible" :title="form.id ? '编辑商品' : '新增商品'" width="880px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="商品名" prop="name">
            <el-input v-model="form.name" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="副标题">
            <el-input v-model="form.subTitle" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="主图URL" prop="image">
            <el-input v-model="form.image" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="分类" prop="category">
            <el-input v-model="form.category" placeholder="如：电子产品" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分类ID">
            <el-select v-model="form.categoryId" clearable filterable placeholder="可选">
              <el-option v-for="c in categoryOptions" :key="c.id" :label="`${c.name}(${c.id})`" :value="c.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="品牌" prop="brand">
            <el-input v-model="form.brand" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="标签">
            <el-input v-model="form.tags" placeholder="示例：新品,热销" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :span="8">
          <el-form-item label="价格(分)" prop="price">
            <el-input-number v-model="form.price" :min="0" :controls="false" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="原价(分)">
            <el-input-number v-model="form.originalPrice" :min="0" :controls="false" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="库存">
            <el-input-number v-model="form.stock" :min="0" :controls="false" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="轮播图URL" prop="bannerImagesText">
        <el-input
          v-model="form.bannerImagesText"
          type="textarea"
          :rows="3"
          placeholder="多个URL请用英文逗号或换行分隔"
        />
      </el-form-item>

      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="SKU价格(分)" prop="skuPrice">
            <el-input-number v-model="form.skuPrice" :min="0" :controls="false" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="SKU库存" prop="skuStock">
            <el-input-number v-model="form.skuStock" :min="0" :controls="false" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="SKU图URL">
        <el-input v-model="form.skuImage" />
      </el-form-item>

      <el-form-item label="SKU规格JSON">
        <el-input
          v-model="form.skuSpecsText"
          type="textarea"
          :rows="2"
          placeholder='示例：{"颜色":"黑色","内存":"256G"}'
        />
      </el-form-item>

      <el-form-item label="详情HTML">
        <el-input v-model="form.detailHtml" type="textarea" :rows="3" />
      </el-form-item>

      <el-form-item label="视频URL">
        <el-input v-model="form.videoUrl" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submit">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="商品详情" size="520px">
    <template v-if="detailData">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="商品ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="商品名">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ detailData.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ detailData.brand || '-' }}</el-descriptions-item>
        <el-descriptions-item label="价格">￥{{ money(detailData.price) }}</el-descriptions-item>
        <el-descriptions-item label="轮播图数">{{ detailData.bannerImages?.length || 0 }}</el-descriptions-item>
        <el-descriptions-item label="SKU数">{{ detailData.skuList?.length || 0 }}</el-descriptions-item>
      </el-descriptions>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { listCategoriesSimple } from '@/api/categories';
import { createItem, deleteItem, getItemDetail, listItems, updateItem } from '@/api/items';
import type { CategoryModel, ItemModel } from '@/types/domain';

const loading = ref(false);
const rows = ref<ItemModel[]>([]);
const total = ref(0);

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  category: '',
  brand: ''
});

const categoryOptions = ref<CategoryModel[]>([]);

const formVisible = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();
const form = reactive({
  id: undefined as number | string | undefined,
  name: '',
  subTitle: '',
  image: '',
  status: 1,
  category: '',
  categoryId: undefined as number | undefined,
  brand: '',
  price: undefined as number | undefined,
  originalPrice: undefined as number | undefined,
  stock: undefined as number | undefined,
  tags: '',
  detailHtml: '',
  videoUrl: '',
  bannerImagesText: '',
  skuPrice: undefined as number | undefined,
  skuStock: undefined as number | undefined,
  skuImage: '',
  skuSpecsText: '{}'
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入商品名', trigger: 'blur' }],
  image: [{ required: true, message: '请输入主图URL', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  brand: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  bannerImagesText: [{ required: true, message: '请输入轮播图URL', trigger: 'blur' }],
  skuPrice: [{ required: true, message: '请输入SKU价格', trigger: 'change' }],
  skuStock: [{ required: true, message: '请输入SKU库存', trigger: 'change' }]
};

const detailVisible = ref(false);
const detailData = ref<any>(null);

function money(amount?: number) {
  if (amount === undefined || amount === null) return '0.00';
  return (Number(amount) / 100).toFixed(2);
}

async function load() {
  loading.value = true;
  try {
    const res = await listItems(query);
    rows.value = res.rows || [];
    total.value = res.total || 0;
  } finally {
    loading.value = false;
  }
}

async function loadCategories() {
  const res = await listCategoriesSimple();
  if (res.code === 200 && Array.isArray(res.data)) {
    categoryOptions.value = res.data;
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
  query.category = '';
  query.brand = '';
  load();
}

function resetForm() {
  form.id = undefined;
  form.name = '';
  form.subTitle = '';
  form.image = '';
  form.status = 1;
  form.category = '';
  form.categoryId = undefined;
  form.brand = '';
  form.price = undefined;
  form.originalPrice = undefined;
  form.stock = undefined;
  form.tags = '';
  form.detailHtml = '';
  form.videoUrl = '';
  form.bannerImagesText = '';
  form.skuPrice = undefined;
  form.skuStock = undefined;
  form.skuImage = '';
  form.skuSpecsText = '{}';
}

function openCreate() {
  resetForm();
  formVisible.value = true;
}

async function openEdit(row: ItemModel) {
  const res = await getItemDetail(row.id);
  if (res.code !== 200 || !res.data) {
    return;
  }
  const detail = res.data;
  resetForm();
  form.id = detail.id;
  form.name = detail.name || '';
  form.subTitle = detail.subTitle || '';
  form.image = detail.image || '';
  form.status = detail.status || 1;
  form.category = detail.category || '';
  form.categoryId = detail.categoryId;
  form.brand = detail.brand || '';
  form.price = detail.price;
  form.originalPrice = detail.originalPrice;
  form.stock = detail.stock;
  form.tags = detail.tags || '';
  form.detailHtml = detail.detailHtml || '';
  form.videoUrl = detail.videoUrl || '';
  form.bannerImagesText = Array.isArray(detail.bannerImages) ? detail.bannerImages.join('\n') : '';
  if (Array.isArray(detail.skuList) && detail.skuList.length > 0) {
    const sku = detail.skuList[0];
    form.skuPrice = sku.price;
    form.skuStock = sku.stock;
    form.skuImage = sku.image || '';
    form.skuSpecsText = JSON.stringify(sku.specs || {}, null, 2);
  }
  formVisible.value = true;
}

function parseBannerImages(text: string): string[] {
  return text
    .split(/[\n,，]/)
    .map((item) => item.trim())
    .filter(Boolean);
}

function parseSkuSpecs(text: string): Record<string, string> {
  if (!text || !text.trim()) {
    return {};
  }
  try {
    const parsed = JSON.parse(text);
    return typeof parsed === 'object' && parsed ? parsed : {};
  } catch {
    return {};
  }
}

function buildPayload() {
  const payload: any = {
    name: form.name,
    subTitle: form.subTitle,
    image: form.image,
    status: form.status,
    category: form.category,
    categoryId: form.categoryId,
    brand: form.brand,
    price: Number(form.price),
    originalPrice: form.originalPrice ? Number(form.originalPrice) : undefined,
    stock: form.stock ? Number(form.stock) : undefined,
    tags: form.tags,
    detailHtml: form.detailHtml,
    videoUrl: form.videoUrl,
    bannerImages: parseBannerImages(form.bannerImagesText),
    specTemplate: [],
    skus: [
      {
        specs: parseSkuSpecs(form.skuSpecsText),
        price: Number(form.skuPrice),
        stock: Number(form.skuStock),
        image: form.skuImage || form.image
      }
    ]
  };
  if (form.id) {
    payload.id = form.id;
  }
  return payload;
}

async function submit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  submitLoading.value = true;
  try {
    const payload = buildPayload();
    const res = form.id ? await updateItem(payload) : await createItem(payload);
    if (res.code === 200) {
      ElMessage.success(form.id ? '更新成功' : '新增成功');
      formVisible.value = false;
      load();
    }
  } finally {
    submitLoading.value = false;
  }
}

async function remove(row: ItemModel) {
  await ElMessageBox.confirm(`确认删除商品「${row.name}」吗？`, '提示', { type: 'warning' });
  const res = await deleteItem(row.id);
  if (res.code === 200) {
    ElMessage.success('删除成功');
    load();
  }
}

async function showDetail(row: ItemModel) {
  const res = await getItemDetail(row.id);
  if (res.code === 200 && res.data) {
    detailData.value = res.data;
    detailVisible.value = true;
  }
}

onMounted(async () => {
  await Promise.all([load(), loadCategories()]);
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