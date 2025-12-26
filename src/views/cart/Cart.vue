<template>
  <div class="cart-page">
    <div class="main-container">
      <!-- 1. 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <h2 class="title">购物车</h2>
          <span class="count-badge" v-if="cartItems.length">共 {{ cartItems.length }} 件商品</span>
        </div>
        <div class="header-right" v-if="cartItems.length">
          <el-button 
            type="danger" 
            plain 
            size="small" 
            :icon="Delete" 
            @click="clearInvalidItems"
            class="action-btn"
          >
            清理失效商品
          </el-button>
        </div>
      </div>

      <!-- 2. 空状态 -->
      <div v-if="cartItems.length === 0" class="empty-state-card">
        <el-empty :image-size="200" description="购物车还是空的，去挑几件喜欢的商品吧~">
          <template #extra>
            <el-button type="primary" size="large" class="go-shopping-btn" @click="router.push('/')">
              前往首页逛逛
            </el-button>
          </template>
        </el-empty>
      </div>

      <!-- 3. 购物车列表 -->
      <div v-else class="cart-content">
        <el-card shadow="never" class="cart-card">
          <el-table 
            :key="tableKey"
            ref="multipleTableRef" 
            :data="cartItems" 
            style="width: 100%;" 
            row-key="id" 
            :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: '600' }"
          >
            <!-- 商品信息 -->
            <el-table-column label="商品信息" min-width="380">
              <template #default="{ row }">
                <div class="product-item" :class="{ 'is-disabled': row.stock <= 0 }">
                  <div class="img-box">
                    <el-image :src="row.image" fit="cover" class="thumb" />
                    <div v-if="row.stock <= 0" class="no-stock-mask">无货</div>
                  </div>
                  <div class="info-box">
                    <div class="prod-title" @click="router.push(`/item/${row.itemId}`)">
                       {{ row.title || row.name || '未知商品' }}
                    </div>
                    <div class="prod-specs" v-if="row.specs">
                      <span v-for="(val, key) in row.specs" :key="key" class="spec-pill">
                        {{ key }}: {{ val }}
                      </span>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <!-- 单价 -->
            <el-table-column label="单价" width="140" align="center">
              <template #default="{ row }">
                <div class="price-text">¥{{ formatPrice(row.price) }}</div>
              </template>
            </el-table-column>
            
            <!-- 数量 -->
            <el-table-column label="数量" width="160" align="center">
              <template #default="{ row }">
                <el-input-number 
                  v-model="row.quantity" 
                  :min="1" 
                  :max="row.stock > 0 ? row.stock : 9999" 
                  :disabled="row.stock <= 0" 
                  size="small" 
                  class="custom-input-number"
                  @change="() => handleQuantityChange(row)" 
                />
                <div class="stock-warning" v-if="row.stock < 10 && row.stock > 0">仅剩 {{ row.stock }} 件</div>
              </template>
            </el-table-column>
            
            <!-- 小计 -->
            <el-table-column label="小计" width="140" align="center">
              <template #default="{ row }">
                <div class="subtotal-text">¥{{ formatSubTotal(row.price, row.quantity) }}</div>
              </template>
            </el-table-column>
            
            <!-- 操作 -->
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button 
                  type="danger" 
                  link 
                  class="del-btn"
                  @click="removeItem(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 4. 底部结算栏 -->
        <div class="footer-bar-wrapper">
          <div class="footer-bar">
            <div class="bar-left">
              <el-button link type="info" @click="batchRemove">清空购物车</el-button>
            </div>
            
            <div class="bar-right">
              <div class="total-info">
                <span class="label">已选 <span class="num-highlight">{{ selectedCount }}</span> 件商品</span>
                <span class="label ml-4">合计：</span>
                <span class="total-price">
                  <span class="symbol">¥</span>
                  <span class="amount">{{ formatPrice(totalPrice) }}</span>
                </span>
              </div>
              
              <el-button 
                type="primary" 
                class="checkout-btn" 
                :disabled="selectedCount === 0" 
                @click="goCheckout"
              >
                去结算
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElTable } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { addItem2Cart, updateCart, deleteCartItem, queryMyCarts, deleteCartItemByIds } from '@/api/cart'

// --- 接口定义 ---
interface CartItem { 
  id: string | number; 
  itemId: number; 
  skuId: number; 
  title: string; 
  name: string; 
  image: string; 
  specs: Record<string, string>; 
  price: number | string; 
  quantity: number; 
  stock: number;
  mergedIds?: (string | number)[]; // 存储所有重复记录的ID
}

// --- 状态定义 ---
const router = useRouter()
const cartItems = ref<CartItem[]>([])
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
const tableKey = ref(0)
let debounceTimer: ReturnType<typeof setTimeout> | null = null

// --- 工具函数 ---
const formatPrice = (price?: number | string | null) => {
  if (price === undefined || price === null || price === '') return '0.00'
  const str = price.toString()
  const padded = str.padStart(3, '0')
  return `${padded.slice(0, -2)}.${padded.slice(-2)}`
}

const formatSubTotal = (price: number | string, quantity: number) => {
  const p = Number(price) || 0
  const q = Number(quantity) || 0
  return formatPrice(p * q)
}

// --- 数据清洗与合并逻辑 ---
const sanitizeCartData = (rawData: any[]): CartItem[] => {
  if (!Array.isArray(rawData)) return []

  const map = new Map<string, CartItem>()

  rawData.forEach((item, index) => {
    let parsedSpecs = {}
    if (typeof item.spec === 'string') { try { parsedSpecs = JSON.parse(item.spec) } catch(e){} } 
    else if (typeof item.specs === 'object') { parsedSpecs = item.specs }

    const itemId = Number(item.itemId) || 0
    const skuId = Number(item.skuId) || 0
    const uniqueKey = `${itemId}-${skuId}`
    
    const currentQty = Number(item.num || item.quantity) || 1
    // 使用真实ID，如果没有则生成
    const currentId = item.id ? String(item.id) : `gen-${index}-${Date.now()}`

    if (map.has(uniqueKey)) {
      const existingItem = map.get(uniqueKey)!
      existingItem.quantity += currentQty
      // 收集所有重复的ID
      if (existingItem.mergedIds) {
        existingItem.mergedIds.push(currentId)
      }
    } else {
      const newItem: CartItem = {
        ...item,
        id: currentId, 
        mergedIds: [currentId], 
        skuId,
        itemId,
        title: item.name || item.title || '未知商品',
        image: item.image || '',
        price: item.price || 0, 
        quantity: currentQty,
        stock: (item.stock !== undefined && item.stock !== null) ? Number(item.stock) : 999,
        specs: parsedSpecs
      }
      map.set(uniqueKey, newItem)
    }
  })

  return Array.from(map.values())
}

// --- 计算属性 ---
const selectedItems = computed(() => cartItems.value)
const totalPrice = computed(() => selectedItems.value.reduce((total, item) => total + (Number(item.price) || 0) * (Number(item.quantity) || 0), 0))
const selectedCount = computed(() => selectedItems.value.length)

// --- 核心逻辑 ---

const initCartData = async () => {
  try {
    const res: any = await queryMyCarts()
    const rawData = Array.isArray(res.data) ? res.data : (res.data?.result || [])
    cartItems.value = sanitizeCartData(rawData)
    tableKey.value++ 
  } catch (e) {
    console.error('加载购物车数据失败:', e)
    ElMessage.error('加载数据失败')
    cartItems.value = []
  }
}

const goCheckout = async () => {
  if (cartItems.value.length === 0) return ElMessage.warning('购物车为空')
  const validItems = cartItems.value.filter(item => item.stock > 0)
  if (validItems.length === 0) return ElMessage.warning('所有商品均无库存')
  router.push('/checkout')
}

// 【关键修复】数量变更逻辑：合并清洗
const handleQuantityChange = (item: CartItem) => {
  if (!item.quantity || item.quantity < 1) item.quantity = 1
  if (debounceTimer) clearTimeout(debounceTimer)
  
  debounceTimer = setTimeout(async () => {
    try {
      if (item.quantity > item.stock) {
        ElMessage.warning(`库存不足，最大可用: ${item.stock}`)
        item.quantity = item.stock
        return
      }
      
      const params: any = { 
        itemId: item.itemId, 
        skuId: item.skuId, 
        num: item.quantity, 
        spec: JSON.stringify(item.specs), 
        price: item.price,
        image: item.image,
        name: item.title
      }
      
      // --- 核心修复逻辑开始 ---
      
      // 检查是否存在重复记录 (mergedIds 长度 > 1)
      if (item.mergedIds && item.mergedIds.length > 1) {
        // 1. 找出除了主ID以外的所有重复ID
        const mainId = item.id
        const duplicateIds:any = item.mergedIds.filter(id => id !== mainId)
        
        // 2. 删除这些多余的记录
        if (duplicateIds.length > 0) {
          await deleteCartItemByIds(duplicateIds)
        }
        
        // 3. 更新主记录到目标数量 (此时数据库只剩这一条了)
        params.id = mainId
        await updateCart(params)
        
        // 4. 更新本地状态，防止下次重复删除
        item.mergedIds = [mainId]
        
      } else {
        // 正常情况：没有重复记录，直接更新
        if (!String(item.id).startsWith('gen-')) {
           params.id = item.id
        }
        await updateCart(params)
      }
      // --- 核心修复逻辑结束 ---

    } catch (e) {
      console.error('更新数量失败:', e)
      ElMessage.error('更新失败')
      initCartData()
    }
  }, 500)
}

const removeItem = (row: any) => {
  ElMessageBox.confirm('确定将该商品移出购物车？', '提示', { type: 'warning' }).then(async () => {
    try {
      // 删除时也要把所有合并的ID都删掉
      if (row.mergedIds && row.mergedIds.length > 0) {
        await deleteCartItemByIds(row.mergedIds)
      } else {
        if (row.id && !String(row.id).startsWith('gen-')) {
          await deleteCartItem(row.id)
        } else {
          await deleteCartItem(row.skuId)
        }
      }
      await initCartData()
      ElMessage.success('已删除')
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

const batchRemove = () => {
  ElMessageBox.confirm('确定清空购物车吗？', '警示', { type: 'warning' }).then(async () => {
    try {
      let allIds: any[] = []
      cartItems.value.forEach(item => {
        if (item.mergedIds && item.mergedIds.length > 0) {
          allIds = [...allIds, ...item.mergedIds]
        } else {
          allIds.push(item.id || item.skuId)
        }
      })
      if (allIds.length) await deleteCartItemByIds(allIds)
      await initCartData()
      ElMessage.success('购物车已清空')
    } catch (e) {
      ElMessage.error('清空失败')
    }
  })
}

const clearInvalidItems = async () => {
  const invalidItems = cartItems.value.filter(item => item.stock <= 0)
  if (invalidItems.length === 0) return ElMessage.info('没有失效商品')
  try {
    let ids: any[] = []
    invalidItems.forEach(item => {
      if (item.mergedIds) ids = [...ids, ...item.mergedIds]
      else ids.push(item.id || item.skuId)
    })
    await deleteCartItemByIds(ids)
    await initCartData()
    ElMessage.success('清理完成')
  } catch (e) {
    ElMessage.error('清理失败')
  }
}

onMounted(() => {
  initCartData()
})

onUnmounted(() => {
  if (debounceTimer) clearTimeout(debounceTimer)
})
</script>

<style scoped lang="scss">
/* 样式保持不变，为了节省篇幅省略，请直接复用上面的 CSS */
$primary-color: #409eff;
$danger-color: #f56c6c;
$text-main: #303133;
$text-sub: #909399;
$bg-color: #f5f7fa;
$card-radius: 12px;

.cart-page {
  background-color: $bg-color;
  min-height: 100vh;
  padding-bottom: 100px;
}

.main-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: baseline;
    gap: 12px;
    
    .title {
      font-size: 26px;
      font-weight: 700;
      color: $text-main;
      margin: 0;
    }
    .count-badge {
      font-size: 14px;
      color: $text-sub;
      background: #eef1f6;
      padding: 2px 8px;
      border-radius: 10px;
    }
  }
}

.empty-state-card {
  background: #fff;
  border-radius: $card-radius;
  padding: 60px 0;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
  
  .go-shopping-btn {
    width: 180px;
    border-radius: 24px;
    font-weight: 600;
    margin-top: 15px;
  }
}

.cart-card {
  border: none;
  border-radius: $card-radius;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
  overflow: hidden;
}

.product-item {
  display: flex;
  gap: 16px;
  padding: 8px 0;
  
  &.is-disabled {
    opacity: 0.6;
    filter: grayscale(0.8);
  }

  .img-box {
    width: 90px;
    height: 90px;
    border-radius: 8px;
    border: 1px solid #ebeef5;
    overflow: hidden;
    position: relative;
    flex-shrink: 0;
    background: #fff;

    .thumb {
      width: 100%;
      height: 100%;
    }
    
    .no-stock-mask {
      position: absolute;
      bottom: 0; left: 0; right: 0;
      background: rgba(0,0,0,0.6);
      color: #fff;
      font-size: 12px;
      text-align: center;
      line-height: 20px;
    }
  }

  .info-box {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 2px 0;

    .prod-title {
      font-size: 15px;
      color: $text-main;
      line-height: 1.4;
      cursor: pointer;
      font-weight: 500;
      transition: color 0.2s;
      
      &:hover {
        color: $primary-color;
      }
      
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      overflow: hidden;
    }

    .prod-specs {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-top: 6px;
      
      .spec-pill {
        font-size: 12px;
        color: #666;
        background: #f4f4f5;
        padding: 2px 8px;
        border-radius: 4px;
      }
    }
  }
}

.price-text {
  font-weight: 600;
  color: $text-main;
}

.subtotal-text {
  font-weight: 700;
  color: $danger-color;
  font-size: 16px;
}

.stock-warning {
  font-size: 12px;
  color: $danger-color;
  margin-top: 4px;
}

.del-btn {
  &:hover {
    font-weight: bold;
  }
}

.footer-bar-wrapper {
  position: fixed;
  bottom: 20px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  z-index: 999;
  pointer-events: none;
}

.footer-bar {
  pointer-events: auto;
  width: 1200px;
  max-width: 95%;
  height: 72px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 36px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  border: 1px solid rgba(255,255,255,0.8);

  .bar-left {
    display: flex;
    align-items: center;
  }

  .bar-right {
    display: flex;
    align-items: center;
    gap: 24px;

    .total-info {
      display: flex;
      align-items: baseline;
      
      .label {
        font-size: 14px;
        color: $text-main;
        &.ml-4 { margin-left: 16px; }
      }
      
      .num-highlight {
        color: $primary-color;
        font-weight: bold;
        font-size: 16px;
        margin: 0 2px;
      }

      .total-price {
        color: $danger-color;
        font-weight: 800;
        margin-left: 6px;
        
        .symbol { font-size: 16px; margin-right: 2px; }
        .amount { font-size: 28px; line-height: 1; }
      }
    }

    .checkout-btn {
      height: 44px;
      border-radius: 22px;
      padding: 0 36px;
      font-size: 16px;
      font-weight: 600;
      background: linear-gradient(135deg, #ff6b6b 0%, #ff4757 100%);
      border: none;
      box-shadow: 0 4px 12px rgba(255, 71, 87, 0.3);
      transition: all 0.3s;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(255, 71, 87, 0.4);
      }
      
      &:disabled {
        background: #dcdfe6;
        box-shadow: none;
      }
    }
  }
}

@media (max-width: 1280px) {
  .footer-bar {
    width: 90%;
  }
}
</style>