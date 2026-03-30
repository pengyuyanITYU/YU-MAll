<template>
  <div class="order-detail-page" v-loading="loading">
    <div class="container">
      
      <!-- 1. 顶部 Header -->
      <div class="page-header">
        <el-page-header @back="goBack">
          <template #content>
            <span class="header-title">订单详情</span>
          </template>
          <template #extra>
            <div class="header-info">
              <span class="order-id">订单号：{{ order.id }}</span>
              <el-tag :type="statusMeta.type" effect="dark" size="large">
                {{ statusMeta.text }}
              </el-tag>
            </div>
          </template>
        </el-page-header>
      </div>

      <!-- 2. 状态进度条与操作区 -->
      <el-card v-if="order.status !== 5" class="section-card" shadow="never">
        <el-steps :active="activeStep" align-center finish-status="success" class="custom-steps">
          <el-step title="提交订单" :description="formatTime(order.createTime)" />
          <el-step title="完成付款" :description="formatTime(order.payTime)" />
          <el-step title="商家发货" :description="formatTime(order.consignTime)" />
          <el-step title="交易完成" :description="formatTime(order.endTime)" />
        </el-steps>
        
        <div class="action-bar">
          <el-button v-if="order.status === 1" type="primary" size="large" @click="handlePay">立即付款</el-button>
          <el-button v-if="order.status === 3" type="success" size="large" @click="handleReceive">确认收货</el-button>
          <el-button v-if="order.status === 1" @click="handleCancel">取消订单</el-button>
          <el-button v-if="order.status === 4 || order.status === 6" type="danger" plain @click="handleDelete">删除订单</el-button>
        </div>
      </el-card>

      <!-- 订单已取消状态 -->
      <el-card v-else class="section-card cancel-card" shadow="never">
        <div class="cancel-content">
          <el-icon :size="50" color="#909399"><CircleCloseFilled /></el-icon>
          <div class="cancel-text">
            <h3>订单已取消</h3>
            <p>取消时间：{{ order.closeTime}}</p>
            <p class="reason">如果您想重新购买，请重新下单</p>
          </div>
        </div>
        <div class="action-bar" style="margin-top: 20px; text-align: center;">
          <el-button type="danger" plain @click="handleDelete">删除订单</el-button>
        </div>
      </el-card>

      <el-row :gutter="20" class="info-row">
        <!-- 左侧：主要信息 -->
        <el-col :xs="24" :sm="16">
          
          <!-- 收货信息 -->
          <el-card shadow="never" class="section-card">
            <template #header>
              <div class="card-header"><el-icon><Location /></el-icon> <span>收货信息</span></div>
            </template>
            <div class="address-box">
              <div class="addr-user">{{ order.receiverContact }} <span class="addr-phone">{{ order.receiverMobile }}</span></div>
              <div class="addr-detail">{{ order.receiverAddress }}</div>
            </div>
          </el-card>

          <!-- 商品列表 (v-for 卡片式布局) -->
          <el-card shadow="never" class="section-card goods-card">
            <template #header>
              <div class="card-header"><el-icon><Goods /></el-icon> <span>商品清单</span></div>
            </template>
            
            <div class="goods-list">
              <div 
                v-for="(item, index) in order.details" 
                :key="item.id || index" 
                class="goods-item"
              >
                <!-- 商品图片 -->
                <el-image :src="item.image" class="goods-img" fit="cover">
                   <template #error><div class="img-slot"><el-icon><Picture /></el-icon></div></template>
                </el-image>

                <!-- 商品主体信息 -->
                <div class="goods-main">
                  <div class="goods-name">{{ item.name }}</div>
                  <div class="goods-props" v-if="item.spec">
                    <span v-for="(val, key) in item.spec" :key="key" class="prop-tag">
                      {{ key }}: {{ val }}
                    </span>
                  </div>
                </div>

                <!-- 价格与数量 -->
                <div class="goods-price-col">
                  <div class="price">¥{{ (item.price / 100).toFixed(2) }}</div>
                  <div class="num">x{{ item.num }}</div>
                </div>

                <!-- 右侧操作区 (小计 + 按钮) -->
                <div class="goods-action-col">
                  <div class="subtotal">
                    合计: <span>¥{{ (item.price * item.num / 100).toFixed(2) }}</span>
                  </div>
                  
                  <!-- 评价逻辑修改区域 -->
                  <div class="btn-wrapper">
                    <!-- 
                      1. 立即评价
                      条件：订单状态为已完成(4) 且 未评价(!order.commented)
                    -->
                    <el-button 
                      v-if="order.status === 4 && !order.commented" 
                      type="primary" 
                      size="small" 
                      round
                      @click="handleComment(item)"
                    >
                      立即评价
                    </el-button>

                    <!-- 
                      2. 已评价 (仅显示标签，不显示按钮)
                      条件：订单状态为已评价(6) 或 order.commented 为 true
                    -->
                    <el-tag 
                      v-else-if="order.status === 6 || order.commented" 
                      type="info" 
                      effect="plain"
                      round
                    >
                      已评价
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：订单金额信息 -->
        <el-col :xs="24" :sm="8">
          <el-card shadow="never" class="section-card">
            <template #header>
              <div class="card-header"><el-icon><List /></el-icon> <span>订单信息</span></div>
            </template>
            <div class="order-meta-list">
              <div class="meta-item">
                <span class="label">订单编号</span><span class="val">{{ route.params.id }}</span>
              </div>
              <div class="meta-item">
                <span class="label">下单时间</span><span class="val">{{ order.createTime }}</span>
              </div>
              <div class="meta-item" v-if="order.status === 5">
                <span class="label">取消时间</span><span class="val">{{ order.closeTime }}</span>
              </div>
              <div class="meta-item">
                <span class="label">支付方式</span><span class="val">{{ getPaymentMethod(order.paymentType) }}</span>
              </div>
            </div>
            <el-divider />
            <div class="fee-section">
              <div class="fee-row">
                <span>商品总额</span><span>¥{{ (order.totalFee / 100).toFixed(2) }}</span>
              </div>
              <div class="fee-row total">
                <span>实付款</span>
                <span class="total-price"><small>¥</small>{{ (order.totalFee / 100).toFixed(2) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Location, Goods, List, Picture, CircleCloseFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, deleteOrder, cancelOrder, type OrderVO, confirmOrder } from '@/api/order'

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

// 状态映射
const statusMap: Record<number, { text: string; type: string; step: number }> = {
  1: { text: '待付款', type: 'danger', step: 0 },
  2: { text: '待发货', type: 'warning', step: 1 },
  3: { text: '待收货', type: 'primary', step: 2 },
  4: { text: '已完成', type: 'success', step: 3 },
  5: { text: '已取消', type: 'info', step: 0 },
  6: { text: '已评价', type: 'success', step: 3 }
}

const statusMeta = computed(() => {
  return statusMap[order.value.status] || { text: '未知状态', type: 'info', step: 0 }
})

const activeStep = computed(() => statusMeta.value.step)

// ------------------------------------------------------
// 跳转评价逻辑
// ------------------------------------------------------
const handleComment = (item: any) => {
  let skuSpecsStr = ''
  if (item.spec) {
    skuSpecsStr = Object.values(item.spec).join(', ')
  }

  // 安全检查
  if (!item.itemId) {
    console.error('商品ID缺失', item)
    ElMessage.warning('商品数据异常，请刷新重试')
    return
  }
  
  router.push({
    path: '/comment/publish',
    query: {
      orderId: order.value.id,
      orderDetailId: item.id,
      itemId: item.itemId, 
      skuId: item.skuId || 0,
      productName: item.name,
      productImage: item.image,
      skuSpecs: skuSpecsStr
    }
  })
}

// ------------------------------------------------------
// 获取详情 + 数据清洗
// ------------------------------------------------------
const fetchDetail = async () => {
  const id:any = route.params.id  
  if (!id) return
  loading.value = true
  try {
    const res: any = await getOrderDetail(id)
    const data = res.data || res
    
    if (data) {
      // 核心处理：确保 details 里的每一项都有 itemId
      if (data.details && data.details.length > 0) {
        data.details.forEach((item: any, index: number) => {
          if (!item.itemId) {
             item.itemId = item.goodsId || item.productId;
             if (!item.itemId) {
               item.itemId = 10000 + index; 
             }
          }
        });
      }
      order.value = data
    }
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

// ------------------------------------------------------
// 其他业务逻辑
// ------------------------------------------------------
const handleCancel = () => {
 const currentId = (route.params.id as string) 
  if (!currentId) return
  ElMessageBox.confirm('确定要取消该订单吗？', '取消确认', {
      confirmButtonText: '确定取消', cancelButtonText: '暂不取消', type: 'warning'
  }).then(async () => {
    try {
      loading.value = true
      await cancelOrder(currentId)
      ElMessage.success('订单已取消')
      await fetchDetail()
    } catch (e) { ElMessage.error('操作失败') }
    finally { loading.value = false }
  }).catch(() => {})
}

const handleReceive = async () => {
  const currentId = (route.params.id as string) 
  if (!currentId) return
  ElMessageBox.confirm('确认收到货品？', '确认收货', { type: 'warning' }).then(async () => {
    try {
      loading.value = true 
      const result: any = await confirmOrder(currentId)
      if (result.code === 200 || result === undefined) { 
        ElMessage.success('收货成功')
        await fetchDetail() 
      } else { ElMessage.error(result.message || '操作失败') }
    } catch (e) { ElMessage.error('操作失败') }
    finally { loading.value = false }
  }).catch(() => {})
}

const handlePay = () => { ElMessage.success('跳转支付页面...') }
const handleDelete = () => {
  ElMessageBox.confirm('删除后无法恢复，确认删除？', '警告', { type: 'error' }).then(async () => {
    await deleteOrder(order.value.id)
    router.replace('/user?tab=orders')
  })
}

const goBack = () => router.back()
const formatTime = (t?: string) => t ? t.replace('T', ' ') : ''
const getPaymentMethod = (t: number) => ({1:'支付宝',2:'微信',3:'余额'}[t] || '在线支付')

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped lang="scss">
.order-detail-page { background: #f5f7fa; min-height: 100vh; padding: 24px 0; }
.container { max-width: 1200px; margin: 0 auto; padding: 0 16px; }

/* 头部样式 */
.page-header {
  background: #fff; padding: 16px 24px; border-radius: 8px; margin-bottom: 20px;
  .header-info { display: flex; align-items: center; gap: 16px; .order-id { color: #606266; font-size: 14px; } }
}

/* 卡片通用 */
.section-card { border: none; border-radius: 8px; margin-bottom: 20px; }
.card-header { display: flex; align-items: center; gap: 8px; font-weight: 600; color: #303133; }
.action-bar { margin-top: 30px; display: flex; justify-content: center; gap: 16px; padding-bottom: 10px; }

/* 进度条微调 */
:deep(.custom-steps .el-step__description) { font-size: 12px; margin-top: 5px; }

/* 收货地址 */
.address-box {
  padding: 10px 0;
  .addr-user { font-size: 16px; font-weight: 500; color: #303133; margin-bottom: 8px; .addr-phone { margin-left: 10px; color: #606266; font-size: 14px; } }
  .addr-detail { color: #606266; font-size: 14px; line-height: 1.5; }
}

/* 商品列表 - 卡片式布局核心样式 */
.goods-list {
  display: flex;
  flex-direction: column;
  
  .goods-item {
    display: flex;
    align-items: center;
    padding: 20px 0;
    border-bottom: 1px solid #f2f2f2;
    
    &:last-child { border-bottom: none; padding-bottom: 0; }
    &:first-child { padding-top: 0; }

    .goods-img {
      width: 80px; height: 80px; border-radius: 6px; border: 1px solid #eee; margin-right: 16px; flex-shrink: 0;
      display: flex; align-items: center; justify-content: center; background: #fff;
    }

    .goods-main {
      flex: 1;
      margin-right: 20px;
      .goods-name { font-size: 14px; color: #303133; margin-bottom: 8px; line-height: 1.4; }
      .goods-props {
        display: flex; flex-wrap: wrap; gap: 6px;
        .prop-tag { font-size: 12px; color: #909399; background: #f4f4f5; padding: 2px 6px; border-radius: 4px; }
      }
    }

    .goods-price-col {
      text-align: right; margin-right: 30px; min-width: 80px;
      .price { font-size: 14px; color: #303133; }
      .num { font-size: 12px; color: #909399; margin-top: 4px; }
    }

    .goods-action-col {
      display: flex; flex-direction: column; align-items: flex-end; gap: 10px; min-width: 100px;
      .subtotal {
        font-size: 13px; color: #606266;
        span { color: #f56c6c; font-weight: 600; font-size: 15px; margin-left: 4px; }
      }
    }
  }
}

/* 订单侧边栏信息 */
.order-meta-list {
  padding: 0 10px;
  .meta-item { display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 13px; .label { color: #909399; } .val { color: #303133; } }
}
.fee-section {
  padding: 0 10px;
  .fee-row { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 13px; color: #606266; &.total { margin-top: 16px; align-items: flex-end; .total-price { color: #f56c6c; font-size: 20px; font-weight: bold; } } }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .goods-item {
    flex-wrap: wrap; position: relative;
    .goods-main { margin-right: 0; width: calc(100% - 100px); } /* 图片80+间距 */
    .goods-price-col { position: absolute; right: 0; top: 20px; margin-right: 0; text-align: right; }
    .goods-action-col { width: 100%; flex-direction: row; justify-content: space-between; align-items: center; margin-top: 12px; padding-top: 12px; border-top: 1px dashed #eee; }
  }
}
</style>