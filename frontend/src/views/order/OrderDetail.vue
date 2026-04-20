<template>
  <div class="order-detail-page" v-loading="loading">
    <div class="order-detail-container">
      <div class="top-bar">
        <el-page-header @back="goBack">
          <template #content>
            <div class="page-title-wrap">
              <span class="page-title">订单详情</span>
              <el-tag :type="statusMeta.type" effect="dark">{{ statusMeta.text }}</el-tag>
            </div>
          </template>
          <template #extra>
            <span class="order-no">订单号：{{ order.id || '-' }}</span>
          </template>
        </el-page-header>
      </div>

      <div class="detail-grid">
        <div class="main-column">
          <el-card shadow="never" class="section-card">
            <div class="section-head">
              <h2>收货信息</h2>
            </div>
            <div class="address-box">
              <div class="receiver">{{ order.receiverContact || '-' }} {{ order.receiverMobile || '' }}</div>
              <div class="address">{{ order.receiverAddress || '暂无收货地址' }}</div>
            </div>
          </el-card>

          <el-card shadow="never" class="section-card">
            <div class="section-head">
              <h2>商品清单</h2>
            </div>
            <div class="goods-list">
              <article v-for="item in order.details" :key="item.id" class="goods-item">
                <el-image class="goods-image" :src="item.image" fit="cover">
                  <template #error>
                    <div class="goods-image-fallback">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>

                <div class="goods-main">
                  <div class="goods-name">{{ item.name }}</div>
                  <div v-if="item.spec && Object.keys(item.spec).length" class="goods-spec">
                    {{ formatSpecs(item.spec) }}
                  </div>
                  <div class="goods-sku">SKU：{{ item.skuId || '默认规格' }}</div>
                </div>

                <div class="goods-side">
                  <div>单价 ¥{{ formatPrice(item.price) }}</div>
                  <div>数量 x{{ item.num }}</div>
                  <strong>小计 ¥{{ formatPrice(Number(item.price) * Number(item.num)) }}</strong>
                  <el-button
                    v-if="canCommentItem(item)"
                    type="primary"
                    size="small"
                    @click="handleComment(item)"
                  >
                    立即评价
                  </el-button>
                  <el-tag v-else-if="isItemCommented(item)" type="info">已评价</el-tag>
                </div>
              </article>
            </div>
          </el-card>
        </div>

        <div class="side-column">
          <el-card shadow="never" class="section-card">
            <div class="section-head">
              <h2>订单信息</h2>
            </div>
            <div class="meta-list">
              <div class="meta-row">
                <span>下单时间</span>
                <span>{{ formatTime(order.createTime) }}</span>
              </div>
              <div class="meta-row">
                <span>支付方式</span>
                <span>{{ getPaymentMethod(order.paymentType) }}</span>
              </div>
              <div v-if="order.payTime" class="meta-row">
                <span>支付时间</span>
                <span>{{ formatTime(order.payTime) }}</span>
              </div>
              <div v-if="order.consignTime" class="meta-row">
                <span>发货时间</span>
                <span>{{ formatTime(order.consignTime) }}</span>
              </div>
              <div v-if="order.endTime" class="meta-row">
                <span>完成时间</span>
                <span>{{ formatTime(order.endTime) }}</span>
              </div>
              <div v-if="order.closeTime" class="meta-row">
                <span>关闭时间</span>
                <span>{{ formatTime(order.closeTime) }}</span>
              </div>
            </div>

            <div class="price-box">
              <div class="meta-row total">
                <span>实付金额</span>
                <strong>¥{{ formatPrice(order.totalFee) }}</strong>
              </div>
            </div>

            <div class="action-list">
              <el-button v-if="order.status === 1" type="primary" @click="handlePay">立即付款</el-button>
              <el-button v-if="order.status === 3" type="success" @click="handleReceive">确认收货</el-button>
              <el-button v-if="order.status === 1" @click="handleCancel">取消订单</el-button>
              <el-button v-if="[4, 5, 6].includes(order.status)" type="danger" plain @click="handleDelete">
                删除订单
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CircleCloseFilled, Picture } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelOrder, confirmOrder, deleteOrder, getOrderDetail, type OrderDetailVO, type OrderVO } from '@/api/order'
import { isHandledRequestError } from '@/utils/request'

const route = useRoute()
const router = useRouter()
const loading = ref(false)

const order = ref<OrderVO>({
  id: '',
  userId: '',
  status: 0,
  totalFee: 0,
  paymentType: 0,
  receiverContact: '',
  receiverMobile: '',
  receiverAddress: '',
  createTime: '',
  payTime: '',
  consignTime: '',
  endTime: '',
  closeTime: '',
  details: [],
  commented: false
})

const statusMap: Record<number, { text: string; type: 'success' | 'warning' | 'info' | 'danger' | 'primary' }> = {
  1: { text: '待付款', type: 'danger' },
  2: { text: '待发货', type: 'warning' },
  3: { text: '待收货', type: 'primary' },
  4: { text: '已完成', type: 'success' },
  5: { text: '已取消', type: 'info' },
  6: { text: '已评价', type: 'success' }
}

const statusMeta = computed(() => statusMap[Number(order.value.status)] || { text: '未知状态', type: 'info' as const })

const formatPrice = (price?: number | string | null) => {
  if (price === undefined || price === null || price === '') {
    return '0.00'
  }
  return (Number(price) / 100).toFixed(2)
}

const formatTime = (value?: string) => {
  return value ? value.replace('T', ' ') : '-'
}

const formatSpecs = (specs?: Record<string, string>) => {
  if (!specs || !Object.keys(specs).length) {
    return '默认规格'
  }
  return Object.entries(specs).map(([key, value]) => `${key}: ${value}`).join(' / ')
}

const getPaymentMethod = (type: number) => ({ 1: '支付宝', 2: '微信支付', 3: '余额支付' }[type] || '在线支付')

const isItemCommented = (item: OrderDetailVO) => Boolean(item.commented)

const canCommentItem = (item: OrderDetailVO) => {
  return [4, 6].includes(Number(order.value.status)) && !isItemCommented(item) && Boolean(item.itemId)
}

const buildCommentQuery = (item: OrderDetailVO) => {
  const query: Record<string, string | number> = {
    orderId: String(order.value.id),
    orderDetailId: item.id,
    itemId: item.itemId,
    productName: item.name,
    productImage: item.image || '',
    skuSpecs: formatSpecs(item.spec)
  }
  if (item.skuId && Number(item.skuId) > 0) {
    query.skuId = item.skuId
  }
  return query
}

const handleComment = (item: OrderDetailVO) => {
  if (!item.itemId) {
    ElMessage.warning('订单明细缺少商品信息，无法评价')
    return
  }
  router.push({ path: '/comment/publish', query: buildCommentQuery(item) })
}

const fetchDetail = async (id?: string) => {
  const currentId = id || (route.params.id as string)
  if (!currentId) {
    return
  }
  loading.value = true
  try {
    const res: any = await getOrderDetail(currentId)
    const data = res?.data || res
    const details = Array.isArray(data?.details)
      ? data.details.map((item: any) => ({
          ...item,
          itemId: item.itemId || item.goodsId || item.productId || '',
          skuId: item.skuId || 0,
          commented: Boolean(item.commented)
        }))
      : []

    order.value = {
      ...order.value,
      ...data,
      details,
      commented: typeof data?.commented === 'boolean'
        ? data.commented
        : details.length > 0 && details.every((item: OrderDetailVO) => Boolean(item.commented))
    }
  } catch (error) {
    console.error('获取订单详情失败', error)
    if (!isHandledRequestError(error)) {
      ElMessage.error('订单服务开小差了，请稍后再试')
    }
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  const currentId = route.params.id as string
  if (!currentId) {
    return
  }
  ElMessageBox.confirm('确认取消该订单吗？', '取消订单', { type: 'warning' })
    .then(async () => {
      try {
        loading.value = true
        await cancelOrder(currentId)
        ElMessage.success('订单已取消')
        await fetchDetail(currentId)
      } catch (error) {
        console.error('取消订单失败', error)
        if (!isHandledRequestError(error)) {
          ElMessage.error('订单服务开小差了，请稍后再试')
        }
      } finally {
        loading.value = false
      }
    })
    .catch(() => undefined)
}

const handleReceive = () => {
  const currentId = route.params.id as string
  if (!currentId) {
    return
  }
  ElMessageBox.confirm('确认已收到货物吗？', '确认收货', { type: 'warning' })
    .then(async () => {
      try {
        loading.value = true
        const result: any = await confirmOrder(currentId)
        if (result?.code && result.code !== 200) {
          ElMessage.error('订单服务开小差了，请稍后再试')
          return
        }
        ElMessage.success('收货成功')
        await fetchDetail(currentId)
      } catch (error) {
        console.error('确认收货失败', error)
        if (!isHandledRequestError(error)) {
          ElMessage.error('订单服务开小差了，请稍后再试')
        }
      } finally {
        loading.value = false
      }
    })
    .catch(() => undefined)
}

const handlePay = () => {
  if (!order.value.id) {
    return
  }
  router.push({
    path: '/pay',
    query: {
      orderId: String(order.value.id),
      amount: Number(order.value.totalFee || 0) / 100
    }
  })
}

const handleDelete = () => {
  ElMessageBox.confirm('删除后无法恢复，确认删除该订单吗？', '警告', {
    type: 'error',
    icon: CircleCloseFilled
  })
    .then(async () => {
      try {
        await deleteOrder(order.value.id)
        ElMessage.success('删除成功')
        router.replace('/user?tab=orders')
      } catch (error) {
        console.error('删除订单失败', error)
        if (!isHandledRequestError(error)) {
          ElMessage.error('订单服务开小差了，请稍后再试')
        }
      }
    })
    .catch(() => undefined)
}

const goBack = () => router.back()

watch(
  () => route.params.id,
  (id) => {
    if (typeof id === 'string' && id) {
      fetchDetail(id)
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.order-detail-page {
  min-height: calc(100vh - 64px);
  background: #f5f7fb;
  padding: 96px 0 88px;
}

.order-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.top-bar,
.section-card {
  border: none;
  border-radius: 18px;
}

.top-bar {
  margin-bottom: 20px;
  background: #fff;
  padding: 16px 20px;
}

.page-title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-weight: 700;
  color: #111827;
}

.order-no {
  color: #6b7280;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 20px;
}

.side-column {
  position: sticky;
  top: 96px;
  align-self: start;
}

.section-card + .section-card {
  margin-top: 20px;
}

.section-head {
  margin-bottom: 16px;
}

.section-head h2 {
  margin: 0;
  font-size: 20px;
  color: #111827;
}

.address-box {
  padding: 18px;
  border-radius: 16px;
  background: #f9fafb;
}

.receiver {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.address {
  margin-top: 8px;
  color: #4b5563;
  line-height: 1.6;
}

.goods-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.goods-item {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr) 160px;
  gap: 16px;
  align-items: center;
  padding: 16px;
  border-radius: 18px;
  background: #f9fafb;
}

.goods-image,
.goods-image-fallback {
  width: 96px;
  height: 96px;
  border-radius: 16px;
  overflow: hidden;
}

.goods-image-fallback {
  display: grid;
  place-items: center;
  background: #e5e7eb;
  color: #6b7280;
}

.goods-name {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.goods-spec,
.goods-sku {
  margin-top: 8px;
  color: #6b7280;
  font-size: 13px;
}

.goods-side {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-end;
  text-align: right;
  color: #374151;
}

.meta-list,
.action-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: #374151;
}

.meta-row.total {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.price-box {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.action-list {
  margin-top: 24px;
}

@media (max-width: 960px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .side-column {
    position: static;
  }
}

@media (max-width: 640px) {
  .goods-item {
    grid-template-columns: 1fr;
  }

  .goods-side {
    align-items: flex-start;
    text-align: left;
  }
}
</style>
