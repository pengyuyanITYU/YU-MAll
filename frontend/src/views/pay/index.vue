<template>
  <div class="pay-container">
    <el-card class="pay-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>收银台</span>
        </div>
      </template>

      <!-- 顶部：订单状态与金额 -->
      <div class="order-info">
        <el-result icon="success" title="订单提交成功" sub-title="请在 15 分钟内完成支付，否则订单将自动取消">
          <template #extra>
            <div class="amount-wrapper">
              <span class="label">应付金额：</span>
              <el-statistic :value="amount" :precision="2" prefix="¥" value-style="color: #f56c6c; font-weight: bold; font-size: 24px" />
            </div>
            <div class="order-no">订单号：{{ orderId }}</div>
          </template>
        </el-result>
      </div>

      <el-divider />

      <!-- 中部：支付方式选择 -->
      <div class="payment-section">
        <h3 class="section-title">选择支付方式</h3>
        
        <el-radio-group v-model="form.paymentType" class="payment-methods">
          <!-- 支付宝 -->
          <div 
            class="pay-item" 
            :class="{ active: form.paymentType === 1 }"
            @click="form.paymentType = 1"
          >
            <div class="icon alipay">支</div>
            <div class="info">
              <span class="name">支付宝</span>
              <span class="desc">推荐支付宝用户使用</span>
            </div>
            <el-radio :label="1" class="radio-btn">
              <span style="display:none"></span> <!-- 隐藏默认文字 -->
            </el-radio>
          </div>

          <!-- 微信支付 -->
          <div 
            class="pay-item" 
            :class="{ active: form.paymentType === 2 }"
            @click="form.paymentType = 2"
          >
            <div class="icon wechat">微</div>
            <div class="info">
              <span class="name">微信支付</span>
              <span class="desc">亿万用户的选择</span>
            </div>
            <el-radio :label="2" class="radio-btn">
               <span style="display:none"></span>
            </el-radio>
          </div>

          <!-- 余额支付 -->
          <div 
            class="pay-item" 
            :class="{ active: form.paymentType === 3 }"
            @click="form.paymentType = 3"
          >
            <div class="icon balance">余</div>
            <div class="info">
              <span class="name">余额支付</span>
              <span class="desc">使用账户余额付款</span>
            </div>
            <el-radio :label="3" class="radio-btn">
               <span style="display:none"></span>
            </el-radio>
          </div>
        </el-radio-group>
      </div>

      <!-- 底部：密码输入与操作 -->
      <div class="action-section">
        <!-- 余额支付密码输入框 (带动画) -->
        <transition name="el-zoom-in-top">
          <div v-if="form.paymentType === 3" class="password-input">
            <el-alert 
              title="安全提醒：请输入您的6位支付密码" 
              type="warning" 
              :closable="false" 
              show-icon 
              style="margin-bottom: 15px;"
            />
            <el-form :model="form" @submit.prevent>
              <el-form-item 
                :error="passwordError"
                label="支付密码"
              >
                <el-input
                  v-model="form.payPassword"
                  type="password"
                  show-password
                  placeholder="请输入6位支付密码"
                  maxlength="6"
                  size="large"
                  style="width: 100%"
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-form>
          </div>
        </transition>

        <div class="submit-btn-wrapper">
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading" 
            class="submit-btn"
            @click="handlePay"
          >
            确认支付 ¥{{ amount.toFixed(2) }}
          </el-button>
        </div>
      </div>

    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Lock } from '@element-plus/icons-vue'; // 需安装 @element-plus/icons-vue
import { payOrder, type PayOrderFormDTO } from '@/api/pay'; // 导入你提供的 API

// Hooks
const route = useRoute();
const router = useRouter();

// 状态
const loading = ref(false);
const passwordError = ref('');
const orderId = ref('');
const amount = ref(0);

// 表单数据
const form = reactive<PayOrderFormDTO>({
  bizOrderNo: '',
  amount: 0,
  paymentType: 1, // 默认支付宝
  payPassword: ''
});

// 初始化
onMounted(() => {
  // 从路由参数获取订单信息
  // 示例 URL: /pay?orderId=123456&amount=88.50
  const qOrderId = route.query.orderId as string;
  const qAmount = route.query.amount;

  if (!qOrderId) {
    ElMessage.error('订单参数错误');
    // router.back(); // 实际项目中可能需要返回
    return;
  }

  orderId.value = qOrderId;
  amount.value = Number(qAmount) || 0;

  // 同步到 DTO
  form.bizOrderNo = orderId.value;
  form.amount = amount.value;
});

// 支付处理
const handlePay = async () => {
  passwordError.value = '';

  // 1. 校验逻辑
  if (form.paymentType === 3) {
    if (!form.payPassword) {
      passwordError.value = '请输入支付密码';
      return;
    }
    if (form.payPassword.length < 6) {
      passwordError.value = '密码长度不足6位';
      return;
    }
  } else {
    // 非余额支付清空密码
    form.payPassword = '';
  }

  // 2. 发送请求
  loading.value = true;
  try {
    await payOrder({
      bizOrderNo: form.bizOrderNo,
      amount: form.amount * 100,
      paymentType: form.paymentType,
      payPassword: form.payPassword
    });

    ElMessage.success('支付成功！');
    
    // 3. 支付成功跳转 (例如跳转到订单列表页)
    router.replace(`/order/${form.bizOrderNo}`);

  } catch (error: any) {
 
    ElMessage.error( '支付失败');
    // 这里的错误提示通常由 request 拦截器处理，如果没有处理，可手动弹出
    // ElMessage.error(error.message || '支付失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped lang="scss">
.pay-container {
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 40px 20px;
  display: flex;
  justify-content: center;
}

.pay-card {
  width: 100%;
  max-width: 800px;
  border-radius: 8px;

  :deep(.el-card__header) {
    padding: 15px 20px;
    font-weight: bold;
    font-size: 16px;
  }
}

.order-info {
  display: flex;
  justify-content: center;
  padding-bottom: 10px;

  :deep(.el-result) {
    padding-top: 0;
    padding-bottom: 20px;
  }

  .amount-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 10px;
    
    .label {
      font-size: 16px;
      color: #606266;
      margin-right: 5px;
    }
  }

  .order-no {
    margin-top: 8px;
    color: #909399;
    font-size: 14px;
  }
}

.payment-section {
  padding: 10px 20px;

  .section-title {
    font-size: 16px;
    margin-bottom: 20px;
    color: #303133;
  }

  .payment-methods {
    display: flex;
    flex-direction: column;
    gap: 15px;
    width: 100%;
  }

  .pay-item {
    display: flex;
    align-items: center;
    padding: 15px 20px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s;
    position: relative;

    &:hover {
      border-color: #409eff;
      background-color: #ecf5ff;
    }

    &.active {
      border-color: #409eff;
      background-color: #ecf5ff;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    }

    .icon {
      width: 40px;
      height: 40px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-weight: bold;
      font-size: 20px;
      margin-right: 15px;

      &.alipay { background-color: #1677ff; }
      &.wechat { background-color: #07c160; }
      &.balance { background-color: #ff9900; }
    }

    .info {
      flex: 1;
      display: flex;
      flex-direction: column;

      .name {
        font-weight: bold;
        font-size: 16px;
        color: #303133;
      }
      .desc {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }

    .radio-btn {
      margin-left: auto;
    }
  }
}

.action-section {
  margin-top: 30px;
  padding: 0 20px 20px;

  .password-input {
    max-width: 400px;
    margin: 0 auto 20px;
  }

  .submit-btn-wrapper {
    text-align: center;
  }

  .submit-btn {
    width: 100%;
    max-width: 400px;
    height: 50px;
    font-size: 18px;
    font-weight: bold;
  }
}
</style>