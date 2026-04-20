<script setup lang="ts">
import { computed, onMounted, ref, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, LocationInformation, Plus } from '@element-plus/icons-vue'
import { getAddressList } from '@/api/address'
import { deleteCartItemByIds, queryMyCarts } from '@/api/cart'
import { addOrder, type OrderDetailDTO, type OrderFormDTO } from '@/api/order'
import { itemApi, type ItemDetailModel } from '@/api/item'
import { markOrderFlowHistoryGuard } from '@/utils/orderFlowHistory'
import { isHandledRequestError } from '@/utils/request'

interface Address {
  id: number
  contact: string
  mobile: string
  province: string
  city: string
  town: string
  street: string
  isDefault: number
}

interface CheckoutItem {
  id: string
  cartIds: number[]
  itemId: number
  skuId: number
  name: string
  image: string
  price: number
  quantity: number
  specs: Record<string, string>
  stock: number
  shopId?: number | null
  shopName?: string
  isSelf?: number | null
  shippingType?: string
  shippingFee?: number | null
  freeShippingThreshold?: number | null
  shippingDesc?: string
}

interface ShopGroup {
  groupKey: string
  shopId: number | null
  shopName: string
  isSelf: number | null
  shippingType?: string
  shippingFee?: number | null
  freeShippingThreshold?: number | null
  shippingDesc?: string
  items: CheckoutItem[]
  goodsAmount: number
  shippingAmount: number
  shippingLabel: string
}

const router = useRouter()
const route = useRoute()

const isLoading = shallowRef(false)
const submitting = shallowRef(false)
const showAddressDialog = shallowRef(false)

const selectedAddress = ref<Address | null>(null)
const addresses = ref<Address[]>([])
const checkoutItems = ref<CheckoutItem[]>([])

const formatPrice = (price?: number | string | null) => {
  if (price === undefined || price === null || price === '') {
    return '0.00'
  }
  return (Number(price) / 100).toFixed(2)
}

const formatAddressText = (address?: Address | null) => {
  if (!address) {
    return ''
  }
  return `${address.province} ${address.city} ${address.town || ''} ${address.street}`.trim()
}

const formatSpecs = (specs?: Record<string, string>) => {
  if (!specs || !Object.keys(specs).length) {
    return '默认规格'
  }
  return Object.entries(specs).map(([key, value]) => `${key}: ${value}`).join(' / ')
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

const calcShippingAmount = (goodsAmount: number, shippingType?: string, shippingFee?: number | null, threshold?: number | null) => {
  if (shippingType === 'FREE') {
    return 0
  }
  if (shippingType === 'FIXED') {
    return Number(shippingFee || 0)
  }
  if (shippingType === 'THRESHOLD_FREE') {
    return goodsAmount >= Number(threshold || 0) ? 0 : Number(shippingFee || 0)
  }
  return 0
}

const buildShippingLabel = (shippingType?: string, shippingFee?: number | null, threshold?: number | null, desc?: string) => {
  if (desc) {
    return desc
  }
  if (shippingType === 'FREE') {
    return '包邮'
  }
  if (shippingType === 'FIXED') {
    return `固定运费 ¥${formatPrice(shippingFee)}`
  }
  if (shippingType === 'THRESHOLD_FREE') {
    return `满 ¥${formatPrice(threshold)} 包邮`
  }
  return '运费规则待补充'
}

const subtotal = computed(() => checkoutItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0))

const shopGroups = computed<ShopGroup[]>(() => {
  const groups = new Map<string, ShopGroup>()

  checkoutItems.value.forEach((item) => {
    const key = `${item.shopId || 0}-${item.shopName || '未绑定店铺'}`
    if (!groups.has(key)) {
      groups.set(key, {
        groupKey: key,
        shopId: item.shopId ?? null,
        shopName: item.shopName || '未绑定店铺',
        isSelf: item.isSelf ?? null,
        shippingType: item.shippingType,
        shippingFee: item.shippingFee,
        freeShippingThreshold: item.freeShippingThreshold,
        shippingDesc: item.shippingDesc,
        items: [],
        goodsAmount: 0,
        shippingAmount: 0,
        shippingLabel: ''
      })
    }
    const group = groups.get(key)!
    group.items.push(item)
    group.goodsAmount += item.price * item.quantity
  })

  return Array.from(groups.values()).map((group) => {
    group.shippingAmount = calcShippingAmount(
      group.goodsAmount,
      group.shippingType,
      group.shippingFee,
      group.freeShippingThreshold
    )
    group.shippingLabel = buildShippingLabel(
      group.shippingType,
      group.shippingFee,
      group.freeShippingThreshold,
      group.shippingDesc
    )
    return group
  })
})

const shippingTotal = computed(() => shopGroups.value.reduce((sum, group) => sum + group.shippingAmount, 0))
const totalAmount = computed(() => subtotal.value + shippingTotal.value)

const loadAddressData = async () => {
  const res: any = await getAddressList()
  addresses.value = Array.isArray(res?.data) ? res.data : []

  const queryAddressId = route.query.addressId ? Number(route.query.addressId) : null
  if (queryAddressId) {
    const matched = addresses.value.find(address => address.id === queryAddressId)
    if (matched) {
      selectedAddress.value = matched
      return
    }
  }

  selectedAddress.value = addresses.value.find(address => address.isDefault === 1) || addresses.value[0] || null
}

const normalizeCartItems = (cartList: any[]): CheckoutItem[] => {
  if (!Array.isArray(cartList)) {
    return []
  }

  return cartList
    .map((raw, index) => {
      const cartId = Number(raw.id || 0)
      return {
        id: String(raw.id || `cart-${index}`),
        cartIds: cartId > 0 ? [cartId] : [],
        itemId: Number(raw.itemId || 0),
        skuId: Number(raw.skuId || 0),
        name: raw.name || raw.title || '未知商品',
        image: raw.image || raw.imageUrl || '',
        price: Number(raw.price || 0),
        quantity: Number(raw.num || raw.quantity || 1),
        specs: parseSpecs(raw.spec || raw.specs),
        stock: Number(raw.stock ?? 9999)
      }
    })
    .filter(item => item.stock > 0)
}

const attachItemTrustFields = async (items: CheckoutItem[]) => {
  const uniqueIds = Array.from(new Set(items.map(item => item.itemId).filter(Boolean)))
  if (!uniqueIds.length) {
    return items
  }

  const detailEntries = await Promise.all(uniqueIds.map(async (itemId) => {
    try {
      const res: any = await itemApi.getItemById(itemId)
      return [itemId, res?.data as ItemDetailModel] as const
    } catch (error) {
      console.error('加载结算商品详情失败', itemId, error)
      return [itemId, null] as const
    }
  }))

  const detailMap = new Map<number, ItemDetailModel | null>(detailEntries)
  return items.map((item) => {
    const detail = detailMap.get(item.itemId)
    if (!detail) {
      return item
    }
    return {
      ...item,
      name: item.name || detail.name,
      image: item.image || detail.image || '',
      shopId: detail.shopId,
      shopName: detail.shopName,
      isSelf: detail.isSelf,
      shippingType: detail.shippingType,
      shippingFee: detail.shippingFee,
      freeShippingThreshold: detail.freeShippingThreshold,
      shippingDesc: detail.shippingDesc
    }
  })
}

const initOrderData = async () => {
  isLoading.value = true
  try {
    await loadAddressData()
    const res: any = await queryMyCarts()
    const rawList = Array.isArray(res?.data) ? res.data : (Array.isArray(res?.data?.result) ? res.data.result : [])
    const normalized = normalizeCartItems(rawList)
    checkoutItems.value = await attachItemTrustFields(normalized)
  } catch (error) {
    console.error('初始化结算页失败', error)
    if (!isHandledRequestError(error)) {
      ElMessage.error('订单服务开小差了，请稍后再试')
    }
    checkoutItems.value = []
  } finally {
    isLoading.value = false
  }
}

const selectAddress = (address: Address) => {
  selectedAddress.value = address
}

const confirmAddress = () => {
  showAddressDialog.value = false
}

const submitOrder = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (!checkoutItems.value.length) {
    ElMessage.warning('当前没有可结算商品')
    return
  }

  submitting.value = true
  try {
    const details: OrderDetailDTO[] = checkoutItems.value.map(item => ({
      itemId: item.itemId,
      skuId: item.skuId || undefined,
      num: item.quantity,
      price: item.price,
      image: item.image,
      specs: item.specs
    }))

    const orderForm: OrderFormDTO = {
      addressId: selectedAddress.value.id,
      paymentType: 1,
      totalFee: totalAmount.value,
      details
    }

    await addOrder(orderForm)

    const allCartIds = checkoutItems.value.flatMap(item => item.cartIds).filter(Boolean)
    if (allCartIds.length) {
      await deleteCartItemByIds(allCartIds)
    }

    markOrderFlowHistoryGuard('/user?tab=orders')
    ElMessage.success('订单提交成功')
    router.replace({ path: '/user', query: { tab: 'orders' } })
  } catch (error: any) {
    console.error('提交订单失败', error)
    if (!isHandledRequestError(error)) {
      ElMessage.error('订单服务开小差了，请稍后再试')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  initOrderData()
})
</script>

<template>
  <div class="checkout-page">
    <div class="checkout-container">
      <section class="hero-card">
        <div>
          <p class="hero-label">确认订单</p>
          <h1 class="hero-title">按店铺和真实规格结算</h1>
          <p class="hero-desc">不同规格不再前端合并，下单会把真实 skuId 和规格快照一起提交。</p>
        </div>
      </section>

      <div class="checkout-grid">
        <div class="main-column">
          <el-card shadow="never" class="section-card">
            <div class="section-head">
              <div>
                <h2>收货地址</h2>
                <p>请选择本次订单的收货地址</p>
              </div>
              <el-button link type="primary" @click="showAddressDialog = true">
                {{ selectedAddress ? '切换地址' : '选择地址' }}
              </el-button>
            </div>

            <div v-if="selectedAddress" class="address-card">
              <div class="address-icon">
                <el-icon><LocationInformation /></el-icon>
              </div>
              <div class="address-body">
                <div class="address-user">
                  <span>{{ selectedAddress.contact }}</span>
                  <span>{{ selectedAddress.mobile }}</span>
                  <span v-if="selectedAddress.isDefault === 1" class="default-tag">默认</span>
                </div>
                <div class="address-text">{{ formatAddressText(selectedAddress) }}</div>
              </div>
            </div>

            <div v-else class="empty-address" @click="showAddressDialog = true">
              <el-icon><Plus /></el-icon>
              <span>点击选择收货地址</span>
            </div>
          </el-card>

          <el-card shadow="never" class="section-card">
            <div class="section-head">
              <div>
                <h2>商品清单</h2>
                <p>共 {{ checkoutItems.length }} 条购物车记录</p>
              </div>
            </div>

            <div v-if="isLoading" class="loading-wrap">
              <el-skeleton animated :rows="5" />
            </div>

            <el-empty v-else-if="!shopGroups.length" description="购物车中暂无可结算商品" :image-size="120" />

            <div v-else class="group-list">
              <article v-for="group in shopGroups" :key="group.groupKey" class="shop-group">
                <div class="group-head">
                  <div class="group-meta">
                    <span class="shop-name">{{ group.shopName }}</span>
                    <span v-if="group.isSelf === 1" class="self-tag">自营</span>
                  </div>
                  <div class="group-shipping">
                    <span>{{ group.shippingLabel }}</span>
                    <strong>运费 ¥{{ formatPrice(group.shippingAmount) }}</strong>
                  </div>
                </div>

                <div v-for="item in group.items" :key="item.id" class="item-row">
                  <img class="item-image" :src="item.image || '/placeholder-image.svg'" :alt="item.name">
                  <div class="item-main">
                    <div class="item-name">{{ item.name }}</div>
                    <div class="item-spec">{{ formatSpecs(item.specs) }}</div>
                    <div class="item-sku">SKU: {{ item.skuId || '无规格' }}</div>
                  </div>
                  <div class="item-side">
                    <div>¥{{ formatPrice(item.price) }}</div>
                    <div>x{{ item.quantity }}</div>
                    <strong>¥{{ formatPrice(item.price * item.quantity) }}</strong>
                  </div>
                </div>
              </article>
            </div>
          </el-card>
        </div>

        <div class="side-column">
          <el-card shadow="never" class="section-card summary-card">
            <div class="section-head">
              <div>
                <h2>费用明细</h2>
                <p>按店铺运费规则实时计算</p>
              </div>
            </div>

            <div class="summary-rows">
              <div class="summary-row">
                <span>商品总额</span>
                <span>¥{{ formatPrice(subtotal) }}</span>
              </div>
              <div class="summary-row">
                <span>运费合计</span>
                <span>¥{{ formatPrice(shippingTotal) }}</span>
              </div>
              <div class="summary-row total">
                <span>应付总额</span>
                <strong>¥{{ formatPrice(totalAmount) }}</strong>
              </div>
            </div>

            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="submitting"
              :disabled="!checkoutItems.length"
              @click="submitOrder"
            >
              提交订单
            </el-button>
          </el-card>
        </div>
      </div>
    </div>

    <el-dialog v-model="showAddressDialog" title="选择地址" width="640px">
      <div v-if="addresses.length" class="address-list">
        <button
          v-for="address in addresses"
          :key="address.id"
          type="button"
          class="address-option"
          :class="{ active: selectedAddress?.id === address.id }"
          @click="selectAddress(address)"
        >
          <div class="address-option-head">
            <span>{{ address.contact }}</span>
            <span>{{ address.mobile }}</span>
            <el-icon v-if="selectedAddress?.id === address.id"><Check /></el-icon>
          </div>
          <div class="address-option-text">{{ formatAddressText(address) }}</div>
        </button>
      </div>
      <el-empty v-else description="暂无地址，请先到个人中心新增地址" :image-size="100" />
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.checkout-page {
  min-height: 100vh;
  padding: 24px 0 48px;
  background: #f5f7fb;
}

.checkout-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.hero-card,
.section-card {
  border: none;
  border-radius: 20px;
}

.hero-card {
  margin-bottom: 20px;
  padding: 28px;
  background: linear-gradient(135deg, #111827, #1f2937);
  color: #fff;
}

.hero-label {
  margin: 0 0 8px;
  color: rgba(255, 255, 255, 0.7);
}

.hero-title {
  margin: 0;
  font-size: 32px;
}

.hero-desc {
  margin: 12px 0 0;
  color: rgba(255, 255, 255, 0.78);
}

.checkout-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 20px;
}

.main-column,
.side-column {
  min-width: 0;
}

.section-card + .section-card {
  margin-top: 20px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 20px;
}

.section-head h2 {
  margin: 0;
  font-size: 20px;
  color: #111827;
}

.section-head p {
  margin: 6px 0 0;
  color: #6b7280;
}

.address-card {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding: 18px;
  border-radius: 16px;
  background: #f9fafb;
}

.address-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: #eff6ff;
  color: #2563eb;
}

.address-user {
  display: flex;
  gap: 12px;
  align-items: center;
  font-weight: 600;
  color: #111827;
}

.default-tag {
  padding: 2px 10px;
  border-radius: 999px;
  background: #dcfce7;
  color: #166534;
  font-size: 12px;
}

.address-text {
  margin-top: 8px;
  color: #4b5563;
  line-height: 1.6;
}

.empty-address {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  border: 1px dashed #cbd5e1;
  border-radius: 16px;
  color: #475569;
  cursor: pointer;
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.shop-group {
  padding: 18px;
  border-radius: 18px;
  background: #f9fafb;
}

.group-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.group-meta {
  display: flex;
  gap: 10px;
  align-items: center;
}

.shop-name {
  font-weight: 700;
  color: #111827;
}

.self-tag {
  padding: 4px 10px;
  border-radius: 999px;
  background: #111827;
  color: #fff;
  font-size: 12px;
}

.group-shipping {
  text-align: right;
  color: #4b5563;
}

.item-row {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr) 120px;
  gap: 16px;
  align-items: center;
  padding: 14px 0;
}

.item-row + .item-row {
  border-top: 1px solid #e5e7eb;
}

.item-image {
  width: 92px;
  height: 92px;
  border-radius: 16px;
  object-fit: cover;
  background: #e5e7eb;
}

.item-main {
  min-width: 0;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.item-spec,
.item-sku {
  margin-top: 8px;
  color: #6b7280;
  font-size: 13px;
}

.item-side {
  text-align: right;
  color: #374151;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-card {
  position: sticky;
  top: 24px;
}

.summary-rows {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  color: #374151;
}

.summary-row.total {
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  font-size: 18px;
  color: #111827;
}

.submit-btn {
  width: 100%;
  margin-top: 24px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-option {
  width: 100%;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.address-option.active {
  border-color: #2563eb;
  background: #eff6ff;
}

.address-option-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  font-weight: 600;
  color: #111827;
}

.address-option-text {
  margin-top: 8px;
  color: #4b5563;
}

@media (max-width: 960px) {
  .checkout-grid {
    grid-template-columns: 1fr;
  }

  .summary-card {
    position: static;
  }
}

@media (max-width: 640px) {
  .group-head,
  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .item-row {
    grid-template-columns: 1fr;
  }

  .item-image {
    width: 100%;
    max-width: 160px;
  }

  .item-side {
    text-align: left;
  }
}
</style>
