<script setup lang="ts">
import { reactive, ref, shallowRef } from 'vue'
import {
  ArrowRight,
  ChatDotRound,
  CircleCheckFilled,
  Lock,
  Monitor,
  Shop,
  User
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SliderCaptchaDialog from '@/components/auth/SliderCaptchaDialog.vue'
import { login } from '@/api/login'
import { useSliderCaptcha } from '@/composables/useSliderCaptcha'
import { useUserStore } from '@/stores/useUserStore'
import { resolveSimpleAuthErrorMessage } from '@/utils/request'

interface LoginPageForm {
  username: string
  password: string
  rememberMe: boolean
}

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = shallowRef(false)
const userStore = useUserStore()
const {
  captchaVisible,
  captchaChallenge,
  captchaLoading,
  captchaVerifying,
  captchaErrorMessage,
  openCaptcha,
  refreshCaptcha,
  submitCaptcha
} = useSliderCaptcha()

const form = reactive<LoginPageForm>({
  username: '',
  password: '',
  rememberMe: false
})

const rules = reactive<FormRules<LoginPageForm>>({
  username: [
    { required: true, message: '请输入用户名或手机号', trigger: 'blur' },
    { min: 2, message: '长度不能少于 2 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于 6 位', trigger: 'blur' }
  ]
})

const trustBadges = ['官方正品', '极速物流', '售后无忧']
const authTabs = ['密码登录', '验证码登录']
const socialEntrances = [
  { label: '站内消息', icon: ChatDotRound },
  { label: '桌面端', icon: Monitor }
]

const resolveLoginErrorMessage = (error: any) => {
  return resolveSimpleAuthErrorMessage(error, '登录失败')
}

const performLogin = async (captchaTicket?: string) => {
  const response: any = await login({
    username: form.username,
    password: form.password,
    rememberMe: form.rememberMe,
    captchaTicket
  })
  const loginVo = response?.data

  if (!loginVo?.token) {
    throw new Error('登录失败')
  }

  localStorage.setItem('Authorization', loginVo.token)
  userStore.updateUserInfo({
    ...loginVo,
    username: form.username
  })
  ElMessage.success('登录成功，欢迎回来')
  await router.push('/')
}

const submitLoginFlow = async (captchaTicket?: string) => {
  try {
    await performLogin(captchaTicket)
  } catch (error: any) {
    if (error?.code === 428 && !captchaTicket) {
      const ticket = await openCaptcha('LOGIN')
      return submitLoginFlow(ticket)
    }
    throw error
  }
}

const handleLogin = async (formEl: FormInstance | undefined) => {
  if (!formEl) return

  const valid = await formEl.validate().then(() => true).catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await submitLoginFlow()
  } catch (error: any) {
    if (!error?.cancelled) {
      ElMessage.error(resolveLoginErrorMessage(error))
    }
  } finally {
    loading.value = false
  }
}

const handleGuest = () => {
  router.push('/')
}

const goRegister = () => {
  router.push('/register')
}
</script>

<template>
  <div class="auth-page">
    <header class="brand-strip">
      <div class="brand-icon">
        <el-icon><Shop /></el-icon>
      </div>
      <span class="brand-name">YU-Mall</span>
    </header>

    <main class="auth-stage">
      <section class="auth-shell">
        <aside class="hero-panel">
          <div class="hero-logo">
            <el-icon><Shop /></el-icon>
          </div>
          <h1 class="hero-title">YU-Mall</h1>
          <p class="hero-subtitle">探索品质生活新方式</p>

          <div class="hero-badges">
            <span v-for="badge in trustBadges" :key="badge" class="hero-badge">
              <el-icon><CircleCheckFilled /></el-icon>
              {{ badge }}
            </span>
          </div>
        </aside>

        <section class="form-panel">
          <div class="auth-tabs" role="tablist" aria-label="登录方式">
            <button
              v-for="(tab, index) in authTabs"
              :key="tab"
              type="button"
              class="tab-button"
              :class="{ active: index === 0 }"
              :disabled="index !== 0"
            >
              {{ tab }}
            </button>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            size="large"
            class="auth-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="手机号 / 邮箱 / 用户名"
                :prefix-icon="User"
                @keyup.enter="handleLogin(formRef)"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入登录密码"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin(formRef)"
              />
            </el-form-item>

            <div class="form-meta">
              <el-checkbox v-model="form.rememberMe">保持登录状态</el-checkbox>
            </div>

            <p class="form-agreement">
              登录即表示你已阅读并同意
              <span>《服务协议》</span>
              和
              <span>《隐私政策》</span>
            </p>

            <el-form-item class="submit-item">
              <el-button
                type="primary"
                class="submit-button"
                :loading="loading"
                @click="handleLogin(formRef)"
              >
                立即登录
              </el-button>
            </el-form-item>
          </el-form>

          <div class="action-links">
            <el-link type="info" underline="never">忘记密码？</el-link>
            <span class="link-divider">|</span>
            <el-link type="primary" underline="never" @click="goRegister">
              没有账号？立即注册
            </el-link>
          </div>

          <div class="other-login">
            <span class="line"></span>
            <span>其他登录方式</span>
            <span class="line"></span>
          </div>

          <div class="social-row">
            <button
              v-for="entry in socialEntrances"
              :key="entry.label"
              type="button"
              class="social-button"
              :aria-label="entry.label"
            >
              <el-icon>
                <component :is="entry.icon" />
              </el-icon>
            </button>
          </div>

          <div class="guest-entry">
            <el-link type="info" underline="never" @click="handleGuest">
              暂不登录，先去逛逛
              <el-icon><ArrowRight /></el-icon>
            </el-link>
          </div>
        </section>
      </section>
    </main>

    <SliderCaptchaDialog
      v-model="captchaVisible"
      :challenge="captchaChallenge"
      :loading="captchaLoading"
      :verifying="captchaVerifying"
      :error-message="captchaErrorMessage"
      @refresh="refreshCaptcha"
      @submit="submitCaptcha"
    />
  </div>
</template>

<style scoped lang="scss">
.auth-page {
  min-height: 100vh;
  padding: 34px 24px 40px;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.96) 0, rgba(255, 255, 255, 0.4) 22%, transparent 48%),
    linear-gradient(180deg, #f7f8fb 0%, #eef2f8 100%);
}

.brand-strip {
  display: flex;
  align-items: center;
  gap: 14px;
  width: min(100%, 1320px);
  margin: 0 auto;
}

.brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: #1660dc;
  color: #fff;
  font-size: 20px;
  box-shadow: 0 12px 24px rgba(22, 96, 220, 0.24);
}

.brand-name {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.02em;
  color: #161c2d;
}

.auth-stage {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 108px);
  padding-top: 28px;
}

.auth-shell {
  width: min(100%, 1200px);
  min-height: 612px;
  display: grid;
  grid-template-columns: minmax(390px, 1.08fr) minmax(500px, 1.42fr);
  background: #fff;
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 32px 80px rgba(29, 55, 112, 0.16);
  margin-bottom: 75px;
}

.hero-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 40px;
  background: linear-gradient(180deg, #1c60dc 0%, #1858cb 100%);
  color: #fff;
  overflow: hidden;
}

.hero-panel::before,
.hero-panel::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
}

.hero-panel::before {
  top: -78px;
  left: -84px;
  width: 270px;
  height: 270px;
}

.hero-panel::after {
  right: -112px;
  bottom: -136px;
  width: 356px;
  height: 356px;
}

.hero-logo {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 96px;
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.22), rgba(255, 255, 255, 0.12));
  border: 1px solid rgba(255, 255, 255, 0.14);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.2);
  font-size: 42px;
}

.hero-title {
  position: relative;
  z-index: 1;
  margin: 28px 0 12px;
  font-size: 48px;
  font-weight: 800;
  letter-spacing: 0.01em;
}

.hero-subtitle {
  position: relative;
  z-index: 1;
  margin: 0;
  font-size: 22px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.88);
}

.hero-badges {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 18px;
  margin-top: 44px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.86);
}

.form-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 52px 70px 40px;
}

.auth-tabs {
  display: flex;
  gap: 28px;
  margin-bottom: 28px;
}

.tab-button {
  padding: 0 2px 12px;
  border: 0;
  border-bottom: 4px solid transparent;
  background: transparent;
  color: #b1b6c0;
  font-size: 16px;
  font-weight: 700;
  cursor: default;
}

.tab-button.active {
  color: #151d29;
  border-bottom-color: #151d29;
}

.form-meta {
  margin: 4px 0 18px;
}

.form-agreement {
  margin: 0 0 20px;
  color: #6f7684;
  font-size: 13px;
  line-height: 1.8;
}

.form-agreement span {
  color: #1d2431;
  font-weight: 600;
}

.submit-button {
  width: 100%;
  height: 48px;
  border: 0;
  border-radius: 14px;
  background: linear-gradient(180deg, #1660dc 0%, #0f57d3 100%);
  box-shadow: 0 18px 32px rgba(22, 96, 220, 0.2);
  font-size: 18px;
  font-weight: 700;
}

.action-links {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 14px;
  margin-top: 18px;
  font-size: 14px;
}

.link-divider {
  color: #cfd5de;
}

.other-login {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-top: 28px;
  color: #c0c5cf;
  font-size: 13px;
}

.line {
  flex: 1;
  height: 1px;
  background: #edf0f4;
}

.social-row {
  display: flex;
  justify-content: center;
  gap: 18px;
  margin-top: 18px;
}

.social-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border: 1px solid #e5e8ef;
  border-radius: 50%;
  background: #fff;
  color: #7f8796;
  cursor: pointer;
  transition: all 0.2s ease;
}

.social-button:hover {
  color: #1660dc;
  border-color: rgba(22, 96, 220, 0.24);
  box-shadow: 0 10px 20px rgba(22, 96, 220, 0.08);
  transform: translateY(-1px);
}

.guest-entry {
  display: flex;
  justify-content: center;
  margin-top: 22px;
}

:deep(.auth-form .el-form-item) {
  margin-bottom: 20px;
}

:deep(.auth-form .submit-item) {
  margin-bottom: 0;
}

:deep(.auth-form .el-input__wrapper) {
  min-height: 46px;
  border-radius: 14px;
  padding: 0 16px;
  background: #f4f6fa;
  box-shadow: none;
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
}

:deep(.auth-form .el-input__wrapper:hover) {
  background: #f1f4f9;
}

:deep(.auth-form .el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1px rgba(22, 96, 220, 0.4), 0 0 0 4px rgba(22, 96, 220, 0.1);
}

:deep(.auth-form .el-input__prefix-inner) {
  color: #bfc6d2;
}

:deep(.auth-form .el-input__inner::placeholder) {
  color: #b7beca;
}

:deep(.auth-form .el-checkbox__label) {
  color: #8b93a0;
}

@media (max-width: 960px) {
  .auth-page {
    padding: 24px 16px 28px;
  }

  .auth-stage {
    min-height: auto;
    padding-top: 18px;
  }

  .auth-shell {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    min-height: 280px;
    padding: 40px 28px;
  }

  .hero-title {
    font-size: 40px;
  }

  .hero-subtitle {
    font-size: 18px;
  }

  .form-panel {
    padding: 36px 26px 30px;
  }
}

@media (max-width: 640px) {
  .brand-strip {
    gap: 12px;
  }

  .brand-name {
    font-size: 18px;
  }

  .brand-icon {
    width: 38px;
    height: 38px;
  }

  .hero-panel {
    min-height: 240px;
    padding: 34px 22px;
  }

  .hero-logo {
    width: 82px;
    height: 82px;
    border-radius: 22px;
    font-size: 36px;
  }

  .hero-title {
    margin-top: 20px;
    font-size: 34px;
  }

  .hero-subtitle {
    font-size: 16px;
    text-align: center;
  }

  .hero-badges {
    gap: 12px;
    margin-top: 30px;
  }

  .hero-badge {
    font-size: 13px;
  }

  .form-panel {
    padding: 28px 18px 24px;
  }

  .auth-tabs {
    gap: 20px;
    margin-bottom: 24px;
  }

  .action-links {
    flex-wrap: wrap;
    gap: 10px;
  }
}
</style>
