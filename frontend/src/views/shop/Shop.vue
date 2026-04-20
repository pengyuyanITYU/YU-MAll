<script setup lang="ts">
import { computed, reactive, shallowRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Goods, House, ShoppingCart, Star, Van } from '@element-plus/icons-vue'
import { addItem2Cart } from '@/api/cart'
import { itemApi, list, type ItemListModel, type ShopModel } from '@/api/item'
import { isHandledRequestError } from '@/utils/request'

const route = useRoute()
const router = useRouter()

const shopLoading = shallowRef(false)
const itemsLoading = shallowRef(false)
const total = shallowRef(0)

const shopData = shallowRef<ShopModel | null>(null)
const productList = shallowRef<ItemListModel[]>([])

const state = reactive({
  pageNo: 1,
  pageSize: 12
})

const shopId = computed(() => {
  const raw = route.params.shopId
  const value = Number(Array.isArray(raw) ? raw[0] : raw)
  return Number.isFinite(value) && value > 0 ? value : 0
})

const pageTitle = computed(() => {
  return shopData.value?.name ? `${shopData.value.name} - 店铺商品` : '店铺商品'
})

const shopSummary = computed(() => {
  const data = shopData.value
  if (!data) {
    return []
  }
  return [
    { label: '店铺', value: data.name, icon: House },
    { label: '配送', value: data.shippingDesc || '运费规则待补充', icon: Van },
    { label: '商品', value: `${total.value} 件在售`, icon: Goods }
  ]
})

const hasShop = computed(() => Boolean(shopData.value))

const formatPrice = (price?: number | string | null) => {
  if (price === undefined || price === null || price === '') {
    return '0.00'
  }
  return (Number(price) / 100).toFixed(2)
}

const getPositiveRateText = (item: ItemListModel) => {
  if (!item.commentCount) {
    return '暂无评价'
  }
  return `${item.positiveRate ?? 0}%好评`
}

const loadShop = async () => {
  if (!shopId.value) {
    shopData.value = null
    return false
  }

  shopLoading.value = true
  try {
    const res: any = await itemApi.getShopById(shopId.value)
    shopData.value = res?.data || null
    if (!shopData.value) {
      ElMessage.error('店铺不存在或已停用')
      return false
    }
    return true
  } catch (error) {
    console.error('加载店铺信息失败', error)
    shopData.value = null
    if (!isHandledRequestError(error)) {
      ElMessage.error('当前内容加载失败，请稍后再试')
    }
    return false
  } finally {
    shopLoading.value = false
  }
}

const loadProducts = async (resetPage = false) => {
  if (!shopId.value) {
    productList.value = []
    total.value = 0
    return
  }
  if (resetPage) {
    state.pageNo = 1
  }

  itemsLoading.value = true
  try {
    const res: any = await list({
      pageNo: state.pageNo,
      pageSize: state.pageSize,
      shopId: shopId.value
    })
    productList.value = Array.isArray(res?.rows) ? res.rows : []
    total.value = Number(res?.total || 0)
  } catch (error) {
    console.error('加载店铺商品失败', error)
    productList.value = []
    total.value = 0
    if (!isHandledRequestError(error)) {
      ElMessage.error('当前内容加载失败，请稍后再试')
    }
  } finally {
    itemsLoading.value = false
  }
}

const refreshShopPage = async () => {
  const loaded = await loadShop()
  if (!loaded) {
    productList.value = []
    total.value = 0
    return
  }
  await loadProducts(true)
}

const handlePageChange = (pageNo: number) => {
  state.pageNo = pageNo
  loadProducts()
}

const handleSizeChange = (pageSize: number) => {
  state.pageSize = pageSize
  state.pageNo = 1
  loadProducts()
}

const goToDetail = (id: number) => {
  router.push(`/item/${id}`)
}

const addToCart = async (item: ItemListModel) => {
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
  () => route.params.shopId,
  () => {
    refreshShopPage()
  },
  { immediate: true }
)

watch(
  pageTitle,
  (title) => {
    document.title = title
  },
  { immediate: true }
)
</script>

<template>
  <div class="shop-page">
    <div class="shop-container">
      <section v-loading="shopLoading" class="hero-panel">
        <template v-if="shopData">
          <div class="hero-copy">
            <div class="hero-label">店铺商品</div>
            <div class="hero-title-row">
              <h1 class="hero-title">{{ shopData.name }}</h1>
              <span v-if="shopData.isSelf === 1" class="self-tag">自营</span>
            </div>
            <p class="hero-desc">{{ shopData.shippingDesc || '运费规则待补充' }}</p>
          </div>
          <div class="hero-stats">
            <div class="stats-value">{{ total }}</div>
            <div class="stats-label">在售商品</div>
          </div>
        </template>
        <el-empty v-else description="店铺不存在或已停用" :image-size="120" />
      </section>

      <section v-if="shopData" class="summary-panel">
        <div v-for="item in shopSummary" :key="item.label" class="summary-card">
          <el-icon class="summary-icon"><component :is="item.icon" /></el-icon>
          <div class="summary-content">
            <div class="summary-label">{{ item.label }}</div>
            <div class="summary-value">{{ item.value }}</div>
          </div>
        </div>
      </section>

      <section v-if="hasShop" v-loading="itemsLoading" class="result-panel">
        <div v-if="productList.length" class="card-grid">
          <article
            v-for="item in productList"
            :key="item.id"
            class="product-card"
            @click="goToDetail(item.id)"
          >
            <div class="cover-wrap">
              <img class="cover-image" :src="item.image || '/placeholder-image.svg'" :alt="item.name">
            </div>

            <div class="card-body">
              <div class="trust-row">
                <div class="shop-meta">
                  <span class="shop-name">{{ item.shopName || shopData?.name }}</span>
                  <span v-if="item.isSelf === 1" class="self-tag">自营</span>
                </div>
                <span class="shipping-tag">{{ item.shippingDesc || shopData?.shippingDesc || '运费规则待补充' }}</span>
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
          description="该店铺暂时没有可展示商品"
          :image-size="180"
        />
      </section>

      <div v-if="hasShop && total > 0" class="pagination-wrap">
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
.shop-page {
  min-height: calc(100vh - 120px);
  background:
    radial-gradient(circle at top left, rgba(249, 115, 22, 0.14), transparent 24%),
    linear-gradient(180deg, #fffaf4 0%, #f7f8fb 42%, #f3f5f9 100%);
  padding: 28px 0 40px;
}

.shop-container {
  width: min(1240px, calc(100% - 32px));
  margin: 0 auto;
}

.hero-panel,
.summary-panel,
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

.hero-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 0 10px;
}

.hero-title {
  margin: 0;
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
  color: rgba(255, 255, 255, 0.72);
}

.summary-panel {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
  padding: 22px 24px;
}

.summary-card {
  display: flex;
  gap: 12px;
  padding: 18px;
  border-radius: 18px;
  background: #fff7ed;
}

.summary-icon {
  color: #f97316;
  font-size: 20px;
}

.summary-label {
  color: #9ca3af;
  font-size: 12px;
}

.summary-value {
  margin-top: 6px;
  color: #111827;
  font-weight: 600;
  line-height: 1.6;
}

.result-panel {
  margin-top: 20px;
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

@media (max-width: 992px) {
  .summary-panel {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero-panel {
    flex-direction: column;
  }

  .hero-stats {
    width: 100%;
    min-width: auto;
  }
}
</style>
