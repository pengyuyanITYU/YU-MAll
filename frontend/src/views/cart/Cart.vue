<template>
  <div class="cart-page">
    <div class="cart-container">
      <div class="page-header">
        <div>
          <h1 class="page-title">购物车</h1>
          <p class="page-subtitle">不同规格必须分开结算，只有同商品同配置才允许叠加数量。</p>
        </div>
        <div v-if="cartItems.length" class="header-actions">
          <el-button plain type="danger" :icon="Delete" @click="clearInvalidItems">清理失效商品</el-button>
          <el-button @click="batchRemove">清空购物车</el-button>
        </div>
      </div>

      <el-card v-if="!cartItems.length" shadow="never" class="empty-card">
        <el-empty description="购物车还是空的">
          <template #extra>
            <el-button type="primary" @click="router.push('/')">去逛逛</el-button>
          </template>
        </el-empty>
      </el-card>

      <template v-else>
        <el-card shadow="never" class="table-card">
          <el-table
            :key="tableKey"
            ref="multipleTableRef"
            :data="cartItems"
            row-key="id"
            style="width: 100%"
          >
            <el-table-column label="商品信息" min-width="360">
              <template #default="{ row }">
                <div class="product-cell" :class="{ disabled: row.stock <= 0 }">
                  <el-image class="product-image" :src="row.image" fit="cover" />
                  <div class="product-meta">
                    <div class="product-name" @click="router.push(`/item/${row.itemId}`)">
                      {{ row.title || row.name || '未知商品' }}
                    </div>
                    <div v-if="Object.keys(row.specs).length" class="spec-list">
                      <span v-for="(value, key) in row.specs" :key="key" class="spec-tag">
                        {{ key }}: {{ value }}
                      </span>
                    </div>
                    <div class="sku-tip">SKU: {{ row.skuId || '无规格' }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="单价" width="140" align="center">
              <template #default="{ row }">
                <span class="price-text">¥{{ formatPrice(row.price) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="数量" width="180" align="center">
              <template #default="{ row }">
                <div class="quantity-cell">
                  <el-input-number
                    v-model="row.quantity"
                    :min="1"
                    :max="Math.max(row.stock, 1)"
                    :disabled="row.stock <= 0"
                    @change="handleQuantityChange(row)"
                  />
                  <span v-if="row.stock > 0 && row.stock < 10" class="stock-tip">仅剩 {{ row.stock }} 件</span>
                  <span v-else-if="row.stock <= 0" class="stock-tip danger">无库存</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="小计" width="140" align="center">
              <template #default="{ row }">
                <span class="subtotal-text">¥{{ formatSubTotal(row.price, row.quantity) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="110" align="center">
              <template #default="{ row }">
                <el-button type="danger" link @click="removeItem(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <div class="footer-bar">
          <div class="footer-summary">
            <span>商品行数 {{ selectedCount }}</span>
            <strong>合计 ¥{{ formatPrice(totalPrice) }}</strong>
          </div>
          <el-button type="primary" size="large" :disabled="!hasValidItems" @click="goCheckout">
            去结算
          </el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElTable } from 'element-plus'
import { deleteCartItem, deleteCartItemByIds, queryMyCarts, updateCart } from '@/api/cart'
import { isHandledRequestError } from '@/utils/request'

interface CartItem {
  id: string | number
  itemId: number
  skuId: number
  title: string
  name: string
  image: string
  specs: Record<string, string>
  price: number
  quantity: number
  stock: number
}

const router = useRouter()
const cartItems = ref<CartItem[]>([])
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
const tableKey = shallowRef(0)
let debounceTimer: ReturnType<typeof setTimeout> | null = null

const formatPrice = (price?: number | string | null) => {
  if (price === undefined || price === null || price === '') {
    return '0.00'
  }
  return (Number(price) / 100).toFixed(2)
}

const formatSubTotal = (price: number | string, quantity: number) => {
  return formatPrice((Number(price) || 0) * (Number(quantity) || 0))
}

const parseSpecs = (spec: unknown): Record<string, string> => {
  if (!spec) {
    return {}
  }
  if (typeof spec === 'object') {
    return spec as Record<string, string>
  }
  if (typeof spec === 'string') {
    try {
      return JSON.parse(spec)
    } catch (error) {
      return spec ? { 规格: spec } : {}
    }
  }
  return {}
}

const sanitizeCartData = (rawData: any[]): CartItem[] => {
  if (!Array.isArray(rawData)) {
    return []
  }
  return rawData.map((item, index) => ({
    id: item.id ?? `cart-${index}`,
    itemId: Number(item.itemId) || 0,
    skuId: Number(item.skuId) || 0,
    title: item.name || item.title || '未知商品',
    name: item.name || item.title || '未知商品',
    image: item.image || '',
    specs: parseSpecs(item.spec ?? item.specs),
    price: Number(item.price) || 0,
    quantity: Number(item.num || item.quantity) || 1,
    stock: item.stock === undefined || item.stock === null ? 9999 : Number(item.stock)
  }))
}

const selectedCount = computed(() => cartItems.value.length)
const totalPrice = computed(() => cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0))
const hasValidItems = computed(() => cartItems.value.some(item => item.stock > 0))

const initCartData = async () => {
  try {
    const res: any = await queryMyCarts()
    const rawData = Array.isArray(res?.data) ? res.data : (res?.data?.result || [])
    cartItems.value = sanitizeCartData(rawData)
    tableKey.value += 1
  } catch (error) {
    console.error('加载购物车失败', error)
    if (!isHandledRequestError(error)) {
      ElMessage.error('购物车服务开小差了，请稍后再试')
    }
    cartItems.value = []
  }
}

const handleQuantityChange = (item: CartItem) => {
  if (!item.quantity || item.quantity < 1) {
    item.quantity = 1
  }
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }

  debounceTimer = setTimeout(async () => {
    try {
      if (item.quantity > item.stock) {
        item.quantity = item.stock
        ElMessage.warning(`库存不足，最多 ${item.stock} 件`)
        return
      }
      const cartId = Number(item.id)
      if (!Number.isFinite(cartId) || cartId <= 0) {
        throw new Error('购物车行 id 缺失')
      }
      await updateCart({
        id: cartId,
        num: item.quantity
      })
    } catch (error) {
      console.error('更新购物车数量失败', error)
      if (!isHandledRequestError(error)) {
        ElMessage.error('购物车服务开小差了，请稍后再试')
      }
      initCartData()
    }
  }, 400)
}

const removeItem = (item: CartItem) => {
  ElMessageBox.confirm('确定将该商品移出购物车？', '提示', { type: 'warning' }).then(async () => {
    try {
      const cartId = Number(item.id)
      if (!Number.isFinite(cartId) || cartId <= 0) {
        throw new Error('购物车行 id 缺失')
      }
      await deleteCartItem(cartId)
      await initCartData()
      ElMessage.success('已删除')
    } catch (error) {
      console.error('删除购物车商品失败', error)
      if (!isHandledRequestError(error)) {
        ElMessage.error('购物车服务开小差了，请稍后再试')
      }
    }
  })
}

const batchRemove = () => {
  ElMessageBox.confirm('确定清空购物车吗？', '警告', { type: 'warning' }).then(async () => {
    try {
      const ids = cartItems.value
        .map(item => Number(item.id))
        .filter(id => Number.isFinite(id) && id > 0)
      if (ids.length) {
        await deleteCartItemByIds(ids)
      }
      await initCartData()
      ElMessage.success('购物车已清空')
    } catch (error) {
      console.error('清空购物车失败', error)
      if (!isHandledRequestError(error)) {
        ElMessage.error('购物车服务开小差了，请稍后再试')
      }
    }
  })
}

const clearInvalidItems = async () => {
  const invalidIds = cartItems.value
    .filter(item => item.stock <= 0)
    .map(item => Number(item.id))
    .filter(id => Number.isFinite(id) && id > 0)
  if (!invalidIds.length) {
    ElMessage.info('没有失效商品')
    return
  }
  try {
    await deleteCartItemByIds(invalidIds)
    await initCartData()
    ElMessage.success('清理完成')
  } catch (error) {
    console.error('清理失效商品失败', error)
    if (!isHandledRequestError(error)) {
      ElMessage.error('购物车服务开小差了，请稍后再试')
    }
  }
}

const goCheckout = () => {
  if (!cartItems.value.length) {
    ElMessage.warning('购物车为空')
    return
  }
  if (!hasValidItems.value) {
    ElMessage.warning('没有可结算商品')
    return
  }
  router.replace('/checkout')
}

onMounted(() => {
  initCartData()
})

onUnmounted(() => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
})
</script>

<style scoped lang="scss">
.cart-page {
  min-height: 100vh;
  background: #f6f7fb;
  padding: 24px 0 48px;
}

.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 30px;
  line-height: 1.2;
  color: #1f2937;
}

.page-subtitle {
  margin: 8px 0 0;
  color: #6b7280;
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.empty-card,
.table-card {
  border: none;
  border-radius: 16px;
}

.product-cell {
  display: flex;
  gap: 16px;
}

.product-cell.disabled {
  opacity: 0.6;
}

.product-image {
  width: 88px;
  height: 88px;
  border-radius: 12px;
  overflow: hidden;
  background: #f3f4f6;
}

.product-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  cursor: pointer;
}

.product-name:hover {
  color: #2563eb;
}

.spec-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.spec-tag {
  padding: 4px 10px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 12px;
}

.sku-tip,
.stock-tip {
  color: #6b7280;
  font-size: 12px;
}

.stock-tip.danger {
  color: #dc2626;
}

.quantity-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.price-text,
.subtotal-text {
  font-weight: 600;
  color: #111827;
}

.footer-bar {
  margin-top: 20px;
  padding: 20px 24px;
  border-radius: 16px;
  background: #111827;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-summary {
  display: flex;
  gap: 24px;
  align-items: center;
}

@media (max-width: 768px) {
  .page-header,
  .footer-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .footer-summary {
    justify-content: space-between;
  }
}
</style>
