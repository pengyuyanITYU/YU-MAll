<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Goods, ShoppingCart, Star } from '@element-plus/icons-vue'
import { addItem2Cart } from '@/api/cart'
import { searchItems, type SearchItemModel } from '@/api/search'
import { applyProductImageFallback, resolveProductImage } from '@/utils/image'
import { isHandledRequestError } from '@/utils/request'

type SortMode = 'default' | 'sold' | 'rating' | 'priceAsc' | 'priceDesc'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const total = ref(0)
const sortMode = ref<SortMode>('default')
const productList = ref<SearchItemModel[]>([])
const state = reactive({
  pageNo: 1,
  pageSize: 20,
  minPrice: '',
  maxPrice: ''
})

const keywordText = computed(() => {
  const raw = route.query.q
  return typeof raw === 'string' ? raw.trim() : ''
})

const categoryText = computed(() => {
  const raw = route.query.category
  return typeof raw === 'string' ? raw.trim() : ''
})

const resultTitle = computed(() => {
  if (categoryText.value && keywordText.value) {
    return `${categoryText.value} / ${keywordText.value}`
  }
  return categoryText.value || keywordText.value || '全部商品'
})

const resultDesc = computed(() => {
  if (categoryText.value && keywordText.value) {
    return `当前按分类“${categoryText.value}”和关键词“${keywordText.value}”筛选商品。`
  }
  if (categoryText.value) {
    return `当前按分类“${categoryText.value}”筛选商品。`
  }
  if (keywordText.value) {
    return `当前按关键词“${keywordText.value}”搜索商品。`
  }
  return '当前展示全部可售商品。'
})

const hasFilter = computed(() => Boolean(keywordText.value || categoryText.value))

const parsePriceToFen = (value: string) => {
  if (!value.trim()) {
    return undefined
  }
  const amount = Number(value)
  if (Number.isNaN(amount) || amount < 0) {
    return null
  }
  return Math.round(amount * 100)
}

const formatPrice = (price?: number | string | null) => {
  if (price === undefined || price === null || price === '') {
    return '0.00'
  }
  return (Number(price) / 100).toFixed(2)
}

const getPositiveRateText = (item: SearchItemModel) => {
  if (!item.commentCount) {
    return '暂无评价'
  }
  return `${item.positiveRate ?? 0}%好评`
}

const buildSortParams = () => {
  if (sortMode.value === 'sold') {
    return { sortBy: 'sold', isAsc: false }
  }
  if (sortMode.value === 'rating') {
    return { sortBy: 'avgScore', isAsc: false }
  }
  if (sortMode.value === 'priceAsc') {
    return { sortBy: 'price', isAsc: true }
  }
  if (sortMode.value === 'priceDesc') {
    return { sortBy: 'price', isAsc: false }
  }
  return {}
}

const fetchProducts = async (resetPage = false) => {
  if (resetPage) {
    state.pageNo = 1
  }

  const minPrice = parsePriceToFen(state.minPrice)
  const maxPrice = parsePriceToFen(state.maxPrice)
  if (minPrice === null || maxPrice === null) {
    ElMessage.warning('价格区间必须是非负数字')
    return
  }
  if (minPrice !== undefined && maxPrice !== undefined && minPrice > maxPrice) {
    ElMessage.warning('最低价不能大于最高价')
    return
  }

  loading.value = true
  try {
    const res: any = await searchItems({
      pageNo: state.pageNo,
      pageSize: state.pageSize,
      keyword: keywordText.value || undefined,
      category: categoryText.value || undefined,
      minPrice,
      maxPrice,
      ...buildSortParams()
    })

    productList.value = Array.isArray(res?.rows) ? res.rows : []
    total.value = Number(res?.total || 0)
  } catch (error) {
    console.error('加载搜索结果失败', error)
    productList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSortChange = (mode: SortMode) => {
  sortMode.value = mode
  fetchProducts(true)
}

const handlePageChange = (pageNo: number) => {
  state.pageNo = pageNo
  fetchProducts()
}

const handleSizeChange = (pageSize: number) => {
  state.pageSize = pageSize
  state.pageNo = 1
  fetchProducts()
}

const goToDetail = (id: number) => {
  router.push(`/item/${id}`)
}

const addToCart = async (item: SearchItemModel) => {
  const token = localStorage.getItem('Authorization')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await addItem2Cart({
      itemId: item.id,
      num: 1,
      price: item.price,
      name: item.name,
      image: item.image,
      skuId: 0
    })
    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error('加入购物车失败', error)
    if (!isHandledRequestError(error)) {
      ElMessage.error('加入购物车失败，请稍后重试')
    }
  }
}

watch(
  [() => route.query.q, () => route.query.category],
  () => {
    fetchProducts(true)
  },
  { immediate: true }
)
</script>

<template>
  <div class="search-page">
    <div class="search-container">
      <section class="hero-panel">
        <div class="hero-copy">
          <div class="hero-label">搜索结果</div>
          <h1 class="hero-title">{{ resultTitle }}</h1>
          <p class="hero-desc">{{ resultDesc }}</p>
        </div>
        <div class="hero-stats">
          <div class="stats-value">{{ total }}</div>
          <div class="stats-label">匹配商品</div>
        </div>
      </section>

      <section class="toolbar-panel">
        <div class="sort-group">
          <button class="sort-chip" :class="{ active: sortMode === 'default' }" @click="handleSortChange('default')">
            综合
          </button>
          <button class="sort-chip" :class="{ active: sortMode === 'sold' }" @click="handleSortChange('sold')">
            销量优先
          </button>
          <button class="sort-chip" :class="{ active: sortMode === 'rating' }" @click="handleSortChange('rating')">
            好评优先
          </button>
          <button class="sort-chip" :class="{ active: sortMode === 'priceAsc' }" @click="handleSortChange('priceAsc')">
            价格从低到高
          </button>
          <button class="sort-chip" :class="{ active: sortMode === 'priceDesc' }" @click="handleSortChange('priceDesc')">
            价格从高到低
          </button>
        </div>
        <div class="price-filter">
          <el-input v-model="state.minPrice" class="price-input" placeholder="最低价" clearable />
          <span class="price-separator">-</span>
          <el-input v-model="state.maxPrice" class="price-input" placeholder="最高价" clearable />
          <el-button type="primary" @click="fetchProducts(true)">筛选</el-button>
        </div>
      </section>

      <section v-loading="loading" class="result-panel">
        <div v-if="productList.length" class="card-grid">
          <article
            v-for="item in productList"
            :key="item.id"
            class="product-card"
            @click="goToDetail(item.id)"
          >
            <div class="cover-wrap">
              <img
                class="cover-image"
                :src="resolveProductImage(item.image)"
                :alt="item.name"
                @error="applyProductImageFallback"
              >
            </div>

            <div class="card-body">
              <div class="trust-row">
                <div class="shop-meta">
                  <span class="shop-name">{{ item.shopName || '店铺信息待补充' }}</span>
                  <span v-if="item.isSelf === 1" class="self-tag">自营</span>
                </div>
                <span class="shipping-tag">{{ item.shippingDesc || '运费规则待补充' }}</span>
              </div>

              <h3 class="product-title">{{ item.name }}</h3>
              <p v-if="item.subTitle" class="product-subtitle">{{ item.subTitle }}</p>

              <div class="info-tags">
                <span v-if="item.brand" class="info-tag">{{ item.brand }}</span>
                <span v-if="item.category" class="info-tag">{{ item.category }}</span>
              </div>

              <div class="rating-row">
                <span class="rating-main">
                  <el-icon><Star /></el-icon>
                  {{ getPositiveRateText(item) }}
                </span>
                <span class="rating-sub">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ item.commentCount || 0 }} 条评价
                </span>
                <span class="rating-sub">
                  <el-icon><Goods /></el-icon>
                  已售 {{ item.sold || 0 }}
                </span>
              </div>

              <div class="price-row">
                <div class="price-main">
                  <span class="currency">¥</span>
                  <span class="amount">{{ formatPrice(item.price) }}</span>
                  <span v-if="item.originalPrice && item.originalPrice > item.price" class="origin-price">
                    ¥{{ formatPrice(item.originalPrice) }}
                  </span>
                </div>
                <el-button type="primary" plain @click.stop="addToCart(item)">
                  <el-icon><ShoppingCart /></el-icon>
                  加购
                </el-button>
              </div>
            </div>
          </article>
        </div>

        <el-empty
          v-else
          :description="hasFilter ? '没有找到符合条件的商品' : '暂无商品数据'"
          :image-size="180"
        >
          <el-button type="primary" @click="router.push('/')">返回首页</el-button>
        </el-empty>
      </section>

      <div v-if="total > 0" class="pagination-wrap">
        <el-pagination
          v-model:current-page="state.pageNo"
          v-model:page-size="state.pageSize"
          :page-sizes="[12, 20, 40]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.search-page {
  min-height: calc(100vh - 120px);
  background:
    radial-gradient(circle at top right, rgba(255, 122, 26, 0.16), transparent 24%),
    linear-gradient(180deg, #fff9f2 0%, #f7f8fb 40%, #f3f5f9 100%);
  padding: 28px 0 40px;
}

.search-container {
  width: min(1240px, calc(100% - 32px));
  margin: 0 auto;
}

.hero-panel,
.toolbar-panel,
.result-panel,
.pagination-wrap {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
}

.hero-panel {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 32px;
  margin-bottom: 20px;
}

.hero-label {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(249, 115, 22, 0.12);
  color: #c2410c;
  font-size: 13px;
  font-weight: 600;
}

.hero-title {
  margin: 16px 0 10px;
  color: #111827;
  font-size: 30px;
  font-weight: 700;
}

.hero-desc {
  margin: 0;
  color: #6b7280;
  line-height: 1.7;
}

.hero-stats {
  min-width: 180px;
  padding: 20px 24px;
  border-radius: 20px;
  background: linear-gradient(135deg, #111827 0%, #1f2937 100%);
  color: #fff;
  display: grid;
  place-items: center;
}

.stats-value {
  font-size: 40px;
  font-weight: 700;
  line-height: 1;
}

.stats-label {
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.7);
}

.toolbar-panel {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 24px;
  margin-bottom: 20px;
}

.sort-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.sort-chip {
  height: 40px;
  padding: 0 18px;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  background: #fff;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sort-chip.active {
  border-color: #f97316;
  background: #fff7ed;
  color: #c2410c;
}

.price-filter {
  display: flex;
  align-items: center;
  gap: 10px;
}

.price-input {
  width: 120px;
}

.price-separator {
  color: #9ca3af;
}

.result-panel {
  padding: 24px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(270px, 1fr));
  gap: 18px;
}

.product-card {
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 22px;
  background: #fff;
  cursor: pointer;
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  border-color: rgba(249, 115, 22, 0.22);
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.12);
}

.cover-wrap {
  aspect-ratio: 1;
  overflow: hidden;
  background: linear-gradient(135deg, #fff7ed 0%, #fef2f2 100%);
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.32s ease;
}

.product-card:hover .cover-image {
  transform: scale(1.04);
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 18px;
}

.trust-row,
.price-row,
.rating-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.shop-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.shop-name {
  max-width: 160px;
  overflow: hidden;
  color: #111827;
  font-size: 13px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.self-tag,
.shipping-tag,
.info-tag {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
}

.self-tag {
  background: #fee2e2;
  color: #b91c1c;
}

.shipping-tag {
  background: #eff6ff;
  color: #1d4ed8;
  white-space: nowrap;
}

.product-title {
  margin: 0;
  color: #111827;
  font-size: 16px;
  line-height: 1.6;
}

.product-subtitle {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.info-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.info-tag {
  background: #f3f4f6;
  color: #374151;
}

.rating-row {
  justify-content: flex-start;
  flex-wrap: wrap;
  color: #6b7280;
  font-size: 13px;
}

.rating-main,
.rating-sub {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.rating-main {
  color: #b45309;
  font-weight: 600;
}

.price-main {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.currency,
.amount {
  color: #dc2626;
  font-weight: 700;
}

.currency {
  font-size: 18px;
}

.amount {
  font-size: 28px;
  line-height: 1;
}

.origin-price {
  color: #9ca3af;
  font-size: 13px;
  text-decoration: line-through;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 18px 24px;
}

@media (max-width: 768px) {
  .hero-panel,
  .toolbar-panel {
    flex-direction: column;
  }

  .hero-stats {
    width: 100%;
    min-width: auto;
  }

  .price-filter {
    flex-wrap: wrap;
  }

  .price-input {
    width: calc(50% - 10px);
    min-width: 120px;
  }
}
</style>
