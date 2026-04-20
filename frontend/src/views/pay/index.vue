<template>
  <div class="pay-page">
    <div class="pay-shell">
      <section class="pay-hero">
        <div class="hero-copy">
          <el-tag effect="dark" type="success" round>订单待支付</el-tag>
          <h1>请在当前页面完成订单支付</h1>
          <p>支付成功后会自动同步订单状态，并跳转到订单详情页。</p>
        </div>
        <div class="hero-amount">
          <span>应付金额</span>
          <strong>¥{{ amount.toFixed(2) }}</strong>
          <em>订单号 {{ orderId }}</em>
        </div>
      </section>

      <div class="pay-layout">
        <el-card class="pay-main" shadow="never">
          <template #header>
            <div class="card-header">
              <div>
                <div class="card-title">支付方式</div>
                <div class="card-subtitle">选择你希望使用的支付渠道</div>
              </div>
              <el-button link type="primary" @click="goToOrderDetail">查看订单</el-button>
            </div>
          </template>

          <section class="section">
            <div class="section-title">选择支付方式</div>
            <el-radio-group v-model="form.paymentType" class="payment-list">
              <label
                v-for="item in paymentOptions"
                :key="item.value"
                class="payment-item"
                :class="{ active: form.paymentType === item.value }"
              >
                <div class="payment-main" @click="form.paymentType = item.value">
                  <div class="payment-icon" :class="item.className">{{ item.shortName }}</div>
                  <div class="payment-copy">
                    <strong>{{ item.label }}</strong>
                    <span>{{ item.desc }}</span>
                  </div>
                </div>
                <el-radio :label="item.value">
                  <span />
                </el-radio>
              </label>
            </el-radio-group>
          </section>

          <section v-if="form.paymentType === 3" class="section">
            <div class="section-title">支付密码</div>
            <el-alert
              title="余额支付需要输入 6 位支付密码。"
              type="warning"
              :closable="false"
              show-icon
              class="password-alert"
            />
            <el-input
              v-model="form.payPassword"
              type="password"
              show-password
              maxlength="6"
              placeholder="请输入 6 位支付密码"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
            <div v-if="passwordError" class="password-error">{{ passwordError }}</div>
          </section>

          <section v-if="confirmHint" class="section">
            <el-alert :title="confirmHint" type="info" :closable="false" show-icon />
          </section>
        </el-card>

        <aside class="pay-side">
          <el-card class="summary-card" shadow="never">
            <div class="side-title">支付概览</div>
            <div class="summary-grid">
              <div class="summary-row">
                <span>订单号</span>
                <strong>{{ orderId }}</strong>
              </div>
              <div class="summary-row">
                <span>当前方式</span>
                <strong>{{ selectedPaymentOption.label }}</strong>
              </div>
              <div class="summary-row total">
                <span>应付金额</span>
                <strong>¥{{ amount.toFixed(2) }}</strong>
              </div>
            </div>

            <div class="side-tip">
              {{ selectedPaymentOption.desc }}
            </div>

            <div class="action-row">
              <el-button plain @click="goToOrderDetail">查看订单详情</el-button>
              <el-button type="primary" :loading="submitting" @click="handlePay">
                确认支付 ¥{{ amount.toFixed(2) }}
              </el-button>
            </div>
          </el-card>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { getOrderDetail } from '@/api/order'
import { payOrder, type PayOrderFormDTO } from '@/api/pay'
import { isHandledRequestError } from '@/utils/request'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const confirming = ref(false)
const passwordError = ref('')
const confirmHint = ref('')
const orderId = ref('')
const amount = ref(0)

const form = reactive<PayOrderFormDTO>({
  bizOrderNo: '',
  amount: 0,
  paymentType: 1,
  payPassword: ''
})

const paymentOptions = [
  { value: 1, shortName: '支', label: '支付宝', desc: '推荐使用支付宝完成付款', className: 'alipay' },
  { value: 2, shortName: '微', label: '微信支付', desc: '使用微信扫码或快捷支付', className: 'wechat' },
  { value: 3, shortName: '余', label: '余额支付', desc: '使用账户余额完成付款', className: 'balance' }
]

const submitting = computed(() => loading.value || confirming.value)
const selectedPaymentOption = computed(() => {
  return paymentOptions.find(item => item.value === form.paymentType) || paymentOptions[0]
})

const parseOrderStatus = (value: unknown) => {
  const status = Number(value)
  return Number.isNaN(status) ? 0 : status
}

const isPaidStatus = (status: number) => [2, 3, 4, 5, 6].includes(status)

const sleep = (ms: number) => new Promise((resolve) => {
  window.setTimeout(resolve, ms)
})

const goToOrderDetail = () => {
  if (!form.bizOrderNo) {
    return
  }
  router.replace(`/order/${form.bizOrderNo}`)
}

const initForm = () => {
  const rawOrderId = route.query.orderId
  const rawAmount = route.query.amount
  const parsedOrderId = typeof rawOrderId === 'string' ? rawOrderId : ''
  const parsedAmount = Number(rawAmount)

  if (!parsedOrderId) {
    ElMessage.error('订单参数缺失')
    router.replace('/user?tab=orders')
    return
  }

  orderId.value = parsedOrderId
  amount.value = Number.isFinite(parsedAmount) ? parsedAmount : 0
  form.bizOrderNo = parsedOrderId
  form.amount = Math.round(amount.value * 100)
}

const waitForPaymentResult = async () => {
  confirming.value = true
  confirmHint.value = '正在确认支付结果，请稍候...'

  for (let attempt = 0; attempt < 10; attempt += 1) {
    try {
      const res: any = await getOrderDetail(form.bizOrderNo)
      const data = res?.data || res
      const status = parseOrderStatus(data?.status)
      if (isPaidStatus(status)) {
        confirmHint.value = ''
        router.replace(`/order/${form.bizOrderNo}`)
        return
      }
    } catch (error) {
      console.error('轮询订单状态失败', error)
    }

    if (attempt < 9) {
      await sleep(1000)
    }
  }

  confirmHint.value = '支付请求已提交，但订单状态仍在同步中。你可以直接进入订单详情页查看最新结果。'
  ElMessage.warning('支付结果确认中，请稍后查看订单详情')
  confirming.value = false
}

const handlePay = async () => {
  passwordError.value = ''

  if (!form.bizOrderNo) {
    ElMessage.error('订单参数缺失')
    return
  }

  if (form.paymentType === 3) {
    if (!form.payPassword) {
      passwordError.value = '请输入支付密码'
      return
    }
    if (form.payPassword.length !== 6) {
      passwordError.value = '支付密码必须为 6 位'
      return
    }
  } else {
    form.payPassword = ''
  }

  loading.value = true
  try {
    await payOrder({
      bizOrderNo: form.bizOrderNo,
      amount: form.amount,
      paymentType: form.paymentType,
      payPassword: form.payPassword
    })
    ElMessage.success('支付请求已提交')
  } catch (error) {
    loading.value = false
    if (!isHandledRequestError(error)) {
      ElMessage.error('支付服务暂时不可用，请稍后再试')
    }
    return
  }

  loading.value = false
  await waitForPaymentResult()
}

onMounted(() => {
  initForm()
})
</script>

<style scoped lang="scss">
.pay-page {
  min-height: calc(100vh - 64px);
  padding: 96px 16px 88px;
  background:
    radial-gradient(circle at top left, rgba(59, 130, 246, 0.12), transparent 28%),
    radial-gradient(circle at right center, rgba(14, 165, 233, 0.10), transparent 22%),
    linear-gradient(180deg, #f5f7fb 0%, #eef4fb 100%);
}

.pay-shell {
  max-width: 1200px;
  margin: 0 auto;
}

.pay-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 24px;
  align-items: center;
  margin-bottom: 24px;
  padding: 28px 32px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.08);
}

.hero-copy {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-copy h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.2;
  color: #111827;
}

.hero-copy p {
  margin: 0;
  color: #64748b;
  font-size: 15px;
}

.hero-amount {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  padding: 20px 24px;
  border-radius: 22px;
  background: linear-gradient(135deg, #eff6ff 0%, #ffffff 100%);
}

.hero-amount span,
.hero-amount em {
  color: #64748b;
  font-style: normal;
}

.hero-amount strong {
  color: #dc2626;
  font-size: 40px;
  line-height: 1;
}

.pay-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 24px;
  align-items: start;
}

.pay-main,
.summary-card {
  border: none;
  border-radius: 24px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
}

.pay-side {
  position: sticky;
  top: 96px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.card-title,
.side-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}

.card-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}

.section + .section {
  margin-top: 28px;
}

.section-title {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.payment-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  width: 100%;
}

.payment-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.payment-item:hover {
  transform: translateY(-1px);
  border-color: #bfdbfe;
}

.payment-item.active {
  border-color: #2563eb;
  background: #eff6ff;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.08);
}

.payment-main {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
}

.payment-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
}

.payment-icon.alipay {
  background: #1677ff;
}

.payment-icon.wechat {
  background: #07c160;
}

.payment-icon.balance {
  background: #f59e0b;
}

.payment-copy {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.payment-copy strong {
  color: #111827;
  font-size: 16px;
}

.payment-copy span {
  color: #6b7280;
  font-size: 13px;
}

.password-alert {
  margin-bottom: 16px;
}

.password-error {
  margin-top: 8px;
  color: #dc2626;
  font-size: 13px;
}

.summary-grid {
  display: grid;
  gap: 16px;
  margin-top: 20px;
}

.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: #475569;
}

.summary-row strong {
  color: #111827;
}

.summary-row.total {
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.summary-row.total strong {
  color: #dc2626;
  font-size: 32px;
}

.side-tip {
  margin-top: 20px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.action-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 24px;
}

.action-row :deep(.el-button) {
  width: 100%;
  height: 44px;
}

@media (max-width: 960px) {
  .pay-page {
    padding: 84px 12px 72px;
  }

  .pay-hero,
  .pay-layout {
    grid-template-columns: 1fr;
  }

  .hero-amount {
    align-items: flex-start;
  }

  .pay-side {
    position: static;
  }
}

@media (max-width: 640px) {
  .pay-hero {
    padding: 22px 20px;
  }

  .hero-copy h1 {
    font-size: 28px;
  }

  .hero-amount strong {
    font-size: 32px;
  }

  .payment-item {
    align-items: flex-start;
  }
}
</style>
