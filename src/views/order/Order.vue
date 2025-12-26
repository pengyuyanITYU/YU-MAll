<template>
  <div class="checkout-page">
    <div class="checkout-container">
      <div class="page-header">
        <h2><el-icon class="header-icon"><Goods /></el-icon> 确认订单</h2>
        <span class="header-desc">请核对您的商品信息与收货地址</span>
      </div>

      <div class="checkout-content">
        <!-- 主要区域：地址与商品 -->
        <div class="main-column">
          <!-- 1. 收货地址 -->
          <div class="checkout-card address-section">
            <div class="card-header">
              <span class="title">收货地址</span>
              <el-button link type="primary" @click="showAddressDialog = true" v-if="selectedAddress">
                切换地址
              </el-button>
            </div>

            <div
              class="address-box"
              :class="{ 'has-address': selectedAddress }"
              @click="!selectedAddress && (showAddressDialog = true)"
            >
              <div v-if="selectedAddress" class="address-content-box">
                <div class="addr-icon-wrapper">
                  <el-icon><LocationInformation /></el-icon>
                </div>
                <div class="addr-details">
                  <div class="user-row">
                    <span class="name">{{ selectedAddress.contact }}</span>
                    <span class="mobile">{{ selectedAddress.mobile }}</span>
                    <el-tag v-if="selectedAddress.isDefault === 1" size="small" type="danger" effect="plain" round>默认</el-tag>
                  </div>
                  <div class="detail-row">
                    {{ formatAddressText(selectedAddress) }}
                  </div>
                </div>
              </div>
              <div v-else class="no-address-placeholder">
                <div class="icon-circle">
                  <el-icon><Plus /></el-icon>
                </div>
                <p>点击添加收货地址</p>
              </div>
            </div>
          </div>

          <!-- 2. 商品清单 -->
          <div class="checkout-card products-section">
            <div class="card-header">
              <span class="title">商品清单</span>
              <span class="subtitle">共 {{ checkoutItems.length }} 件商品</span>
            </div>

            <div v-if="isLoading" class="loading-state">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else-if="checkoutItems.length === 0" class="empty-state">
              <el-empty description="暂无待结算商品" :image-size="100" />
            </div>

            <div v-else class="products-list">
              <div v-for="item in checkoutItems" :key="item.id" class="product-row">
                <div class="product-img-box">
                  <img :src="item.imageUrl || 'https://via.placeholder.com/80'" alt="商品图片" />
                </div>
                <div class="product-detail">
                  <div class="p-name">{{ item.name }}</div>
                  <div class="p-specs" v-if="item.specs">
                    <span v-for="(val, key) in item.specs" :key="key" class="spec-item">{{ key }}: {{ val }}</span>
                  </div>
                </div>
                <div class="product-calc">
                  <div class="p-price">¥{{ formatPrice(item.price) }}</div>
                  <div class="p-num">x {{ item.quantity }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部悬浮结算栏 -->
      <div class="checkout-footer-bar">
        <div class="footer-content">
          <div class="left-info">
            <span v-if="selectedAddress" class="footer-addr">
              寄送至：{{ formatAddressText(selectedAddress) }}
            </span>
            <span v-else class="footer-addr text-gray">请先选择收货地址</span>
          </div>
          <div class="right-action">
            <div class="final-text">
              <span class="fee-item">运费: ¥{{ formatPrice(shippingFee) }}</span>
              <span class="total-label">应付总额：</span>
              <span class="price-display">
                <span class="symbol">¥</span><span class="amount">{{ formatPrice(totalAmount) }}</span>
              </span>
            </div>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              @click="submitOrder"
              :loading="submitting"
              :disabled="!selectedAddress || checkoutItems.length === 0"
            >
              提交订单
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 地址选择弹窗 -->
    <el-dialog
      v-model="showAddressDialog"
      title="选择收货地址"
      width="540px"
      align-center
    >
      <div class="dialog-address-list">
        <div
          v-for="address in addresses"
          :key="address.id"
          class="dialog-addr-item"
          :class="{ active: selectedAddress?.id === address.id }"
          @click="selectAddress(address)"
        >
          <div class="addr-check-icon">
            <el-icon v-if="selectedAddress?.id === address.id"><Check /></el-icon>
          </div>
          <div class="addr-info-col">
            <div class="top-row">
              <span class="d-name">{{ address.contact }}</span>
              <span class="d-mobile">{{ address.mobile }}</span>
              <el-tag v-if="address.isDefault === 1" size="small" type="danger">默认</el-tag>
            </div>
            <div class="bottom-row">{{ formatAddressText(address) }}</div>
          </div>
        </div>
        <el-empty v-if="addresses.length === 0" description="暂无收货地址" :image-size="60" />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button link type="primary" @click="router.push('/user?tab=address')">
            <el-icon><Plus /></el-icon> 管理地址
          </el-button>
          <div>
            <el-button @click="showAddressDialog = false">取消</el-button>
            <el-button type="primary" @click="confirmAddress">确定</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Check, Goods, LocationInformation } from '@element-plus/icons-vue'

// API 引入
import { getAddressList } from '@/api/address'
import { deleteCartItemByIds, queryMyCarts } from '@/api/cart'
import { addOrder } from '@/api/order'

// --- 类型定义 ---
export interface OrderDetailDTO {
  itemId: string | number;
  num: number;
  price: string | number;
  image: string;
  specs: Record<string, string>;
}

export interface OrderFormDTO {
  addressId: number;
  paymentType: number;
  totalFee: number;
  details: OrderDetailDTO[];
}

interface Address {
  id: number; contact: string; mobile: string; province: string; city: string; town: string; street: string; isDefault: number;
}

interface CartItem {
  id: string; name: string; price: number | string; quantity: number; imageUrl?: string; specs?: Record<string, string>; skuId?: number; itemId?: number; cartIds?: string[];
}

// --- 状态 ---
const router = useRouter()
const route = useRoute()

const checkoutItems = ref<CartItem[]>([])
const selectedAddress = ref<Address | null>(null)
const addresses = ref<Address[]>([])
const showAddressDialog = ref(false)
const submitting = ref(false)
const isLoading = ref(false)

// --- 工具函数 ---
const formatPrice = (price?: number | string | null) => {
  if (price === undefined || price === null || price === '') return '0.00'
  const num = Number(price)
  if (isNaN(num)) return '0.00'
  const padded = Math.abs(num).toString().padStart(3, '0')
  return `${padded.slice(0, -2)}.${padded.slice(-2)}`
}

const formatAddressText = (addr: Address) => {
  if (!addr) return ''
  return `${addr.province} ${addr.city} ${addr.town || ''} ${addr.street}`
}

// --- 计算属性 ---
const subtotal = computed(() => {
  return checkoutItems.value.reduce((sum, item) => {
    const p = Number(item.price) || 0
    const q = Number(item.quantity) || 0
    return sum + (p * q)
  }, 0)
})

const shippingFee = computed(() => subtotal.value >= 9900 ? 0 : 1000)

const totalAmount = computed(() => {
  return subtotal.value + shippingFee.value
})

// --- 核心逻辑 ---
const initOrderData = async () => {
  isLoading.value = true
  try {
    await initAddressData()
    const res: any = await queryMyCarts()
    const cartList = Array.isArray(res.data) ? res.data : (res.data?.result || [])

    const validItems = cartList.filter((item: any) => (item.stock === undefined || item.stock > 0))
    const mergedMap = new Map<string | number, any>()

    validItems.forEach((item: any) => {
      const uniqueKey = item.skuId || item.itemId
      if (mergedMap.has(uniqueKey)) {
        const existingItem = mergedMap.get(uniqueKey)
        existingItem.num = (Number(existingItem.num) || 0) + (Number(item.num) || 0)
        if (!existingItem.cartIds) existingItem.cartIds = [existingItem.id]
        existingItem.cartIds.push(item.id)
      } else {
        const newItem = { ...item }
        newItem.cartIds = [item.id]
        mergedMap.set(uniqueKey, newItem)
      }
    })

    checkoutItems.value = Array.from(mergedMap.values()).map((item: any) => {
      let specsObj = {}
      if (typeof item.spec === 'string') {
        try { specsObj = JSON.parse(item.spec) } catch (e) {}
      } else if (item.specs) {
        specsObj = item.specs
      }

      return {
        id: String(item.id),
        name: item.title || item.name || '未知商品',
        price: item.price || 0,
        quantity: Number(item.num) || 1,
        imageUrl: item.image,
        skuId: item.skuId,
        itemId: item.itemId,
        specs: specsObj,
        cartIds: item.cartIds
      }
    })
  } catch (error) {
    console.error('初始化失败:', error)
  } finally {
    isLoading.value = false
  }
}

const initAddressData = async () => {
  try {
    const res: any = await getAddressList()
    addresses.value = Array.isArray(res.data) ? res.data : []
    const queryAddressId = route.query.addressId ? Number(route.query.addressId) : null
    if (queryAddressId) {
      const target = addresses.value.find(addr => addr.id === queryAddressId)
      if (target) { selectedAddress.value = target; return; }
    }
    const defaultAddr = addresses.value.find(addr => addr.isDefault === 1)
    selectedAddress.value = defaultAddr || addresses.value[0] || null
  } catch (error) {
    console.error('获取地址失败:', error)
  }
}

const submitOrder = async () => {
  if (!selectedAddress.value) return ElMessage.warning('请选择收货地址')
  if (checkoutItems.value.length === 0) return ElMessage.warning('清单为空')

  submitting.value = true
  try {
    const orderForm: OrderFormDTO = {
      addressId: selectedAddress.value.id,
      paymentType: 1,
      totalFee: totalAmount.value,
      details: checkoutItems.value.map(item => ({
        itemId: Number(item.skuId || item.itemId),
        num: item.quantity,
        price: item.price,
        image: item.imageUrl || '',
        specs: item.specs || {}
      })),
    }

    await addOrder(orderForm)
    ElMessage.success('订单提交成功')

    try {
      const allCartIds: any = checkoutItems.value.flatMap(item => item.cartIds || [item.id])
      if (allCartIds.length > 0) await deleteCartItemByIds(allCartIds)
    } catch (e) {}

    router.push('/user?tab=orders')
  } catch (error: any) {
    ElMessage.error(error.msg || '提交失败')
  } finally {
    submitting.value = false
  }
}

const selectAddress = (addr: Address) => selectedAddress.value = addr
const confirmAddress = () => { showAddressDialog.value = false }

onMounted(() => { initOrderData() })
</script>

<style scoped lang="scss">
.checkout-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 100px;
}

.checkout-container {
  max-width: 800px; /* 调整为更窄的单列布局 */
  margin: 0 auto;
  padding: 30px 20px;
}

.page-header {
  margin-bottom: 24px;
  h2 {
    display: flex;
    align-items: center;
    font-size: 22px;
    color: #303133;
    margin: 0 0 8px 0;
    .header-icon { margin-right: 10px; color: #409eff; }
  }
  .header-desc { font-size: 14px; color: #909399; }
}

.checkout-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    .title {
      font-size: 17px;
      font-weight: 600;
      color: #303133;
      position: relative;
      padding-left: 12px;
      &::before {
        content: ''; position: absolute; left: 0; top: 50%; transform: translateY(-50%);
        width: 4px; height: 16px; background: #409eff; border-radius: 2px;
      }
    }
    .subtitle { font-size: 13px; color: #909399; }
  }
}

/* 地址样式 */
.address-box {
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  background-color: #fcfcfc;
  &.has-address {
    border: 1px solid #c6e2ff;
    background: linear-gradient(to right, #f4f9ff, #fff);
    position: relative;
    &::after { content: ''; position: absolute; top: 0; left: 0; width: 4px; height: 100%; background: #409eff; }
  }
}

.address-content-box {
  display: flex;
  align-items: center;
  gap: 16px;
  .addr-icon-wrapper {
    width: 40px; height: 40px; border-radius: 50%; background: #e1f3d8; color: #67c23a;
    display: flex; align-items: center; justify-content: center; font-size: 20px;
  }
  .addr-details {
    .user-row {
      margin-bottom: 6px; display: flex; align-items: center; gap: 10px;
      .name { font-size: 16px; font-weight: bold; color: #303133; }
      .mobile { color: #606266; }
    }
    .detail-row { font-size: 14px; color: #909399; line-height: 1.4; }
  }
}

.no-address-placeholder {
  text-align: center; color: #909399;
  .icon-circle {
    width: 36px; height: 36px; border-radius: 50%; background: #f0f2f5;
    margin: 0 auto 8px; display: flex; align-items: center; justify-content: center;
  }
}

/* 商品清单 */
.products-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  .product-row {
    display: flex; gap: 16px; padding: 12px; border-bottom: 1px solid #f0f0f0; align-items: center;
    &:last-child { border-bottom: none; }
    .product-img-box {
      width: 72px; height: 72px; border-radius: 6px; overflow: hidden; border: 1px solid #ebeef5;
      img { width: 100%; height: 100%; object-fit: contain; }
    }
    .product-detail {
      flex: 1; min-width: 0;
      .p-name { font-size: 14px; color: #303133; margin-bottom: 6px; }
      .p-specs {
        display: flex; flex-wrap: wrap; gap: 6px;
        .spec-item { font-size: 11px; color: #909399; background: #f5f7fa; padding: 2px 6px; border-radius: 4px; }
      }
    }
    .product-calc {
      text-align: right;
      .p-price { font-size: 15px; font-weight: 600; color: #303133; }
      .p-num { font-size: 12px; color: #909399; margin-top: 4px; }
    }
  }
}

/* 底部结算栏样式优化 */
.checkout-footer-bar {
  position: fixed; bottom: 0; left: 0; right: 0; background: #fff;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.06); z-index: 100;
  .footer-content {
    max-width: 800px; margin: 0 auto; padding: 12px 20px; display: flex; justify-content: space-between; align-items: center;
  }
  .footer-addr { font-size: 13px; color: #606266; max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .right-action {
    display: flex; align-items: center; gap: 20px;
    .final-text {
      display: flex; align-items: center; gap: 12px;
      .fee-item { font-size: 12px; color: #909399; }
      .total-label { font-size: 14px; color: #303133; }
    }
    .price-display {
      color: #f56c6c;
      .symbol { font-size: 14px; font-weight: 600; }
      .amount { font-size: 24px; font-weight: bold; }
    }
    .submit-btn {
      width: 140px; height: 44px; font-weight: 600; border-radius: 22px; font-size: 16px;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
    }
  }
}

/* 弹窗地址列表 */
.dialog-address-list {
  max-height: 400px; overflow-y: auto; display: flex; flex-direction: column; gap: 12px; padding: 4px;
  .dialog-addr-item {
    border: 1px solid #dcdfe6; border-radius: 8px; padding: 16px; cursor: pointer; display: flex; gap: 12px;
    &.active { border-color: #409eff; background-color: #f0f7ff; .addr-check-icon { color: #409eff; } }
    .addr-check-icon { width: 20px; color: transparent; font-weight: bold; }
    .addr-info-col {
      flex: 1;
      .top-row { margin-bottom: 4px; .d-name { font-weight: bold; margin-right: 10px; } .d-mobile { color: #606266; } }
      .bottom-row { font-size: 13px; color: #909399; }
    }
  }
}

.dialog-footer { display: flex; justify-content: space-between; align-items: center; }
</style>