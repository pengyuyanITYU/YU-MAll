<template>
  <el-card class="page-card" shadow="never">
    <div class="toolbar">
      <el-form :inline="true" :model="query">
        <el-form-item label="商品名称">
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
      <el-table-column type="index" label="序号" :index="rowIndex" width="80" />
      <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
      <el-table-column label="店铺" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="shop-cell">
            <span>{{ row.shopName || '-' }}</span>
            <el-tag v-if="row.isSelf === 1" size="small" type="danger" effect="plain">自营</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" min-width="120" />
      <el-table-column prop="brand" label="品牌" width="120" />
      <el-table-column label="价格" width="120">
        <template #default="{ row }">¥{{ money(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column prop="sold" label="销量" width="90" />
      <el-table-column label="运费说明" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.shippingDesc || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
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

  <el-dialog v-model="formVisible" :title="form.id ? '编辑商品' : '新增商品'" width="960px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="商品名称" prop="name">
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
          <el-form-item label="所属店铺" prop="shopId">
            <el-select v-model="form.shopId" clearable filterable placeholder="请选择店铺" style="width: 100%">
              <el-option v-for="shop in shopOptions" :key="shop.id" :label="shop.name" :value="Number(shop.id)">
                <div class="shop-option">
                  <span>{{ shop.name }}</span>
                  <span class="shop-option-meta">{{ shop.isSelf === 1 ? '自营' : '商家' }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="主图" prop="image">
            <div class="image-upload-block">
              <el-upload :show-file-list="false" :http-request="uploadMainImage" :before-upload="beforeImageUpload">
                <el-button type="primary" plain :loading="mainImageUploading">上传主图</el-button>
              </el-upload>
              <el-image v-if="form.image" :src="form.image" fit="cover" class="preview-image" />
              <el-button v-if="form.image" link type="danger" @click="form.image = ''">清空</el-button>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分类" prop="categoryId">
            <el-select v-model="form.categoryId" clearable filterable placeholder="请选择分类" style="width: 100%" @change="onCategoryChange">
              <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="品牌" prop="brand">
            <el-select v-model="form.brand" clearable filterable placeholder="请选择品牌" style="width: 100%">
              <el-option v-for="brand in brandOptions" :key="brand.id" :label="brand.name" :value="brand.name" />
            </el-select>
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

      <el-form-item label="轮播图" prop="bannerImagesText">
        <el-upload
          v-model:file-list="bannerImageFileList"
          list-type="picture-card"
          :http-request="uploadBannerImage"
          :before-upload="beforeImageUpload"
          :on-success="handleBannerUploadSuccess"
          :on-remove="handleBannerRemove"
          :disabled="bannerImageUploading"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>

      <el-form-item label="SKU 列表" required>
        <div class="sku-list-editor">
          <div v-for="(sku, skuIndex) in form.skus" :key="sku.localKey" class="sku-card">
            <div class="sku-card-head">
              <span class="sku-title">SKU {{ skuIndex + 1 }}</span>
              <el-button link type="danger" :disabled="form.skus.length === 1" @click="removeSku(skuIndex)">删除 SKU</el-button>
            </div>

            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item label="价格(分)" label-width="70px">
                  <el-input-number v-model="sku.price" :min="0" :controls="false" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="库存" label-width="70px">
                  <el-input-number v-model="sku.stock" :min="0" :controls="false" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="图片" label-width="70px">
                  <div class="image-upload-block">
                    <el-upload
                      :show-file-list="false"
                      :http-request="(options) => uploadSkuImage(options, skuIndex)"
                      :before-upload="beforeImageUpload"
                    >
                      <el-button type="primary" plain size="small" :loading="skuUploadingIndex === skuIndex">上传</el-button>
                    </el-upload>
                    <el-image v-if="sku.image" :src="sku.image" fit="cover" class="preview-image preview-image-small" />
                    <el-button v-if="sku.image" link type="danger" @click="sku.image = ''">清空</el-button>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>

            <div class="sku-specs-editor">
              <div v-for="(value, key) in sku.specsObj" :key="`${sku.localKey}-${key}`" class="spec-item">
                <el-input :model-value="key" disabled style="width: 160px" />
                <el-input v-model="sku.specsObj[key]" placeholder="规格值" style="width: 180px" />
                <el-button type="danger" link @click="deleteSkuSpec(skuIndex, key as string)">删除</el-button>
              </div>
              <el-button type="primary" link @click="addSkuSpec(skuIndex)">+ 添加规格</el-button>
            </div>
          </div>

          <el-button type="primary" plain @click="addSku">+ 新增 SKU</el-button>
        </div>
      </el-form-item>

      <el-form-item label="详情 HTML">
        <el-input v-model="form.detailHtml" type="textarea" :rows="3" />
      </el-form-item>

      <el-form-item label="视频 URL">
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
        <el-descriptions-item label="商品 ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="所属店铺">
          {{ detailData.shopName || '-' }}
          <el-tag v-if="detailData.isSelf === 1" size="small" type="danger" effect="plain">自营</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="运费说明">{{ detailData.shippingDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ detailData.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ detailData.brand || '-' }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ money(detailData.price) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="轮播图数量">{{ detailData.bannerImages?.length || 0 }}</el-descriptions-item>
        <el-descriptions-item label="SKU 数量">{{ detailData.skuList?.length || 0 }}</el-descriptions-item>
      </el-descriptions>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadProps, type UploadRequestOptions, type UploadUserFile } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listBrandsSimple } from '@/api/brands'
import { listCategoriesSimple } from '@/api/categories'
import { createItem, deleteItem, getItemDetail, listItems, updateItem } from '@/api/items'
import { listShopSimple } from '@/api/shops'
import { uploadFile } from '@/api/upload'
import type { BrandModel, CategoryModel, ItemModel, ShopModel } from '@/types/domain'

type ItemStatusValue = number | string | boolean | { value?: ItemStatusValue } | null | undefined

interface SkuEditModel {
  localKey: string
  id?: number | string
  price?: number
  stock?: number
  image: string
  specsObj: Record<string, string>
}

const loading = ref(false)
const rows = ref<ItemModel[]>([])
const total = ref(0)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  category: '',
  brand: ''
})

const categoryOptions = ref<CategoryModel[]>([])
const brandOptions = ref<BrandModel[]>([])
const shopOptions = ref<ShopModel[]>([])

const formVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const mainImageUploading = ref(false)
const bannerImageUploading = ref(false)
const bannerImageFileList = ref<UploadUserFile[]>([])
const skuUploadingIndex = ref<number | null>(null)

let skuKeySeed = 0
const nextSkuKey = () => {
  skuKeySeed += 1
  return `sku-${Date.now()}-${skuKeySeed}`
}

const createSku = (initial?: Partial<SkuEditModel>): SkuEditModel => ({
  localKey: nextSkuKey(),
  id: initial?.id,
  price: initial?.price,
  stock: initial?.stock,
  image: initial?.image || '',
  specsObj: initial?.specsObj ? { ...initial.specsObj } : {}
})

const normalizeItemStatus = (status: ItemStatusValue, fallback: 1 | 2 = 2): 1 | 2 => {
  if (typeof status === 'object' && status !== null && 'value' in status) {
    return normalizeItemStatus(status.value, fallback)
  }
  if (status === true || status === 'true' || status === '上架') {
    return 1
  }
  if (status === false || status === 'false' || status === '下架') {
    return 2
  }
  if (typeof status === 'number') {
    if (status === 1) {
      return 1
    }
    if (status === 2 || status === 0) {
      return 2
    }
  }
  if (typeof status === 'string') {
    const trimmed = status.trim()
    if (trimmed === '1') {
      return 1
    }
    if (trimmed === '2' || trimmed === '0') {
      return 2
    }
  }
  return fallback
}

const getStatusText = (status: ItemStatusValue) => (normalizeItemStatus(status) === 1 ? '上架' : '下架')
const getStatusTagType = (status: ItemStatusValue) => (normalizeItemStatus(status) === 1 ? 'success' : 'info')

const form = reactive({
  id: undefined as number | string | undefined,
  name: '',
  subTitle: '',
  image: '',
  status: 1 as 1 | 2,
  category: '',
  categoryId: undefined as number | undefined,
  shopId: undefined as number | undefined,
  brand: '',
  price: undefined as number | undefined,
  originalPrice: undefined as number | undefined,
  stock: undefined as number | undefined,
  tags: '',
  detailHtml: '',
  videoUrl: '',
  bannerImagesText: '',
  specTemplate: [] as any[],
  skus: [createSku()] as SkuEditModel[]
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  image: [{ required: true, message: '请上传主图', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  shopId: [{ required: true, message: '请选择所属店铺', trigger: 'change' }],
  brand: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  bannerImagesText: [{ required: true, message: '请上传轮播图', trigger: 'change' }]
}

const detailVisible = ref(false)
const detailData = ref<any>(null)

const money = (amount?: number) => {
  if (amount === undefined || amount === null) {
    return '0.00'
  }
  return (Number(amount) / 100).toFixed(2)
}

const rowIndex = (index: number) => (query.pageNo - 1) * query.pageSize + index + 1

const extractUploadedUrl = (response: any) => response?.data?.url || response?.url || response?.data?.data?.url || ''

const syncBannerImagesTextFromList = () => {
  form.bannerImagesText = bannerImageFileList.value
    .map((file) => (file.url || '').trim())
    .filter(Boolean)
    .join('\n')
}

const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isImage = rawFile.type.startsWith('image/')
  const isLt5M = rawFile.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只允许上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const uploadMainImage = async (options: UploadRequestOptions) => {
  mainImageUploading.value = true
  try {
    const res = await uploadFile(options.file as File)
    const url = extractUploadedUrl(res)
    if (!url) {
      throw new Error('上传结果缺少图片地址')
    }
    form.image = url
    options.onSuccess?.(res as any)
    ElMessage.success('主图上传成功')
  } catch (error) {
    options.onError?.(error as any)
    ElMessage.error('主图上传失败')
  } finally {
    mainImageUploading.value = false
  }
}

const uploadBannerImage = async (options: UploadRequestOptions) => {
  bannerImageUploading.value = true
  try {
    const res = await uploadFile(options.file as File)
    const url = extractUploadedUrl(res)
    if (!url) {
      throw new Error('上传结果缺少图片地址')
    }
    options.onSuccess?.({ ...res, data: { ...(res as any).data, url } } as any)
    ElMessage.success('轮播图上传成功')
  } catch (error) {
    options.onError?.(error as any)
    ElMessage.error('轮播图上传失败')
  } finally {
    bannerImageUploading.value = false
  }
}

const handleBannerUploadSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  const url = extractUploadedUrl(response)
  if (!url) {
    ElMessage.error('轮播图地址解析失败')
    return
  }
  uploadFile.url = url
  syncBannerImagesTextFromList()
}

const handleBannerRemove: UploadProps['onRemove'] = () => {
  syncBannerImagesTextFromList()
}

const uploadSkuImage = async (options: UploadRequestOptions, skuIndex: number) => {
  skuUploadingIndex.value = skuIndex
  try {
    const res = await uploadFile(options.file as File)
    const url = extractUploadedUrl(res)
    if (!url) {
      throw new Error('上传结果缺少图片地址')
    }
    const sku = form.skus[skuIndex]
    if (sku) {
      sku.image = url
    }
    options.onSuccess?.(res as any)
    ElMessage.success('SKU 图片上传成功')
  } catch (error) {
    options.onError?.(error as any)
    ElMessage.error('SKU 图片上传失败')
  } finally {
    skuUploadingIndex.value = null
  }
}

const load = async () => {
  loading.value = true
  try {
    const res = await listItems({ ...query })
    rows.value = Array.isArray(res.rows) ? res.rows : []
    total.value = Number(res.total || 0)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  const res = await listCategoriesSimple()
  if (res.code === 200 && Array.isArray(res.data)) {
    categoryOptions.value = res.data
  }
}

const loadBrands = async () => {
  const res = await listBrandsSimple()
  if (res.code === 200 && Array.isArray(res.data)) {
    brandOptions.value = res.data
  }
}

const loadShops = async () => {
  const res = await listShopSimple()
  const data = Array.isArray(res?.data) ? res.data : []
  shopOptions.value = data
}

const onCategoryChange = (value?: number) => {
  if (value === undefined || value === null) {
    form.category = ''
    return
  }
  const matched = categoryOptions.value.find((c) => c.id === Number(value))
  form.category = matched?.name || ''
}

const search = () => {
  query.pageNo = 1
  load()
}

const reset = () => {
  query.pageNo = 1
  query.pageSize = 10
  query.name = ''
  query.category = ''
  query.brand = ''
  load()
}

const resetForm = () => {
  form.id = undefined
  form.name = ''
  form.subTitle = ''
  form.image = ''
  form.status = 1
  form.category = ''
  form.categoryId = undefined
  form.shopId = undefined
  form.brand = ''
  form.price = undefined
  form.originalPrice = undefined
  form.stock = undefined
  form.tags = ''
  form.detailHtml = ''
  form.videoUrl = ''
  form.bannerImagesText = ''
  bannerImageFileList.value = []
  form.specTemplate = []
  form.skus = [createSku()]
}

const openCreate = () => {
  resetForm()
  formVisible.value = true
}

const addSku = () => {
  form.skus.push(createSku({ image: form.image || '' }))
}

const removeSku = (index: number) => {
  if (form.skus.length === 1) {
    form.skus[0] = createSku({ image: form.image || '' })
    return
  }
  form.skus.splice(index, 1)
}

const addSkuSpec = (skuIndex: number) => {
  ElMessageBox.prompt('请输入规格名称', '添加规格', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '规格名称不能为空'
  })
    .then(({ value }) => {
      const sku = form.skus[skuIndex]
      if (!sku) {
        return
      }
      if (!Object.prototype.hasOwnProperty.call(sku.specsObj, value)) {
        sku.specsObj[value] = ''
      }
    })
    .catch(() => undefined)
}

const deleteSkuSpec = (skuIndex: number, key: string) => {
  const sku = form.skus[skuIndex]
  if (!sku) {
    return
  }
  delete sku.specsObj[key]
}

const sanitizeSpecs = (specsObj: Record<string, string> | undefined) => {
  if (!specsObj) {
    return {}
  }
  return Object.entries(specsObj).reduce<Record<string, string>>((acc, [key, value]) => {
    const specKey = (key || '').trim()
    const specValue = (value || '').trim()
    if (specKey && specValue) {
      acc[specKey] = specValue
    }
    return acc
  }, {})
}

const parseBannerImages = (text: string): string[] => {
  return text
    .split(/[\n,，]/)
    .map((item) => item.trim())
    .filter(Boolean)
}

const toOptionalNumber = (value?: number) => {
  if (value === undefined || value === null) {
    return undefined
  }
  return Number(value)
}

const validateSkuList = () => {
  if (!Array.isArray(form.skus) || form.skus.length === 0) {
    ElMessage.error('请至少添加一个 SKU')
    return false
  }

  const signatureSet = new Set<string>()
  for (let i = 0; i < form.skus.length; i += 1) {
    const sku = form.skus[i]
    if (sku.price === undefined || sku.price === null) {
      ElMessage.error(`请填写 SKU ${i + 1} 的价格`)
      return false
    }
    if (sku.stock === undefined || sku.stock === null) {
      ElMessage.error(`请填写 SKU ${i + 1} 的库存`)
      return false
    }
    const specs = sanitizeSpecs(sku.specsObj)
    if (Object.keys(specs).length === 0) {
      ElMessage.error(`请填写 SKU ${i + 1} 的规格`)
      return false
    }
    const signature = Object.entries(specs)
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([key, value]) => `${key}:${value}`)
      .join('|')
    if (signatureSet.has(signature)) {
      ElMessage.error(`SKU ${i + 1} 的规格与其他 SKU 重复`)
      return false
    }
    signatureSet.add(signature)
  }

  return true
}

const openEdit = async (row: ItemModel) => {
  const res = await getItemDetail(row.id)
  if (res.code !== 200 || !res.data) {
    return
  }

  const detail = res.data
  resetForm()
  form.id = detail.id
  form.name = detail.name || ''
  form.subTitle = detail.subTitle || ''
  form.image = detail.image || ''
  form.status = normalizeItemStatus(detail.status, 1)
  form.category = detail.category || ''
  form.categoryId = detail.categoryId
  form.shopId = detail.shopId ? Number(detail.shopId) : undefined
  if (form.categoryId !== undefined && form.categoryId !== null) {
    onCategoryChange(form.categoryId)
  } else if (form.category) {
    const matchedCategory = categoryOptions.value.find((c) => c.name === form.category)
    if (matchedCategory) {
      form.categoryId = matchedCategory.id
      onCategoryChange(matchedCategory.id)
    }
  }
  form.brand = detail.brand || ''
  form.price = detail.price
  form.originalPrice = detail.originalPrice
  form.stock = detail.stock
  form.tags = detail.tags || ''
  form.detailHtml = detail.detailHtml || ''
  form.videoUrl = detail.videoUrl || ''
  form.specTemplate = Array.isArray(detail.specTemplate) ? [...detail.specTemplate] : []
  const bannerImages = Array.isArray(detail.bannerImages) ? detail.bannerImages.filter(Boolean) : []
  form.bannerImagesText = bannerImages.join('\n')
  bannerImageFileList.value = bannerImages.map((url: string, index: number) => ({
    name: `banner-${index + 1}`,
    url,
    status: 'success'
  }))

  if (Array.isArray(detail.skuList) && detail.skuList.length > 0) {
    form.skus = detail.skuList.map((sku: any) =>
      createSku({
        id: sku.id,
        price: sku.price,
        stock: sku.stock,
        image: sku.image || '',
        specsObj: sanitizeSpecs(sku.specs)
      })
    )
  } else {
    form.skus = [createSku({ image: form.image || '' })]
  }

  formVisible.value = true
}

const buildPayload = () => {
  const payloadSkus = form.skus.map((sku) => {
    const payloadSku: any = {
      specs: sanitizeSpecs(sku.specsObj),
      price: Number(sku.price),
      stock: Number(sku.stock),
      image: sku.image || form.image
    }
    if (sku.id !== undefined && sku.id !== null) {
      payloadSku.id = Number(sku.id)
    }
    return payloadSku
  })

  const payload: any = {
    name: form.name,
    subTitle: form.subTitle,
    image: form.image,
    status: normalizeItemStatus(form.status, 1),
    category: categoryOptions.value.find((c) => c.id === form.categoryId)?.name || form.category,
    categoryId: form.categoryId,
    shopId: form.shopId,
    brand: form.brand,
    price: Number(form.price),
    originalPrice: toOptionalNumber(form.originalPrice),
    stock: toOptionalNumber(form.stock),
    tags: form.tags,
    detailHtml: form.detailHtml,
    videoUrl: form.videoUrl,
    bannerImages: parseBannerImages(form.bannerImagesText),
    specTemplate: Array.isArray(form.specTemplate) ? form.specTemplate : [],
    skus: payloadSkus
  }

  if (form.id) {
    payload.id = form.id
  }
  return payload
}

const submit = async () => {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || !validateSkuList()) {
    return
  }

  submitLoading.value = true
  try {
    const payload = buildPayload()
    const res = form.id ? await updateItem(payload) : await createItem(payload)
    if (res.code === 200) {
      ElMessage.success(form.id ? '更新成功' : '新增成功')
      formVisible.value = false
      load()
    }
  } finally {
    submitLoading.value = false
  }
}

const remove = async (row: ItemModel) => {
  await ElMessageBox.confirm(`确认删除商品“${row.name}”吗？`, '提示', { type: 'warning' })
  const res = await deleteItem(row.id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    load()
  }
}

const showDetail = async (row: ItemModel) => {
  const res = await getItemDetail(row.id)
  if (res.code === 200 && res.data) {
    detailData.value = res.data
    detailVisible.value = true
  }
}

onMounted(async () => {
  await Promise.all([load(), loadCategories(), loadBrands(), loadShops()])
})
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

.image-upload-block {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.preview-image {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  border: 1px solid rgba(15, 42, 70, 0.14);
}

.preview-image-small {
  width: 40px;
  height: 40px;
}

.sku-list-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sku-card {
  border: 1px solid rgba(15, 42, 70, 0.12);
  border-radius: 8px;
  padding: 12px;
  background: #fafcff;
}

.sku-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.sku-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
}

.sku-specs-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.spec-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.shop-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.shop-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.shop-option-meta {
  color: #909399;
  font-size: 12px;
}
</style>
